package de.foxysoft.rhidmo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.mozilla.javascript.Undefined;

public class GlobalFunctions {
	private GlobalFunctions() {
	}

	private static final Log APPL_LOG = Log
			.get("de.foxysoft.rhidmo.Application");

	private static final Log LOG = Log.get(GlobalFunctions.class);

	public static void uWarning(String m) {
		APPL_LOG.warn(m);
	}

	public static void uError(String m) {
		APPL_LOG.error(m);
	}

	public static String uSelect(String sqlStatement,
			Object rowSeparator,
			Object columnSeparator) {
		final String M = "uSelect: ";
		LOG.debug(M
				+ "Entering sqlStatement={}, rowSeparator={}, columnSeparator={}",
				sqlStatement,
				rowSeparator,
				columnSeparator);

		String result = null;
		if (rowSeparator == null || Undefined.instance == rowSeparator
				|| "".equals(rowSeparator)) {
			rowSeparator = "!!";
			LOG.debug(M + "Using default rowSeparator={}",
					rowSeparator);
		}

		if (columnSeparator == null
				|| Undefined.instance == columnSeparator
				|| "".equals(columnSeparator)) {
			columnSeparator = "|";
			LOG.debug(M + "Using default columnSeparator={}",
					columnSeparator);
		}

		Connection c = null;
		ResultSet rs = null;
		PreparedStatement ps = null;

		try {
			c = Utl.getConnection();
			ps = c.prepareStatement(sqlStatement);
			rs = ps.executeQuery();

			StringBuffer resultBuffer = new StringBuffer();
			int columnCount = rs.getMetaData()
					.getColumnCount();

			for (int i = 1; rs.next(); ++i) {
				if (i > 1) {
					resultBuffer.append(rowSeparator);
				}
				for (int j = 1; j <= columnCount; ++j) {
					if (j > 1) {
						resultBuffer.append(columnSeparator);
					}
					resultBuffer.append(rs.getObject(j));
				}
			}

			result = resultBuffer.toString();

		} catch (Exception e) {
			LOG.error(e);
			result = "!ERROR: " + Log.getStackTrace(e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
				}
			}
			if (ps != null) {
				try {
					ps.close();
				} catch (Exception e) {
				}
			}
			if (c != null) {
				try {
					c.close();
				} catch (Exception e) {
				}
			}
		}

		LOG.debug(M + "Returning {}",
				result);
		return result;
	}

}
