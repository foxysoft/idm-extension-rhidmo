package de.foxysoft.rhidmo.test.jdbc;

import static org.hamcrest.MatcherAssert.assertThat;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.mockito.Mockito;
import de.foxysoft.rhidmo.PackageScript;
import de.foxysoft.rhidmo.RhidmoConfiguration;
import de.foxysoft.rhidmo.Utl;
import de.foxysoft.rhidmo.test.util.JdbcUnitTest;

public class TestT002FetchScriptsFromOwnAndOtherPackage extends JdbcUnitTest {

  private class MockUtl extends Utl {
    @Override
    public Connection getConnection() throws Exception {
      return TestT002FetchScriptsFromOwnAndOtherPackage.this.m_connection;
    }
  }

  public TestT002FetchScriptsFromOwnAndOtherPackage() {
    super("T002");
  }

  @Test
  public void testMainFromOwnReqFromOther() throws Exception {
    // Mock Utl returning JDBC connection to JdbcUnitTest's H2 database
    Utl mockUtl = new MockUtl();

    // Mock RhidmoConfiguration returning mc_package_scripts.mcisobsoleted doesn't exist
    RhidmoConfiguration mockConfig = Mockito.mock(RhidmoConfiguration.class);
    Mockito.when(mockConfig.getIsObsoletedColumnAvailable()).thenReturn(false);
    mockUtl.setRhidmoConfiguration(mockConfig);

    List<PackageScript> packageScripts = new ArrayList<PackageScript>(5);
    packageScripts.add(new PackageScript("s1"));
    packageScripts.add(new PackageScript("p2/s2"));
    packageScripts.add(new PackageScript("p2/s3"));
    packageScripts.add(new PackageScript("p2/s4"));

    mockUtl.fetchScriptSource(packageScripts, 1);

    for (int i = 0; i < packageScripts.size(); ++i) {
      PackageScript ps = packageScripts.get(i);
      assertThat("Script source of [" + ps.getScriptName() + "] not available",
          ps.getScriptSource() != null);
      assertThat(
          "Script source of [" + ps.getScriptName() + "] doesn't contain function <scriptname>",
          ps.getScriptSource().contains("function " + ps.getScriptName()));
      
      // This final assertion makes sense only for the specific test data hand-crafted
      // for this test. Normally, the script source code wouldn't necessarily contain
      // the package name. Our test data, however, does.
      assertThat(
          "Script source of [" + ps.getScriptName() + "] doesn't contain <packagename>",
          ps.getPackageName() == null || ps.getScriptSource().contains(ps.getPackageName()));
    }
  }

}
