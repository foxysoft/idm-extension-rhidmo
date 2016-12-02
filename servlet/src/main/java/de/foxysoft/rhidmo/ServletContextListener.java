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
