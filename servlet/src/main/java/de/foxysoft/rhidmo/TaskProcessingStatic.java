package de.foxysoft.rhidmo;

import java.util.Locale;

import net.bytebuddy.implementation.bind.annotation.RuntimeType;

public class TaskProcessingStatic {
	/**
	 * public IdMValueChange[] onSubmit( Locale locale , int subjectMSKEY , int
	 * objectMSKEY , Task task , IdMSubmitData validate)
	 * 
	 * throws IdMExtensionException
	 * 
	 * @return
	 */
	public static @RuntimeType Object[] onSubmit(Locale locale,
			int subjectMSKEY,
			int objectMSKEY,
			Object task,
			Object validate) {
		final String M = "TaskProcessingStatic.onSubmit: ";
		Utl.trc(M + "Entering");
		Object[] result = null;

		// Return null to indicate: no changes to loaded data		
		
		Utl.trc(M + "Returning " + result);
		return result;
	}

	/**
	 * public IdmValue[] onLoad( Locale locale , int subjectMSKEY , int
	 * objectMSKEY , Task task , IdMLoadData data)
	 * 
	 * throws IdMExtensionException
	 * 
	 * @return
	 */
	public static @RuntimeType Object[] onLoad(Locale locale,
			int subjectMSKEY,
			int objectMSKEY,
			Object task,
			Object data) {
		final String M = "TaskProcessingStatic.onLoad: ";
		Utl.trc(M + "Entering");
		Object[] result = null;

		// Return null to indicate: no changes to loaded data

		Utl.trc(M + "Returning " + result);
		return result;
	}
}
