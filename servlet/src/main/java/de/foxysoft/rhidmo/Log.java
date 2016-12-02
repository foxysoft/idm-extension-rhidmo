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

	public void debug(String m) {
		m_logger.debug(m);
	}

	public void debug(String m,
			Object... args) {
		m_logger.debug(m,
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
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		m_logger.error(sw.toString());
	}
}
