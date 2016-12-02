package de.foxysoft.rhidmo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Locale;

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
		final String M = "onSubmit: ";
		LOG.debug(M + "Entering");
		Object[] result = null;

		// Return null to indicate: no changes to loaded data

		LOG.debug(M + "Returning " + result);
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
	public static @RuntimeType Object[] onLoad(Locale locale,
			int subjectMSKEY,
			int objectMSKEY,
			Object task,
			Object data) {
		final String M = "onLoad: ";
		LOG.debug(M + "Entering");
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
							new Object[] { "SCRIPT" });
			if (scriptName != null) {
				c = Utl.getConnection();
				ps = c.prepareStatement("select a.mcscriptdefinition"
						+ "    from mc_package_scripts a"
						+ "    inner join mxp_tasks b"
						+ "    on a.mcpackageid=b.mcpackageid"
						+ "    and b.taskid=?");
				ps.setInt(1, taskId);
				ps.execute();
				rs = ps.getResultSet();
				if (rs.next()) {
					scriptDefinition = rs.getString(1)
							.substring("{B64}".length());
					LOG.debug(M + "scriptDefinition = {}",
							scriptDefinition);
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
		// Return null to indicate: no changes to loaded data

		LOG.debug(M + "Returning " + result);
		return result;
	}
}
