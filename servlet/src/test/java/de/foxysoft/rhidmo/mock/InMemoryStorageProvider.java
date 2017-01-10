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
package de.foxysoft.rhidmo.mock;

import javax.xml.bind.DatatypeConverter;

import de.foxysoft.rhidmo.KeyStorageProvider;

public class InMemoryStorageProvider implements KeyStorageProvider {

	private String algorithmName, keyIndex;

	@Override
	public void setAlgorithmName(String algorithm) {
		this.algorithmName = algorithm;
	}

	@Override
	public void setKeyIndex(String keyIndex) {
		this.keyIndex = keyIndex;
	}

	@Override
	public byte[] getKey() {
		return DatatypeConverter.parseHexBinary("33787F51D0659F770DEAA3EA74ADAE1265E624937D08BB17");
	}

	@Override
	public String getCipherName() {
		return "DESede/CBC/PKCS5Padding";
	}

	@Override
	public String getSecretKeyName() {
		return "DESede";
	}

	@Override
	public byte[] getCurrentKey() {
		return this.getKey();
	}

	@Override
	public String getDefaultCipherName() {
		return this.getCipherName();
	}

	@Override
	public String getDefaultSecretKeyName() {
		return this.getSecretKeyName();
	}

	@Override
	public String getDefaultAlgorithmDescription() {
		return "{DES3CBC}";
	}

	@Override
	public String getCurrentKeyDescription() {
		return "1";
	}

}
