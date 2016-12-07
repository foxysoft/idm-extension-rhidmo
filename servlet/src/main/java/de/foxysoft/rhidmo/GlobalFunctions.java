package de.foxysoft.rhidmo;

public class GlobalFunctions {
	private GlobalFunctions() {
	}

	private static final Log LOG = Log
			.get("de.foxysoft.rhidmo.Application");

	public static void uWarning(String m) {
		LOG.warn(m);
	}

}
