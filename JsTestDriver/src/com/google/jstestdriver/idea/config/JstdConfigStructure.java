package com.google.jstestdriver.idea.config;

import com.google.common.collect.Sets;
import com.google.jstestdriver.config.ParsedConfiguration;
import com.google.jstestdriver.config.ResolvedConfiguration;
import com.google.jstestdriver.idea.util.JstdConfigParsingUtils;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class JstdConfigStructure {

  private static final Logger LOG = Logger.getInstance(JstdConfigStructure.class);

  private final File myBasePath;
  private final Set<File> myLoadFiles;
  private final Set<File> myTestFiles;

  private JstdConfigStructure(@NotNull File basePath,
                              @NotNull List<File> loadFiles,
                              @NotNull List<File> testFiles) {
    myBasePath = basePath;
    myLoadFiles = map2CanonicalFiles(loadFiles);
    myTestFiles = map2CanonicalFiles(testFiles);
  }

  @NotNull
  private static Set<File> map2CanonicalFiles(@NotNull Collection<File> files) {
    Set<File> canonicalFiles = Sets.newHashSetWithExpectedSize(files.size());
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

  @NotNull
  public static JstdConfigStructure parseConfigStructure(@NotNull File jstdConfigFile) {
    ParsedConfiguration parsed = JstdConfigParsingUtils.parseConfiguration(jstdConfigFile);
    ResolvedConfiguration resolved = JstdConfigParsingUtils.resolveConfiguration(parsed);
    List<File> loadFiles = JstdConfigParsingUtils.mapFileInfos2Files(resolved.getFilesList());
    List<File> testFiles = JstdConfigParsingUtils.mapFileInfos2Files(resolved.getTests());
    File basePath = JstdConfigParsingUtils.getSingleBasePath(resolved.getBasePaths(), jstdConfigFile);
    return new JstdConfigStructure(basePath, loadFiles, testFiles);
  }

}
