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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import de.foxysoft.rhidmo.IniKeyStorageProvider;
import de.foxysoft.rhidmo.test.util.UnitTestUtil;
import java.io.File;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class TestIniKeyStorageProvider {
  private static File tempDir;
  private static File keysIniFile;
  private static String RESOURCE_PATH = "/de/foxysoft/rhidmo/test/ini/";

  private static byte[] CAFEBABE = {(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE};
  private static byte[] DEADBEEF = {(byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF};

  private IniKeyStorageProvider cut;

  private IniKeyStorageProvider newCutWithIni(String name) throws Exception {
    UnitTestUtil.copyResourceToFile(RESOURCE_PATH + name, keysIniFile);
    return new IniKeyStorageProvider(keysIniFile);
  }

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    tempDir = UnitTestUtil.setUpTempDir();
    keysIniFile = new File(tempDir, "Keys.ini");
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    UnitTestUtil.tearDownTempDir(tempDir);
  }

  @Test
  public void testGetKey1() throws Exception {
    this.cut = newCutWithIni("Keys.ini");
    assertThatGetKeyAtIndexOneEqualsCafebabe();
  }

  @Test
  public void testGetKey2() throws Exception {
    this.cut = newCutWithIni("Keys.ini");
    assertThatGetKeyAtIndexTwoEqualsDeadbeef();
  }

  @Test
  public void testGetCurrentKey() throws Exception {
    this.cut = newCutWithIni("Keys.ini");
    byte[] actual = cut.getCurrentKey();
    byte[] expected = UnitTestUtil.repeatBytes(CAFEBABE, 24);
    assertArrayEquals(expected, actual);
  }

  @Test
  public void testGetCurrentKeySizeAlgorithmNotSet() throws Exception {
    this.cut = newCutWithIni("empty-file.ini");
    int actual = cut.getCurrentKeySize();
    assertEquals("getCurrentKeySize() must return zero when algorithm not set", 0, actual);
  }

  @Test
  public void testGetCurrentKeySizeAlgorithmDes3Cbc() throws Exception {
    assertThatGetCurrentKeySizeForAlgorithmEqualsInteger("DES3CBC", 0);
  }

  @Test
  public void testGetCurrentKeySizeAlgorithmAes128Cbc() throws Exception {
    assertThatGetCurrentKeySizeForAlgorithmEqualsInteger("AES128CBC", 16);
  }

  @Test
  public void testGetCurrentKeySizeAlgorithmAes192Cbc() throws Exception {
    assertThatGetCurrentKeySizeForAlgorithmEqualsInteger("AES192CBC", 24);
  }

  @Test
  public void testGetCurrentKeySizeAlgorithmAes256Cbc() throws Exception {
    assertThatGetCurrentKeySizeForAlgorithmEqualsInteger("AES256CBC", 32);
  }

  @Test
  public void testCaseSensitiveKey() throws Exception {
    this.cut = newCutWithIni("case-sensitive-key.ini");
    assertThatGetKeyAtIndexOneThrows("Lookup using key with different case key must throw");
  }

  @Test
  public void testCaseSensitiveSection() throws Exception {
    this.cut = newCutWithIni("case-sensitive-section.ini");
    assertThatGetKeyAtIndexOneThrows("Lookup using section with different case section must throw");
  }

  @Test
  public void testWhitespaceSurroundedKey() throws Exception {
    this.cut = newCutWithIni("whitespace-surrounded-equals.ini");
    assertThatGetKeyAtIndexOneEqualsCafebabe();
  }

  @Test
  public void testWhitespaceSurroundedSection() throws Exception {
    this.cut = newCutWithIni("whitespace-surrounded-section.ini");
    assertThatGetKeyAtIndexOneEqualsCafebabe();
  }

  @Test
  @Ignore
  public void testGetCipherName() {}

  @Test
  @Ignore
  public void testGetSecretKeyName() {}

  @Test
  @Ignore
  public void testGetDefaultCipherName() {}

  @Test
  @Ignore
  public void testGetDefaultSecretKeyName() {}

  @Test
  @Ignore
  public void testGetDefaultAlgorithmDescription() {}

  @Test
  @Ignore
  public void testGetCurrentKeyDescription() {}

  @Test
  @Ignore
  public void testGetAlgorithmDescription() {}

  private void assertThatGetKeyAtIndexOneEqualsCafebabe() throws Exception {
    this.cut.setKeyIndex("1");
    byte[] actual = cut.getKey();
    byte[] expected = UnitTestUtil.repeatBytes(CAFEBABE, 24);
    assertArrayEquals(expected, actual);
  }

  private void assertThatGetKeyAtIndexTwoEqualsDeadbeef() throws Exception {
    this.cut.setKeyIndex("2");
    byte[] actual = cut.getKey();
    byte[] expected = UnitTestUtil.repeatBytes(DEADBEEF, 24);
    assertArrayEquals(expected, actual);
  }

  private void assertThatGetKeyAtIndexOneThrows(String msg) throws Exception {
    this.cut.setKeyIndex("1");
    try {
      this.cut.getKey();
      fail(msg);
    } catch (Exception e) {
      assertNotNull(e);
    }
  }

  private void assertThatGetCurrentKeySizeForAlgorithmEqualsInteger(
      String algorithm, int expectedKeySize) throws Exception {
    this.cut = newCutWithIni("empty-file.ini");
    this.cut.setAlgorithmName(algorithm);
    int actualKeySize = cut.getCurrentKeySize();
    String msg =
        "getCurrentKeySize() must return ["
            + expectedKeySize
            + "] when algorithm is ["
            + algorithm
            + "]";
    assertEquals(msg, expectedKeySize, actualKeySize);
  }
}
