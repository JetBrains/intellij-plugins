package com.google.jstestdriver.idea.rt.util;

import com.google.common.collect.Lists;
import com.google.jstestdriver.FileInfo;
import com.google.jstestdriver.FlagsImpl;
import com.google.jstestdriver.PathResolver;
import com.google.jstestdriver.Plugin;
import com.google.jstestdriver.config.*;
import com.google.jstestdriver.model.BasePaths;
import com.google.jstestdriver.util.DisplayPathSanitizer;
import com.google.jstestdriver.util.ManifestLoader;
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

  private static final String COVERAGE_MODULE_NAME = "com.google.jstestdriver.coverage.CoverageModule";

  private JstdConfigParsingUtils() {}

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
      Collections.emptySet(),
      new DisplayPathSanitizer()
    );
    FlagsImpl flags = new FlagsImpl();
    flags.setServer("test:1");
    Configuration resolved = parsedConfiguration.resolvePaths(pathResolver, flags);
    return (ResolvedConfiguration) resolved;
  }

  @NotNull
  public static List<File> mapFileInfos2Files(@NotNull Collection<? extends FileInfo> fileInfos) {
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

  /**
   * Wiping coverage section in a configuration file makes sense because:
   * <ul>
   *   <li>running tests without coverage (via Shift+F10) doesn't handle coverage output</li>
   *   <li>running tests with coverage has its own special configuration</li>
   * </ul>
   * @param configuration
   */
  public static void wipeCoveragePlugin(@NotNull ParsedConfiguration configuration) {
    ManifestLoader manifestLoader = new ManifestLoader();
    Iterator<Plugin> iterator = configuration.getPlugins().iterator();
    while (iterator.hasNext()) {
      Plugin plugin = iterator.next();
      if (isCoveragePlugin(plugin, manifestLoader)) {
        iterator.remove();
      }
    }
  }

  private static boolean isCoveragePlugin(@NotNull Plugin plugin, @NotNull ManifestLoader loader) {
    try {
      String moduleName = plugin.getModuleName(loader);
      if (COVERAGE_MODULE_NAME.equals(moduleName)) {
        return true;
      }
    } catch (Exception ignored) {
    }
    return false;
  }

}
