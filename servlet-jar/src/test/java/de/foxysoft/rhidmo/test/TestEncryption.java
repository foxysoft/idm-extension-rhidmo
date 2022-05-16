/*******************************************************************************
 * Copyright 2017, 2018 Sietze Roorda
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
package de.foxysoft.rhidmo.test;

import javax.crypto.Cipher;

import org.junit.Test;

import de.foxysoft.rhidmo.GlobalFunctions;
import de.foxysoft.rhidmo.mock.InMemoryStorageProvider;

public class TestEncryption {

	@Test
	public void doTestEncryptionDefaults() throws Exception {
		this.doTestEncryptionCharsetEncoding("", "");
	}
	
	@Test
	public void doTestEncryptionUTF8() throws Exception {
		this.doTestEncryptionCharsetEncoding("UTF8", "AES256CBC");
	}

	@Test
	public void doTestEncryptionCp1252() throws Exception {
		this.doTestEncryptionCharsetEncoding("Cp1252", "AES256CBC");
	}

	@Test
	public void doTestEncryptionAES128() throws Exception {
		this.doTestEncryptionCharsetEncoding("", "AES128CBC");
	}

	@Test
	public void doTestEncryptionAES192() throws Exception {
		this.doTestEncryptionCharsetEncoding("", "AES192CBC");
	}

	@Test
	public void doTestEncryptionAES256() throws Exception {
		this.doTestEncryptionCharsetEncoding("", "AES256CBC");
	}

	public void doTestEncryptionCharsetEncoding(String charsetEncoding, String algorithm) throws Exception {
		int maxAllowedKeySize = Cipher.getMaxAllowedKeyLength("AES");
		if (maxAllowedKeySize == 128 && (algorithm == "AES192CBC" || algorithm == "AES256CBC")) {
			System.out.println("Install unlimited crypto policy files. Max size = [" + maxAllowedKeySize + "]");
			return;
		}

		InMemoryStorageProvider myKeyStorage = new InMemoryStorageProvider();
		GlobalFunctions gf = new GlobalFunctions(myKeyStorage);

		String clearText = charsetEncoding + "VeryDeviousSecret01%$!";
		String cipherText = gf.uEncrypt(clearText, algorithm, "", charsetEncoding);

		// Check algorithm
		if("".equals(algorithm)) {
			if(!cipherText.startsWith(myKeyStorage.getDefaultAlgorithmDescription())) {
				throw new Exception("Wrong default algorithm used for encryption");
			}
		}
		else {
			if(!cipherText.startsWith("{" + algorithm + "}")) {
				throw new Exception("Wrong algorithm used for encryption");
			}
		}

		String clearText2 = gf.uDecrypt(cipherText, algorithm, charsetEncoding);
		if(!clearText.equals(clearText2)) {
			throw new Exception("No match");
		}		
	}
}
