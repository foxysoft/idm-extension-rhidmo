package de.foxysoft.rhidmo;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.codec.binary.Base64;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.Scriptable;

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

	public static void registerPublicStaticMethodsInScope(Class<?> c, Scriptable scope) {
		final String M = "registerGlobalFunctions: ";
		Method[] methods = c.getMethods();
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

	public static String getDecodedScript(String scriptName,
			int taskId) throws Exception {
		final String M = "getDecodedScript: ";
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String decodedScript = null;

		try {
			c = Utl.getConnection();
			ps = c.prepareStatement("select a.mcscriptdefinition"
					+ "    from mc_package_scripts a"
					+ "    inner join mxp_tasks b"
					+ "    on a.mcpackageid=b.mcpackageid"
					+ "    and b.taskid=?");
			ps.setInt(1,
					taskId);
			ps.execute();
			rs = ps.getResultSet();
			if (rs.next()) {
				String encodedScript = rs.getString(1)
						.substring("{B64}".length());
				LOG.debug(M + "encodedScript = {}",
						encodedScript);

				decodedScript = new String(
						Base64.decodeBase64(encodedScript), "UTF-8");
				LOG.debug(M + "decodedScript = {}",
						decodedScript);
			} // if(rs.next())
			else {
				throw new RuntimeException("Script " + scriptName
						+ " not found in package of task " + taskId);
			}
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (ps != null)
				try {
					ps.close();
				} catch (Exception e) {
				}
			if (c != null)
				try {
					c.close();
				} catch (Exception e) {
				}
		}

		LOG.debug(M + "Returning {}",
				decodedScript);
		return decodedScript;

	}// getDecodedScript

}
