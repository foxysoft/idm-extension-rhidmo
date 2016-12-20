/*******************************************************************************
 * Copyright 2017 Lambert Boskamp
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package de.foxysoft.rhidmo;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.codec.binary.Base64;
import org.mozilla.javascript.Function;
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

	public static void registerPublicStaticMethodsInScope(Class<?> c,
			Scriptable scope) {
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
				LOG.debug(M + "Registered {}",
						methodName);
			}

		} // if public static method
	}

	public static String getDecodedScript(String scriptName,
			int taskId) throws Exception {
		final String M = "getDecodedScript: ";
		LOG.debug(M + "Entering scriptName={}, taskId={}",
				scriptName,
				taskId);

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
					+ "    and b.taskid=?"
					+ "    where a.mcscriptname=?");

			ps.setInt(1,
					taskId);
			ps.setString(2,
					scriptName);

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

	public static List<PackageScript> getScriptNamesOfTask(Object task,
			String eventName) throws Exception {
		final String M = "getScriptNamesOfTask: ";

		List<PackageScript> result = new ArrayList<PackageScript>();

		String mainScriptName = (String) task.getClass()
				.getMethod("getParameter",
						new Class<?>[] { String.class })
				.invoke(task,
						new Object[] { eventName });

		LOG.debug(M + "Main script: {} = {}",
				eventName,
				mainScriptName);

		if (mainScriptName != null) {
			result.add(new PackageScript(mainScriptName));
		} else {
			throw new ErrorException(
					"Missing task parameter " + eventName);
		}

		// Always try all parameter names from REQ0 to REQ9,
		// and do not require continuous numbering. That is,
		//
		// REQ0 = x, REQ1 = y, REQ2 = z
		//
		// would be OK, as well as
		//
		// REQ3 = x, REQ5 = y, REQ9 = z
		//
		// Starting with key REQ10, however, keys are required
		// to use CONTINUOUS numbering. The first key that
		// does not exist will stop processing.
		//
		// REQ1 = x, REQ10 = y, REQ11 = z
		//
		// will be OK. However,
		//
		// REQ1 = x, REQ10 = y, REQ12 = z
		//
		// will NOT work as expected because the processing
		// will stop after trying REQ11, which doesn't exist.
		// Script z, given as REQ12, will not be loaded.
		boolean haveMoreParameters = true;
		for (int i = 0; i < 10 || haveMoreParameters; ++i) {
			String requiredScriptParameterName = "REQ" + i;
			String requiredScriptParameterValue = (String) task
					.getClass()
					.getMethod("getParameter",
							new Class<?>[] { String.class })
					.invoke(task,
							new Object[] {
									requiredScriptParameterName });
			if (requiredScriptParameterValue != null) {
				LOG.debug(M + "Required scripts: {} = {}",
						requiredScriptParameterName,
						requiredScriptParameterValue);
				result.add(new PackageScript(
						requiredScriptParameterValue));
			} else {
				haveMoreParameters = false; // ======= exit on next iteration
			}
		}
		return result;
	}

}
