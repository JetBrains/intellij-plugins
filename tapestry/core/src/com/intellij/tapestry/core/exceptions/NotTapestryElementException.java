package com.intellij.tapestry.core.exceptions;

/**
 * Thrown when an action that only works on a Tapestry component is executed in a not Tapestry component.
 */
public class NotTapestryElementException extends RuntimeException {

	private static final long serialVersionUID = 7399150281613596699L;

	public NotTapestryElementException(String message) {
        super(message);
    }//NotTapestryElementException
	
}//NotTapestryElementException
