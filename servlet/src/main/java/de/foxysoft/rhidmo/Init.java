package de.foxysoft.rhidmo;

import javax.naming.InitialContext;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

public class Init {
	private static final Log LOG = Log.get(Init.class);	
	private static boolean g_initialized = false;

	public static synchronized void doInit() throws Exception {
		final String M = "doInit: ";
		LOG.debug(M + "Entering");

		if (!g_initialized) {

			InitialContext ctx = new InitialContext();
			Object idmFactory = ctx.lookup("IDM");
			LOG.debug(M + "idmFactory = {}", idmFactory);

			// The JMX class loader has access
			// to all the IDM Extension Framework classes
			ClassLoader jmxClassLoader = idmFactory.getClass()
					.getClassLoader();
			LOG.debug(M + "jmxClassLoader = {}", jmxClassLoader);

			// The context classloader has access to all Rhidmo classes
			ClassLoader contextClassLoader = Thread.currentThread()
					.getContextClassLoader();
			LOG.debug(M + "contextClassLoader = {}", contextClassLoader);

			// The junction classloader combines both using delegation
			ClassLoader combinedClassLoader = new SequentialDelegationClassLoader(
					contextClassLoader, jmxClassLoader);

			// Get adapter base class from JMX class loader
			Class<?> baseClass = Class.forName(
					"com.sap.idm.extension.TaskProcessingAdapter",
					true,
					jmxClassLoader);
			LOG.debug(M + "baseClass.getName() = {}", baseClass.getName());

			// Create a new sub class dynamically
			// which intercepts onLoad and onSubmit
			Class<?> subClass = new ByteBuddy().subclass(baseClass)
					.name("de.foxysoft.rhidmo.TaskProcessingDynamic")
					.method(ElementMatchers.named("onSubmit"))
					.intercept(MethodDelegation
							.to(TaskProcessingStatic.class))
					.method(ElementMatchers.named("onLoad"))
					.intercept(MethodDelegation
							.to(TaskProcessingStatic.class))
					.make()
					.load(combinedClassLoader)
					.getLoaded();
			LOG.debug(M + "subClass.getName() = {}", subClass.getName());

			// Register the sub class as task processing plug-in
			Class<?> interfaceClass = Class.forName(
					"com.sap.idm.extension.ITaskProcessing",
					true,
					jmxClassLoader);
			idmFactory.getClass()
					.getMethod("registerTaskProcessingInterface",
							new Class<?>[] { interfaceClass })
					.invoke(idmFactory,
							new Object[] { subClass.newInstance() });
			LOG.debug(M + "subClass registered");
			g_initialized = true;
		} else {
			LOG.debug(M + "Already initialized");
		}
		LOG.debug(M + "Returning");
	}

}
