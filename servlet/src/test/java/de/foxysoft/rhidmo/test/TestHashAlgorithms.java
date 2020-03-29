package de.foxysoft.rhidmo.test;

import org.junit.Test;

import de.foxysoft.rhidmo.GlobalFunctions;
import de.foxysoft.rhidmo.mock.InMemoryStorageProvider;

public class TestHashAlgorithms
{
	final String password1 = "Welcome123!",
			password2 = "Pas5Wördt@€!äÖ";

	InMemoryStorageProvider myKeyStorage = null;
	GlobalFunctions gf;

	void TestHashAlgorithm()
	{
		myKeyStorage = new InMemoryStorageProvider();
		gf = new GlobalFunctions(myKeyStorage);
	}

	@Test
	public void doMD5Hash() throws Exception
	{
		if(this.myKeyStorage == null) {
			this.myKeyStorage = new InMemoryStorageProvider();
			this.gf = new GlobalFunctions(myKeyStorage);
		}
		String hash = gf.uMD5(password1, null);
		if(!hash.equalsIgnoreCase("23cb2d3d426b10abdf03417cdb095f08")) {
			throw new Exception("ERROR: MD5 password1");
		}
		hash = gf.uMD5(password1, "");
		if(!hash.equalsIgnoreCase("23cb2d3d426b10abdf03417cdb095f08")) {
			throw new Exception("ERROR: MD5 password1 \"\"");
		}
		hash = gf.uMD5(password1, "UTF-8");
		if(!hash.equalsIgnoreCase("23cb2d3d426b10abdf03417cdb095f08")) {
			throw new Exception("ERROR: MD5 password1 UTF-8");
		}
	}

	@Test
	public void doSHA1Hash() throws Exception
	{
		if(this.myKeyStorage == null) {
			this.myKeyStorage = new InMemoryStorageProvider();
			this.gf = new GlobalFunctions(myKeyStorage);
		}
		String hash = gf.uSHA1(password1, null);
		if(!hash.equalsIgnoreCase("{SHA}Y8G9w3Gr8Xk7wCpfl3mOr8KCbr4")) {
			throw new Exception("ERROR: SHA1 password1");
		}
		hash = gf.uSHA1(password1, "");
		if(!hash.equalsIgnoreCase("{SHA}Y8G9w3Gr8Xk7wCpfl3mOr8KCbr4")) {
			throw new Exception("ERROR: SHA1 password1 \"\"");
		}
		hash = gf.uSHA1(password1, "UTF-8");
		if(!hash.equalsIgnoreCase("{SHA}Y8G9w3Gr8Xk7wCpfl3mOr8KCbr4")) {
			throw new Exception("ERROR: SHA1 password1 UTF-8");
		}
	}

	@Test
	public void doSHA256Hash() throws Exception {
		if(this.myKeyStorage == null) {
			this.myKeyStorage = new InMemoryStorageProvider();
			this.gf = new GlobalFunctions(myKeyStorage);
		}

		if(!gf.uCompareHash(password1, "{SHA256}7YbB+DReRnxEus3w7kJUlw2OKsowoUmhKwQhguH8BK2YFGk3G1Wzbr5lvP7T04uY", "")) {
			throw new Exception("ERROR: SHA256 password1 \"\"");
		}
	}

	@Test
	public void doSHA384Hash() throws Exception {
		if(this.myKeyStorage == null) {
			this.myKeyStorage = new InMemoryStorageProvider();
			this.gf = new GlobalFunctions(myKeyStorage);
		}

		if(!gf.uCompareHash(password1, "{SHA384}/5zB0epFRwh4T7z/MLcHEQ2crLXp3CPHtktTxVN13R6GcXl9RFFyT0B56CQCWMkiVXDjrNHNRtrdkUKbOQAHgg", "")) {
			throw new Exception("ERROR: SHA384 password1 \"\"");
		}
	}

	@Test
	public void doSHA512Hash() throws Exception {
		if(this.myKeyStorage == null) {
			this.myKeyStorage = new InMemoryStorageProvider();
			this.gf = new GlobalFunctions(myKeyStorage);
		}

		if(!gf.uCompareHash(password1, "{SHA512}8gZc/haFuVaS/GxsVWimcgx8r8sPODQ3vHq8Qbx+vZNwMuH9L735Q8MnihzfgJcDeHn4olZG0i78Znw0Kg89rXIwoglLv4HiUCay5quUonc", "")) {
			throw new Exception("ERROR: SHA512 password1 \"\"");
		}
	}

	@Test
	public void doPBKDF2withSHA1() throws Exception {
		if(this.myKeyStorage == null) {
			this.myKeyStorage = new InMemoryStorageProvider();
			this.gf = new GlobalFunctions(myKeyStorage);
		}
		// Check if algorithm PBKDF2WithHmacSHA1 is available
		if(!gf.uCompareHash(password1, "{PBKDF2_SHA1}baneJs13pVtmXtIhkAlNAY6NxDeOoy11lrKc3lDl3BRobxUfO+rj8UCsYnwxoa6Z", "")) {
			throw new Exception("ERROR: PBKDF2_SHA1 password1 \"\"");
		}
	}

	@Test
	public void doCompareSSHA() throws Exception {
		this.computeAndCompare(password2, "SSHA", null);
		this.computeAndCompare(password2, "SSHA", "");
		this.computeAndCompare(password2, "SSHA", "UTF-8");
	}

	@Test
	public void doCompareSHA256() throws Exception {
		this.computeAndCompare(password2, "SHA256", null);
		this.computeAndCompare(password2, "SHA256", "");
		this.computeAndCompare(password2, "SHA256", "UTF-8");
	}

	@Test
	public void doCompareSHA384() throws Exception {
		this.computeAndCompare(password2, "SHA384", null);
		this.computeAndCompare(password2, "SHA384", "");
		this.computeAndCompare(password2, "SHA384", "UTF-8");
	}

	@Test
	public void doCompareSHA512() throws Exception {
		this.computeAndCompare(password2, "SHA512", null);
		this.computeAndCompare(password2, "SHA512", "");
		this.computeAndCompare(password2, "SHA512", "UTF-8");
	}

	@Test
	public void doCompareDefaultHash() throws Exception {
		this.computeAndCompare(password2, "", null);
		this.computeAndCompare(password2, "", "");
		this.computeAndCompare(password2, "", "UTF-8");
	}

	void computeAndCompare(String value, String algorithm, String charSet) throws Exception {
		if(this.myKeyStorage == null || this.gf == null) {
			this.myKeyStorage = new InMemoryStorageProvider();
			this.gf = new GlobalFunctions(myKeyStorage);
		}

		String hash = this.gf.uGenHash(value, algorithm, charSet);
		if(!gf.uCompareHash(value, hash, charSet)) {
			throw new Exception("Error: " + algorithm + " " + charSet);
		}
	}
}