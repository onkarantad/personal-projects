package com.sapiens.ssi.exceptions;

// this exception is gets thrown when record injestion faild to mongoDb, thrown up till source class
public class MessageInjestionFailedException extends Exception {


	private static final long serialVersionUID = 1L;

	public MessageInjestionFailedException(String message)
	  {
	    super(message);
	  }
}
