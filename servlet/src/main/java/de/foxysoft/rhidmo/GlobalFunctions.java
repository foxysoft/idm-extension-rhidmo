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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

import java.security.AlgorithmParameters;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.xml.bind.DatatypeConverter;

import javax.naming.InitialContext;

import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.ScriptableObject;

import org.ini4j.Ini;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;

public class GlobalFunctions extends ScriptableObject {
	public GlobalFunctions() {
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

	public static String uDecrypt(String cipherText, String providedKey, String charEncoding) {
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
		final String[][] algoTable = {{"DES3CBC", "DESede/CBC/PKCS5Padding", "DESede"}, {"", "", ""}};
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
			encodedClearText = new String(clearText, charEncoding);
		}
		catch(Exception e) {
			LOG.error(e);
			return "!ERROR: Unknown character encoding";
		}
		return encodedClearText;
	}
	
	public static String uEncrypt(String clearText, String algorithm, String key, String charEncoding) {
		final String M = "uEncrypt: ";
		LOG.debug(M + "Entering");
		return "!ERROR: Not implemented yet";
	}

	@Override
	public String getClassName() {
		return getClass().getName();
	}
}
