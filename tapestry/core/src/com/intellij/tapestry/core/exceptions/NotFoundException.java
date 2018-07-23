package com.intellij.tapestry.core.exceptions;

/**
 * Thrown when the information the caller was looking for wasn't found.
 */
public class NotFoundException extends Exception {

	private static final long serialVersionUID = 321998954117261469L;

	public NotFoundException() {
        super();
    }//Constructor

    public NotFoundException(Throwable cause) {
        super(cause);
    }//Constructor
    
}//NotFoundException
