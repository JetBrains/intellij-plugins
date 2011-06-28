package com.intellij.flex.uiDesigner.debug;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.flex.uiDesigner.DebugPathManager;
import com.intellij.lang.javascript.flex.debug.FlexDebugProcess;
import com.intellij.lang.javascript.flex.run.FlexBaseRunner;
import com.intellij.lang.javascript.flex.run.FlexRunnerParameters;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.util.StringBuilderSpinAllocator;
import com.intellij.xdebugger.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

// we need SILENTLY_DETACH_ON_CLOSE, but RunContentManagerImpl provides only ProcessHandler.SILENTLY_DESTROY_ON_CLOSE, so,
// we override destroyProcess as detachProcess
public class MyFlexBaseRunner extends FlexBaseRunner {
  @Override
  protected RunContentDescriptor doLaunch(final Project project, final Executor executor, RunProfileState state,
                                          RunContentDescriptor contentToReuse, final ExecutionEnvironment env, final Sdk flexSdk,
                                          final FlexRunnerParameters flexRunnerParameters) throws ExecutionException {
    return XDebuggerManager.getInstance(project).startSession(this, env, contentToReuse, new XDebugProcessStarter() {
      @NotNull
      public XDebugProcess start(@NotNull final XDebugSession session) throws ExecutionException {
        try {
          return DebugPathManager.IS_DEV ? new MyFlexDebugProcessAbleToResolveFileDebugId(session, flexSdk, flexRunnerParameters) : new MyFlexDebugProcess(session, flexSdk, flexRunnerParameters);
        }
        catch (IOException e) {
          throw new ExecutionException(e.getMessage(), e);
        }
      }
    }).getRunContentDescriptor();
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

  public static class MyDefaultDebugProcessHandler extends DefaultDebugProcessHandler {
    @Override
    public void destroyProcess() {
      if (DebugPathManager.IS_DEV) {
        super.destroyProcess();
      }
      else {
        detachProcess();
      }
    }

    public void myDestroyProcess() {
      super.destroyProcess();
    }
  }

  private static class MyFlexDebugProcess extends FlexDebugProcess {
    public MyFlexDebugProcess(XDebugSession session, Sdk flexSdk, FlexRunnerParameters flexRunnerParameters) throws IOException {
      super(session, flexSdk, flexRunnerParameters);
    }

    @Override
    public void stop() {
      if (DebugPathManager.IS_DEV) {
        super.stop();
      }
    }

    @Override
    protected ProcessHandler doGetProcessHandler() {
      return new MyDefaultDebugProcessHandler();
    }
  }

  private class MyFlexDebugProcessAbleToResolveFileDebugId extends MyFlexDebugProcess {
    public MyFlexDebugProcessAbleToResolveFileDebugId(XDebugSession session, Sdk flexSdk, FlexRunnerParameters flexRunnerParameters)
      throws IOException {
      super(session, flexSdk, flexRunnerParameters);
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
        else if (line.startsWith("/Users/develar/workspace/flex_sdk_4.5_modified", spaceIndex + 1)) {
          final int beginIndex = "/Users/develar/workspace/flex_sdk_4.5_modified/frameworks/projects/".length() + spaceIndex + 1;
          final int endIndex = line.indexOf('/', beginIndex + 2);
          String libName = line.substring(beginIndex, endIndex);
          fullPath = "/Users/develar/.m2/repository/com/adobe/flex/framework/" + libName + "/4.5.0.20968/" + libName + "-4.5.0.20968-sources.jar!" + line.substring(endIndex + "src".length() + 1, commaPos);
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