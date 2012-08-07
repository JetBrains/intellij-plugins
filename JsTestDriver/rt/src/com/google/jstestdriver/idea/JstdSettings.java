package com.google.jstestdriver.idea;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.jstestdriver.idea.util.EscapeUtils;
import com.google.jstestdriver.idea.util.TestFileScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
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

  public JstdSettings(@NotNull String serverUrl,
                      @NotNull List<File> configFiles,
                      @Nullable File runAllConfigsInDirectory,
                      @NotNull TestFileScope testFileScope,
                      @Nullable File ideCoverageFile,
                      @NotNull List<String> filesExcludedFromCoverage)
  {
    myServerUrl = serverUrl;
    myConfigFiles = configFiles;
    myRunAllConfigsInDirectory = runAllConfigsInDirectory;
    myTestFileScope = testFileScope;
    myIdeCoverageFile = ideCoverageFile;
    myFilesExcludedFromCoverage = ImmutableList.copyOf(filesExcludedFromCoverage);
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
  public ImmutableList<String> getFilesExcludedFromCoverage() {
    return myFilesExcludedFromCoverage;
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
    return new JstdSettings(
      serverUrl,
      configFiles,
      runAllConfigsInDirectory,
      testFileScope,
      ideCoverageFile,
      excludedPaths
    );
  }

  private static String notNullize(@Nullable String str) {
    return str == null ? "" : str;
  }

}
