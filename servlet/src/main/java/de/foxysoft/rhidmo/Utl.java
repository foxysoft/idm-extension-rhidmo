package de.foxysoft.rhidmo;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;

import javax.naming.InitialContext;
import javax.script.ScriptEngine;
import javax.sql.DataSource;

import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

public class Utl {
	private static final Log LOG = Log.get(Utl.class);

	public static Connection getConnection() throws Exception {
		final String M = "getConnection: ";
		LOG.debug(M + "Entering");
		InitialContext ic = new InitialContext();
		// TODO: lookup doesn't work with java:comp/env prefix
		DataSource datasource = (DataSource) ic
				.lookup("jdbc/RHIDMO_RT");
		Connection result = datasource.getConnection();
		LOG.debug(M + "Returning {}",
				result);
		return result;
	}

	public static void registerGlobalFunctions(Scriptable scope) {
		final String M = "registerGlobalFunctions: ";
		Method[] methods = GlobalFunctions.class.getMethods();
		FunctionObject functionObject = null;
		for (int i = 0; i < methods.length; ++i) {
			int modifiers = methods[i].getModifiers();
			if (Modifier.isPublic(modifiers)
					&& Modifier.isStatic(modifiers)) {

				String methodName = methods[i].getName();
				functionObject = new FunctionObject(methodName,
						methods[i],
						scope);
				scope.put(methodName,
						scope,
						functionObject);
				LOG.debug(M + "Registered {}" + methodName);
			}

		} // if public static method
	}

}
