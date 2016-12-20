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
package de.foxysoft.rhidmo.test;

import org.junit.Assert;
import org.junit.Test;

import de.foxysoft.rhidmo.ErrorException;
import de.foxysoft.rhidmo.PackageScript;

public class TestPackageScript {
	private static final String SCRIPT_NAME = "Z_TEST";
	private static final String PKG_NAME = "de.foxysoft.test";
	private static final String SEPARATOR = "/";
	private static final String QUALIFIED_NAME = PKG_NAME + SEPARATOR
			+ SCRIPT_NAME;

	@Test
	public void testNoPkg() throws Exception {
		PackageScript p = new PackageScript(SCRIPT_NAME);
		Assert.assertEquals(p.getPackageName(),
				null);
		Assert.assertEquals(p.getScriptName(),
				SCRIPT_NAME);
	}

	@Test
	public void testWithPkg() throws Exception {
		PackageScript p = new PackageScript(QUALIFIED_NAME);
		Assert.assertEquals(p.getPackageName(),
				PKG_NAME);
		Assert.assertEquals(p.getScriptName(),
				SCRIPT_NAME);
	}

	@Test
	public void testTrailingSeparator() throws Exception {
		try {
			String qname = PKG_NAME + SEPARATOR;
			new PackageScript(qname);
			Assert.fail(qname + " didn't throw any exception");
		} catch (ErrorException e) {
		}
	}

	@Test
	public void testLeadingSeparator() throws Exception {
		try {
			String qname = SEPARATOR + SCRIPT_NAME;
			new PackageScript(qname);
			Assert.fail(qname + " didn't throw any exception");
		} catch (ErrorException e) {
		}
	}
}
