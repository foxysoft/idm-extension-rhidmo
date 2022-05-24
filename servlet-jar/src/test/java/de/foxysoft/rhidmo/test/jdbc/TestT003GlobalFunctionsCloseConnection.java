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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;
import java.sql.Connection;
import org.junit.Test;
import de.foxysoft.rhidmo.GlobalFunctions;
import de.foxysoft.rhidmo.RhidmoConfiguration;
import de.foxysoft.rhidmo.Utl;
import de.foxysoft.rhidmo.mock.Task;
import de.foxysoft.rhidmo.test.util.JdbcUnitTest;

public class TestT003GlobalFunctionsCloseConnection extends JdbcUnitTest {
  public TestT003GlobalFunctionsCloseConnection() {
    super("T003");
  }

  private class GlobalFunctionsWithConnectionSpy {
    private Connection m_connectionSpy;
    private GlobalFunctions m_globalFunctions;

    public GlobalFunctionsWithConnectionSpy() throws Exception {
      RhidmoConfiguration conf = mock(RhidmoConfiguration.class);
      when(conf.getProperties()).thenReturn(null);
      Utl utl = mock(Utl.class);

      // Spy on protected member m_connection of super class JdbcUnitTest
      Connection connectionSpy = spy(m_connection);

      when(utl.getConnection()).thenReturn(connectionSpy);
      Task task = mock(Task.class);
      when(task.getID()).thenReturn(1);
      GlobalFunctions functions = new GlobalFunctions(task, conf, utl);

      m_connectionSpy = connectionSpy;
      m_globalFunctions = functions;
    }

    public Connection getConnectionSpy() {
      return m_connectionSpy;
    }

    public GlobalFunctions getGlobalFunctions() {
      return m_globalFunctions;
    }
  }

  @Test
  public void testThatuIS_GetValueCallsClose() throws Exception {
    GlobalFunctionsWithConnectionSpy gfSpy = new GlobalFunctionsWithConnectionSpy();
    gfSpy.getGlobalFunctions().uIS_GetValue(1, 0, "MSKEYVALUE");
    verify(gfSpy.getConnectionSpy()).close();
  }

  @Test
  public void testThatuGetIDStoreCallsClose() throws Exception {
    GlobalFunctionsWithConnectionSpy gfSpy = new GlobalFunctionsWithConnectionSpy();
    gfSpy.getGlobalFunctions().uGetIDStore();
    verify(gfSpy.getConnectionSpy()).close();
  }

  @Test
  public void testThatuIS_nGetValuesCallsClose() throws Exception {
    GlobalFunctionsWithConnectionSpy gfSpy = new GlobalFunctionsWithConnectionSpy();
    gfSpy.getGlobalFunctions().uIS_nGetValues(1, "MSKEYVALUE", "|");
    verify(gfSpy.getConnectionSpy()).close();
  }

  @Test
  public void testThatuSelectCallsClose() throws Exception {
    GlobalFunctionsWithConnectionSpy gfSpy = new GlobalFunctionsWithConnectionSpy();
    gfSpy.getGlobalFunctions().uSelect("select count(*) from idmv_value_basic", null, null);
    verify(gfSpy.getConnectionSpy()).close();
  }

}
