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

import java.lang.reflect.Member;
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
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.UniqueTag;

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

	public static void registerPublicMethodsInScope(Class<?> c,
			Scriptable parentScope, Scriptable scope) {
		final String M = "registerGlobalFunctions: ";
		Method[] methods = c.getMethods();
		FunctionObject functionObject = null;
		for (int i = 0; i < methods.length; ++i) {
			int modifiers = methods[i].getModifiers();
			if (Modifier.isPublic(modifiers)
					&& (methods[i].getName().startsWith("u") || methods[i].getName().startsWith("rhidmo"))) {

				String methodName = methods[i].getName();
				functionObject = new MyFunctionObject(methodName,
						methods[i],
						scope);
				parentScope.put(methodName,
						parentScope,
						functionObject);
				LOG.debug(M + "Registered {}",
						methodName);
			}

		} // if public static method
	}

	public static Function execScriptsInScope(
			List<PackageScript> packageScripts,
			Scriptable scope,
			Context context,
			int taskId) throws Exception {

		final String M = "execScriptsInScope: ";
		Function result = null;

		for (int i = 0; i < packageScripts.size(); ++i) {
			PackageScript psi = packageScripts.get(i);

			if (psi.getScriptSource() != null) {
				Script script = context.compileString(
						psi.getScriptSource(),
						psi.getScriptName(),
						1,
						null);

				script.exec(context,
						scope);
			} // if
			else {
				LOG.warn(M
						+ "Script {} referenced by parameters of task {} does not exist",
						psi.getScriptName(),
						taskId);
			}
		} // for (int i = 0; i < packageScripts.size(); ++i) {

		String mainScriptName = packageScripts.get(0)
				.getScriptName();
		Object resultObj = scope.get(mainScriptName,
				scope);

		if (UniqueTag.NOT_FOUND.equals(resultObj)) {
			throw new ErrorException("Script " + mainScriptName
					+ " doesn't define any property named "
					+ mainScriptName);
		}

		if (resultObj instanceof Function) {
			result = (Function) resultObj;
		} else {
			throw new ErrorException("Property " + mainScriptName
					+ " is not a function. Class name: "
					+ resultObj.getClass()
							.getName());
		}

		LOG.debug(M + "Returning ",
				result);
		return result;
	}

	public static void fetchScriptSource(
			List<PackageScript> packageScripts,
			int taskId) throws Exception {
		final String M = "fetchScriptSource: ";
		LOG.debug(M + "Entering packageScripts = {}",
				packageScripts);
		if (packageScripts != null && !packageScripts.isEmpty()) {
			Connection c = null;
			PreparedStatement ps = null;
			ResultSet rs = null;
			String decodedScript = null;

			try {
				c = Utl.getConnection();
				StringBuffer sb = new StringBuffer(
						"select a.mcscriptdefinition,"
								+ "    a.mcscriptname"
								+ "    from mc_package_scripts a"
								+ "    inner join mxp_tasks b"
								+ "    on a.mcpackageid=b.mcpackageid"
								+ "    and b.taskid=?"
								+ "    where a.mcscriptname in (");
				for (int i = 0; i < packageScripts.size(); ++i) {
					if (i > 0) {
						sb.append(',');
					}
					sb.append('?');
				}
				sb.append(") and a.mcEnabled = 1 and a.mcScriptLanguage = 'JScript'");

				RhidmoConfiguration myConf = RhidmoConfiguration.getInstance();
				if(myConf.getIsObsoletedColumnAvailable()) {
					sb.append(" and a.mcIsObsoleted = 0");
				}
				String sql = sb.toString();
				LOG.debug(M + "sql = {}",
						sql);

				ps = c.prepareStatement(sql);

				ps.setInt(1,
						taskId);

				for (int i = 0; i < packageScripts.size(); ++i) {
					ps.setString(2 + i,
							packageScripts.get(i)
									.getScriptName());
				}

				ps.execute();
				rs = ps.getResultSet();

				while (rs.next()) {
					String scriptName = rs.getString(2);
					LOG.debug(M + "scriptName = {}",
							scriptName);

					String encodedScript = rs.getString(1)
							.substring("{B64}".length());
					LOG.debug(M + "encodedScript = {}",
							encodedScript);

					decodedScript = new String(
							Base64.decodeBase64(encodedScript),
							"UTF-8");
					LOG.debug(M + "decodedScript = {}",
							decodedScript);

					// Expect i <= 10, so O(i^2) cost for nested loop is
					// acceptable
					for (int i = 0; i < packageScripts.size(); ++i) {
						PackageScript psi = packageScripts.get(i);
						if (scriptName.equals(psi.getScriptName())) {
							psi.setScriptSource(decodedScript);
							break;
						}
					}
				} // while(rs.next())
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
		} // if (packageScripts != null && !packageScripts.isEmpty()) {

		LOG.debug(M + "Returning void, packageScripts = {}",
				packageScripts);
	}// getDecodedScripts

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

	private static class MyFunctionObject extends FunctionObject {
		private static final long serialVersionUID = 1L;

		private MyFunctionObject(String name, Member methodOrConstructor, Scriptable parentScope) {
			super(name, methodOrConstructor, parentScope);
		}

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			return super.call(cx, scope, getParentScope(), args);
		}
	}
}
