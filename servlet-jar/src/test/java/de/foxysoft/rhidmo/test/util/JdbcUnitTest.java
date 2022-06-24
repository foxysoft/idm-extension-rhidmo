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
package de.foxysoft.rhidmo.test.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.After;
import org.junit.Before;

public class JdbcUnitTest {

  protected Connection m_connection;
  protected String m_testName;

  private static final String[] TABLE_NAMES =
      new String[] {"mc_package_scripts", "mxp_tasks", "mc_package", "idmv_value_basic", "mxi_attributes"};
  private static final String SCHEMA_PATH = "/de/foxysoft/rhidmo/test/schema/";
  private static final String DATA_PATH = "/de/foxysoft/rhidmo/test/data/";

  public JdbcUnitTest(String testName) {
    m_testName = testName;
  }

  @Before
  public void setup() throws SQLException, IOException {
    m_connection = DriverManager.getConnection("jdbc:h2:mem:");
    createTables();
    populateTables();
  }

  @After
  public void teardown() {
    if (m_connection != null) {
      try {
        if(false == m_connection.isClosed()) {
          m_connection.close();
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  private void populateTables() throws SQLException {
    for (int i = 0; i < TABLE_NAMES.length; ++i) {
      String tableName = TABLE_NAMES[i];
      String csvName = m_testName + "_" + tableName + ".csv";
      URL csvUrl = this.getClass().getResource(DATA_PATH + csvName);
      if (csvUrl == null) {
        continue;
      }

      Statement s = null;
      try {
        s = m_connection.createStatement();
        s.execute(
            "insert into " + tableName + " select * from csvread('" + csvUrl.toString() + "')");
      } finally {
        if (s != null) {
          try {
            s.close();
          } catch (SQLException e) {
            e.printStackTrace();
          }
        }
      } // finally
    }
  }

  private void createTables() throws IOException, SQLException {
    for (int i = 0; i < TABLE_NAMES.length; ++i) {
      String sqlName = TABLE_NAMES[i] + ".sql";

      String sql = getResourceAsString(SCHEMA_PATH + sqlName);
      if(sql == null) {
        continue;
      }
      
      PreparedStatement ps = null;
      try {
        ps = m_connection.prepareStatement(sql);
        ps.execute();
      } finally {
        if (ps != null) {
          try {
            ps.close();
          } catch (SQLException e) {
            e.printStackTrace();
          }
        }
      } // finally
    } // for
  }

  private String getResourceAsString(String resourceName) throws IOException {
    String result;
    InputStream is = this.getClass().getResourceAsStream(resourceName);
    
    if (is != null) {
      Reader reader = new InputStreamReader(is);
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int length;
      try {
        while ((length = is.read(buffer)) != -1) {
          bos.write(buffer, 0, length);
        }
      } finally {
        if (reader != null) {
          try {
            reader.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
        if (bos != null) {
          try {
            bos.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      } // finally
      result = bos.toString("UTF-8");
    } // if
    else {
      result = null;
    }
    return result;
  }

}
