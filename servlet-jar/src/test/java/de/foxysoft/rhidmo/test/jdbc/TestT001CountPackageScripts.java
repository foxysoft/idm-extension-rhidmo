/*******************************************************************************
 * Copyright 2022 Lambert Giese
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
package de.foxysoft.rhidmo.test.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.Assert;
import org.junit.Test;
import de.foxysoft.rhidmo.test.util.JdbcUnitTest;

public class TestT001CountPackageScripts extends JdbcUnitTest {
  public TestT001CountPackageScripts() {
    super("T001");
  }

  @Test
  public void testSelect() throws Exception {
    PreparedStatement ps = m_connection.prepareStatement("select count(*) from mc_package_scripts");
    ResultSet rs = ps.executeQuery();
    Assert.assertTrue(rs.next());
    int count = rs.getInt(1);
    Assert.assertTrue(count > 0);
  }

}
