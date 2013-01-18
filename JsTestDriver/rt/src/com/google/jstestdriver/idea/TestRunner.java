/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.jstestdriver.idea;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import com.google.jstestdriver.*;
import com.google.jstestdriver.config.Configuration;
import com.google.jstestdriver.config.ConfigurationException;
import com.google.jstestdriver.config.ParsedConfiguration;
import com.google.jstestdriver.embedded.JsTestDriverBuilder;
import com.google.jstestdriver.hooks.PluginInitializer;
import com.google.jstestdriver.hooks.ResourcePreProcessor;
import com.google.jstestdriver.hooks.TestListener;
import com.google.jstestdriver.idea.coverage.CoverageReport;
import com.google.jstestdriver.idea.coverage.CoverageSerializationUtils;
import com.google.jstestdriver.idea.coverage.CoverageSession;
import com.google.jstestdriver.idea.execution.tree.TreeManager;
import com.google.jstestdriver.idea.server.JstdServerFetchResult;
import com.google.jstestdriver.idea.server.JstdServerUtilsRt;
import com.google.jstestdriver.idea.util.EscapeUtils;
import com.google.jstestdriver.idea.util.JstdConfigParsingUtils;
import com.google.jstestdriver.idea.util.JstdUtils;
import com.google.jstestdriver.output.TestResultHolder;
import com.google.jstestdriver.runner.RunnerMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Main class of JsTestDriver test runner, that runs tests in a separate process and streams messages
 * via stdout/stderr to IDEA process, which will update the UI with our results.
 *
 * @author alexeagle@google.com (Alex Eagle)
 */
public class TestRunner {

  public static final String DEBUG_SESSION_STARTED = "debug session started";

  public enum ParameterKey {
    SERVER_URL,
    CONFIG_FILES,
    ALL_CONFIGS_IN_DIRECTORY,
    TESTS,
    COVERAGE_OUTPUT_FILE,
    COVERAGE_EXCLUDED_PATHS,
    DEBUG
  }

  private final JstdSettings mySettings;
  private final TreeManager myTreeManager;
  private final CoverageSession myCoverageSession;

  public TestRunner(@NotNull JstdSettings settings, @NotNull TreeManager treeManager) {
    mySettings = settings;
    myTreeManager = treeManager;
    File ideCoverageFile = mySettings.getIdeCoverageFile();
    if (ideCoverageFile != null) {
      myCoverageSession = new CoverageSession(ideCoverageFile);
    } else {
      myCoverageSession = null;
    }
  }

  public void executeAll() {
    for (File config : mySettings.getConfigFiles()) {
      executeTests(config);
    }
    if (myCoverageSession != null) {
      myCoverageSession.finish();
    }
  }

  private void executeTests(@NotNull File config) {
    Exception exception = null;
    PrintStream nullSystemOut = new PrintStream(new NullOutputStream());
    try {
      System.setOut(nullSystemOut);
      myTreeManager.onJstdConfigRunningStarted(config);
      String runScope = mySettings.getTestFileScope().toJstdScope();
      runTests(config, new String[]{"--dryRunFor", runScope}, true);
      runTests(config, new String[]{"--tests", runScope}, false);
    } catch (ConfigurationException ce) {
      exception = ce;
    } catch (Exception e) {
      exception = new Exception("Can't run tests. Details:", e);
    } finally {
      myTreeManager.onJstdConfigRunningFinished(exception, mySettings.getTestFileScope());
      nullSystemOut.close();
      System.setOut(myTreeManager.getSystemOutStream());
    }
  }

  @SuppressWarnings("deprecation")
  private void runTests(@NotNull final File configFile, @NotNull String[] extraArgs, final boolean dryRun) throws ConfigurationException {
    JsTestDriverBuilder builder = new JsTestDriverBuilder();

    final ParsedConfiguration parsedConfiguration;
    try {
      parsedConfiguration = JstdConfigParsingUtils.parseConfiguration(configFile);
    } catch (Exception e) {
      throw new ConfigurationException("Configuration file parsing failed.\n" +
                                       "See http://code.google.com/p/js-test-driver/wiki/ConfigurationFile for clarification.\n\n" +
                                       "Details:", e);
    }
    final File singleBasePath = JstdConfigParsingUtils.getSingleBasePath(parsedConfiguration.getBasePaths(), configFile);
    myTreeManager.setCurrentBasePath(singleBasePath.getAbsolutePath());
    JstdConfigParsingUtils.wipeCoveragePlugin(parsedConfiguration);
    builder.setDefaultConfiguration(parsedConfiguration);
    builder.withPluginInitializer(new PluginInitializer() {
      @Override
      public Module initializeModule(Flags flags, Configuration config) {
        return new AbstractModule() {
          @Override
          public void configure() {
            Multibinder<TestListener> testListeners = Multibinder.newSetBinder(binder(), TestListener.class);
            testListeners.addBinding().to(TestResultHolder.class);
            testListeners.addBinding().toInstance(new IdeaTestListener(
              myTreeManager,
              configFile,
              singleBasePath,
              dryRun,
              mySettings.getTestFileScope()
            ));
          }
        };
      }
    });

    builder.setRunnerMode(RunnerMode.QUIET);
    builder.setServer(mySettings.getServerUrl());

    List<String> flagArgs = Lists.newArrayList("--captureConsole", "--server", mySettings.getServerUrl());
    if (dryRun && JstdUtils.isJasmineTests(parsedConfiguration)) {
      // https://github.com/ibolmo/jasmine-jstd-adapter/pull/21
      flagArgs.add("--reset");
    }
    flagArgs.addAll(Arrays.asList(extraArgs));
    List<String> coverageExcludedFiles = null;
    File emptyOutputDir = null;
    boolean runCoverage = false;
    if (myCoverageSession != null && !dryRun) {
      emptyOutputDir = createTempDir();
      if (emptyOutputDir != null) {
        flagArgs.add("--testOutput");
        flagArgs.add(emptyOutputDir.getAbsolutePath());
        List<String> testPaths = getTestFilePaths(parsedConfiguration);
        coverageExcludedFiles = Lists.newArrayList(testPaths);
        coverageExcludedFiles.addAll(mySettings.getFilesExcludedFromCoverageRec());
        PluginInitializer coverageInitializer = getCoverageInitializer(coverageExcludedFiles);
        if (coverageInitializer != null) {
          builder.withPluginInitializer(coverageInitializer);
          builder.withPluginInitializer(new DependenciesTouchFix());
          runCoverage = true;
        }
      }
    }

    builder.setFlags(toStringArray(flagArgs));
    JsTestDriver jstd = builder.build();
    jstd.runConfiguration();
    if (runCoverage) {
      File[] coverageReportFiles = emptyOutputDir.listFiles(new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return name.endsWith("-coverage.dat");
        }
      });
      if (coverageReportFiles != null && coverageReportFiles.length == 1) {
        try {
          CoverageReport coverageReport = CoverageSerializationUtils.readLCOV(coverageReportFiles[0]);
          for (String excludedPath : coverageExcludedFiles) {
            coverageReport.clearReportByFilePath(excludedPath);
          }
          myCoverageSession.mergeReport(coverageReport);
        }
        catch (Exception e) {
          myTreeManager.printThrowable(e);
        }
      }
    }
  }

  @NotNull
  private static List<String> getTestFilePaths(@NotNull ParsedConfiguration parsedConfiguration) {
    Configuration resolvedConfiguration = JstdConfigParsingUtils.resolveConfiguration(parsedConfiguration);
    List<FileInfo> testFileInfos = resolvedConfiguration.getTests();
    List<String> paths = Lists.newArrayListWithExpectedSize(testFileInfos.size());
    for (FileInfo fileInfo : testFileInfos) {
      paths.add(fileInfo.getFilePath());
    }
    return paths;
  }

  @Nullable
  private static PluginInitializer getCoverageInitializer(List<String> filesExcludedFromCoverage) {
    File[] coverageJarFiles = new File(".").listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.startsWith("coverage") && name.endsWith(".jar");
      }
    });
    if (coverageJarFiles != null && coverageJarFiles.length == 1) {
      Plugin plugin = new Plugin(
        "coverage",
        coverageJarFiles[0].getAbsolutePath(),
        "com.google.jstestdriver.coverage.CoverageModule",
        filesExcludedFromCoverage
      );
      PluginLoader pluginLoader = new PluginLoader();
      final List<Module> modules = pluginLoader.load(Collections.singletonList(plugin));
      if (modules.size() == 1) {
        return new PluginInitializer() {
          @Override
          public Module initializeModule(Flags flags, Configuration config) {
            return modules.get(0);
          }
        };
      }
    }
    return null;
  }

  @SuppressWarnings("SSBasedInspection")
  @Nullable
  private static File createTempDir() {
    try {
      File file = File.createTempFile("jstestdriver-coverage-", "-tmp");
      if (!file.delete()) {
        return null;
      }
      if (!file.mkdir()) {
        return null;
      }
      file.deleteOnExit();
      return file;
    } catch (IOException e) {
      return null;
    }
  }

  public static void main(String[] args) throws Exception {
    Map<ParameterKey, String> paramMap = parseParams(args);
    JstdSettings settings = JstdSettings.build(paramMap);
    TreeManager treeManager = new TreeManager(settings.getRunAllConfigsInDirectory());
    if (!validateServer(settings, treeManager)) {
      System.exit(1);
      return;
    }
    if (settings.isDebug()) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
      String line;
      while ((line = reader.readLine()) != null) {
        if (DEBUG_SESSION_STARTED.equals(line)) {
          break;
        }
      }
    }
    try {
      new TestRunner(settings, treeManager).executeAll();
    } catch (Exception ex) {
      treeManager.printThrowable("Unexpected crash!", ex);
    } finally {
      treeManager.onTestingFinished();
    }
  }

  private static boolean validateServer(@NotNull JstdSettings settings, @NotNull TreeManager treeManager) throws IOException {
    String serverUrl = settings.getServerUrl();
    JstdServerFetchResult fetchResult = JstdServerUtilsRt.syncFetchServerInfo(serverUrl);
    String message = null;
    if (fetchResult.isError()) {
      message = "Could not connect to a JsTestDriver server running at " + serverUrl + "\n" +
                "Check that the server is running.";
    } else if (fetchResult.getServerInfo().getCapturedBrowsers().isEmpty()) {
      message = "No captured browsers found.\n" +
                "To capture browser open '" + serverUrl + "' in browser.";
    }
    if (message != null) {
      treeManager.reportRootError(message);
      return false;
    }
    return true;
  }

  private static Map<ParameterKey, String> parseParams(String[] args) {
    Map<ParameterKey, String> params = Maps.newHashMap();
    for (String arg : args) {
      List<String> elements = EscapeUtils.split(arg, '=');
      if (arg.startsWith("--") && elements.size() == 2) {
        String key = elements.get(0).substring(2);
        String value = elements.get(1);
        ParameterKey parameterKey = ParameterKey.valueOf(key.toUpperCase());
        params.put(parameterKey, value);
      }
    }
    return params;
  }

  @SuppressWarnings("SSBasedInspection")
  @NotNull
  private static String[] toStringArray(@NotNull List<String> list) {
    return list.toArray(new String[list.size()]);
  }

  private static class DependenciesTouchFix implements PluginInitializer {
    @Override
    public Module initializeModule(Flags flags, Configuration config) {
      return new AbstractModule() {
        @Override
        protected void configure() {
          Multibinder.newSetBinder(binder(), ResourcePreProcessor.class)
            .addBinding().toInstance(new ResourcePreProcessor() {
            @Override
            public List<FileInfo> processDependencies(List<FileInfo> files) {
              List<FileInfo> out = Lists.newArrayList();
              for (FileInfo file : files) {
                FileInfo touchedFileInfo = file.fromResolvedPath(
                  file.getFilePath(),
                  file.getDisplayPath(),
                  new DateTime().toInstant().getMillis()
                );
                out.add(touchedFileInfo);
              }
              return out;
            }

            @Override
            public List<FileInfo> processTests(List<FileInfo> files) {
              return files;
            }

            @Override
            public List<FileInfo> processPlugins(List<FileInfo> files) {
              return files;
            }
          });
        }
      };
    }
  }

  private static final class NullOutputStream extends OutputStream {

    /** Discards the specified byte. */
    @Override public void write(int b) {
    }

    /** Discards the specified byte array. */
    @Override public void write(byte[] b, int off, int len) {
    }
  }

}
