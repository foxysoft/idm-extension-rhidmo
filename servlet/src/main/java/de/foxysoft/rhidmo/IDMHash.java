package de.foxysoft.rhidmo;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class IDMHash
{
	enum HashConfiguration {

		MD5("MD5", 0),
		SHA("SHA", 0),
		SSHA("SHA", 12),
		SHA256("SHA-256", 16),
		SHA384("SHA-384", 16),
		SHA512("SHA-512", 16),
		PBKDF2_SHA1("PBKDF2WithHmacSHA1", 16) {
			@Override
			byte[] computeHash(String message, byte[] salt, String charSet) throws NoSuchAlgorithmException, InvalidKeySpecException
			{
				SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(this.getJavaName());
		        PBEKeySpec pbeKeySpec = new PBEKeySpec(message.toCharArray(), salt, 10000, 256);
		        SecretKey key = keyFactory.generateSecret(pbeKeySpec);
		        pbeKeySpec.clearPassword();
		        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getEncoded(), "AES");
		        return secretKeySpec.getEncoded();
			}
		};

		private final String javaName;
		private final int lengthOfSalt;

		HashConfiguration(String javaName, int lengthOfSalt) {
			this.javaName = javaName;
			this.lengthOfSalt = lengthOfSalt;
		}

		String getJavaName() {
			return this.javaName;
		}

		int getLengthOfSalt() {
			return this.lengthOfSalt;
		}

		public String generateHash(String message, String charSet) throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException {
			// Generate a new salt.
			byte[] salt = new byte[this.lengthOfSalt];
		    SecureRandom random = new SecureRandom();
		    random.nextBytes(salt);

		    byte[] hash = computeHash(message, salt, charSet);
		    byte[] hashAndSalt = new byte[hash.length + salt.length];

		    System.arraycopy(hash, 0, hashAndSalt, 0, hash.length);
		    System.arraycopy(salt, 0, hashAndSalt, hash.length, this.lengthOfSalt);

		    if(this.name() == "MD5") {
		    	return byteToHex(hashAndSalt);
		    }
		    else {
		    	return "{" + this.name() + "}" + Base64.encode(hashAndSalt);
		    }
		}

		byte[] computeHash(String message, byte[] salt, String charSet) throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException {
			MessageDigest messageDigest = MessageDigest.getInstance(this.javaName);
			messageDigest.update(charSet == null || charSet == "" || charSet.length() == 0 ? message.getBytes() : message.getBytes(charSet));
			return messageDigest.digest(salt);
		}

		boolean compareHash(String clearText, String hash, String charSet) throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException {
			// Remove algorithm from hash value (if existing)
			int end = hash.indexOf("}");
			if(end >= 0) {
				hash = hash.substring(end + 1);
			}

			// System.out.println("Hash algorithm [" + this.javaName + "] Hash [" + hash + "]");
			byte[] rawHash = Base64.decode(hash);

			// Get the salt (if available)
			byte[] salt = new byte[this.lengthOfSalt];
			System.arraycopy(rawHash, rawHash.length - salt.length, salt, 0, salt.length);
			byte[] computedHash = computeHash(clearText, salt, charSet);

			// Check for MD5 and perform special stuff.
			if(end < 0) {
				return hash.equalsIgnoreCase(byteToHex(computedHash));
			}
			return MessageDigest.isEqual(Arrays.copyOfRange(rawHash, 0, rawHash.length - salt.length), computedHash);
		}

		static public HashConfiguration getHashConfiguration(String hash) {
			// Figure out hash algorithm. Assume MD5 if nothing is found.
			HashConfiguration myHash;
			int start = hash.indexOf("{"), end = hash.indexOf("}");
			if(start >= 0 && end >= 0) {
				// Must something else as MD5
				String algorithm = hash.substring(start  + 1, end);
				myHash = HashConfiguration.valueOf(algorithm);
			}
			else {
				// Must be MD5
				myHash = HashConfiguration.valueOf("MD5");
			}
			return myHash;
		}

		private static String byteToHex(byte[] bytes) {
			final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

			char[] buff = new char[bytes.length * 2];
			for (int i = 0, j = 0; i < bytes.length; ) {
				int k = bytes[i++];
				buff[j++] = HEX_DIGITS[k >>> 4 & 0xF];
				buff[j++] = HEX_DIGITS[k & 0xF];
			 }
			 return new String(buff);
		}
	};

	static boolean callCompareHash(String clearText, String hash) throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException {
		HashConfiguration myConf = HashConfiguration.getHashConfiguration(hash);
		if(myConf == null) {
			System.out.println("Unable to get hashing algorithm");
			return false;
		}
		return myConf.compareHash(clearText, hash, null);
	}

	public static void main(String[] args) {
		// Security.addProvider( new BouncyCastleProvider() );
		for ( Provider provider : Security.getProviders() ) {
		    // System.out.println( provider );
		    for ( Provider.Service service : provider.getServices() ) {
		        if ( "SecretKeyFactory".equals( service.getType() ) ) {
		            System.out.println("SecretKeyFactory " + service.getAlgorithm() );
		        }
		    }
		}

		String clearText = "Welcome123!";

		// SAP results of hashing the string "Welcome123!" (without the quotes).
		String[][] sapResults = {
				{"MD5", "23cb2d3d426b10abdf03417cdb095f08"},
				{"SSHA", "{SSHA}fS6ogx8FP1l0aYNVJK4kCBjv828/7OXhf2tBxLddmrE"},
				{"SHA256", "{SHA256}7YbB+DReRnxEus3w7kJUlw2OKsowoUmhKwQhguH8BK2YFGk3G1Wzbr5lvP7T04uY"},
				{"SHA384", "{SHA384}/5zB0epFRwh4T7z/MLcHEQ2crLXp3CPHtktTxVN13R6GcXl9RFFyT0B56CQCWMkiVXDjrNHNRtrdkUKbOQAHgg"},
				{"SHA512", "{SHA512}8gZc/haFuVaS/GxsVWimcgx8r8sPODQ3vHq8Qbx+vZNwMuH9L735Q8MnihzfgJcDeHn4olZG0i78Znw0Kg89rXIwoglLv4HiUCay5quUonc"},
				{"PBKDF2_SHA1", "{PBKDF2_SHA1}baneJs13pVtmXtIhkAlNAY6NxDeOoy11lrKc3lDl3BRobxUfO+rj8UCsYnwxoa6Z"}
		};

		try {
			Date startDate, endDate;
			for(int i = 0; i < sapResults.length; i++) {
				System.out.print("Hash algorithm [" + i + "] is [" + sapResults[i][0] + "] SAP Hash [" + sapResults[i][1] + "] ");
				startDate = new Date();
				System.out.print("My result [" + callCompareHash(clearText, sapResults[i][1]) + "]");
				endDate = new Date();
				long duration = endDate.getTime() - startDate.getTime();
				System.out.println(" Duration [" + duration + "] ms");
			}

			// A few more times, to see if there is set up time and/or a hot spot compiler kicking in.
			long totalTime = 0, times = 0;
			for(int j = 0; j < times; j++) {
				startDate = new Date();
				callCompareHash(clearText, sapResults[5][1]);
				endDate = new Date();
				// System.out.println("\nSecond time [" + (endDate.getTime() - startDate.getTime()) + "] ms");
				totalTime += endDate.getTime() - startDate.getTime();
			}
			if(times != 0) System.out.println("Avarage of ["+ times + "] times is [" + totalTime / times + "] ms");
		} catch(Exception e) {
			e.printStackTrace();
			return;
		}

		// Generate a few hashes.
		clearText = "Pas5Wördt@€!äÖ";

		String charSet = "";
		String[] algorithms = {"MD5", "SHA", "SSHA", "SHA256", "SHA384", "SHA512", "PBKDF2_SHA1"};

		try {
			for(int k = 0; k < algorithms.length; k++)
			{
				HashConfiguration hc = HashConfiguration.valueOf(algorithms[k]);
				String hash = hc.generateHash(clearText, charSet);
				System.out.println("Algorithm [" + algorithms[k] + "] Hash is [" + hash + "] Comparison [" +
						hc.compareHash(clearText, hash, charSet) + "]");
			}
		} catch(Exception e) {
			e.printStackTrace();
			return;
		}
		System.out.println("End");
	}
}
