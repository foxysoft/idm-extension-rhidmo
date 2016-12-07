package de.foxysoft.rhidmo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Locale;

import org.apache.commons.codec.binary.Base64;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.Scriptable;

import net.bytebuddy.implementation.bind.annotation.RuntimeType;

public class TaskProcessingStatic {
	private static final Log LOG = Log.get(TaskProcessingStatic.class);

	/**
	 * public IdMValueChange[] onSubmit( Locale locale , int subjectMSKEY , int
	 * objectMSKEY , Task task , IdMSubmitData validate)
	 * 
	 * throws IdMExtensionException
	 * 
	 * @return
	 */
	public static @RuntimeType Object[] onSubmit(Locale locale,
			int subjectMSKEY,
			int objectMSKEY,
			Object task,
			Object validate) {
		return service("ONSUBMIT",
				locale,
				subjectMSKEY,
				objectMSKEY,
				task,
				validate);
	}

	/**
	 * public IdmValue[] onLoad( Locale locale , int subjectMSKEY , int
	 * objectMSKEY , Task task , IdMLoadData data)
	 * 
	 * throws IdMExtensionException
	 * 
	 * @return
	 */
	public static @RuntimeType Object[] onLoad(Locale locale,
			int subjectMSKEY,
			int objectMSKEY,
			Object task,
			Object data) {
		return service("ONLOAD",
				locale,
				subjectMSKEY,
				objectMSKEY,
				task,
				data);
	}

	public static Object[] service(String eventName,
			Locale locale,
			int subjectMSKEY,
			int objectMSKEY,
			Object task,
			Object data) {
		final String M = "service: ";
		LOG.debug(M + "Entering eventName = {}",
				eventName);
		Object[] result = null;
		String scriptDefinition = null;
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			int taskId = (Integer) task.getClass()
					.getMethod("getID",
							(Class<?>[]) null)
					.invoke(task,
							(Object[]) null);
			LOG.debug(M + "taskId = {}",
					taskId);

			String scriptName = (String) task.getClass()
					.getMethod("getParameter",
							new Class<?>[] { String.class })
					.invoke(task,
							new Object[] { eventName });
			if (scriptName != null) {
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
					scriptDefinition = rs.getString(1)
							.substring("{B64}".length());
					LOG.debug(M + "scriptDefinition = {}",
							scriptDefinition);

					String scriptSource = new String(
							Base64.decodeBase64(scriptDefinition),
							"UTF-8");
					LOG.debug(M + "scriptSource = {}",
							scriptSource);

					Context context = Context.enter();
					try {

						Scriptable scope = context
								.initStandardObjects();

						Utl.registerGlobalFunctions(scope);
						
						Scriptable thisObj = context.newObject(scope);

						Function f = context.compileFunction(scope,
								scriptSource,
								scriptName,
								1,
								null);
						LOG.debug(M + "f = {}",
								f);

						Object resultJS = f.call(context,
								scope,
								thisObj,
								new Object[] { locale, subjectMSKEY,
										objectMSKEY, task, data });
						result = (Object[]) Context.jsToJava(resultJS,
								Object[].class);
					} finally {
						Context.exit();
					}
				} else {
					LOG.error(M
							+ "Script {} not found in package of task {}",
							scriptName,
							taskId);
				}
			} // if(scriptName != null)
			else {
				LOG.warn(M
						+ "Task {} has no parameter SCRIPT; onLoad will do nothing");
			}
		} catch (Exception e) {
			LOG.error(e);
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
				result);
		return result;
	}
}
