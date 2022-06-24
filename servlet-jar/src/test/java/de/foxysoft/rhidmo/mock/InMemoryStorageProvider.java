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

import de.foxysoft.rhidmo.IniKeyStorageProvider;
import de.foxysoft.rhidmo.KeyStorageProvider;

public class InMemoryStorageProvider extends IniKeyStorageProvider implements KeyStorageProvider {

	public InMemoryStorageProvider() {
		super(null);
	}
	
	@Override
	public void setKeyIndex(String keyIndex) {
	}

	@Override
	public byte[] getKey() {
		return DatatypeConverter.parseHexBinary("33787F51D0659F770DEAA3EA74ADAE1265E624937D08BB17");
	}

	@Override
	public byte[] getCurrentKey() {
		return this.getKey();
	}

	@Override
	public String getDefaultCipherName() {
		return "DESede/CBC/PKCS5Padding";
	}

	@Override
	public String getDefaultSecretKeyName() {
		return "DESede";
	}

	@Override
	public String getCurrentKeyDescription() {
		return "1";
	}
	
	@Override
	public String getDefaultAlgorithmDescription() {
		return "{DES3CBC}";
	}
}
