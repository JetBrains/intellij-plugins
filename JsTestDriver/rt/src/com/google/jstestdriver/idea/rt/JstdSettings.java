package com.google.jstestdriver.idea.rt;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.jstestdriver.idea.rt.util.EscapeUtils;
import com.google.jstestdriver.idea.rt.util.TestFileScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
* @author Sergey Simonchik
*/
public class JstdSettings {

  private final String myServerUrl;
  private final List<File> myConfigFiles;
  private final File myRunAllConfigsInDirectory;
  private final TestFileScope myTestFileScope;
  private final File myIdeCoverageFile;
  private final ImmutableList<String> myFilesExcludedFromCoverage;
  private final boolean myDebug;

  public JstdSettings(@NotNull String serverUrl,
                      @NotNull List<File> configFiles,
                      @Nullable File runAllConfigsInDirectory,
                      @NotNull TestFileScope testFileScope,
                      @Nullable File ideCoverageFile,
                      @NotNull List<String> filesExcludedFromCoverage,
                      boolean debug)
  {
    myServerUrl = serverUrl;
    myConfigFiles = configFiles;
    myRunAllConfigsInDirectory = runAllConfigsInDirectory;
    myTestFileScope = testFileScope;
    myIdeCoverageFile = ideCoverageFile;
    myFilesExcludedFromCoverage = ImmutableList.copyOf(filesExcludedFromCoverage);
    myDebug = debug;
  }

  @NotNull
  public String getServerUrl() {
    return myServerUrl;
  }

  @NotNull
  public List<File> getConfigFiles() {
    return myConfigFiles;
  }

  @Nullable
  public File getRunAllConfigsInDirectory() {
    return myRunAllConfigsInDirectory;
  }

  @NotNull
  public TestFileScope getTestFileScope() {
    return myTestFileScope;
  }

  @Nullable
  public File getIdeCoverageFile() {
    return myIdeCoverageFile;
  }

  @NotNull
  public List<String> getFilesExcludedFromCoverageRec() {
    List<String> out = new ArrayList<>();
    for (String path : myFilesExcludedFromCoverage) {
      File file = new File(path);
      if (file.isDirectory()) {
        collectFiles(file, out);
      }
      else {
        out.add(path);
      }
    }
    return out;
  }

  private static void collectFiles(@NotNull File dir, List<String> paths) {
    File[] files = dir.listFiles();
    if (files == null) return;
    for (File file : files) {
      if (file.isDirectory()) {
        collectFiles(file, paths);
      }
      else {
        paths.add(file.getAbsolutePath());
      }
    }
  }

  public boolean isDebug() {
    return myDebug;
  }

  @NotNull
  public static JstdSettings build(@NotNull Map<TestRunner.ParameterKey, String> parameters) {
    String serverUrl = parameters.get(TestRunner.ParameterKey.SERVER_URL);
    if (serverUrl == null) {
      throw new RuntimeException(TestRunner.ParameterKey.SERVER_URL + " parameter must be specified");
    }
    String configFilesStr = notNullize(parameters.get(TestRunner.ParameterKey.CONFIG_FILES));
    List<String> paths = EscapeUtils.split(configFilesStr, ',');
    List<File> configFiles = Lists.newArrayList();
    for (String path : paths) {
      File file = new File(path);
      if (file.isFile()) {
        configFiles.add(file);
      }
    }
    if (configFiles.isEmpty()) {
      throw new RuntimeException("No valid config files found");
    }
    String runAllConfigsInDirectoryPath = parameters.get(TestRunner.ParameterKey.ALL_CONFIGS_IN_DIRECTORY);
    File runAllConfigsInDirectory = null;
    if (runAllConfigsInDirectoryPath != null && !runAllConfigsInDirectoryPath.isEmpty()) {
      runAllConfigsInDirectory = new File(runAllConfigsInDirectoryPath);
      if (!runAllConfigsInDirectory.isDirectory()) {
        runAllConfigsInDirectory = null;
      }
    }

    String testsScope = parameters.get(TestRunner.ParameterKey.TESTS);
    final TestFileScope testFileScope;
    if (testsScope == null) {
      testFileScope = TestFileScope.allScope();
    }
    else {
      testFileScope = TestFileScope.deserialize(testsScope);
    }

    String coverageFilePath = notNullize(parameters.get(TestRunner.ParameterKey.COVERAGE_OUTPUT_FILE));
    File ideCoverageFile = null;
    List<String> excludedPaths = Collections.emptyList();
    if (!coverageFilePath.isEmpty()) {
      ideCoverageFile = new File(coverageFilePath);
      String joinedPaths = notNullize(parameters.get(TestRunner.ParameterKey.COVERAGE_EXCLUDED_PATHS));
      excludedPaths = EscapeUtils.split(joinedPaths, ',');
    }
    boolean debug = Boolean.TRUE.toString().equals(parameters.get(TestRunner.ParameterKey.DEBUG));
    return new JstdSettings(
      serverUrl,
      configFiles,
      runAllConfigsInDirectory,
      testFileScope,
      ideCoverageFile,
      excludedPaths,
      debug
    );
  }

  private static String notNullize(@Nullable String str) {
    return str == null ? "" : str;
  }

}
