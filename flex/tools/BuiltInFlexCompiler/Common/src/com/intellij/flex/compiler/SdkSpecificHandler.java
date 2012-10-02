package com.intellij.flex.compiler;

import flex2.compiler.Logger;

public abstract class SdkSpecificHandler {

  private static ThreadLocal<Logger> ourLoggers = new ThreadLocal<Logger>();

  public static Logger getLogger() {
    return ourLoggers.get();
  }

  /**
   * if needed - inheritors should be similar to flex2.tools.oem.internal.LibraryCompiler.init()
   */
  public void initThreadLocals(final Logger logger) {
    ourLoggers.set(logger);
  }

  /**
   * similar to flex2.tools.oem.internal.LibraryCompiler.clean()
   */
  public void cleanThreadLocals() {
    ourLoggers.remove();
  }

  public boolean isOmitTrace(final boolean isSwf, final String[] params) {
    return false;
  }

  public void setupOmitTraceOption(final boolean omitTrace) {
  }

  public abstract void compileSwf(String[] args);

  public abstract void compileSwc(String[] args);
}
