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
		DataSource datasource = (DataSource) ic
				.lookup("jdbc/notx/IDM_DataSource");
		Connection result = datasource.getConnection();
		LOG.debug(M + "Returning {}",
				result);
		return result;
	}

}
