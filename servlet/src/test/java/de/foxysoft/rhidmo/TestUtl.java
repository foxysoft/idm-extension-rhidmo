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

import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.Scriptable;

public class TestUtl {
	static class MockGlobalFunctions {
		public static void someMethod(String s,
				int i) {
		}
	}

	@Test
	public void testRegisterGlobalFunctions() throws Exception {
		Scriptable mockScope = Mockito.mock(Scriptable.class);
		Utl.registerPublicStaticMethodsInScope(
				MockGlobalFunctions.class,
				mockScope);
		Mockito.verify(mockScope)
				.put(Matchers.eq("someMethod"),
						Matchers.eq(mockScope),
						Matchers.argThat(
								new ArgumentMatcher<FunctionObject>() {
									@Override
									public boolean matches(
											Object argument) {
										FunctionObject fo = (FunctionObject) argument;
										return fo.getFunctionName()
												.equals("someMethod")
												&& fo.getArity() == 2;
									}
								}));
	}
}
