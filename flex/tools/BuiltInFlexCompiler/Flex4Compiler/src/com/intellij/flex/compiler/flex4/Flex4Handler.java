package com.intellij.flex.compiler.flex4;

import com.intellij.flex.compiler.SdkSpecificHandler;
import flash.localization.LocalizationManager;
import flash.localization.ResourceBundleLocalizer;
import flex2.compiler.CompilerAPI;
import flex2.compiler.Logger;
import flex2.compiler.common.CompilerConfiguration;
import flex2.compiler.common.Configuration;
import flex2.compiler.common.DefaultsConfigurator;
import flex2.compiler.config.ConfigurationBuffer;
import flex2.compiler.config.ConfigurationException;
import flex2.compiler.util.ThreadLocalToolkit;
import flex2.tools.CommandLineConfiguration;
import flex2.tools.Compc;
import flex2.tools.CompcConfiguration;
import flex2.tools.Mxmlc;
import macromedia.asc.util.ContextStatics;

import java.io.IOException;

public class Flex4Handler extends SdkSpecificHandler {

  public void initThreadLocals(final Logger logger) {
    super.initThreadLocals(logger);

    CompilerAPI.useAS3();
    CompilerAPI.usePathResolver();
    final LocalizationManager localizationManager = new LocalizationManager();
    localizationManager.addLocalizer(new ResourceBundleLocalizer());
    ThreadLocalToolkit.setLocalizationManager(localizationManager);
    ThreadLocalToolkit.setLogger(logger);
  }

  public void cleanThreadLocals() {
    CompilerAPI.removePathResolver();
    ThreadLocalToolkit.setLogger(null);
    ThreadLocalToolkit.setLocalizationManager(null);

    super.cleanThreadLocals();
  }

  public boolean isOmitTrace(final boolean isSwf, final String[] params) {
    try {
      final Configuration configuration = processConfiguration(isSwf, params);
      final CompilerConfiguration cc = configuration.getCompilerConfiguration();
      return !cc.debug() && cc.omitTraceStatements();
    } catch (Throwable t) {
      // if API changed
      return false;
    }
  }

  /**
   * similar to flex2.tools.oem.internal.LibraryCompiler.getCompcConfiguration(String[] args)
   * or flex2.tools.oem.internal.ApplicationCompiler.getCommandLineConfiguration(String[] args)
   */
  private Configuration processConfiguration(final boolean isSwf, final String[] params) throws ConfigurationException, IOException {
    if (isSwf) {
      final ConfigurationBuffer cfgbuf = new ConfigurationBuffer(CommandLineConfiguration.class, CommandLineConfiguration.getAliases());
      cfgbuf.setDefaultVar("file-specs");
      DefaultsConfigurator.loadDefaults(cfgbuf);
      return Mxmlc.processConfiguration(ThreadLocalToolkit.getLocalizationManager(), "mxmlc", params, cfgbuf,
        CommandLineConfiguration.class, "file-specs");
    } else {
      final ConfigurationBuffer cfgbuf = new ConfigurationBuffer(CompcConfiguration.class, CompcConfiguration.getAliases());
      cfgbuf.setDefaultVar("include-classes");
      DefaultsConfigurator.loadCompcDefaults(cfgbuf);
      return Mxmlc.processConfiguration(ThreadLocalToolkit.getLocalizationManager(), "compc", params, cfgbuf,
        CompcConfiguration.class, "include-classes");
    }
  }

  public void setupOmitTraceOption(final boolean omitTrace) {
    try {
      ContextStatics.omitTrace = omitTrace;
    } catch (Throwable t) {/* if API changed */}
  }

  public void compileSwf(String[] args) {
    Mxmlc.mxmlc(args);
  }

  public void compileSwc(String[] args) {
    Compc.compc(args);
  }
}
