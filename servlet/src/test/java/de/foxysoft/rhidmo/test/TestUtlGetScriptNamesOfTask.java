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

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import de.foxysoft.rhidmo.PackageScript;
import de.foxysoft.rhidmo.Utl;
import de.foxysoft.rhidmo.mock.Task;

public class TestUtlGetScriptNamesOfTask {
	private static final String MAIN = "MAIN_SCRIPT";
	private static final String EVENT = "ONSUBMIT";
	private static final String REQ1_KEY = "REQ1";
	private static final String REQ1_VALUE = "FIRST_REQUIRED_SCRIPT";

	@Test
	public void onlyMain() throws Exception {

		List<PackageScript> exp = new ArrayList<PackageScript>(1);
		exp.add(new PackageScript(MAIN));

		Task mockTask = Mockito.mock(Task.class);
		Mockito.when(mockTask.getParameter(Matchers.eq(EVENT)))
				.thenReturn(MAIN);
		List<PackageScript> act = Utl.getScriptNamesOfTask(mockTask,
				EVENT);
		Assert.assertEquals(exp,
				act);
	}

	@Test
	public void mainWithOneRequired() throws Exception {
		List<PackageScript> exp = new ArrayList<PackageScript>(2);
		exp.add(new PackageScript(MAIN));
		exp.add(new PackageScript(REQ1_VALUE));

		Task mockTask = Mockito.mock(Task.class);
		Mockito.when(mockTask.getParameter(Matchers.eq(EVENT)))
				.thenReturn(MAIN);
		Mockito.when(mockTask.getParameter(Matchers.eq(REQ1_KEY)))
				.thenReturn(REQ1_VALUE);

		List<PackageScript> act = Utl.getScriptNamesOfTask(mockTask,
				EVENT);
		Assert.assertEquals(exp,
				act);
	}
}
