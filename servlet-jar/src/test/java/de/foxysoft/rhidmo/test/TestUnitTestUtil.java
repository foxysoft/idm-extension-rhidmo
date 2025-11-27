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

import de.foxysoft.rhidmo.test.util.UnitTestUtil;
import org.junit.Test;

public class TestUnitTestUtil {
  @Test
  public void testRepeatBytes() throws Exception {
    byte[] p = new byte[] {(byte) 0x01, (byte) 0x02};
    byte[] exp = new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x01};
    byte[] act = UnitTestUtil.repeatBytes(p, 3);
    assertArrayEquals(exp, act);
  }
}
