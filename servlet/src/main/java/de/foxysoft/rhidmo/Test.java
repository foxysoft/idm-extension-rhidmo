package de.foxysoft.rhidmo;

import javax.naming.InitialContext;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;

public class Test {
	public static void main(String[] args) throws Exception {
		if (args == null || args.length == 0
				|| args[0].equalsIgnoreCase("jndi")) {
			doJndi();
		} else if (args[0].equalsIgnoreCase("subclass")) {
			doSubclass();
		}
	}

	private static void doSubclass() throws Exception {
		Class c = Class.forName("com.sap.idm.extension.TaskProcessingAdapter");
		DynamicType.Unloaded<?> dynamicType = new ByteBuddy().subclass(c)
				.name("de.foxysoft.rhidmo.TaskProcessing").make();
	}

	private static void doJndi() throws Exception {
		final String M = "doJndi: ";
		InitialContext ctx = new InitialContext();
		Object o = ctx.lookup("IDM");
		trc(M + "o=" + o);

	}

	private static String trc(String msg) {
		System.out.println(msg);
		return msg;
	}
}
