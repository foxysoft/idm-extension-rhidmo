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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log {
	private Logger m_logger;

	private Log() {
	}

	private Log(Logger logger) {
		m_logger = logger;
	}

	public static Log get(Class<?> c) {
		return new Log(LoggerFactory.getLogger(c));
	}

	public static Log get(String name) {
		return new Log(LoggerFactory.getLogger(name));
	}

	public void debug(String m) {
		m_logger.debug(m);
	}

	public void debug(String m,
			Object... args) {
		m_logger.debug(m,
				args);
	}

	public void warn(String m) {
		m_logger.warn(m);
	}

	public void warn(String m,
			Object... args) {
		m_logger.warn(m,
				args);
	}

	public void error(String m) {
		m_logger.debug(m);
	}

	public void error(String m,
			Object... args) {
		m_logger.debug(m,
				args);
	}

	public void error(Throwable t) {
		m_logger.error(Log.getStackTrace(t));
	}

	public static String getStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return sw.toString();
	}
}
