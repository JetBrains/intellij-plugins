package com.intellij.flex.compiler.flex4;

import com.intellij.flex.compiler.FlexCompilerUtil;
import flex2.compiler.CompilerAPI;
import flex2.compiler.common.Configuration;
import flex2.compiler.common.DefaultsConfigurator;
import flex2.compiler.config.ConfigurationBuffer;
import flex2.compiler.config.ConfigurationException;
import flex2.compiler.io.VirtualFile;
import flex2.compiler.util.ThreadLocalToolkit;
import flex2.tools.CompcConfiguration;
import flex2.tools.Mxmlc;
import flex2.tools.WebTierAPI;
import flex2.tools.oem.Builder;
import flex2.tools.oem.Library;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Flex4CompcHandler extends Flex4Handler {

  public Configuration processConfiguration(final String[] params) throws ConfigurationException, IOException {
    final ConfigurationBuffer cfgbuf = new ConfigurationBuffer(CompcConfiguration.class, CompcConfiguration.getAliases());
    cfgbuf.setDefaultVar("include-classes");
    DefaultsConfigurator.loadCompcDefaults(cfgbuf);
    return Mxmlc.processConfiguration(ThreadLocalToolkit.getLocalizationManager(), "compc", params, cfgbuf,
                                      CompcConfiguration.class, "include-classes");
  }

  public Builder createBuilder(final Configuration configuration)
    throws ConfigurationException, FileNotFoundException, URISyntaxException {

    final Library library = new Library();

    Flex4MxmlcHandler.setupConfiguration(library, configuration);

    final String outputFilePath = ((CompcConfiguration)configuration).getOutput();
    if (outputFilePath == null) {
      throw new ConfigurationException("Output file not set");
    }
    final File outputFile = new File(outputFilePath);
    FlexCompilerUtil.ensureFileCanBeCreated(outputFile);
    library.setOutput(outputFile);

    final CompcConfiguration compcConfiguration = (CompcConfiguration)configuration;

    for (final Object includedClass : compcConfiguration.getClasses()) {
      library.addComponent((String)includedClass);
    }

    final List fileList = CompilerAPI
      .getVirtualFileList(compcConfiguration.getIncludeSources(), new HashSet<String>(Arrays.asList(WebTierAPI.getSourcePathMimeTypes())));

    for (final Object includedSource : fileList) {
      library.addComponent(new File(((VirtualFile)includedSource).getName()));
    }

    final Map stylesheetNameToPathMap = compcConfiguration.getStylesheets();
    for (final Object stylesheetNameToPath : stylesheetNameToPathMap.entrySet()) {
      final Map.Entry stylesheetNameToPathEntry = (Map.Entry)stylesheetNameToPath;
      library.addStyleSheet((String)stylesheetNameToPathEntry.getKey(),
                            new File(((VirtualFile)stylesheetNameToPathEntry.getValue()).getName()));
    }

    for (final Object includedResourceBundle : compcConfiguration.getIncludeResourceBundles()) {
      library.addResourceBundle((String)includedResourceBundle);
    }

    for (final Object includedNamespace : compcConfiguration.getNamespaces()) {
      library.addComponent(new URI((String)includedNamespace));
    }

    final Map includedFileNameToPathMap = compcConfiguration.getFiles();
    for (final Object includedFileNameToPath : includedFileNameToPathMap.entrySet()) {
      Map.Entry includedFileNameToPathEntry = (Map.Entry)includedFileNameToPath;
      library.addArchiveFile((String)includedFileNameToPathEntry.getKey(),
                             new File(((VirtualFile)includedFileNameToPathEntry.getValue()).getName()));
    }
    return library;
  }
}
