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
import com.google.jstestdriver.Flags;
import com.google.jstestdriver.JsTestDriver;
import com.google.jstestdriver.Plugin;
import com.google.jstestdriver.PluginLoader;
import com.google.jstestdriver.config.Configuration;
import com.google.jstestdriver.config.UserConfigurationSource;
import com.google.jstestdriver.embedded.JsTestDriverBuilder;
import com.google.jstestdriver.hooks.PluginInitializer;
import com.google.jstestdriver.hooks.TestListener;
import com.google.jstestdriver.idea.execution.tree.JstdTestRunnerFailure;
import com.google.jstestdriver.idea.server.JstdServerFetchResult;
import com.google.jstestdriver.idea.server.JstdServerUtils;
import com.google.jstestdriver.idea.util.EnumUtils;
import com.google.jstestdriver.output.TestResultHolder;
import com.google.jstestdriver.runner.RunnerMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.LogManager;
import java.util.regex.Pattern;

/**
 * Run JSTD in its own process, and stream messages via a socket to a server that lives in the IDEA process,
 * which will update the UI with our results.
 *
 * @author alexeagle@google.com (Alex Eagle)
 */
public class TestRunner {

  public enum ParameterKey {
    PORT,
    SERVER_URL,
    CONFIG_FILE,
    TEST_CASE,
    TEST_METHOD,
    OUTPUT_COVERAGE_FILE
  }

  private final Settings mySettings;
  private final ObjectOutput myTestResultProtocolMessageOutput;

  public TestRunner(Settings settings, ObjectOutput testResultProtocolMessageOutput) {
    mySettings = settings;
    myTestResultProtocolMessageOutput = testResultProtocolMessageOutput;
  }

  public void executeAll() throws InterruptedException {
    for (File config : mySettings.getConfigFiles()) {
      executeConfig(config);
    }
  }

  public void executeConfig(@NotNull File config) throws InterruptedException {
    try {
      unsafeExecuteConfig(config);
    } catch (Exception e) {
      String message = formatMessage(null, e);
      JstdTestRunnerFailure failure = new JstdTestRunnerFailure(
          JstdTestRunnerFailure.FailureType.SINGLE_JSTD_CONFIG, message, config
      );
      try {
        myTestResultProtocolMessageOutput.writeObject(failure);
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }
  }

  private void unsafeExecuteConfig(@NotNull File config) {
    final String testCaseName;
    if (!mySettings.getTestCaseName().isEmpty()) {
      if (!mySettings.getTestMethodName().isEmpty()) {
        testCaseName = mySettings.getTestCaseName() + "." + mySettings.getTestMethodName();
      } else {
        testCaseName = mySettings.getTestCaseName();
      }
    } else {
      testCaseName = "all";
    }

    if (mySettings.getIdeCoverageFile() == null) {
      runTests(config, new String[]{"--reset", "--dryRunFor", testCaseName}, false);
    }
    runTests(config, new String[]{"--tests", testCaseName}, true);
  }

  private void runTests(@NotNull File config, @NotNull String[] extraArgs, boolean runTests) {
    PrintStream old = System.out;
    System.setOut(new PrintStream(new NullOutputStream()));
    try {
      doRunTests(config, extraArgs, runTests);
    } finally {
      System.setOut(old);
    }
  }

  @SuppressWarnings("deprecation")
  private void doRunTests(@NotNull final File configFile, @NotNull String[] extraArgs, boolean runTests) {
    JsTestDriverBuilder builder = new JsTestDriverBuilder();

    builder.setConfigurationSource(new UserConfigurationSource(configFile));
    builder.setPort(mySettings.getPort());
    builder.withPluginInitializer(new PluginInitializer() {
      @Override
      public Module initializeModule(Flags flags, Configuration config) {
        return new AbstractModule() {
          @Override
          public void configure() {
            Multibinder<TestListener> listeners = Multibinder.newSetBinder(binder(), TestListener.class);
            listeners.addBinding().to(TestResultHolder.class);
            TestListener reporter = new IDETestListener(myTestResultProtocolMessageOutput, configFile);
            listeners.addBinding().toInstance(reporter);
          }
        };
      }
    });

    builder.setRunnerMode(RunnerMode.QUIET);
    builder.setServer(mySettings.getServerUrl());

    List<String> flagArgs = Lists.newArrayList("--captureConsole", "--server", mySettings.getServerUrl());
    flagArgs.addAll(Arrays.asList(extraArgs));
    File ideCoverageFilePath = mySettings.getIdeCoverageFile();
    File dir = null;
    boolean runCoverage = false;
    if (runTests && ideCoverageFilePath != null) {
      dir = createTempDir();
      if (dir != null) {
        flagArgs.add("--testOutput");
        flagArgs.add(dir.getAbsolutePath());
        PluginInitializer coverageInitializer = getCoverageInitializer();
        if (coverageInitializer != null) {
          builder.withPluginInitializer(coverageInitializer);
          runCoverage = true;
        }
      }
    }

    String[] args = flagArgs.toArray(new String[flagArgs.size()]);
    builder.setFlags(args);
    JsTestDriver jstd = builder.build();
    jstd.runConfiguration();
    if (runCoverage) {
      File[] coverageFiles = dir.listFiles(new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return name.endsWith("-coverage.dat");
        }
      });
      if (coverageFiles != null && coverageFiles.length == 1) {
        copyFile(ideCoverageFilePath, coverageFiles[0]);
      }
    }
  }

  private static void copyFile(@NotNull File from, @NotNull File to) {
    BufferedWriter out = null;
    BufferedReader in = null;
    try {
      out = new BufferedWriter(new FileWriter(from));
      in = new BufferedReader(new FileReader(to));
      int ch;
      while ((ch = in.read()) != -1) {
        out.write(ch);
      }
    }
    catch (IOException e) {
    }
    finally {
      if (out != null) {
        try {
          out.close();
        }
        catch (IOException e) {}
      }
      if (in != null) {
        try {
          in.close();
        }
        catch (IOException e) {}
      }
    }
  }

  @Nullable
  private static PluginInitializer getCoverageInitializer() {
    File[] coverageFiles = new File(".").listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.startsWith("coverage") && name.endsWith(".jar");
      }
    });
    if (coverageFiles != null && coverageFiles.length == 1) {
      Plugin plugin = new Plugin(
        "coverage",
        coverageFiles[0].getAbsolutePath(),
        "com.google.jstestdriver.coverage.CoverageModule",
        Collections.<String>emptyList()
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

  private static String formatMessage(@Nullable String message, @NotNull Throwable t) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    try {
      t.printStackTrace(pw);
    } finally {
      pw.close();
    }
    if (message == null) {
      return sw.toString();
    } else {
      return message + "\n\n" + sw.toString();
    }
  }

  public static void main(String[] args) throws Exception {
    LogManager.getLogManager().readConfiguration(RunnerMode.QUIET.getLogConfig());

    Map<ParameterKey, String> paramMap = parseParams(args);
    Settings settings = Settings.build(paramMap);
    ObjectOutput testResultProtocolMessageOutput = fetchSocketObjectOutput(settings.getPort());//new ConsoleObjectOutput();//
    if (!validateServer(testResultProtocolMessageOutput, settings)) {
      return;
    }
    try {
      new TestRunner(settings, testResultProtocolMessageOutput).executeAll();
    } catch (Exception ex) {
      String message = formatMessage("JsTestDriver crashed!", ex);
      testResultProtocolMessageOutput.writeObject(new JstdTestRunnerFailure(JstdTestRunnerFailure.FailureType.WHOLE_TEST_RUNNER, message, null));
    } finally {
      try {
        testResultProtocolMessageOutput.close();
      } catch (Exception e) {
        System.err.println("Exception occurred while closing testResultProtocolMessageOutput");
        e.printStackTrace();
      }
    }
  }

  static boolean validateServer(ObjectOutput testResultProtocolMessageOutput, Settings settings) throws IOException {
    String serverUrl = settings.getServerUrl();
    JstdServerFetchResult fetchResult = JstdServerUtils.syncFetchServerInfo(serverUrl);
    String message = null;
    if (fetchResult.isError()) {
      message = "Could not connect to a JsTestDriver server running at " + serverUrl + "\n" +
                "Check that the server is running.";
    } else if (fetchResult.getServerInfo().getCapturedBrowsers().isEmpty()) {
      message = "No captured browsers found.\n" +
                "To capture browser open '" + serverUrl + "' in browser.";
    }
    if (message == null) {
      return true;
    }
    testResultProtocolMessageOutput.writeObject(new JstdTestRunnerFailure(JstdTestRunnerFailure.FailureType.WHOLE_TEST_RUNNER, message, null));
    return false;
  }

  private static Map<ParameterKey, String> parseParams(String[] args) {
    Map<ParameterKey, String> params = Maps.newHashMap();
    for (String arg : args) {
      int delimiterIndex = arg.indexOf('=');
      if (delimiterIndex != -1) {
        String key = arg.substring(0, delimiterIndex);
        String value = arg.substring(delimiterIndex + 1, arg.length());
        if (key.startsWith("--")) {
          key = key.substring(2);
          ParameterKey parameterKey = EnumUtils.findEnum(ParameterKey.class, key, false);
          if (parameterKey != null) {
            params.put(parameterKey, value);
          }
        }
      }
    }
    return params;
  }

  @NotNull
  private static ObjectOutput fetchSocketObjectOutput(int port) {
    try {
      SocketAddress endpoint = new InetSocketAddress(InetAddress.getByName(null), port);
      final Socket socket = connectToServer(endpoint, 2 * 1000, 5);
      try {
        return new ObjectOutputStream(socket.getOutputStream()) {
          @Override
          public void close() throws IOException {
            socket.close(); // socket's input and output streams are closed too
          }
        };
      } catch (IOException inner) {
        closeSocketSilently(socket);
        throw inner;
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not connect to IDE, address: " +
          "'localhost:" + port + "'", e);
    }
  }

  private static Socket connectToServer(SocketAddress endpoint, int connectTimeoutMillis,
                                        int retries) throws IOException {
    IOException saved = null;
    for (int i = 0; i < retries; i++) {
      Socket socket = new Socket();
      try {
        socket.connect(endpoint, connectTimeoutMillis);
        return socket;
      } catch (IOException e) {
        closeSocketSilently(socket);
        saved = e;
      }
    }
    throw saved;
  }

  private static void closeSocketSilently(Socket socket) {
    try {
      socket.close();
    } catch (Exception e) {
      // swallow exception
    }
  }

  private static class Settings {
    private final int myPort;
    private final String myServerUrl;
    private final List<File> myConfigFiles;
    private final String myTestCaseName;
    private final String myTestMethodName;
    private final File myIdeCoverageFile;

    private Settings(int port,
                     @NotNull String serverUrl,
                     @NotNull List<File> configFiles,
                     @NotNull String testCaseName,
                     @NotNull String testMethodName,
                     @Nullable File ideCoverageFile)
    {
      myPort = port;
      myServerUrl = serverUrl;
      myConfigFiles = configFiles;
      myTestCaseName = testCaseName;
      myTestMethodName = testMethodName;
      myIdeCoverageFile = ideCoverageFile;
    }

    public int getPort() {
      return myPort;
    }

    @NotNull
    public String getServerUrl() {
      return myServerUrl;
    }

    @NotNull
    public List<File> getConfigFiles() {
      return myConfigFiles;
    }

    @NotNull
    public String getTestCaseName() {
      return myTestCaseName;
    }

    @NotNull
    public String getTestMethodName() {
      return myTestMethodName;
    }

    @Nullable
    public File getIdeCoverageFile() {
      return myIdeCoverageFile;
    }

    @NotNull
    private static Settings build(@NotNull Map<ParameterKey, String> parameters) {
      int port = Integer.parseInt(parameters.get(ParameterKey.PORT));
      String serverUrl = parameters.get(ParameterKey.SERVER_URL);
      if (serverUrl == null) {
        throw new RuntimeException("server_url parameter must be specified");
      }
      String configFilesStr = notNullize(parameters.get(ParameterKey.CONFIG_FILE));
      String[] paths = configFilesStr.split(Pattern.quote(","));
      List<File> configFiles = Lists.newArrayList();
      for (String urlEncodedPath : paths) {
        try {
          String path = URLDecoder.decode(urlEncodedPath, "UTF-8");
          File file = new File(path);
          if (file.isFile()) {
            configFiles.add(file);
          }
        } catch (UnsupportedEncodingException ignored) {}
      }
      if (configFiles.isEmpty()) {
        throw new RuntimeException("No valid config files found");
      }
      String testCaseName = notNullize(parameters.get(ParameterKey.TEST_CASE));
      String testMethodName = notNullize(parameters.get(ParameterKey.TEST_METHOD));
      String coverageFilePath = notNullize(parameters.get(ParameterKey.OUTPUT_COVERAGE_FILE));
      File ideCoverageFile = null;
      if (!coverageFilePath.isEmpty()) {
        ideCoverageFile = new File(coverageFilePath);
      }
      return new Settings(port, serverUrl, configFiles, testCaseName, testMethodName, ideCoverageFile);
    }
  }

  private static String notNullize(@Nullable String str) {
    return str == null ? "" : str;
  }

  private final class NullOutputStream extends OutputStream {
    @Override public void write(int b) {
    }

    @Override public void write(byte[] b, int off, int len) {
    }
  }

}
