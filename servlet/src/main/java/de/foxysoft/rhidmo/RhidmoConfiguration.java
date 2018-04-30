/*******************************************************************************
 * Copyright 2017 Sietze Roorda
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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Properties;

import javax.mail.Session;

public class RhidmoConfiguration {
	private static final Log LOG = Log.get(RhidmoConfiguration.class);

	static RhidmoConfiguration myConfiguration = null;
	Properties myProperties;
	Session myEmailSession;
	boolean isObsoletedColumnAvailable = false;
	
	private RhidmoConfiguration () {
		final String M = "Constructor: ";

		// Check if column mcIsObsoleted is there or not.
		try {
			Connection conn = Utl.getConnection();
			DatabaseMetaData md = conn.getMetaData();
			ResultSet rs = md.getColumns(null,  null,  "mc_package_scripts", "mcIsObsoleted");
			if(rs.next()) {
				this.isObsoletedColumnAvailable = true;
				LOG.debug(M + "Column mcIsobsoleted is available in table mc_package_scripts");
			}
		}
		catch(Exception e) {
			LOG.error(M + "Getting database meta data");
			LOG.error(e);
		}
	}

	public static synchronized RhidmoConfiguration getInstance() {
		if(myConfiguration == null) {
			myConfiguration = new RhidmoConfiguration();			
		}
		return myConfiguration;		
	}
	
	public void setProperties(Properties prop) {
		this.myProperties = prop;
	}
	
	public Properties getProperties() {
		return this.myProperties;
	}

	public void setEmailSession(Session mailSession) {
		this.myEmailSession = mailSession;
	}
	
	public Session getEmailSession() {
		return this.myEmailSession;
	}
	
	public boolean getIsObsoletedColumnAvailable() throws Exception {
		return isObsoletedColumnAvailable;
	}
}