/*******************************************************************************
 * Copyright 2017, 2018 Lambert Boskamp & Sietze Roorda
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package de.foxysoft.rhidmo;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.security.AlgorithmParameters;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;
import javax.xml.bind.DatatypeConverter;

import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

import de.foxysoft.rhidmo.IDMHash.HashConfiguration;

public class GlobalFunctions extends ScriptableObject {
	private static final long serialVersionUID = 1L;

	Object myTask;
	RhidmoConfiguration myConf = RhidmoConfiguration.getInstance();
	KeyStorageProvider myKeyStorage;

	public GlobalFunctions() {
		this(null);
	}

	public GlobalFunctions(Object task) {
		this.myTask = task;

		final String M = "Constructor: ";
		Properties props = myConf.getProperties();
		if (props == null) {
			LOG.error(M + "No properties found");
			return;
		}

		String filename = props
				.getProperty("de.foxysoft.idm.crypt.keyfile");
		LOG.debug(M + "Keys.ini = {}",
				filename);

		myKeyStorage = new IniKeyStorageProvider(new File(filename));
	}

	public GlobalFunctions(KeyStorageProvider myKeyStorageProvider) {
		final String M = "Constructor (ks): ";
		LOG.debug(M + "Setting my keystorage provider");
		this.myKeyStorage = myKeyStorageProvider;
	}

	private static final Log APPL_LOG = Log
			.get("de.foxysoft.rhidmo.Application");

	private static final Log LOG = Log.get(GlobalFunctions.class);

	public static void uWarning(String m) {
		APPL_LOG.warn(m);
	}

	public static void uError(String m) {
		APPL_LOG.error(m);
	}

	public void uErrMsg(int type, String message) {
		final String M = "uErrMsg: ";

		switch (type) {
		case 0:
			APPL_LOG.debug(message);
			break;
		case 1:
			APPL_LOG.warn(message);
			break;
		case 2:
			APPL_LOG.error(message);
			break;
		default:
			LOG.error(M + "!ERROR: Unknown error level = {}", type);
		}
	}

	public static String uSelect(String sqlStatement,
			Object rowSeparator,
			Object columnSeparator) {
		final String M = "uSelect: ";
		LOG.debug(M
				+ "Entering sqlStatement={}, rowSeparator={}, columnSeparator={}",
				sqlStatement,
				rowSeparator,
				columnSeparator);

		String result = null;
		if (rowSeparator == null || Undefined.instance == rowSeparator
				|| "".equals(rowSeparator)) {
			rowSeparator = "!!";
			LOG.debug(M + "Using default rowSeparator={}",
					rowSeparator);
		}

		if (columnSeparator == null
				|| Undefined.instance == columnSeparator
				|| "".equals(columnSeparator)) {
			columnSeparator = "|";
			LOG.debug(M + "Using default columnSeparator={}",
					columnSeparator);
		}

		Connection c = null;
		ResultSet rs = null;
		PreparedStatement ps = null;

		try {
			c = Utl.getConnection();
			ps = c.prepareStatement(sqlStatement);
			rs = ps.executeQuery();

			StringBuffer resultBuffer = new StringBuffer();
			int columnCount = rs.getMetaData()
					.getColumnCount();

			for (int i = 1; rs.next(); ++i) {
				if (i > 1) {
					resultBuffer.append(rowSeparator);
				}
				for (int j = 1; j <= columnCount; ++j) {
					if (j > 1) {
						resultBuffer.append(columnSeparator);
					}
					resultBuffer.append(rs.getObject(j));
				}
			}

			result = resultBuffer.toString();

		} catch (Exception e) {
			LOG.error(e);
			result = "!ERROR: " + Log.getStackTrace(e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
				}
			}
			if (ps != null) {
				try {
					ps.close();
				} catch (Exception e) {
				}
			}
			if (c != null) {
				try {
					c.close();
				} catch (Exception e) {
				}
			}
		}

		LOG.debug(M + "Returning {}",
				result);
		return result;
	}

	public static String uFromHex(String hexString,
			String characterEncoding) {
		final String M = "uFromHex: ";
		LOG.debug(M + "Entering hexString={}, characterEncoding={}",
				hexString,
				characterEncoding);

		String result = "";
		if (Undefined.instance == hexString || null == hexString
				|| "".equals(hexString)) {
			hexString = "";
			LOG.debug(M + "Defaulted hexString={}",
					hexString);
		}

		if (Undefined.instance == characterEncoding
				|| null == characterEncoding
				|| "".equals(characterEncoding)) {
			characterEncoding = Charset.defaultCharset()
					.name();
			LOG.debug(M + "Defaulted characterEncoding={}",
					characterEncoding);
		}

		int offset = hexString.startsWith("{HEX}") ? "{HEX}".length()
				: 0;
		int dataLen = hexString.length() - offset;
		byte[] data = new byte[dataLen / 2];
		for (int i = 0; i < dataLen; i += 2) {
			data[i / 2] = (byte) ((Character.digit(
					hexString.charAt(i + offset),
					16) << 4)
					+ Character.digit(hexString.charAt(i + offset + 1),
							16));
		}
		result = new String(data, Charset.forName(characterEncoding));

		LOG.debug(M + "Returning " + result);
		return result;

	}

	private static SecretKey determineSecretKey(byte[] binaryKey, String keyType, int keySize) throws Exception {
		final String M = "determineSecretKey: ";

		LOG.debug(M + "Keysize = {}, AES Maximum {}", keySize, Cipher.getMaxAllowedKeyLength("AES") / 8);
		if(keyType == "AES") {
			return new SecretKeySpec(Arrays.copyOf(binaryKey, keySize), "AES");
		}
		else {
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(keyType);
			DESedeKeySpec spec = new DESedeKeySpec(binaryKey);
			return keyFactory.generateSecret(spec);
		}
	}

	public String uDecrypt(String cipherText,
			String providedKey,
			Object charEncoding) {
		final String M = "uDecrypt: ";
		LOG.debug(M + "Entering Parameters = {}",
				cipherText,
				providedKey,
				charEncoding);

		// Algorithm
		int algoStart = cipherText.indexOf("{"),
				algoEnd = cipherText.indexOf("}");
		if (algoStart < 0 || algoEnd < 0) {
			LOG.error(M + "No algorithm identifier found");
			return "!ERROR: No algorithm identifier found";
		}
		String algorithm = cipherText.substring(algoStart + 1,
				algoEnd);
		this.myKeyStorage.setAlgorithmName(algorithm);

		// Key
		int keyIndexEnd = cipherText.indexOf(":");
		if (keyIndexEnd < 0) {
			LOG.error(M + "No Key index end found");
			return "!ERROR: No key index end found";
		}
		String keyIndex = cipherText.substring(algoEnd + 1,
				keyIndexEnd);
		this.myKeyStorage.setKeyIndex(keyIndex);
		byte[] key = this.myKeyStorage.getKey();

		// Initialization Vector
		int ivEnd = cipherText.indexOf("-");
		if (ivEnd < 0) {
			LOG.error(M + "No IV found");
			return "!ERROR: No IV found";
		}
		String initializationVector = cipherText.substring(
				keyIndexEnd + 1,
				ivEnd);

		byte[] clearText = null;
		try {
			Cipher cp = Cipher
					.getInstance(this.myKeyStorage.getCipherName());
			SecretKey cipherKey = determineSecretKey(key, this.myKeyStorage.getSecretKeyName(), this.myKeyStorage.getCurrentKeySize());

			AlgorithmParameters algParams = AlgorithmParameters
					.getInstance(this.myKeyStorage.getSecretKeyName());
			algParams.init(new IvParameterSpec(DatatypeConverter
					.parseHexBinary(initializationVector)));

			cp.init(javax.crypto.Cipher.DECRYPT_MODE, cipherKey, algParams);
			clearText = cp.doFinal(DatatypeConverter
					.parseHexBinary(cipherText.substring(ivEnd + 1)));
		} catch (Exception e) {
			LOG.error(e);
			return "!ERROR: Decryption error";
		}

		// Check if a certain characterset encoding is needed
		if (charEncoding == null || Undefined.instance == charEncoding
				|| "".equals(charEncoding)) {
			return new String(clearText);
		}

		String encodedClearText;
		try {
			encodedClearText = new String(clearText,
					(String) charEncoding);
		} catch (Exception e) {
			LOG.error(e);
			return "!ERROR: Unknown character encoding";
		}
		return encodedClearText;
	}

	public String uEncrypt(String clearText,
			String providedAlgorithm,
			String providedKey,
			Object charEncoding) {
		final String M = "uEncrypt: ";
		LOG.debug(M + "Entering. Algorithm = {}, charEncoding = {}", providedAlgorithm, charEncoding);

		try {
			if(providedAlgorithm instanceof String && providedAlgorithm != null && ! "".equals(providedAlgorithm)) {
				this.myKeyStorage.setAlgorithmName(providedAlgorithm);
				LOG.debug(M + "Using algorithm = {}", providedAlgorithm);
			}

			String cipherName = this.myKeyStorage.getCipherName();
			LOG.debug(M + "Using cipher = {}", cipherName);
			Cipher cp = Cipher.getInstance(cipherName);
			SecretKey key = determineSecretKey(this.myKeyStorage.getCurrentKey(), this.myKeyStorage.getSecretKeyName(), this.myKeyStorage.getCurrentKeySize());
			cp.init(javax.crypto.Cipher.ENCRYPT_MODE, key);
			byte[] cipherText = cp.doFinal(clearText.getBytes());

			return this.myKeyStorage.getAlgorithmDescription()
					+ this.myKeyStorage.getCurrentKeyDescription() + ":"
					+ DatatypeConverter.printHexBinary(cp.getIV())
							.toLowerCase()
					+ "-" + DatatypeConverter.printHexBinary(cipherText)
							.toLowerCase();
		} catch (Exception e) {
			LOG.error(e);
			return "!ERROR: Encryption problem";
		}
	}

	public static String uIS_GetValue(int mskey,
			int idStore,
			String attrName) {
		final String M = "uIS_GetValue: ";
		LOG.debug(M + "Entering");

		String ret = "";
		ResultSet rs = null;
		try {
			Connection con = Utl.getConnection();

			// Figure out identity store from mskey if no identity store is
			// given.
			if (idStore == 0) {
				PreparedStatement getIdStoreStatement = con
						.prepareStatement(
								"Select max(is_id) from idmv_value_basic where mskey = ?");
				getIdStoreStatement.setInt(1,
						mskey);

				rs = getIdStoreStatement.executeQuery();
				if (rs.next()) {
					idStore = rs.getInt(1);
					LOG.debug(M + "Found identity store = {}",
							idStore);
				} else {
					rs.close();
					return "!ERROR: Unable to get identity store from mskey";
				}
				rs.close();
			}

			// Check if attribute exists and is a single value attribute
			PreparedStatement getAttributeStatement = con
					.prepareStatement(
							"select MultiValue, ReferenceObjectClass "
									+ " from MXI_Attributes where AttrName = ? and is_id = ? and multiValue = 0 and ReferenceObjectClass is null");
			getAttributeStatement.setString(1,
					attrName);
			getAttributeStatement.setInt(2,
					idStore);
			rs = getAttributeStatement.executeQuery();
			if (!rs.next()) {
				rs.close();
				return "!ERROR: Attribute does not exist in this identity store or is not a single value attribute";
			} else {
				LOG.debug(M + "Attribute values = {}",
						rs.getInt(1),
						rs.getInt(2));
			}
			rs.close();

			// Read value from database (we always have an identity store)
			String sqlStatement = "select avalue from idmv_value_basic where mskey = ? and attrname = ?  and is_id = ?";

			PreparedStatement getValueStatement = con
					.prepareStatement(sqlStatement);
			getValueStatement.setInt(1,
					mskey);
			getValueStatement.setString(2,
					attrName);
			getValueStatement.setInt(3,
					idStore);
			rs = getValueStatement.executeQuery();
			if (rs.next()) {
				ret = rs.getString(1);
			} else {
				return "!ERROR: No value found";
			}
		} catch (Exception e) {
			LOG.error(e);
			return "!ERROR: Executing SQL statement";
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
			}
		}

		LOG.debug(M + "Returning = {}",
				ret);
		return ret;
	}

	public int uGetIDStore() {
		final String M = "uGetIDStore: ";
		LOG.debug(M + "Entering");

		ResultSet rs = null;
		int idStore = 0;
		try {
			int taskId = ((Integer) this.myTask.getClass()
					.getMethod("getID")
					.invoke(this.myTask)).intValue();
			LOG.debug(M + "Task ID = {}",
					taskId);

			String sqlStatement = "SELECT IDStore FROM MXP_Tasks where TaskID = ?";
			Connection con = Utl.getConnection();
			PreparedStatement getValueStatement = con
					.prepareStatement(sqlStatement);
			getValueStatement.setInt(1,
					taskId);
			rs = getValueStatement.executeQuery();
			if (rs.next()) {
				idStore = rs.getInt(1);
			}
		} catch (Exception e) {
			LOG.error(e);
			return 0;
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
			}
		}

		LOG.debug(M + "Returning ID Store = {}",
				idStore);
		return idStore;
	}

	public static String uIS_nGetValues(int mskey,
			String attrName,
			Object separator) {
		final String M = "uIS_nGetValues: ";
		LOG.debug(M + "Entering = {}",
				mskey,
				attrName,
				separator);

		String separator2Use = "";
		if (separator == null || Undefined.instance == separator
				|| "".equals(separator)) {
			separator2Use = "!!";
			LOG.debug(M + "Setting separator");
		} else {
			separator2Use = (String) separator;
		}

		ResultSet rs = null;
		String ret = "";
		try {
			Connection con = Utl.getConnection();
			PreparedStatement getValuesStatement = con.prepareStatement(
					"select avalue from idmv_value_basic where mskey = ? and attrname = ?");
			getValuesStatement.setInt(1,
					mskey);
			getValuesStatement.setString(2,
					attrName);
			rs = getValuesStatement.executeQuery();
			while (rs.next()) {
				if (ret != "") {
					ret += separator2Use;
				}
				ret += rs.getString(1);
			}
		} catch (Exception e) {
			LOG.error(e);
			return "!ERROR: Unable to execute sql statement";
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
			}
		}

		LOG.debug(M + "Returning = {}",
				ret);
		return ret;
	}

	public String uSendSMTPMessage(String sender, String recipients, String subject,
			String messageOrFilename, String smtpHost, Object contentType, Object port,
			Object attachment, Object attachmentType) {
		final String M = "uSendSMTPMessage: ";
		LOG.debug(M + "Entering = {}", sender, recipients, subject, messageOrFilename, smtpHost,
				contentType, port, attachment, attachmentType);

		if (contentType == null || Undefined.instance == contentType
				|| "".equals(contentType)) {
			contentType = "text/plain;";
			LOG.debug(M + "Using default content type = {}", contentType);
		}

		// Parse recipients to see if there are more than one.
		String[] recipientList = null;
		if(recipients.indexOf(';') >= 0 ) {
			recipientList = recipients.split(";");
		}
		else {
			if(recipients.indexOf(",") >= 0) {
				recipientList = recipients.split(",");
			}
		}

		Session mailSession = myConf.getEmailSession();

		try {
			MimeMessage emailMessage = new MimeMessage(mailSession);
			MimeBodyPart bodyPart;

			// Check if the message is in reality a file
			File inputFile = new File(messageOrFilename);
			if(inputFile.exists()) {
				bodyPart = new MimeBodyPart(new FileInputStream(inputFile));
			}
			else {
				// Must be the body itself
				bodyPart = new MimeBodyPart();
				bodyPart.setContent(messageOrFilename, (String) contentType);
			}
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(bodyPart);

			// Check for attachment
			if(Undefined.instance != attachment) {
				MimeBodyPart attachmentBodypart = new MimeBodyPart();
				attachmentBodypart.attachFile((String) attachment);
				attachmentBodypart.setFileName(new File((String) attachment).getName());

				multipart.addBodyPart(attachmentBodypart);
				if(Undefined.instance != attachmentType) {
					attachmentBodypart.setHeader("Content-Type", (String) attachmentType);
				}
			}
			emailMessage.setContent(multipart);
			emailMessage.setSubject(subject);

			if(recipientList == null) {
				if(recipients.startsWith("cc:")) {
					emailMessage.setRecipients(RecipientType.CC, recipients.substring(3));
				} else if(recipients.startsWith("bcc:")) {
					emailMessage.setRecipients(RecipientType.BCC, recipients.substring(4));
				} else
					emailMessage.setRecipients(RecipientType.TO, recipients);
			}
			else
			{
				for(int i = 0; i < recipientList.length; i++) {
					if(recipientList[i].startsWith("cc:")) {
						emailMessage.addRecipients(RecipientType.CC, recipientList[i].substring(3));
					} else if(recipientList[i].startsWith("bcc:")) {
						emailMessage.addRecipients(RecipientType.BCC, recipientList[i].substring(4));
					}
					else
						emailMessage.addRecipients(RecipientType.TO, recipientList[i]);
				}
			}

			// Set sender
			InternetAddress[] fromAddress = new InternetAddress[] {new InternetAddress(sender)};
			emailMessage.addFrom(fromAddress);

			Transport.send(emailMessage);
		} catch (Exception e) {
			LOG.error(e);
			return "!ERROR: Got exception";
		}

		LOG.debug(M + "Returning");
		return "";
	}

	// OutString=uGenHash(String Value[, StringAlgorithm[, String CharacterEncoding]]);
	public String uGenHash(String value, Object algorithm, Object characterEncoding) {
		final String M = "uGenHash: ";

		String algorithm2Use = "";
		if (algorithm == null || Undefined.instance == algorithm || "".equals(algorithm)) {
			LOG.debug(M + "Reading algorithm from keys.ini file");
			algorithm2Use = this.myKeyStorage.getDefaultHashName();
			if(algorithm2Use == null) {
				LOG.error(M + "Unable to get default hash algorithm (forgotten to specify keys.ini?)");
				return "!ERROR: Unable to generate hash";
			}
		} else {
			algorithm2Use = (String) algorithm;
		}

		String encoding2Use = "";
		if (characterEncoding == null || Undefined.instance == characterEncoding || "".equals(characterEncoding)) {
			encoding2Use = null;
			LOG.debug(M + "Setting character encoding");
		} else {
			encoding2Use = (String) characterEncoding;
		}

		String hash = null;

		try {
			HashConfiguration hc = HashConfiguration.valueOf(algorithm2Use);
			hash = hc.generateHash(value, encoding2Use);
		} catch(Exception e) {
			LOG.error(e);
			return "!ERROR: Unable to generate hash";
		}
		return hash;
	}

	// OutString=uCompareHash(StringClearTextValue, String CompareHashedValue[, StringCharacterEncoding]);
	public boolean uCompareHash(String clearText, String hashValue, Object characterEncoding) {
		final String M = "uCompareHash: ";
		String encoding2Use = "";
		if (characterEncoding == null || Undefined.instance == characterEncoding || "".equals(characterEncoding)) {
			encoding2Use = null;
			LOG.debug(M + "Setting character encoding");
		} else {
			encoding2Use = (String) characterEncoding;
		}

		try {
			HashConfiguration hc = HashConfiguration.getHashConfiguration(hashValue);
			if(hc == null) {
				LOG.error("!ERROR: Cannot determine hash algorithm = {}", hashValue);
				return false;
			}
			return hc.compareHash(clearText, hashValue, encoding2Use);
		} catch(Exception e) {
			LOG.error(e);
			return false;
		}
	}

	// OutString=uMD5(String InString[, StringCharacterEncoding]);
	public String uMD5(String clearText, Object characterEncoding) {
		return uGenHash(clearText, "MD5", characterEncoding).toLowerCase();
	}

	// OutString=uSHA1(String Input[, StringCharacterEncoding]);
	public String uSHA1(String clearText, Object characterEncoding) {
		return uGenHash(clearText, "SHA", characterEncoding);
	}

	@Override
	public String getClassName() {
		return getClass().getName();
	}
}
