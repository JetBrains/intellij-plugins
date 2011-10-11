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
import com.google.inject.name.Names;
import com.google.jstestdriver.*;
import com.google.jstestdriver.config.Configuration;
import com.google.jstestdriver.config.ConfigurationSource;
import com.google.jstestdriver.config.UserConfigurationSource;
import com.google.jstestdriver.config.YamlParser;
import com.google.jstestdriver.hooks.FileParsePostProcessor;
import com.google.jstestdriver.idea.execution.tree.JstdTestRunnerFailure;
import com.google.jstestdriver.idea.server.JstdServerFetchResult;
import com.google.jstestdriver.idea.server.JstdServerUtils;
import com.google.jstestdriver.idea.util.EnumUtils;
import com.google.jstestdriver.idea.util.ObjectUtils;
import com.google.jstestdriver.output.MultiTestResultListener;
import com.google.jstestdriver.output.TestResultHolder;
import com.google.jstestdriver.output.TestResultListener;
import com.google.jstestdriver.runner.RunnerMode;
import com.google.jstestdriver.util.DisplayPathSanitizer;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.LogManager;
import java.util.regex.Pattern;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

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
    TEST_METHOD
  }

  private final Settings mySettings;
  private final ObjectOutput myTestResultProtocolMessageOutput;

  public TestRunner(Settings settings, ObjectOutput testResultProtocolMessageOutput) {
    mySettings = settings;
    myTestResultProtocolMessageOutput = testResultProtocolMessageOutput;
  }

  public void execute() throws InterruptedException {
    for (File config : mySettings.getConfigFiles()) {
      try {
        execute(config);
      } catch (Exception e) {
        String message = formatMessage(null, e);
        JstdTestRunnerFailure failure = new JstdTestRunnerFailure(JstdTestRunnerFailure.FailureType.SINGLE_JSTD_CONFIG, message, config);
        try {
          myTestResultProtocolMessageOutput.writeObject(failure);
        } catch (IOException ioe) {
          ioe.printStackTrace();
        }
      }
    }
  }

  private void execute(File config) {
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
    List<String> testCaseNames = Collections.singletonList(testCaseName);
    final ActionRunner dryRunRunner =
        makeActionBuilder(config).dryRunFor(testCaseNames).build();
    final ActionRunner testRunner =
        makeActionBuilder(config).addTests(testCaseNames).build();
    //TODO(alexeagle): support client-side reset action
    final ActionRunner resetRunner =
        makeActionBuilder(config).resetBrowsers().build();

    dryRunRunner.runActions();
    testRunner.runActions();
    resetRunner.runActions();
  }

  private IDEPluginActionBuilder makeActionBuilder(File configFile) {
    FlagsImpl flags = new FlagsImpl();
    flags.setServer(mySettings.getServerUrl());
    flags.setCaptureConsole(true);
    Configuration configuration = resolveConfiguration(configFile, flags);
    IDEPluginActionBuilder builder =
        new IDEPluginActionBuilder(configuration, flags);
    List<Module> modules = new PluginLoader().load(configuration.getPlugins());
    for (Module module : modules) {
      builder.install(module);
    }
    builder.install(createTestResultPrintingModule(configFile));
    return builder;
  }

  private Configuration resolveConfiguration(File configFile, FlagsImpl flags) {
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
    } catch (Exception e) {
      throw new RuntimeException("Failed to read settings file " + configFile, e);
    }
  }

  private Module createTestResultPrintingModule(final File configFile) {
    return new AbstractModule() {
      @Override
      protected void configure() {
        bind(ObjectOutput.class).annotatedWith(Names.named("testResultProtocolMessageOutput"))
            .toInstance(myTestResultProtocolMessageOutput);
        bind(File.class).annotatedWith(Names.named("jstdConfigFile")).toInstance(configFile);
        Multibinder<TestResultListener> testResultListeners =
            newSetBinder(binder(), TestResultListener.class);

        testResultListeners.addBinding().to(TestResultHolder.class);

        bind(TestResultListener.class).to(MultiTestResultListener.class);
        newSetBinder(binder(),
            ResponseStreamFactory.class).addBinding().to(TestRunnerResponseStreamFactory.class);
      }
    };
  }

  private static String formatMessage(String message, Throwable t) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    t.printStackTrace(pw);
    pw.close();
    if (message == null) {
      return sw.toString();
    } else {
      return message + "\n\n" + sw.toString();
    }
  }

  public static void main(String[] args) throws Exception {
//    System.out.println(Arrays.toString(args));
    LogManager.getLogManager().readConfiguration(RunnerMode.QUIET.getLogConfig());

    Map<ParameterKey, String> paramMap = parseParams(args);
    Settings settings = Settings.build(paramMap);

    ObjectOutput testResultProtocolMessageOutput = fetchSocketObjectOutput(settings.getPort());
    if (!validateServer(testResultProtocolMessageOutput, paramMap)) {
      return;
    }
    try {
      new TestRunner(settings, testResultProtocolMessageOutput).execute();
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

  static boolean validateServer(ObjectOutput testResultProtocolMessageOutput, Map<ParameterKey, String> paramMap) throws IOException {
    String serverUrl = paramMap.get(ParameterKey.SERVER_URL);
    if (serverUrl != null && !serverUrl.isEmpty()) {
      JstdServerFetchResult fetchResult = JstdServerUtils.syncFetchServerInfo(serverUrl);
      String message = null;
      if (fetchResult.isError()) {
        message = "Could not connect to a JsTestDriver server running at " + serverUrl + "\n" +
              "Check that the server is running.";
      } else if (fetchResult.getServerInfo().getCapturedBrowsers().isEmpty()) {
        message = "No captured browsers found.\n" +
            "To capture browser open '" + serverUrl + "' in browser.";
      }
      if (message != null) {
        testResultProtocolMessageOutput.writeObject(new JstdTestRunnerFailure(JstdTestRunnerFailure.FailureType.WHOLE_TEST_RUNNER, message, null));
        return false;
      }
    }
    return true;
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

    private Settings(int port, String serverUrl, List<File> configFiles, String testCaseName, String testMethodName) {
      myPort = port;
      myServerUrl = serverUrl;
      myConfigFiles = configFiles;
      myTestCaseName = testCaseName;
      myTestMethodName = testMethodName;
    }

    public int getPort() {
      return myPort;
    }

    @NotNull
    public String getServerUrl() {
      return myServerUrl;
    }

    public List<File> getConfigFiles() {
      return myConfigFiles;
    }

    public String getTestCaseName() {
      return myTestCaseName;
    }

    public String getTestMethodName() {
      return myTestMethodName;
    }

    @NotNull
    private static Settings build(Map<ParameterKey, String> parameters) {
      int port = Integer.parseInt(parameters.get(ParameterKey.PORT));
      String serverUrl = parameters.get(ParameterKey.SERVER_URL);
      if (serverUrl == null) {
        throw new RuntimeException("server_url parameter must be specified");
      }
      String configFilesStr = ObjectUtils.notNull(parameters.get(ParameterKey.CONFIG_FILE), "");
      String[] pathes = configFilesStr.split(Pattern.quote(","));
      List<File> configFiles = Lists.newArrayList();
      for (String urlEncodedPath : pathes) {
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
      String testCaseName = ObjectUtils.notNull(parameters.get(ParameterKey.TEST_CASE), "");
      String testMethodName = ObjectUtils.notNull(parameters.get(ParameterKey.TEST_METHOD), "");
      return new Settings(port, serverUrl, configFiles, testCaseName, testMethodName);
    }
  }
}
