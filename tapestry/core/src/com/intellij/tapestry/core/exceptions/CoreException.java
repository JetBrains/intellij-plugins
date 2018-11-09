package com.intellij.tapestry.core.exceptions;

/**
 * A generic error occured in the core Loomy code.
 */
public class CoreException extends RuntimeException {
	
	private static final long serialVersionUID = -4569489779744949143L;

	public CoreException(Throwable cause) {
        super(cause);
    }//Constructor
	
}//CoreException
