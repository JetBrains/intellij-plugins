package com.intellij.flex.compiler.flex45;

import com.intellij.flex.compiler.FlexCompilerUtil;
import flex2.compiler.common.*;
import flex2.compiler.config.ConfigurationBuffer;
import flex2.compiler.config.ConfigurationException;
import flex2.compiler.util.ThreadLocalToolkit;
import flex2.tools.CommandLineConfiguration;
import flex2.tools.Mxmlc;
import flex2.tools.oem.Application;
import flex2.tools.oem.Builder;
import flex2.tools.oem.internal.OEMConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class Flex45MxmlcHandler extends Flex45Handler {

  public Configuration processConfiguration(final String[] params) throws ConfigurationException, IOException {
    final ConfigurationBuffer cfgbuf = new ConfigurationBuffer(CommandLineConfiguration.class, CommandLineConfiguration.getAliases());
    cfgbuf.setDefaultVar("file-specs");
    DefaultsConfigurator.loadDefaults(cfgbuf);
    return Mxmlc.processConfiguration(ThreadLocalToolkit.getLocalizationManager(), "mxmlc", params, cfgbuf,
                                      CommandLineConfiguration.class, "file-specs");
  }

  public Builder createBuilder(final Configuration configuration) throws FileNotFoundException, ConfigurationException {
    final Application application;

    final List fileSpecs = ((CommandLineConfiguration)configuration).getFileSpecs();
    if (fileSpecs.size() >= 0) {
      final String inputFilePath = (String)fileSpecs.get(fileSpecs.size() - 1);
      application = new Application(new File(inputFilePath));
    }
    else {
      application = new Application();
    }

    setupConfiguration(application, configuration);

    final String outputFilePath = ((CommandLineConfiguration)configuration).getOutput();
    if (outputFilePath == null) {
      throw new ConfigurationException("Output file not set");
    }
    final File outputFile = new File(outputFilePath);
    FlexCompilerUtil.ensureFileCanBeCreated(outputFile);
    application.setOutput(outputFile);

    final List<String> includedResourceBundles = ((CommandLineConfiguration)configuration).getIncludeResourceBundles();
    if (includedResourceBundles != null) {
      ((OEMConfiguration)application.getConfiguration()).setIncludeResourceBundles(toStrings(includedResourceBundles));
    }

    return application;
  }
}
