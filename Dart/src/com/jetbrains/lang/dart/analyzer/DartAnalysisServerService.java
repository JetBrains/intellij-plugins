package com.jetbrains.lang.dart.analyzer;

import com.google.dart.server.AnalysisServerListener;
import com.google.dart.server.GetErrorsConsumer;
import com.google.dart.server.GetFixesConsumer;
import com.google.dart.server.generated.types.*;
import com.google.dart.server.internal.remote.DebugPrintStream;
import com.google.dart.server.internal.remote.RemoteAnalysisServerImpl;
import com.google.dart.server.internal.remote.StdioServerSocket;
import com.intellij.ProjectTopics;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.concurrency.Semaphore;
import com.intellij.util.containers.hash.HashSet;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.net.NetUtils;
import com.jetbrains.lang.dart.sdk.DartSdk;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DartAnalysisServerService {

  private static final Logger LOG = Logger.getInstance("#com.jetbrains.lang.dart.analyzer.DartAnalysisServerService");

  private static final long GET_ERRORS_TIMEOUT = ApplicationManager.getApplication().isUnitTestMode() ? TimeUnit.SECONDS.toMillis(50)
                                                                                                      : TimeUnit.SECONDS.toMillis(5);
  private static final long GET_FIXES_TIMEOUT = TimeUnit.SECONDS.toMillis(1);

  private String mySdkHome = null;

  public static final String MIN_SDK_VERSION = "1.7";

  // todo these lists need to be stored in relation to the project that they came from
  // so that the lists can be updated as projects may change content roots
  private ArrayList<String> myIncludedAnalysisRoots = new ArrayList<String>();
  private ArrayList<String> myExcludedAnalysisRoots = new ArrayList<String>();

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
      if (isFatal) {
        stopServer();
        String dartSdkHome = getSdkHome();
        if (dartSdkHome != null) {
          startServer(dartSdkHome);
        }
      }
    }

    public void serverStatus(AnalysisStatus status) {
    }
  };

  private RemoteAnalysisServerImpl myServer;

  public DartAnalysisServerService() {
    String dartSdkHome = getSdkHome();
    if (dartSdkHome != null) {
      startServer(dartSdkHome);
    }
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

  @Nullable
  public static String getSdkHome() {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      return System.getProperty("dart.sdk");
    }
    else {
      DartSdk dartSdk = DartSdk.getGlobalDartSdk();
      if (dartSdk != null) {
        return dartSdk.getHomePath();
      }
    }
    return null;
  }

  public void updateContent(@Nullable final Map<String, Object> files) {
    if (files == null || files.isEmpty()) return;
    myServer.analysis_updateContent(files);
  }

  @Nullable
  public AnalysisError[] analysis_getErrors(@NotNull final DartAnalysisServerAnnotator.AnnotatorInfo info) {
    if (!serverReadyForRequest(info)) {
      return null;
    }

    final Ref<AnalysisError[]> resultError = new Ref<AnalysisError[]>();

    final Semaphore semaphore = new Semaphore();
    semaphore.down();

    try {
      final String path = FileUtil.toSystemDependentName(info.myFilePath);
      myServer.analysis_getErrors(path, new GetErrorsConsumer() {
        @Override
        public void computedErrors(final AnalysisError[] errors) {
          if (semaphore.tryUp()) {
            resultError.set(errors);
          }
          else {
            // semaphore unlocked by timeout, schedule to highlight the file again
            LOG.info("analysis_getErrors() took too long for file " + path + ", restarting daemon");

            ApplicationManager.getApplication().runReadAction(new Runnable() {
              @Override
              public void run() {
                final VirtualFile vFile = info.myProject.isDisposed() ? null
                                                                      : LocalFileSystem.getInstance().findFileByPath(info.myFilePath);
                final PsiFile psiFile = vFile == null ? null : PsiManager.getInstance(info.myProject).findFile(vFile);
                if (psiFile != null) {
                  DaemonCodeAnalyzer.getInstance(info.myProject).restart(psiFile);
                }
              }
            });
          }
        }

        @Override
        public void onError(final RequestError error) {
          semaphore.up();
          LOG.error("Error from analysis_getErrors() for file " + path + ", code=" + error.getCode() + ": " + error.getMessage());
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
  public List<AnalysisErrorFixes> analysis_getFixes(@NotNull final DartAnalysisServerAnnotator.AnnotatorInfo info, final int offset) {
    if (!serverReadyForRequest(info)) {
      return null;
    }

    final Ref<List<AnalysisErrorFixes>> resultFixes = new Ref<List<AnalysisErrorFixes>>();

    final Semaphore semaphore = new Semaphore();
    semaphore.down();

    final String path = FileUtil.toSystemDependentName(info.myFilePath);
    myServer.edit_getFixes(path, offset, new GetFixesConsumer() {
      @Override
      public void computedFixes(final List<AnalysisErrorFixes> fixes) {
        semaphore.up();
        resultFixes.set(fixes);
      }

      @Override
      public void onError(final RequestError error) {
        semaphore.up();
        LOG.warn("Error from edit_getFixes() for file " + path + ", code=" + error.getCode() + ": " + error.getMessage());
      }
    });

    final long t0 = System.currentTimeMillis();
    semaphore.waitFor(GET_FIXES_TIMEOUT);

    if (semaphore.tryUp()) {
      LOG.info("edit_getFixes() took too long for file " + path + ": " + (System.currentTimeMillis() - t0) + "ms");
      return null;
    }

    return resultFixes.get();
  }

  private void startServer(@NotNull final String sdkHome) {
    mySdkHome = sdkHome;
    final String runtimePath = mySdkHome + "/bin/dart";
    final String analysisServerPath = mySdkHome + "/bin/snapshots/analysis_server.dart.snapshot";
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
    myServer.analysis_updateOptions(new AnalysisOptions(true, true, true, false, true));
    myServer.addAnalysisServerListener(myListener);
  }

  private void updateAnalysisRootsWithProject(@NotNull final Project project) {
    for (final Module module : ModuleManager.getInstance(project).getModules()) {
      for (final VirtualFile contentRoot : ModuleRootManager.getInstance(module).getContentRoots()) {
        final String root = FileUtil.toSystemDependentName(contentRoot.getPath());
        if(!myIncludedAnalysisRoots.contains(root)) {
          myIncludedAnalysisRoots.add(root);
        }
      }
      for (final VirtualFile excludedRoot : ModuleRootManager.getInstance(module).getExcludeRoots()) {
        final String root = FileUtil.toSystemDependentName(excludedRoot.getPath());
        if(!myExcludedAnalysisRoots.contains(root)) {
          myExcludedAnalysisRoots.add(root);
        }
      }
    }
    myServer.analysis_setAnalysisRoots(myIncludedAnalysisRoots, myExcludedAnalysisRoots, null);
  }

  private boolean serverReadyForRequest(@NotNull final DartAnalysisServerAnnotator.AnnotatorInfo info) {
    if (info.mySdkHome == null) {
      LOG.info("Dart SDK of version " + MIN_SDK_VERSION + " or higher must be set.");
      return false;
    }

    if (mySdkHome == null || !info.mySdkHome.equals(mySdkHome)) {
      stopServer();
      startServer(info.mySdkHome);
    }
    updateAnalysisRootsWithProject(info.myProject);
    return true;
  }

  private void stopServer() {
    if (myServer != null) {
      myServer.server_shutdown();
      mySdkHome = null;
    }
  }
}
