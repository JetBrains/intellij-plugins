package com.jetbrains.lang.dart.analyzer;

import com.google.dart.server.AnalysisServerListener;
import com.google.dart.server.GetErrorsConsumer;
import com.google.dart.server.GetFixesConsumer;
import com.google.dart.server.generated.types.*;
import com.google.dart.server.internal.remote.DebugPrintStream;
import com.google.dart.server.internal.remote.RemoteAnalysisServerImpl;
import com.google.dart.server.internal.remote.StdioServerSocket;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.concurrency.Semaphore;
import com.intellij.util.net.NetUtils;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DartAnalysisServerService {

  private static final Logger LOG = Logger.getInstance("#com.jetbrains.lang.dart.analyzer.DartAnalysisServerService");

  private static final long GET_ERRORS_TIMEOUT = TimeUnit.SECONDS.toMillis(5);
  private static final long GET_FIXES_TIMEOUT = TimeUnit.SECONDS.toMillis(1);

  private final AnalysisServerListener myListener = new AnalysisServerListener() {

    public void computedCompletion(String completionId, int replacementOffset, int replacementLength,
                                   List<CompletionSuggestion> completions, boolean isLast) {
    }

    public void computedErrors(String file, List<AnalysisError> errors) {

    }

    public void computedHighlights(String file, List<HighlightRegion> highlights) {
    }

    public void computedLaunchData(String file, String kind, String[] referencedFiles) {
    }

    public void computedNavigation(String file, List<NavigationRegion> targets) {
    }

    public void computedOccurrences(String file, List<Occurrences> occurrencesArray) {
    }

    public void computedOutline(String file, Outline outline) {
    }

    public void computedOverrides(String file, List<OverrideMember> overrides) {
    }

    public void computedSearchResults(String searchId, List<SearchResult> results, boolean last) {
    }

    public void flushedResults(List<String> files) {
    }

    public void serverConnected() {
    }

    public void serverError(boolean isFatal, String message, String stackTrace) {
    }

    public void serverStatus(AnalysisStatus status) {
    }
  };

  private RemoteAnalysisServerImpl myServer;

  public DartAnalysisServerService() {
    startServer();
    Disposer.register(ApplicationManager.getApplication(), new Disposable() {
      public void dispose() {
        stopServer();
      }
    });
  }

  @NotNull
  public static DartAnalysisServerService getInstance() {
    return ServiceManager.getService(DartAnalysisServerService.class);
  }

  public void updateContent(@Nullable final Map<String, Object> files) {
    if (files == null || files.isEmpty()) return;
    myServer.analysis_updateContent(files);
  }

  @Nullable
  public AnalysisError[] analysis_getErrors(@NotNull final PsiFile psiFile) {
    final VirtualFile vFile = DartResolveUtil.getRealVirtualFile(psiFile);
    if (vFile == null) return null;

    final Ref<AnalysisError[]> resultError = new Ref<AnalysisError[]>();

    final Semaphore semaphore = new Semaphore();
    semaphore.down();

    try {
      myServer.analysis_getErrors(FileUtil.toSystemDependentName(vFile.getPath()), new GetErrorsConsumer() {
        @Override
        public void computedErrors(final AnalysisError[] errors) {
          if (semaphore.tryUp()) {
            resultError.set(errors);
          }
          else {
            // semaphore unlocked by timeout, schedule to highlight the file again
            ApplicationManager.getApplication().runReadAction(new Runnable() {
              @Override
              public void run() {
                final Project project = psiFile.isValid() ? psiFile.getProject() : null;
                if (project != null && !project.isDisposed()) {
                  DaemonCodeAnalyzer.getInstance(project).restart(psiFile);
                }
              }
            });
          }
        }

        @Override
        public void onError(final RequestError requestError) {
          semaphore.up();
          LOG.error(requestError.getMessage(), requestError.getStackTrace());
        }
      });

      semaphore.waitFor(GET_ERRORS_TIMEOUT);
    }
    finally {
      semaphore.up(); // make sure that semaphore is unlock so that computedErrors() can understand when it was unlocked by timeout
    }

    return resultError.get();
  }

  @Nullable
  public List<AnalysisErrorFixes> analysis_getFixes(@NotNull final PsiFile psiFile, final int offset) {
    final VirtualFile vFile = DartResolveUtil.getRealVirtualFile(psiFile);
    if (vFile == null) return null;

    final Ref<List<AnalysisErrorFixes>> resultError = new Ref<List<AnalysisErrorFixes>>();

    final Semaphore semaphore = new Semaphore();
    semaphore.down();

    try {
      myServer.edit_getFixes(FileUtil.toSystemDependentName(vFile.getPath()), offset, new GetFixesConsumer() {
        @Override
        public void computedFixes(final List<AnalysisErrorFixes> fixes) {
          if (semaphore.tryUp()) {
            resultError.set(fixes);
          }
          else {
            // semaphore unlocked by timeout, schedule to highlight the file again
            ApplicationManager.getApplication().runReadAction(new Runnable() {
              @Override
              public void run() {
                final Project project = psiFile.isValid() ? psiFile.getProject() : null;
                if (project != null && !project.isDisposed()) {
                  DaemonCodeAnalyzer.getInstance(project).restart(psiFile);
                }
              }
            });
          }
        }

        @Override
        public void onError(final RequestError requestError) {
          semaphore.up();
          LOG.warn(requestError.getMessage());
        }
      });

      semaphore.waitFor(GET_FIXES_TIMEOUT);
    }
    finally {
      semaphore.up(); // make sure that semaphore is unlock so that computedErrors() can understand when it was unlocked by timeout
    }

    return resultError.get();
  }

  private void startServer() {
    final DartSdk sdk = DartSdk.getGlobalDartSdk();
    if (sdk == null) {
      LOG.error("No SDK");
      return;
    }

    final String sdkPath = sdk.getHomePath();
    final String runtimePath = sdkPath + "/bin/dart";
    final String analysisServerPath = sdkPath + "/bin/snapshots/analysis_server.dart.snapshot";
    final DebugPrintStream debugStream = new DebugPrintStream() {
      @Override
      public void println(String str) {
        //System.out.println("debugStream: " + str);
      }
    };

    final StdioServerSocket serverSocket;
    if (!ApplicationManager.getApplication().isInternal()) {
      serverSocket = new StdioServerSocket(runtimePath, analysisServerPath, null, debugStream, new String[]{}, false, false, 0, false);
    }
    else {
      int availablePort = 10000;
      try {
        availablePort = NetUtils.findAvailableSocketPort();
      }
      catch (IOException e) {
        LOG.error(e.getMessage(), e);
      }
      LOG.debug("Go to http://localhost:" + availablePort + "/status to see status of analysis server");
      serverSocket =
        new StdioServerSocket(runtimePath, analysisServerPath, null, debugStream, new String[]{"--port=" + availablePort}, false, false, 0,
                              false);
    }

    myServer = new RemoteAnalysisServerImpl(serverSocket);
    try {
      myServer.start();
    }
    catch (Exception e) {
      LOG.debug(e.getMessage(), e);
    }
    setAnalysisRoots(ProjectManager.getInstance().getOpenProjects());
    setOptions();
    myServer.addAnalysisServerListener(myListener);
  }

  private void setOptions() {
    myServer.analysis_updateOptions(new AnalysisOptions(true, true, true, false, true));
  }

  private void setAnalysisRoots(@NotNull final Project[] projects) {
    ArrayList<String> included = new ArrayList<String>();
    ArrayList<String> excluded = new ArrayList<String>();
    for (final Project project : projects) {
      for (final Module module : ModuleManager.getInstance(project).getModules()) {
        for (final VirtualFile contentRoot : ModuleRootManager.getInstance(module).getContentRoots()) {
          included.add(contentRoot.getPath());
        }
        for (final VirtualFile excludedRoot : ModuleRootManager.getInstance(module).getExcludeRoots()) {
          excluded.add(excludedRoot.getPath());
        }
      }
    }
    myServer.analysis_setAnalysisRoots(included, excluded, null);
  }

  private void stopServer() {
    myServer.server_shutdown();
  }
}
