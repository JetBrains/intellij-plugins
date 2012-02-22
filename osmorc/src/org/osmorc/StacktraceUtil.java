package org.osmorc;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * I am pretty sure a class like this already exists but I can't seem to find it.
 *
 * @author <a href="janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id:$
 */
public class StacktraceUtil {

  private StacktraceUtil() {
  }

  /**
   * Converts a stacktrace to string.
   *
   * @param throwable exception which's stacktrace is to be converted.
   * @return the stacktrace as a string
   */
  public static String stackTraceToString(final Throwable throwable) {
    StringWriter sw = new StringWriter();
    throwable.printStackTrace(new PrintWriter(sw));
    return sw.toString();
  }
}
