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

public interface KeyStorageProvider {
	static final String[][] algoTable = {{"DES3CBC", "DESede/CBC/PKCS5Padding", "DESede", "{DES3CBC}"}, {"", "", "", ""}};
	int index = 0;

	void setAlgorithmName(String algorithm);

	void setKeyIndex(String keyIndex);

	byte[] getKey();
	
	String getCipherName();
	
	String getSecretKeyName();
	
	byte[] getCurrentKey();
	
	String getDefaultCipherName();
	
	String getDefaultSecretKeyName();
	
	String getDefaultAlgorithmDescription();
	
	String getCurrentKeyDescription();
}
