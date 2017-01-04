/*******************************************************************************
 * Copyright 2017 Lambert Boskamp
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
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.xml.bind.DatatypeConverter;

import org.ini4j.Ini;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

public class GlobalFunctions extends ScriptableObject {
	private static final long serialVersionUID = 1L;

	Object myTask;

	public GlobalFunctions() {
	}
	
	public GlobalFunctions(Object task) {
		this.myTask = task;
	}

	static final String[][] algoTable = {{"DES3CBC", "DESede/CBC/PKCS5Padding", "DESede", "{DES3CBC}"}, {"", "", "", ""}};
	private static final Log APPL_LOG = Log
			.get("de.foxysoft.rhidmo.Application");

	private static final Log LOG = Log.get(GlobalFunctions.class);

	public static void uWarning(String m) {
		APPL_LOG.warn(m);
	}

	public static void uError(String m) {
		APPL_LOG.error(m);
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

	public static String uDecrypt(String cipherText, String providedKey, Object charEncoding) {
		final String M = "uDecrypt: ";
		LOG.debug(M + "Entering Parameters = {}", cipherText, providedKey, charEncoding);

		RhidmoConfiguration myConf = RhidmoConfiguration.getInstance();
		Properties props = myConf.getProperties();
		if(props == null) {
			LOG.error(M + "No properties found");
			return "!ERROR: No properties found";
		}
			
		String filename = props.getProperty("de.foxysoft.idm.crypt.keyfile");
		LOG.debug(M + "Keys.ini = {}", filename);
		
		File keysIniFile = new File(filename);
		if(!keysIniFile.exists()) {
			LOG.error(M + "Keys.ini file {} does not exist", filename);
			return "!ERROR: Keys.ini file not found";
		}

		Ini keysIni;
		try {
			keysIni = new Ini(keysIniFile);
		}
		catch(IOException e) {
			LOG.error(e);
			return "!ERROR: Unable to read keys.ini file";
		}
		
		// Algorithm
		int algoStart = cipherText.indexOf("{"), algoEnd = cipherText.indexOf("}");
		if(algoStart < 0 || algoEnd < 0) {
			LOG.error(M + "No algorithm identifier found");
			return "!ERROR: No algorithm identifier found";
		}
		String algorithm = cipherText.substring(algoStart + 1, algoEnd);
		int algorithmIndex = 0;
		for(; algoTable[algorithmIndex][0].length() > 0; algorithmIndex++) {
			if(algoTable[algorithmIndex][0].equalsIgnoreCase(algorithm))
				break;
		}		
		if(algoTable[algorithmIndex][0].length() == 0) {
			LOG.error(M + "Unknown algorithm = {}", algorithm);
			return "!ERROR: Unknown algorithm";
		}
		LOG.debug(M + "Algorithm = {}", algoTable[algorithmIndex][1], algoTable[algorithmIndex][2]);

		// Key
		int keyIndexEnd = cipherText.indexOf(":");
		if(keyIndexEnd < 0) {
			LOG.error(M + "No Key index end found");
			return "!ERROR: No key index end found";
		}
		String keyIndex = cipherText.substring(algoEnd + 1, keyIndexEnd);
		keyIndex = "KEY" + "000000".substring(0, 3 - keyIndex.length()) + keyIndex;
		String key = keysIni.get("KEYS", keyIndex);
		
		// Initialization Vector
		int ivEnd = cipherText.indexOf("-");
		if(ivEnd < 0) {
			LOG.error(M + "No IV found");
			return "!ERROR: No IV found";
		}
		String initializationVector = cipherText.substring(keyIndexEnd + 1, ivEnd);

		byte[] clearText = null;
		try {
			Cipher cp = Cipher.getInstance(algoTable[algorithmIndex][1]);
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(algoTable[algorithmIndex][2]);
			DESedeKeySpec spec = new DESedeKeySpec(DatatypeConverter.parseHexBinary(key));
			Key cipherKey = keyFactory.generateSecret(spec);

			AlgorithmParameters algParams = AlgorithmParameters.getInstance(algoTable[algorithmIndex][2]);
			algParams.init(new IvParameterSpec(DatatypeConverter.parseHexBinary(initializationVector)));

			cp.init(javax.crypto.Cipher.DECRYPT_MODE, cipherKey, algParams);
			clearText = cp.doFinal(DatatypeConverter.parseHexBinary(cipherText.substring(ivEnd + 1)));
		} catch(Exception e) {
			LOG.error(e);
			return "!ERROR: Decryption error";
		}
		
		// Check if a certain characterset encoding is needed
		if (charEncoding == null
				|| Undefined.instance == charEncoding
				|| "".equals(charEncoding)) {
			return new String(clearText);
		}
		
		String encodedClearText;
		try {
			encodedClearText = new String(clearText, (String) charEncoding);
		}
		catch(Exception e) {
			LOG.error(e);
			return "!ERROR: Unknown character encoding";
		}
		return encodedClearText;
	}
	
	public static String uEncrypt(String clearText, String providedAlgorithm, String providedKey, Object charEncoding) {
		final String M = "uEncrypt: ";
		LOG.debug(M + "Entering");

		RhidmoConfiguration myConf = RhidmoConfiguration.getInstance();
		Properties props = myConf.getProperties();
		if(props == null) {
			LOG.error(M + "No properties found");
			return "!ERROR: No properties found";
		}
			
		String filename = props.getProperty("de.foxysoft.idm.crypt.keyfile");
		LOG.debug(M + "Keys.ini = {}", filename);
		
		File keysIniFile = new File(filename);
		if(!keysIniFile.exists()) {
			LOG.error(M + "Keys.ini file {} does not exist", filename);
			return "!ERROR: Keys.ini file not found";
		}

		Ini keysIni;
		try {
			keysIni = new Ini(keysIniFile);
		}
		catch(IOException e) {
			LOG.error(e);
			return "!ERROR: Unable to read keys.ini file";
		}

		String keyNumber = keysIni.get("CURRENT", "KEY"), algorithm = keysIni.get("ALGORITHMS", "ENCRYPTION");
		LOG.debug(M + "KeyNumber, algorithm = {}", keyNumber, algorithm);
		
		int algorithmIndex = 0;
		for(; algoTable[algorithmIndex][0].length() > 0; algorithmIndex++) {
			if(algoTable[algorithmIndex][0].equalsIgnoreCase(algorithm))
				break;
		}
		if(algoTable[algorithmIndex][0].length() == 0) {
			return "!ERROR: Unknown algorithm [" + algorithm + "]";
		}

		try {
			Cipher cp = Cipher.getInstance(algoTable[algorithmIndex][1]);
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(algoTable[algorithmIndex][2]);
			byte[] currentKey = DatatypeConverter.parseHexBinary(keysIni.get("KEYS", keyNumber));
			Key key = keyFactory.generateSecret(new DESedeKeySpec(currentKey));
			cp.init(javax.crypto.Cipher.ENCRYPT_MODE, key);
			byte [] cipherText = cp.doFinal(clearText.getBytes());

			return algoTable[algorithmIndex][3] + keyNumber.replaceFirst("^KEY0+(?!$)", "") + ":" + DatatypeConverter.printHexBinary(cp.getIV()).toLowerCase()
					+ "-" + DatatypeConverter.printHexBinary(cipherText).toLowerCase();
		} catch(Exception e) {
			LOG.error(e);
			return "!ERROR: Encryption problem";
		}
	}
	
	public static String uIS_GetValue(int mskey, int idStore, String attrName) {
		final String M = "uIS_GetValue: ";
		LOG.debug(M + "Entering");

		String ret = "";
		ResultSet rs = null;
		try {
			Connection con = Utl.getConnection();
			
			// Figure out identity store from mskey if no identity store is given.
			if(idStore == 0) {
				PreparedStatement getIdStoreStatement = con.prepareStatement("Select max(is_id) from idmv_value_basic where mskey = ?");
				getIdStoreStatement.setInt(1, mskey);
				
				rs = getIdStoreStatement.executeQuery();
				if(rs.next()) {
					idStore = rs.getInt(1);
					LOG.debug(M + "Found identity store = {}", idStore);
				}
				else {
					rs.close();
					return "!ERROR: Unable to get identity store from mskey";
				}
				rs.close();
			}

			// Check if attribute exists and is a single value attribute
			PreparedStatement getAttributeStatement = con.prepareStatement("select MultiValue, ReferenceObjectClass " +
					" from MXI_Attributes where AttrName = ? and is_id = ? and multiValue = 0 and ReferenceObjectClass is null");
			getAttributeStatement.setString(1, attrName);
			getAttributeStatement.setInt(2, idStore);
			rs = getAttributeStatement.executeQuery();
			if(!rs.next()) {
				rs.close();
				return "!ERROR: Attribute does not exist in this identity store or is not a single value attribute";
			}
			else {
				LOG.debug(M + "Attribute values = {}", rs.getInt(1), rs.getInt(2));
			}
			rs.close();
			
			// Read value from database (we always have an identity store)
			String sqlStatement = "select avalue from idmv_value_basic where mskey = ? and attrname = ?  and is_id = ?";

			PreparedStatement getValueStatement = con.prepareStatement(sqlStatement);
			getValueStatement.setInt(1, mskey);
			getValueStatement.setString(2, attrName);
			getValueStatement.setInt(3, idStore);
			rs = getValueStatement.executeQuery();
			if(rs.next()) {
					ret = rs.getString(1);
			}
			else {
				return "!ERROR: No value found";
			}
		} catch(Exception e) {
			LOG.error(e);
			return "!ERROR: Executing SQL statement";
		}
		finally {
			try { if(rs != null) rs.close(); } catch(Exception e) {}
		}
		
		LOG.debug(M + "Returning = {}", ret);
		return ret;
	}

	public int uGetIDStore() {
		final String M = "uGetIDStore: ";
		LOG.debug(M + "Entering");

		ResultSet rs = null;
		int idStore = 0;
		try {
			int taskId = ((Integer) this.myTask.getClass().getMethod("getID").invoke(this.myTask)).intValue();
			LOG.debug(M + "Task ID = {}", taskId);
			
			String sqlStatement = "SELECT IDStore FROM MXP_Tasks where TaskID = ?";
			Connection con = Utl.getConnection();
			PreparedStatement getValueStatement = con.prepareStatement(sqlStatement);
			getValueStatement.setInt(1, taskId);
			rs = getValueStatement.executeQuery();
			if(rs.next()) {
				idStore = rs.getInt(1);
			}
		} catch(Exception e) {
			LOG.error(e);
			return 0;
		}
		finally {
			try { if(rs != null) rs.close(); } catch(Exception e) {}
		}
		
		LOG.debug(M + "Returning ID Store = {}", idStore);
		return idStore;
	}

	public static String uIS_nGetValues(int mskey, String attrName, Object separator) {
		final String M = "uIS_nGetValues: ";
		LOG.debug(M + "Entering = {}", mskey, attrName, separator);

		String separator2Use = "";
		if (separator == null
				|| Undefined.instance == separator
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
			PreparedStatement getValuesStatement = con.prepareStatement("select avalue from idmv_value_basic where mskey = ? and attrname = ?");
			getValuesStatement.setInt(1, mskey);
			getValuesStatement.setString(2, attrName);
			rs = getValuesStatement.executeQuery();
			while(rs.next()) {
				if(ret != "") {
					ret += separator2Use;
				}
				ret += rs.getString(1);
			}
		} catch(Exception e) {
			LOG.error(e);
			return "!ERROR: Unable to execute sql statement";
		}
		finally {
			try { if(rs != null) rs.close(); } catch(Exception e) {}
		}

		LOG.debug(M + "Returning = {}", ret);
		return ret;
	}

	@Override
	public String getClassName() {
		return getClass().getName();
	}
}
