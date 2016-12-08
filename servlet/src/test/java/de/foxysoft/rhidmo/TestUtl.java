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
