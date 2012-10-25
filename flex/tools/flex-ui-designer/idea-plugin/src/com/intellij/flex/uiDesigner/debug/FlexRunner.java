package com.intellij.flex.uiDesigner.debug;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.flex.uiDesigner.DebugPathManager;
import com.intellij.lang.javascript.flex.debug.FlexDebugProcess;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.run.BCBasedRunnerParameters;
import com.intellij.lang.javascript.flex.run.RemoteFlashRunConfiguration;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.impl.libraries.ProjectLibraryTable;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.StringBuilderSpinAllocator;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.StringTokenizer;

// we need SILENTLY_DETACH_ON_CLOSE, but RunContentManagerImpl provides only ProcessHandler.SILENTLY_DESTROY_ON_CLOSE
public class FlexRunner extends GenericProgramRunner {
  private static final String SKIP_MARKER = "^";
  
  private final Callback callback;
  private FlexBuildConfiguration buildConfiguration;

  public FlexRunner(Callback callback, FlexBuildConfiguration buildConfiguration) {
    this.callback = callback;

    this.buildConfiguration = buildConfiguration;
  }

  protected RunContentDescriptor doExecute(final Project project, final Executor executor, final RunProfileState state,
                                           final RunContentDescriptor contentToReuse, final ExecutionEnvironment env)
      throws ExecutionException {
    final BCBasedRunnerParameters parameters = ((RemoteFlashRunConfiguration)env.getRunProfile()).getRunnerParameters();

    RunContentDescriptor runContentDescriptor = XDebuggerManager.getInstance(project).startSession(this, env, contentToReuse,
      new XDebugProcessStarter() {
        @NotNull
        public XDebugProcess start(@NotNull final XDebugSession session) throws ExecutionException {
          try {
            return DebugPathManager.IS_DEV
              ? new MyFlexDebugProcessAbleToResolveFileDebugId(callback, session, buildConfiguration, parameters)
              : new MyFlexDebugProcess(callback, session, buildConfiguration, parameters);
          }
          catch (IOException e) {
            throw new ExecutionException(e.getMessage(), e);
          }
          catch (RuntimeConfigurationError e) {
            throw new ExecutionException(e.getMessage(), e);
          }
          finally {
            buildConfiguration = null;
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

    public MyFlexDebugProcess(Callback callback, XDebugSession session, FlexBuildConfiguration buildConfiguration,
                              BCBasedRunnerParameters parameters) throws IOException, RuntimeConfigurationError {
      super(session, buildConfiguration, parameters);
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
    public MyFlexDebugProcessAbleToResolveFileDebugId(Callback callback, XDebugSession session, FlexBuildConfiguration buildConfiguration, BCBasedRunnerParameters parameters)
      throws IOException, RuntimeConfigurationError {
      super(callback, session, buildConfiguration, parameters);
    }

    @Override
    protected void processShowFilesResult(StringTokenizer tokenizer) {
      final Map<String, String> libNameToSourceRoot = new THashMap<String, String>();
      while (tokenizer.hasMoreTokens()) {
        final String line = tokenizer.nextToken();
        final int spaceIndex = line.indexOf(' ');
        final int commaPos = line.indexOf(',');

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
          else if (line.indexOf("$framework/mx/effects/Effect.as", spaceIndex) != -1) {
            fullPath = DebugPathManager.getFudHome() + "/flex-injection/src/main/flex/mx/effects/Effect.as";
          }
          else {
            int firstSlashIndex = line.indexOf('/');
            StringBuilder builder = StringBuilderSpinAllocator.alloc();
            try {
              boolean isBackSlash = firstSlashIndex == -1;
              if (isBackSlash) {
                firstSlashIndex = line.indexOf('\\');
              }
              builder.append(myAppSdkHome).append("/frameworks/projects/").append(line, spaceIndex + 2, firstSlashIndex).append("/src/");
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
        else if (line.startsWith("/Users/develar/Documents/", spaceIndex + 1)) {
          fullPath = getFromAttachedSources(line, libNameToSourceRoot, commaPos);
          if (fullPath == null) {
            fullPath = line.substring(spaceIndex + 1, commaPos).replace(File.separatorChar, '/');
          }
        }
        else if (line.indexOf("FtyleProtoChain.as", spaceIndex) != -1) {
          fullPath = "/Developer/SDKs/flex_4.5.1/frameworks/projects/framework/src/mx/styles/StyleProtoChain.as";
        }
        else if (line.indexOf("Fffect.as", spaceIndex) != -1) {
          fullPath = "/Developer/SDKs/flex_4.5.1/frameworks/projects/framework/src/mx/effects/Effect.as";
        }
        else {
          fullPath = line.substring(spaceIndex + 1, commaPos).replace(File.separatorChar, '/');
        }

        String shortName = line.substring(commaPos + 2);
        myFilePathToIdMap.put(fullPath, id);
        addToMap(myFileNameToPathsMap, shortName, fullPath);
      }
    }

    private static String getFromAttachedSources(String line, Map<String, String> libNameToSourceRoot, int commaPos) {
      String srcDir = "/src/main/flex/";
      int srcFirstSlashIndex = line.lastIndexOf(srcDir);
      if (srcFirstSlashIndex == -1) {
        srcDir = "/src/";
        srcFirstSlashIndex = line.lastIndexOf(srcDir);
        if (srcFirstSlashIndex == -1) {
          return null;
        }
      }

      // must be wrapped — we check type (may be rb.swc, i. e. resource bundle)
      final String libName = ":" + line.substring(StringUtil.lastIndexOf(line, '/', 1, srcFirstSlashIndex - 1) + 1, srcFirstSlashIndex) + ":swc";
      String fullPath = libNameToSourceRoot.get(libName);
      if (fullPath == null) {
        for (Project project : ProjectManager.getInstance().getOpenProjects()) {
          for (Library library : ProjectLibraryTable.getInstance(project).getLibraries()) {
            if (library.getName().contains(libName)) {
              String[] urls = library.getUrls(OrderRootType.SOURCES);
              assert urls.length == 1;
              fullPath = urls[0];
              break;
            }
          }
        }

        if (fullPath == null) {
          libNameToSourceRoot.put(libName, SKIP_MARKER);
          return null;
        }
        else {
          libNameToSourceRoot.put(libName, fullPath);
        }
      }
      else if (fullPath.equals(SKIP_MARKER)) {
        return null;
      }

      return fullPath + line.substring(srcFirstSlashIndex + srcDir.length() - 1, commaPos);
    }
  }
}