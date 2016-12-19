package de.foxysoft.rhidmo;

public class MessageException extends BaseException {
	private final static long serialVersionUID = 1L;

	public MessageException(Throwable cause) {
		super(cause);
	}
	
	public MessageException(String m) {
		super(m);
	}
}
