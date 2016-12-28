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
import java.util.Properties;

import javax.naming.InitialContext;

import org.mozilla.javascript.ContextFactory;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;

public class Init {

	private static final Log LOG = Log.get(Init.class);
	private static boolean g_initialized = false;

	public static synchronized void doInit() throws Exception {
		final String M = "doInit: ";
		LOG.debug(M + "Entering");

		if (!g_initialized) {

			InitialContext ctx = new InitialContext();
			Object idmFactory = ctx.lookup("IDM");
			LOG.debug(M + "idmFactory = {}",
					idmFactory);

			// The JMX class loader has access
			// to all the IDM Extension Framework classes
			ClassLoader jmxClassLoader = idmFactory.getClass()
					.getClassLoader();
			LOG.debug(M + "jmxClassLoader = {}",
					jmxClassLoader);

			// The context classloader has access to all Rhidmo classes
			ClassLoader contextClassLoader = Thread.currentThread()
					.getContextClassLoader();
			LOG.debug(M + "contextClassLoader = {}",
					contextClassLoader);

			// The junction classloader combines both using delegation
			ClassLoader combinedClassLoader = new SequentialDelegationClassLoader(
					contextClassLoader, jmxClassLoader);

			// Get adapter base class from JMX class loader
			Class<?> baseClass = Class.forName(
					"com.sap.idm.extension.TaskProcessingAdapter",
					true,
					jmxClassLoader);
			LOG.debug(M + "baseClass.getName() = {}",
					baseClass.getName());

			// Create a new sub class dynamically
			// which intercepts onLoad and onSubmit
			Class<?> subClass = new ByteBuddy().subclass(baseClass)
					.name("de.foxysoft.rhidmo.TaskProcessingDynamic")
					.method(ElementMatchers.named("onSubmit"))
					.intercept(MethodDelegation
							.to(TaskProcessingStatic.class))
					.method(ElementMatchers.named("onLoad"))
					.intercept(MethodDelegation
							.to(TaskProcessingStatic.class))
					.make()
					.load(combinedClassLoader)
					.getLoaded();
			LOG.debug(M + "subClass.getName() = {}",
					subClass.getName());

			// Register the sub class as task processing plug-in
			Class<?> interfaceClass = Class.forName(
					"com.sap.idm.extension.ITaskProcessing",
					true,
					jmxClassLoader);
			idmFactory.getClass()
					.getMethod("registerTaskProcessingInterface",
							new Class<?>[] { interfaceClass })
					.invoke(idmFactory,
							new Object[] { subClass.newInstance() });
			LOG.debug(M + "subClass registered");

			ContextFactory.getGlobal()
					.initApplicationClassLoader(combinedClassLoader);
			LOG.debug(M + "Rhino classloader initialized");
			
			// Get the application properties
			Object appCfg = null;
			try {
				appCfg = ctx.lookup("ApplicationConfiguration");
				if(appCfg == null) {
					LOG.error(M + "Unable to read application configuration");
				}
				else
				{
					LOG.debug(M + "ApplicationConfiguration = {}", appCfg);
				
					Method getApplicationProperties = appCfg.getClass().getMethod("getApplicationProperties");
					Properties props = (Properties) getApplicationProperties.invoke(appCfg);
					if(props == null) {
						LOG.error(M + "No application properties found");
					}
					
					RhidmoConfiguration myConf = RhidmoConfiguration.getInstance();
					myConf.setProperties(props);
				}
			}
			catch(Exception e) {
				LOG.error(e);
			}

			g_initialized = true;
		} else {
			LOG.debug(M + "Already initialized");
		}
		
		LOG.debug(M + "Returning");
	}

}
