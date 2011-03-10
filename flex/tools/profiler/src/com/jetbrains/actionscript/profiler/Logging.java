package com.jetbrains.actionscript.profiler;

import com.intellij.idea.LoggerFactory;
import com.intellij.openapi.diagnostic.Logger;

/** 
 * User: Maxim
 * Date: 27.09.2010
 * Time: 19:29:26
 */
public class Logging {
  private static final Logger LOG = LoggerFactory.getInstance().getLoggerInstance(Logging.class.getName());
  public static final boolean doStat = LOG.isDebugEnabled();
  public static boolean is_debug = true;

  static final void log(String s) {
    LOG.warn(s);
  }

  static void log(Throwable ex) {
    LOG.error(ex);
  }

  public static void stat(String s) {
    LOG.debug(s);
  }
}
