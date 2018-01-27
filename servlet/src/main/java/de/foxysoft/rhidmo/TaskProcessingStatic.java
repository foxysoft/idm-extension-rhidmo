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

import java.util.List;
import java.util.Locale;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

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
			Object validate) throws Exception {
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
			Object data) throws Exception {
		return service("ONLOAD",
				locale,
				subjectMSKEY,
				objectMSKEY,
				task,
				data);
	}

	private static Object[] service(String eventName,
			Locale locale,
			int subjectMSKEY,
			int objectMSKEY,
			Object task,
			Object data) throws Exception {
		final String M = "service: ";
		LOG.debug(M + "Entering eventName = {}",
				eventName);
		Object[] result = null;

		Context context = Context.enter();

		// Begin OUTER try: handle ANY exception to ensure proper logging
		try {

			int taskId = (Integer) task.getClass()
					.getMethod("getID",
							(Class<?>[]) null)
					.invoke(task,
							(Object[]) null);
			LOG.debug(M + "taskId = {}",
					taskId);

			List<PackageScript> packageScripts = Utl
					.getScriptNamesOfTask(task,
							eventName);

			Utl.fetchScriptSource(packageScripts,
					taskId);

			// Scriptable scope = context.initStandardObjects();
			Scriptable scope = new ImporterTopLevel(Context.getCurrentContext());

			Scriptable gf = new GlobalFunctions(task);
			gf.setParentScope(scope);

			Utl.registerPublicMethodsInScope(
					GlobalFunctions.class,
					scope, gf);

			Function f = Utl.execScriptsInScope(packageScripts,
					scope,
					context,
					taskId);

			Scriptable thisObj = context.newObject(scope);

			// Begin INNER try: handle exceptions from JS function
			// Case 1: IdMExtensionException from app to display UI message
			// Case 2: any other exception from app or Rhino on error
			try {

				Object resultJS = f.call(context,
						scope,
						thisObj,
						new Object[] { locale, subjectMSKEY,
								objectMSKEY, task, data });

				if(resultJS != null && Undefined.instance != resultJS) {
					result = (Object[]) Context.jsToJava(resultJS, Object[].class);
				}

			} // End INNER try
			catch (JavaScriptException jse) {
				Object jsObject = jse.getValue();
				LOG.debug(M + "jsObject = {}",
						jsObject);

				Object idmExceptionObject = Context.jsToJava(jsObject,
						Object.class);
				LOG.debug(M + "idmExceptionObject = {}",
						idmExceptionObject);

				if (idmExceptionObject != null) {
					ClassLoader combinedClassLoader = Context
							.getCurrentContext()
							.getApplicationClassLoader();
					LOG.debug(M + "combinedClassLoader = {}",
							combinedClassLoader);
					Class<?> idmExceptionClass = Class.forName(
							"com.sap.idm.extension.IdMExtensionException",
							false,
							combinedClassLoader);
					if (idmExceptionClass
							.isInstance(idmExceptionObject)) {
						Exception toThrow = (Exception) idmExceptionObject;
						LOG.debug(M
								+ "Application raised IdMExtensionException {}",
								Log.getStackTrace(toThrow));

						// Case 1: IdMExtensionException => re-throw wrapped
						throw new MessageException(toThrow);

					} // if(idmExceptionClass.isInstance(idmExceptionObject))
				} // if(idmExceptionObject != null)

				// Case 2: other JavaScriptException => re-throw as is
				throw jse;
			} // catch (JavaScriptException jse) {

		} // End OUTER try
		catch (MessageException me) {
			throw (Exception) me.getCause();
		} catch (Exception e) {
			LOG.error(e);
			throw e;
		} finally {
			Context.exit();
		}
		LOG.debug(M + "Returning {}",
				result);
		return result;
	}
}
