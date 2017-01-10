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
package de.foxysoft.rhidmo.test;

import org.junit.Test;

import de.foxysoft.rhidmo.GlobalFunctions;
import de.foxysoft.rhidmo.mock.InMemoryStorageProvider;

public class TestEncryption {

	@Test
	public void doTestEncryption() throws Exception {
		this.doTestEncryptionCharsetEncoding("");
	}
	
	@Test
	public void doTestEncryption2() throws Exception {
		this.doTestEncryptionCharsetEncoding("UTF8");
	}

	@Test
	public void doTestEncryption3() throws Exception {
		this.doTestEncryptionCharsetEncoding("Cp1252");
	}

	public void doTestEncryptionCharsetEncoding(String charsetEncoding) throws Exception {
		InMemoryStorageProvider myKeyStorage = new InMemoryStorageProvider();
		GlobalFunctions gf = new GlobalFunctions(myKeyStorage);

		String clearText = charsetEncoding + "VeryDeviousSecret01%$!";
		String cipherText = gf.uEncrypt(clearText, "", "", charsetEncoding);
		String clearText2 = gf.uDecrypt(cipherText, "", charsetEncoding);

		if(!clearText.equals(clearText2)) {
			throw new Exception("No match");
		}
	}
}
