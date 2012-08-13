package com.google.jstestdriver.idea.debug;

import com.google.jstestdriver.CapturedBrowsers;
import com.google.jstestdriver.SlaveBrowser;
import com.google.jstestdriver.config.ParsedConfiguration;
import com.google.jstestdriver.config.YamlParser;
import com.google.jstestdriver.idea.execution.JstdRunConfiguration;
import com.google.jstestdriver.idea.execution.JstdRunConfigurationVerifier;
import com.google.jstestdriver.idea.execution.JstdTestRunnerCommandLineState;
import com.google.jstestdriver.idea.server.JstdServerState;
import com.google.jstestdriver.model.BasePaths;
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
  protected RunContentDescriptor doExecute(Project project,
                                           Executor executor,
                                           RunProfileState state,
                                           RunContentDescriptor contentToReuse,
                                           ExecutionEnvironment env) throws ExecutionException {
    JstdRunConfiguration runConfiguration = (JstdRunConfiguration) env.getRunProfile();

    JstdRunConfigurationVerifier.isJstdLocalServerReady(project, runConfiguration.getRunSettings(), true);
    JstdServerState jstdServerState = JstdServerState.getInstance();
    if (!jstdServerState.isServerRunning()) {
      throw new ExecutionException("JsTestDriver server isn't running");
    }
    Context<?> context = getContext(jstdServerState);
    if (context == null) {
      throw new ExecutionException("Debug is available in Firefox and Chrome.\n" +
                                   "Please capture one of these browsers and try again.");
    }


    // start debugger
    RunContentDescriptor descriptor = startSession(project, contentToReuse, env, context, executor, runConfiguration);

    return descriptor;
  }

  @Nullable
  private <Connection> RunContentDescriptor startSession(Project project,
                                                         RunContentDescriptor contentToReuse,
                                                         final ExecutionEnvironment env,
                                                         @NotNull Context<Connection> context,
                                                         final Executor executor,
                                                         @NotNull JstdRunConfiguration runConfiguration) throws ExecutionException {
    final JSDebugEngine<Connection> debugEngine = context.getDebugEngine();
    if (!debugEngine.prepareDebugger(project)) return null;

    final Connection connection = debugEngine.openConnection();

    FileDocumentManager.getInstance().saveAllDocuments();
    final String url = "http://localhost:9876" + context.getUrl();
    File configFile = new File(runConfiguration.getRunSettings().getConfigFile());
    List<RemoteJavaScriptDebugConfiguration.RemoteUrlMappingBean> mapping = extractMappings(configFile);

    final JstdTestRunnerCommandLineState runState = runConfiguration.getState(executor, env);
    final DebuggableFileFinder fileFinder = new RemoteDebuggingFileFinder(project, mapping);
    final XDebugSession
      debugSession = XDebuggerManager.getInstance(project).startSession(this, env, contentToReuse, new XDebugProcessStarter() {
      @NotNull
      public XDebugProcess start(@NotNull final XDebugSession session) {
        try {
          ExecutionResult executionResult = runState.execute(executor, JstdDebugProgramRunner.this);
          return debugEngine.createDebugProcess(session, fileFinder, connection, url, executionResult);
        }
        catch (ExecutionException e) {
          throw new RuntimeException(e);
        }
      }
    });

    return debugSession.getRunContentDescriptor();
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

  @Nullable
  private static Context<?> getContext(@NotNull JstdServerState jstdServerState) {
    JSDebugEngine<?>[] engines = JSDebugEngine.getEngines();
    CapturedBrowsers browsers = jstdServerState.getCaptured();
    if (browsers == null) {
      return null;
    }
    for (SlaveBrowser slaveBrowser : browsers.getSlaveBrowsers()) {
      String browserName = slaveBrowser.getBrowserInfo().getName();
      for (JSDebugEngine<?> engine : engines) {
        if (engine.getId().equalsIgnoreCase(browserName)) {
          return new Context(engine, slaveBrowser.getCaptureUrl());
        }
      }
    }
    return null;
  }

  private static class Context<C> {
    private final JSDebugEngine<C> myDebugEngine;
    private final String myUrl;

    private Context(@NotNull JSDebugEngine<C> debugEngine, @NotNull String url) {
      myUrl = url;
      myDebugEngine = debugEngine;
    }

    @NotNull
    public JSDebugEngine<C> getDebugEngine() {
      return myDebugEngine;
    }

    @NotNull
    public String getUrl() {
      return myUrl;
    }

  }

}
