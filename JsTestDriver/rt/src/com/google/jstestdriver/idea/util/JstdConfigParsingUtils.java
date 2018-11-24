package com.google.jstestdriver.idea.util;

import com.google.common.collect.Lists;
import com.google.jstestdriver.FileInfo;
import com.google.jstestdriver.FlagsImpl;
import com.google.jstestdriver.PathResolver;
import com.google.jstestdriver.config.*;
import com.google.jstestdriver.hooks.FileParsePostProcessor;
import com.google.jstestdriver.model.BasePaths;
import com.google.jstestdriver.util.DisplayPathSanitizer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class JstdConfigParsingUtils {
  private JstdConfigParsingUtils() {
  }

  public static ParsedConfiguration parseConfiguration(@NotNull File configFile) {
    BasePaths basePaths = new BasePaths(configFile.getParentFile());
    ConfigurationSource configurationSource = new UserConfigurationSource(configFile);
    Configuration configuration = configurationSource.parse(basePaths, new YamlParser());
    return (ParsedConfiguration) configuration;
  }

  @NotNull
  public static ResolvedConfiguration resolveConfiguration(@NotNull ParsedConfiguration parsedConfiguration) {
    PathResolver pathResolver = new PathResolver(
      parsedConfiguration.getBasePaths(),
      Collections.<FileParsePostProcessor>emptySet(),
      new DisplayPathSanitizer()
    );
    FlagsImpl flags = new FlagsImpl();
    flags.setServer("test:1");
    Configuration resolved = parsedConfiguration.resolvePaths(pathResolver, flags);
    return (ResolvedConfiguration) resolved;
  }

  @NotNull
  public static List<File> mapFileInfos2Files(@NotNull Collection<FileInfo> fileInfos) {
    List<File> files = Lists.newArrayListWithExpectedSize(fileInfos.size());
    for (FileInfo fileInfo : fileInfos) {
      File file = fileInfo.toFile();
      if (file.isFile()) {
        files.add(file);
      }
    }
    return files;
  }

  @NotNull
  public static File getSingleBasePath(@NotNull BasePaths basePaths, @NotNull File jstdConfigFile) {
    Iterator<File> iterator = basePaths.iterator();
    if (iterator.hasNext()) {
      return iterator.next();
    }
    return jstdConfigFile.getParentFile();
  }
}
