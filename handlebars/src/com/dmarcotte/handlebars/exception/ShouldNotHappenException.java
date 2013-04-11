package com.dmarcotte.handlebars.exception;

/**
 * A ShouldNotHappenException asserts:
 * <p/>
 * The code should NEVER get here, so fail noisily if we DO get here since that probably means we broke something...
 * <p/>
 * ... also print an embarrassing message so that when this inevitably ends up in front of a user, they ideally smile
 */
public class ShouldNotHappenException extends RuntimeException {
  public ShouldNotHappenException() {
    super("You are seeing this exception because I thought it would NEVER happen.  I was wrong.  Again.");
  }
}
