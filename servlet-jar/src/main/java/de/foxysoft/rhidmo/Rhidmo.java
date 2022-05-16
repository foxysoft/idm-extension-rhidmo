package de.foxysoft.rhidmo;

/**
 * Central point to obtain instances of all Rhidmo singletons. This class
 * wires Rhidmo components up with each other, thereby providing a very
 * simplistic dependency injection surrogate.
 * 
 * Rhidmo initialization has two stages:
 * 
 * <ol>
 * <li><strong>Stage 1:</strong> Wiring up Rhidmo components that have mutual
 * dependencies on each other. This stage is the responsibility
 * of the Rhidmo class. During class initialization, it creates
 * instances of each component and then calls their setXYZ() methods 
 * as required to inject all of their required dependencies. The
 * rationale is to avoid cyclic dependencies of components on each other.
 * <li><strong>Stage 2:</strong> Registering an ITaskProcessing instance 
 * in JMX, obtaining the J2EE application properties of the IDM JMX service
 * and obtaining the J2EE mail session. This stage is the responsibility
 * of de.foxysoft.rhidmo.Init, which is itself triggered by
 * de.foxysoft.rhidmo.ServletContextListener.</li>
 * </ol>
 * 
 * <div>Obtaining Rhidmo components (currently RhidmoConfiguration and Utl)
 * instances via this class' static methods ensures that stage 1 initialization
 * has been performed.</div>
 * <div>Stage 2 initialization, on the other hand, may not have
 * been performed yet. It will typically happen only in a real servlet
 * environment when the Rhidmo web application is started by the container.</div>
 * <div>For unit tests where you need to mock or modify the behavior of
 * one or more Rhidmo components, you should create <strong>local</strong> 
 * instances of the Rhidmo components under test instead of getting
 * components from the Rhidmo class.</div>
 * <div>In particular, <strong>do not</strong> modify the singleton instances
 * returned by this class in any way, e.g. by mocking or overriding their
 * methods or changing their instance data. This is to avoid leaking any of
 * your mocked components leaking into subsequent tests, which could
 * easily happen if you fail to revert/cleanup all your modifications
 * after test.</div>   
 * 
 * @author lambert
 *
 */
public class Rhidmo {
  private static Utl g_utl;
  private static RhidmoConfiguration g_rhidmoConfiguration;

  static {
    g_utl = new Utl();
    g_rhidmoConfiguration = new RhidmoConfiguration();
    g_utl.setRhidmoConfiguration(g_rhidmoConfiguration);
    g_rhidmoConfiguration.setUtl(g_utl);
  }

  /**
   * Disallow creation of instances
   */
  private Rhidmo() {}

  /**
   * Get the singleton implementation of de.foxysoft.rhidmo.Utl
   * 
   * @return
   */
  public static Utl getUtl() {
    return g_utl;
  }
  
  /**
   * Get the singleton implementation of de.foxysoft.rhidmo.RhidmoConfiguration
   */
  public static RhidmoConfiguration getRhidmoConfiguration() {
    return g_rhidmoConfiguration;
  }

}
