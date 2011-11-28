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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class JstdConfigStructure {

  private static final Logger LOG = Logger.getInstance(JstdConfigStructure.class);

  private final File myJstdConfigFile;
  private final File myBasePath;
  private final Set<File> myLoadFiles;
  private final Set<File> myTestFiles;

  private JstdConfigStructure(@NotNull File jstdConfigFile,
                              @NotNull File basePath,
                              @NotNull Collection<File> loadFiles,
                              @NotNull Collection<File> testFiles) {
    myJstdConfigFile = jstdConfigFile;
    myBasePath = basePath;
    myLoadFiles = map2CanonicalFiles(loadFiles);
    myTestFiles = map2CanonicalFiles(testFiles);
  }

  @NotNull
  private static Set<File> map2CanonicalFiles(@NotNull Collection<File> files) {
    Set<File> canonicalFiles = Sets.newHashSet();
    for (File file : files) {
      try {
        canonicalFiles.add(file.getCanonicalFile());
      } catch (IOException e) {
        LOG.warn("Can't find canonical file for '" + file.getAbsolutePath() + "'... Just skipping.", e);
      }
    }
    return canonicalFiles;
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

  @NotNull
  public Set<File> getTestFiles() {
    return myTestFiles;
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
        if (myLoadFiles.contains(canonicalFile) || myTestFiles.contains(canonicalFile)) {
          return file;
        }
      } catch (IOException ignored) {
      }
    }
    return null;
  }

  public static JstdConfigStructure newConfigStructure(@NotNull File jstdConfigFile) {
    Configuration resolvedConfiguration = resolveConfiguration(jstdConfigFile);
    Collection<File> loadFiles = mapFileInfos2Files(resolvedConfiguration.getFilesList());
    Collection<File> testFiles = mapFileInfos2Files(resolvedConfiguration.getTests());
    return new JstdConfigStructure(jstdConfigFile, resolvedConfiguration.getBasePath(), loadFiles, testFiles);
  }

  private static List<File> mapFileInfos2Files(Collection<FileInfo> fileInfos) {
    List<File> files = Lists.newArrayList();
    for (FileInfo fileInfo : fileInfos) {
      File file = fileInfo.toFile(null);
      if (file.isFile()) {
        files.add(file);
      }
    }
    return files;
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
