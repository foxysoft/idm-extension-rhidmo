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

import java.util.Locale;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
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

	public static Object[] service(String eventName,
			Locale locale,
			int subjectMSKEY,
			int objectMSKEY,
			Object task,
			Object data) throws Exception {
		final String M = "service: ";
		LOG.debug(M + "Entering eventName = {}",
				eventName);
		Object[] result = null;

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
				String scriptContent = Utl.getDecodedScript(scriptName,
						taskId);

				Context context = Context.enter();

				Scriptable scope = context.initStandardObjects();

				Utl.registerPublicStaticMethodsInScope(
						GlobalFunctions.class,
						scope);

				Scriptable thisObj = context.newObject(scope);

				Function f = context.compileFunction(scope,
						scriptContent,
						scriptName,
						1,
						null);
				LOG.debug(M + "f = {}",
						f);
				try {

					Object resultJS = f.call(context,
							scope,
							thisObj,
							new Object[] { locale, subjectMSKEY,
									objectMSKEY, task, data });

					result = (Object[]) Context.jsToJava(resultJS,
							Object[].class);

				} catch (JavaScriptException jse) {
					Object jsObject = jse.getValue();
					LOG.debug(M + "jsObject = {}",
							jsObject);

					Object idmExceptionObject = Context.jsToJava(
							jsObject,
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

							// Throw inner IdMExtensionException
							throw toThrow;

						} // if
							// (idmExceptionClass.isInstance(idmExceptionObject))
					} // if(idmExceptionObject != null)

					// Otherwise log and throw original JavaScriptException
					LOG.error(jse);
					throw jse;

				} // catch(JavaScriptException e)
			} // if (scriptName != null)
			else {
				LOG.warn(M
						+ "Task {} has no parameter {}; will do nothing",
						taskId,
						eventName);
			}
		} finally {
			Context.exit();
		}
		LOG.debug(M + "Returning {}",
				result);
		return result;
	}
}
