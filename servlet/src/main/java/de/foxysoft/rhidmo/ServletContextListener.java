package de.foxysoft.rhidmo;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletContextEvent;

public class ServletContextListener
		implements javax.servlet.ServletContextListener {
	public void contextInitialized(ServletContextEvent event) {
		final String M = "ServletContextListener.contextInitialized: ";
		Utl.trc(M + "Entering");
		try {
			Init.doInit();
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			Utl.trc(M + sw);
		}
		Utl.trc(M + "Exiting");
	}

	public void contextDestroyed(ServletContextEvent event) {
		final String M = "ServletContextListener.contextDestroyed: ";
		Utl.trc(M + "Entering");
		Utl.trc(M + "Exiting");
	}

}
