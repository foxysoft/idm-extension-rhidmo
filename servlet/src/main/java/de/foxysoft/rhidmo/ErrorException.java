package de.foxysoft.rhidmo;

public class ErrorException extends BaseException {
	private final static long serialVersionUID = 1L;

	public ErrorException(Throwable cause) {
		super(cause);
	}

	public ErrorException(String m) {
		super(m);
	}

}
