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
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class JstdConfigStructure {

  private static final Logger LOG = Logger.getInstance(JstdConfigStructure.class);

  private final File myJstdConfigFile;
  private final File myBasePath;
  private final Set<File> myLoadFiles;

  public JstdConfigStructure(@NotNull File jstdConfigFile, @NotNull File basePath, @NotNull List<File> loadFiles) {
    myJstdConfigFile = jstdConfigFile;
    myBasePath = basePath;
    myLoadFiles = Sets.newHashSet();
    for (File loadFile : loadFiles) {
      try {
        myLoadFiles.add(loadFile.getCanonicalFile());
      } catch (IOException e) {
        LOG.warn(e);
      }
    }
  }

  @NotNull
  public File getJstdConfigFile() {
    return myJstdConfigFile;
  }

  @NotNull
  public File getBasePath() {
    return myBasePath;
  }

  @NotNull
  public Set<File> getLoadFiles() {
    return myLoadFiles;
  }

  @Nullable
  public File findLoadFile(@NotNull String filePath) {
    File file = new File(myBasePath, filePath);
    if (!file.isFile()) {
      File absoluteFile = new File(filePath);
      if (absoluteFile.isAbsolute() && absoluteFile.isFile()) {
        file = absoluteFile;
      }
    }
    if (file.isFile()) {
      try {
        File canonicalFile = file.getCanonicalFile();
        if (myLoadFiles.contains(canonicalFile)) {
          return file;
        }
      } catch (IOException ignored) {
      }
    }
    return null;
  }

  public static JstdConfigStructure newConfigStructure(@NotNull File jstdConfigFile) {
    Configuration resolvedConfiguration = resolveConfiguration(jstdConfigFile);
    Set<FileInfo> fileInfoSet = resolvedConfiguration.getFilesList();
    List<File> loadFiles = Lists.newArrayList();
    for (FileInfo fileInfo : fileInfoSet) {
      File file = fileInfo.toFile(null);
      loadFiles.add(file);
    }
    return new JstdConfigStructure(jstdConfigFile, resolvedConfiguration.getBasePath(), loadFiles);
  }

  private static Configuration resolveConfiguration(File jstdConfigFile) {
    FlagsImpl flags = new FlagsImpl();
    flags.setServer("test:1");
    try {
      ConfigurationSource confSrc = new UserConfigurationSource(jstdConfigFile);
      File initialBasePath = jstdConfigFile.getParentFile();
      Configuration parsedConf = confSrc.parse(initialBasePath, new YamlParser());
      File resolvedBasePath = parsedConf.getBasePath().getCanonicalFile();
      PathResolver pathResolver = new PathResolver(
          resolvedBasePath,
          Collections.<FileParsePostProcessor>emptySet(),
          new DisplayPathSanitizer(resolvedBasePath)
      );
      return parsedConf.resolvePaths(pathResolver, flags);
    } catch (IOException e) {
      throw new RuntimeException("Failed to read settings file " + jstdConfigFile, e);
    }
  }

}
