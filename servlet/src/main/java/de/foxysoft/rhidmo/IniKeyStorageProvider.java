/*******************************************************************************
 * Copyright 2017 Sietze Roorda
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

import javax.xml.bind.DatatypeConverter;

import org.ini4j.Ini;

public class IniKeyStorageProvider implements KeyStorageProvider {
	Ini keysIni;
	int algorithmIndex = 0;
	String keyIndex;

	private static final Log LOG = Log.get(IniKeyStorageProvider.class);
	public IniKeyStorageProvider(File keysIniFile) {
		final String M = "IniKeyStorageProvider: ";
		if(!keysIniFile.exists()) {
			LOG.error(M + "Keys.ini file {} does not exist", keysIniFile.getAbsoluteFile());
			return;
		}

		try {
			this.keysIni = new Ini(keysIniFile);
		}
		catch(IOException e) {
			LOG.error(e);
			return;
		}
	}

	@Override
	public void setAlgorithmName(String algorithm) {
		final String M = "Set Algorithm Name: ";
		for(algorithmIndex = 0; algoTable[algorithmIndex][0].length() > 0; algorithmIndex++) {
			if(algoTable[algorithmIndex][0].equalsIgnoreCase(algorithm))
				break;
		}
		if(algoTable[algorithmIndex][0].length() == 0) {
			LOG.error(M + "Unknown algorithm = {}", algorithm);
			return;
		}
		LOG.debug(M + "Algorithm = {}", algoTable[algorithmIndex][1], algoTable[algorithmIndex][2]);
	}

	@Override
	public void setKeyIndex(String keyIndex) {
		this.keyIndex = "KEY" + "000000".substring(0, 3 - keyIndex.length()) + keyIndex;
	}

	@Override
	public byte[] getKey() {
		String key = this.keysIni.get("KEYS", this.keyIndex);
		return DatatypeConverter.parseHexBinary(key);
	}

	@Override
	public String getCipherName() {
		return algoTable[this.algorithmIndex][1];
	}

	@Override
	public String getSecretKeyName() {
		return algoTable[this.algorithmIndex][2];
	}

	@Override
	public byte[] getCurrentKey() {
		String keyNumber = keysIni.get("CURRENT", "KEY");
		return DatatypeConverter.parseHexBinary(keysIni.get("KEYS", keyNumber));
	}

	@Override
	public String getDefaultCipherName() {
		final String M = "getDefaultCipherName: ";
		String algorithm = this.keysIni.get("ALGORITHMS", "ENCRYPTION");
		
		int defaultIndex = 0;
		for(; algoTable[defaultIndex][0].length() > 0; defaultIndex++) {
			if(algoTable[defaultIndex][0].equalsIgnoreCase(algorithm))
				break;
		}
		if(algoTable[defaultIndex][0].length() == 0) {
			LOG.error(M + "Unknown algorithm = {}", algorithm);
			return null;
		}

		return KeyStorageProvider.algoTable[defaultIndex][1];
	}

	@Override
	public String getDefaultSecretKeyName() {
		final String M = "getDefaultSecretKeyName: ";
		String algorithm = this.keysIni.get("ALGORITHMS", "ENCRYPTION");
		
		int defaultIndex = 0;
		for(; algoTable[defaultIndex][0].length() > 0; defaultIndex++) {
			if(algoTable[defaultIndex][0].equalsIgnoreCase(algorithm))
				break;
		}
		if(algoTable[defaultIndex][0].length() == 0) {
			LOG.error(M + "Unknown algorithm = {}", algorithm);
			return null;
		}

		return KeyStorageProvider.algoTable[defaultIndex][2];
	}

	@Override
	public String getDefaultAlgorithmDescription() {
		final String M = "getDefaultAlgorithmDescription: ";
		String algorithm = this.keysIni.get("ALGORITHMS", "ENCRYPTION");
		
		int defaultIndex = 0;
		for(; algoTable[defaultIndex][0].length() > 0; defaultIndex++) {
			if(algoTable[defaultIndex][0].equalsIgnoreCase(algorithm))
				break;
		}
		if(algoTable[defaultIndex][0].length() == 0) {
			LOG.error(M + "Unknown algorithm = {}", algorithm);
			return null;
		}

		return KeyStorageProvider.algoTable[defaultIndex][3];
	}

	@Override
	public String getCurrentKeyDescription() {
		String keyNumber = keysIni.get("CURRENT", "KEY");
		return keyNumber.replaceFirst("^KEY0+(?!$)", "");
	}
}
