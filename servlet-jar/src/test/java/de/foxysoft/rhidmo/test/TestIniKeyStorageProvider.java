/*******************************************************************************
 * Copyright 2025 Lambert Giese
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

import static org.junit.Assert.assertArrayEquals;

import java.io.File;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.foxysoft.rhidmo.IniKeyStorageProvider;
import de.foxysoft.rhidmo.test.util.UnitTestUtil;

public class TestIniKeyStorageProvider {
	private static File tempDir;
	private static File keysIniFile;
	private static byte[] CAFEBABE = { (byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE };
	private static byte[] DEADBEEF = { (byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF };

	private IniKeyStorageProvider cut;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		tempDir = UnitTestUtil.setUpTempDir();
		keysIniFile = new File(tempDir, "Keys.ini");
		UnitTestUtil.copyResourceToFile("/de/foxysoft/rhidmo/test/ini/Keys.ini", keysIniFile);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		UnitTestUtil.tearDownTempDir(tempDir);
	}

	@Before
	public void setUp() throws Exception {
		cut = new IniKeyStorageProvider(keysIniFile);
	}

	@Test
	public void testGetKey1() {
		cut.setKeyIndex("1");
		byte[] actual = cut.getKey();
		byte[] expected = UnitTestUtil.repeatBytes(CAFEBABE, 24);
		assertArrayEquals(expected, actual);
	}

	@Test
	public void testGetKey2() {
		cut.setKeyIndex("2");
		byte[] actual = cut.getKey();
		byte[] expected = UnitTestUtil.repeatBytes(DEADBEEF, 24);
		assertArrayEquals(expected, actual);
	}

	@Test
	@Ignore
	public void testGetCipherName() {

	}

	@Test
	@Ignore
	public void testGetSecretKeyName() {

	}

	@Test
	@Ignore
	public void testGetCurrentKey() {

	}

	@Test
	@Ignore
	public void testGetDefaultCipherName() {

	}

	@Test
	@Ignore
	public void testGetDefaultSecretKeyName() {

	}

	@Test
	@Ignore
	public void testGetDefaultAlgorithmDescription() {

	}

	@Test
	@Ignore
	public void testGetCurrentKeyDescription() {

	}

	@Test
	@Ignore
	public void testGetCurrentKeySize() {

	}

	@Test
	@Ignore
	public void testGetAlgorithmDescription() {

	}

}
