package com.intellij.flex.compiler;

import flex2.compiler.Logger;
import flex2.compiler.common.Configuration;
import flex2.compiler.config.ConfigurationException;
import flex2.tools.oem.Builder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

public abstract class SdkSpecificHandler {

  /**
   * similar to flex2.tools.oem.internal.LibraryCompiler.init()
   */
  public abstract void initThreadLocals(final Logger logger);

  /**
   * similar to flex2.tools.oem.internal.LibraryCompiler.clean()
   */
  public abstract void cleanThreadLocals();

  /**
   * similar to flex2.tools.oem.internal.LibraryCompiler.getCompcConfiguration(String[] args)
   * or flex2.tools.oem.internal.ApplicationCompiler.getCommandLineConfiguration(String[] args)
   */
  public abstract Configuration processConfiguration(final String[] params) throws ConfigurationException, IOException;

  /**
   * similar to flex2.tools.oem.internal.LibraryCompiler.run(...)
   * or flex2.tools.oem.internal.ApplicationCompiler.run(...)
   */
  public abstract Builder createBuilder(final Configuration configuration) throws FileNotFoundException, ConfigurationException, URISyntaxException;

  public boolean omitTrace(final Configuration configuration) {
    return false;
  }

  public void setupOmitTraceOption(final boolean omitTrace){

  }
}
