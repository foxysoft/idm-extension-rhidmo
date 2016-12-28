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
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import de.foxysoft.rhidmo.Utl;
import de.foxysoft.rhidmo.mock.Task;

public class TestUtl {

	class MockGlobalFunctions extends ScriptableObject {
		public void rhidmo_someMethod(String s,
				int i) {
		}
		
		@Override
		public String getClassName() {
			return getClass().getName();
		}	
	}

	@Test
	public void testRegisterGlobalFunctions() throws Exception {
		Scriptable mockScope = Mockito.mock(Scriptable.class);
		Scriptable mockGlobalFunctionScope = new MockGlobalFunctions();
		Utl.registerPublicMethodsInScope(
				MockGlobalFunctions.class,
				mockScope, mockGlobalFunctionScope);
		Mockito.verify(mockScope)
				.put(Matchers.eq("rhidmo_someMethod"),
						Matchers.eq(mockScope),
						Matchers.argThat(
								new ArgumentMatcher<FunctionObject>() {
									@Override
									public boolean matches(
											Object argument) {
										FunctionObject fo = (FunctionObject) argument;
										return fo.getFunctionName()
												.equals("rhidmo_someMethod")
												&& fo.getArity() == 2;
									}
								}));
	}

}
