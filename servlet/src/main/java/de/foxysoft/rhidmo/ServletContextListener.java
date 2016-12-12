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

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletContextEvent;

public class ServletContextListener
		implements javax.servlet.ServletContextListener {
	private static final Log LOG = Log.get(ServletContextListener.class);

	public void contextInitialized(ServletContextEvent event) {
		final String M = "contextInitialized: ";
		LOG.debug(M + "Entering");
		try {
			Init.doInit();
		} catch (Exception e) {
			// TODO: check writing of stacktrace by SLF4J
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			LOG.error(M + "Exception occurred",
					e);
		}
		LOG.debug(M + "Exiting");
	}

	public void contextDestroyed(ServletContextEvent event) {
		final String M = "contextDestroyed: ";
		LOG.debug(M + "Entering");
		LOG.debug(M + "Exiting");
	}

}
