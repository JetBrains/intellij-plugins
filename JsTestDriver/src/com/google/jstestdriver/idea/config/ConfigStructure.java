package com.google.jstestdriver.idea.config;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.jstestdriver.FileInfo;
import com.google.jstestdriver.FlagsImpl;
import com.google.jstestdriver.PathResolver;
import com.google.jstestdriver.config.Configuration;
import com.google.jstestdriver.config.ConfigurationSource;
import com.google.jstestdriver.config.UserConfigurationSource;
import com.google.jstestdriver.config.YamlParser;
import com.google.jstestdriver.hooks.FileParsePostProcessor;
import com.google.jstestdriver.util.DisplayPathSanitizer;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ConfigStructure {

  private final File myJstdConfigPath;
  private final File myBasePath;
  private final Set<File> myLoadFiles;

  public ConfigStructure(File jstdConfigPath, File basePath, List<File> loadFiles) {
    myJstdConfigPath = jstdConfigPath;
    myBasePath = basePath;
    myLoadFiles = Sets.newHashSet();
    for (File loadFile : loadFiles) {
      try {
        myLoadFiles.add(loadFile.getCanonicalFile());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public File getJstdConfigPath() {
    return myJstdConfigPath;
  }

  public File getBasePath() {
    return myBasePath;
  }

  public File findLoadFile(String relativeFilePath) {
    try {
      File file = new File(myBasePath, relativeFilePath).getCanonicalFile();
      if (myLoadFiles.contains(file)) {
        return file;
      }
    } catch (IOException ignored) {
    }
    return null;
  }

  public static ConfigStructure newConfigStructure(File jstdConfigPath) {
    Configuration resolvedConfiguration = resolveConfiguration(jstdConfigPath);
    Set<FileInfo> fileInfoSet = resolvedConfiguration.getFilesList();
    List<File> loadFiles = Lists.newArrayList();
    for (FileInfo fileInfo : fileInfoSet) {
      File file = fileInfo.toFile(null);
      loadFiles.add(file);
    }
    return new ConfigStructure(jstdConfigPath, resolvedConfiguration.getBasePath(), loadFiles);
  }

  private static Configuration resolveConfiguration(File configFile) {
    FlagsImpl flags = new FlagsImpl();
    flags.setServer("test:1");
    try {
      ConfigurationSource confSrc = new UserConfigurationSource(configFile);
      File initialBasePath = configFile.getParentFile();
      Configuration parsedConf = confSrc.parse(initialBasePath, new YamlParser());
      File resolvedBasePath = parsedConf.getBasePath().getCanonicalFile();
      PathResolver pathResolver = new PathResolver(
          resolvedBasePath,
          Collections.<FileParsePostProcessor>emptySet(),
          new DisplayPathSanitizer(resolvedBasePath)
      );
      return parsedConf.resolvePaths(pathResolver, flags);
    } catch (IOException e) {
      throw new RuntimeException("Failed to read settings file " + configFile, e);
    }
  }

}
