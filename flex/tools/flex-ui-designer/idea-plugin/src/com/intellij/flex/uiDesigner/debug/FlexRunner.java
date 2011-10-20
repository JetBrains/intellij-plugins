package com.intellij.flex.uiDesigner.debug;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.flex.uiDesigner.DebugPathManager;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.debug.FlexDebugProcess;
import com.intellij.lang.javascript.flex.run.FlexIdeRunConfiguration;
import com.intellij.lang.javascript.flex.run.FlexRunConfiguration;
import com.intellij.lang.javascript.flex.run.FlexRunnerParameters;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.util.StringBuilderSpinAllocator;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

// we need SILENTLY_DETACH_ON_CLOSE, but RunContentManagerImpl provides only ProcessHandler.SILENTLY_DESTROY_ON_CLOSE
public class FlexRunner extends GenericProgramRunner {
  private final Callback callback;
  private Module module;

  public FlexRunner(Callback callback, Module module) {
    this.callback = callback;
    this.module = module;
  }

  protected RunContentDescriptor doExecute(final Project project, final Executor executor, final RunProfileState state,
                                           final RunContentDescriptor contentToReuse, final ExecutionEnvironment env)
      throws ExecutionException {
    final FlexRunnerParameters flexRunnerParameters = ((FlexRunConfiguration)env.getRunProfile()).getRunnerParameters();
    final Sdk flexSdk = FlexUtils.getFlexSdkForFlexModuleOrItsFlexFacets(module);
    module = null;

    RunContentDescriptor runContentDescriptor = XDebuggerManager.getInstance(project).startSession(this, env, contentToReuse,
        new XDebugProcessStarter() {
          @NotNull
          public XDebugProcess start(@NotNull final XDebugSession session) throws ExecutionException {
            try {
              return DebugPathManager.IS_DEV
                     ? new MyFlexDebugProcessAbleToResolveFileDebugId(callback, session, flexSdk, flexRunnerParameters)
                     : new MyFlexDebugProcess(callback, session, flexSdk, flexRunnerParameters);
            }
            catch (IOException e) {
              throw new ExecutionException(e.getMessage(), e);
            }
          }
        }).getRunContentDescriptor();

    ProcessHandler processHandler = runContentDescriptor.getProcessHandler();
    assert processHandler != null;
    //noinspection deprecation
    processHandler.putUserData(ProcessHandler.SILENTLY_DESTROY_ON_CLOSE, true);

    return runContentDescriptor;
  }

  @Override
  @NotNull
  public String getRunnerId() {
    return "FlexDebugRunnerForDesignView";
  }

  @Override
  public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
    return true;
  }

  private static class MyFlexDebugProcess extends FlexDebugProcess {
    private final Callback callback;

    public MyFlexDebugProcess(Callback callback, XDebugSession session, Sdk flexSdk,
                              FlexRunnerParameters flexRunnerParameters) throws IOException {
      super(session, flexSdk, flexRunnerParameters);
      this.callback = callback;
    }

    @Override
    public void stop() {
      if (DebugPathManager.IS_DEV) {
        super.stop();
      }
    }

    @Override
    protected void notifyFdbWaitingForPlayerStateReached() {
      callback.processStarted(getSession().getRunContentDescriptor());
    }
  }

  private static class MyFlexDebugProcessAbleToResolveFileDebugId extends MyFlexDebugProcess {
    public MyFlexDebugProcessAbleToResolveFileDebugId(Callback callback, XDebugSession session, Sdk flexSdk, FlexRunnerParameters flexRunnerParameters)
      throws IOException {
      super(callback, session, flexSdk, flexRunnerParameters);
    }

    @Override
    protected void processShowFilesResult(StringTokenizer tokenizer) {
      while (tokenizer.hasMoreTokens()) {
        String line = tokenizer.nextToken();
        int spaceIndex = line.indexOf(' ');
        int commaPos = line.indexOf(',');

        if (spaceIndex == -1 || commaPos == -1) {
          log("Unexpected string format:" + line);
          continue;
        }

        String id = line.substring(0, spaceIndex);
        String fullPath;
        if (line.charAt(spaceIndex + 1) == '$') {
          if (line.indexOf("$framework/mx/styles/StyleProtoChain.as", spaceIndex) != -1) {
            fullPath = DebugPathManager.getFudHome() + "/flex-injection/src/main/flex/mx/styles/StyleProtoChain.as";
          }
          else {
            int firstSlashIndex = line.indexOf('/');
            StringBuilder builder = StringBuilderSpinAllocator.alloc();
            try {
              boolean isBackSlash = firstSlashIndex == -1;
              if (isBackSlash) {
                firstSlashIndex = line.indexOf('\\');
              }
              builder.append(mySdkLocation).append("/frameworks/projects/").append(line, spaceIndex + 2, firstSlashIndex).append("/src/");
              if (isBackSlash) {
                builder.append(line.substring(firstSlashIndex + 1, commaPos).replace('\\', '/'));
              }
              else {
                builder.append(line, firstSlashIndex + 1, commaPos);
              }

              fullPath = builder.toString();
            }
            finally {
              StringBuilderSpinAllocator.dispose(builder);
            }
          }
        }
        else if (line.startsWith("/Users/develar/Documents/flex", spaceIndex + 1)) {
          final int beginIndex = "/Users/develar/Documents/flex/frameworks/projects/".length() + spaceIndex + 1;
          final int endIndex = line.indexOf('/', beginIndex + 2);
          String libName = line.substring(beginIndex, endIndex);
          fullPath = "/Users/develar/.m2/repository/com/adobe/flex/framework/" + libName + "/4.5.0.20968/" + libName + "-4.5.0.20968-sources.jar!" + line.substring(endIndex + "src".length() + 1, commaPos);
        }
        else if (line.startsWith("/Users/develar/workspace/cocoa", spaceIndex + 1)) {
          final int beginIndex = "/Users/develar/workspace/cocoa/".length() + spaceIndex + 1;
          final int endIndex = line.indexOf('/', beginIndex + 2);
          String libName = line.substring(beginIndex, endIndex);
          fullPath = "/Users/develar/.m2/repository/org/flyti/cocoa/" + libName + "/1.4-SNAPSHOT/" + libName + "-1.4-SNAPSHOT-sources.jar!" + line.substring(endIndex + "src/main/flex".length() + 1, commaPos);
        }
        else if (line.indexOf("FtyleProtoChain.as", spaceIndex) != -1) {
          fullPath = "/Developer/SDKs/flex_4.5.1/frameworks/projects/framework/src/mx/styles/StyleProtoChain.as";
        }
        else {
          fullPath = line.substring(spaceIndex + 1, commaPos).replace(File.separatorChar, '/');
        }

        String shortName = line.substring(commaPos + 2);
        myFilePathToIdMap.put(fullPath, id);
        addToMap(myFileNameToPathsMap, shortName, fullPath);
      }
    }
  }
}