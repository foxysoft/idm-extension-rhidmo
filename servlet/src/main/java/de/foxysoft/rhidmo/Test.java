package de.foxysoft.rhidmo;

import java.util.Locale;

import javax.naming.InitialContext;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.matcher.ElementMatchers;

public class Test {
	public static void main(String[] args) throws Exception {
		final String M = "main: ";
		InitialContext ctx = new InitialContext();
		Object o = ctx.lookup("IDM");
		trc(M + "o = " + o);

		// The JMX class loader has access to all the IDM Extension Framework classes
		ClassLoader jmxClassLoader = o.getClass().getClassLoader();
		trc(M + "jmxClassLoader = " + jmxClassLoader);

		// The context classloader has access to all Rhidmo classes
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		trc(M + "contextClassLoader = " + contextClassLoader);

		// The junction classloader combines both using delegation 
		ClassLoader junctionClassLoader = new JunctionClassLoader(contextClassLoader, jmxClassLoader);

		Class<?> baseClass = Class.forName("com.sap.idm.extension.TaskProcessingAdapter", true, jmxClassLoader);
		trc(M + "c.getName() = " + baseClass.getName());
		Class<?> subClass = new ByteBuddy().subclass(baseClass).name("de.foxysoft.rhidmo.TaskProcessing")
				.method(ElementMatchers.named("onSubmit")).intercept(MethodDelegation.to(Test.class))
				.method(ElementMatchers.named("onLoad")).intercept(MethodDelegation.to(Test.class)).make()
				.load(junctionClassLoader).getLoaded();
		trc(M + "subClass.getName() = " + subClass.getName());

		Class<?> interfaceClass = Class.forName("com.sap.idm.extension.ITaskProcessing", true, jmxClassLoader);
		o.getClass().getMethod("registerTaskProcessingInterface", new Class<?>[] { interfaceClass }).invoke(o,
				new Object[] { subClass.newInstance() });
		trc(M + "subClass registered");
	}

	private static String trc(String msg) {
		System.out.println(msg);
		return msg;
	}

	/**
	 * public IdMValueChange[] onSubmit( Locale locale , int subjectMSKEY , int
	 * objectMSKEY , Task task , IdMSubmitData validate)
	 * 
	 * throws IdMExtensionException
	 * 
	 * @return
	 */
	public static @RuntimeType Object[] onSubmit(Locale locale, int subjectMSKEY, int objectMSKEY, Object task,
			Object validate) {
		final String M = "onSubmit: ";
		trc(M + "called");
		Object[] result = null;
		try {
			result = (Object[]) validate.getClass().getMethod("getChangeList", (Class<?>[]) null).invoke(validate,
					(Object[]) null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	/**
	 * public IdmValue[] onLoad( Locale locale , int subjectMSKEY , int
	 * objectMSKEY , Task task , IdMLoadData data)
	 * 
	 * throws IdMExtensionException
	 * 
	 * @return
	 */
	public static @RuntimeType Object[] onLoad(Locale locale, int subjectMSKEY, int objectMSKEY, Object task,
			Object data) {
		final String M = "onLoad: ";
		trc(M + "called");
		Object[] result = null;
		try {
			result = (Object[]) data.getClass().getMethod("getValues", (Class<?>[]) null).invoke(data, (Object[]) null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return result;
	}
}
