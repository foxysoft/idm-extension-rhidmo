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

public class PackageScript {

	private String m_packageName;
	private String m_scriptName;
	private String m_scriptSource;

	public PackageScript(String qualifiedName) {
		int separatorIndex = qualifiedName.indexOf('/');
		if (separatorIndex != -1) {
			m_packageName = qualifiedName.substring(0,
					separatorIndex);

			// Disallow leading and trailing separator
			if (separatorIndex > 0
					&& separatorIndex < qualifiedName.length() - 1) {
				m_scriptName = qualifiedName
						.substring(separatorIndex + 1);
			} else {
				throw new ErrorException(qualifiedName
						+ " is not a valid package script qualifiedName.");
			}
		} else {
			m_packageName = null;
			m_scriptName = qualifiedName;
		}
	}

	public void setScriptSource(String scriptSource) {
		m_scriptSource = scriptSource;
	}

	public String getScriptSource() {
		return m_scriptSource;
	}

	public String getPackageName() {
		return m_packageName;
	}

	public String getScriptName() {
		return m_scriptName;
	}

}
