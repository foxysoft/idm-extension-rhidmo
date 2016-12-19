package de.foxysoft.rhidmo;

public class BaseException extends RuntimeException {
	private final static long serialVersionUID = 1L;

	public BaseException(Throwable cause) {
		super(cause);
	}
	
	public BaseException(String m) {
		super(m);
	}

}
