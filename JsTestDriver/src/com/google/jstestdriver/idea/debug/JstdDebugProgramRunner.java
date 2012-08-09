package com.google.jstestdriver.idea.debug;

import com.google.jstestdriver.config.ParsedConfiguration;
import com.google.jstestdriver.config.YamlParser;
import com.google.jstestdriver.idea.TestRunner;
import com.google.jstestdriver.idea.execution.JstdRunConfiguration;
import com.google.jstestdriver.idea.execution.JstdRunConfigurationVerifier;
import com.google.jstestdriver.idea.execution.JstdTestRunnerCommandLineState;
import com.google.jstestdriver.idea.server.ui.JstdToolWindowPanel;
import com.google.jstestdriver.model.BasePaths;
import com.intellij.chromeConnector.connection.ChromeConnectionManager;
import com.intellij.chromeConnector.connection.impl.ChromeConnectionManagerImpl;
import com.intellij.chromeConnector.connection.impl.ExistentTabProviderFactory;
import com.intellij.chromeConnector.debugger.ChromeDebuggerEngine;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.javascript.debugger.engine.JSDebugEngine;
import com.intellij.javascript.debugger.execution.RemoteDebuggingFileFinder;
import com.intellij.javascript.debugger.execution.RemoteJavaScriptDebugConfiguration;
import com.intellij.javascript.debugger.impl.DebuggableFileFinder;
import com.intellij.javascript.debugger.impl.JSDebugProcess;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtilRt;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class JstdDebugProgramRunner extends GenericProgramRunner {

  private static final String DEBUG_RUNNER_ID = JstdDebugProgramRunner.class.getSimpleName();

  @NotNull
  @Override
  public String getRunnerId() {
    return DEBUG_RUNNER_ID;
  }

  @Override
  public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
    return DefaultDebugExecutor.EXECUTOR_ID.equals(executorId) && profile instanceof JstdRunConfiguration;
  }

  @Override
  protected RunContentDescriptor doExecute(@NotNull Project project,
                                           @NotNull Executor executor,
                                           RunProfileState state,
                                           @Nullable RunContentDescriptor contentToReuse,
                                           @NotNull ExecutionEnvironment env) throws ExecutionException {
    JstdRunConfiguration runConfiguration = (JstdRunConfiguration) env.getRunProfile();
    if (runConfiguration.getRunSettings().isExternalServerType()) {
      throw new ExecutionException("Debug is available only for local browsers captured by a local JsTestDriver server.");
    }

    JstdRunConfigurationVerifier.checkJstdServerAndBrowserEnvironment(project, runConfiguration.getRunSettings(), true);
    JstdDebugBrowserInfo<?> context = JstdDebugBrowserInfo.build();
    if (context == null) {
      throw new ExecutionException("Can not find a browser that supports debugging.");
    }

    RunContentDescriptor descriptor = startSession(project, contentToReuse, env, context, executor, runConfiguration);

    return descriptor;
  }

  @Nullable
  private <Connection> RunContentDescriptor startSession(@NotNull Project project,
                                                         @Nullable RunContentDescriptor contentToReuse,
                                                         @NotNull ExecutionEnvironment env,
                                                         @NotNull JstdDebugBrowserInfo<Connection> debugBrowserInfo,
                                                         @NotNull Executor executor,
                                                         @NotNull JstdRunConfiguration runConfiguration) throws ExecutionException {
    FileDocumentManager.getInstance().saveAllDocuments();

    final JSDebugEngine<Connection> debugEngine = debugBrowserInfo.getDebugEngine();
    if (!debugEngine.prepareDebugger(project)) return null;

    final String url;
    final Connection connection;
    if (debugEngine instanceof ChromeDebuggerEngine) {
      ChromeConnectionManagerImpl chromeConnectionManager = (ChromeConnectionManagerImpl) ChromeConnectionManager.getInstance();
      ExistentTabProviderFactory tabProviderFactory = ExistentTabProviderFactory.getInstance();
      connection = (Connection) chromeConnectionManager.createConnection(tabProviderFactory);
      url = "http://localhost:" + JstdToolWindowPanel.serverPort + debugBrowserInfo.getCapturedBrowserUrl();
    }
    else {
      connection = debugEngine.openConnection();
      url = null;
    }

    JstdTestRunnerCommandLineState runState = runConfiguration.getState(env, null, true);
    final ExecutionResult executionResult = runState.execute(executor, JstdDebugProgramRunner.this);

    File configFile = new File(runConfiguration.getRunSettings().getConfigFile());
    List<RemoteJavaScriptDebugConfiguration.RemoteUrlMappingBean> mapping = extractMappings(configFile);

    final DebuggableFileFinder fileFinder = new RemoteDebuggingFileFinder(project, mapping);

    XDebuggerManager xDebuggerManager = XDebuggerManager.getInstance(project);
    XDebugSession xDebugSession = xDebuggerManager.startSession(this, env, contentToReuse, new XDebugProcessStarter() {
      @NotNull
      public XDebugProcess start(@NotNull final XDebugSession session) {
        JSDebugProcess debugProcess = debugEngine.createDebugProcess(session, fileFinder, connection, url, executionResult);
        return debugProcess;
      }
    });
    PrintWriter writer = new PrintWriter(executionResult.getProcessHandler().getProcessInput());
    writer.println(TestRunner.DEBUG_SESSION_STARTED + "\n");
    writer.flush();

    return xDebugSession.getRunContentDescriptor();
  }

  private List<RemoteJavaScriptDebugConfiguration.RemoteUrlMappingBean> extractMappings(@NotNull File configFile) throws ExecutionException {
    VirtualFile virtualFile = VfsUtil.findFileByIoFile(configFile, false);
    if (virtualFile == null) {
      throw new ExecutionException("Can not find config file " + configFile.getAbsolutePath());
    }
    BasePaths dirBasePaths = new BasePaths(configFile.getParentFile());
    byte[] content;
    try {
      content = virtualFile.contentsToByteArray();
    }
    catch (IOException e) {
      throw new ExecutionException("Can not read " + configFile.getAbsolutePath());
    }
    Reader reader = new InputStreamReader(new ByteArrayInputStream(content), Charset.defaultCharset());
    try {
      BasePaths allBasePaths = readBasePath(reader, dirBasePaths);
      List<RemoteJavaScriptDebugConfiguration.RemoteUrlMappingBean> mapping = ContainerUtilRt.newArrayList();
      for (File basePath : allBasePaths) {
        String normalizedPath = basePath.toURI().normalize().getPath();
        mapping.add(new RemoteJavaScriptDebugConfiguration.RemoteUrlMappingBean(normalizedPath, "http://localhost:9876/test"));
      }
      return mapping;
    }
    catch (Exception e) {
    }
    finally {
      try {
        reader.close();
      }
      catch (IOException ignored) {
      }
    }
    throw new ExecutionException("Unknown error");
  }

  private BasePaths readBasePath(@NotNull Reader configFileReader, @NotNull BasePaths initialBasePaths) {
    YamlParser yamlParser = new YamlParser();
    ParsedConfiguration parsedConfiguration = (ParsedConfiguration) yamlParser.parse(configFileReader, initialBasePaths);
    return parsedConfiguration.getBasePaths();
  }

}
