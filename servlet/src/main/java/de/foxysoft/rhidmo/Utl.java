package de.foxysoft.rhidmo;

import java.sql.Connection;

import javax.naming.InitialContext;
import javax.sql.DataSource;

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

}
