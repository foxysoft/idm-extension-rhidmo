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
