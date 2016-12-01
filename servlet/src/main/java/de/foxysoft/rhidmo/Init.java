package de.foxysoft.rhidmo;

import java.lang.reflect.Method;

import javax.naming.InitialContext;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

public class Init {
	private static boolean g_initialized = false;

	public static synchronized void doInit() throws Exception {
		final String M = "Init.doInit: ";
		Utl.trc(M + "Entering");

		if (!g_initialized) {

			InitialContext ctx = new InitialContext();
			Object idmFactory = ctx.lookup("IDM");
			Utl.trc(M + "idmFactory = " + idmFactory);

			Method[] m = idmFactory.getClass()
					.getMethods();
			for (int i = 0; i < m.length; ++i) {
				Utl.trc("idmFactory method[" + i + "]: " + m[i]);
			}

			// The JMX class loader has access
			// to all the IDM Extension Framework classes
			ClassLoader jmxClassLoader = idmFactory.getClass()
					.getClassLoader();
			Utl.trc(M + "jmxClassLoader = " + jmxClassLoader);

			// The context classloader has access to all Rhidmo classes
			ClassLoader contextClassLoader = Thread.currentThread()
					.getContextClassLoader();
			Utl.trc(M + "contextClassLoader = " + contextClassLoader);

			// The junction classloader combines both using delegation
			ClassLoader junctionClassLoader = new JunctionClassLoader(
					contextClassLoader, jmxClassLoader);

			// Get adapter base class from JMX class loader
			Class<?> baseClass = Class.forName(
					"com.sap.idm.extension.TaskProcessingAdapter",
					true,
					jmxClassLoader);
			Utl.trc(M + "baseClass.getName() = " + baseClass.getName());

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
					.load(junctionClassLoader)
					.getLoaded();
			Utl.trc(M + "subClass.getName() = " + subClass.getName());

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
			Utl.trc(M + "subClass registered");
			g_initialized = true;
		} else {
			Utl.trc(M + "Already initialized");
		}
		Utl.trc(M + "Returning");
	}

}
