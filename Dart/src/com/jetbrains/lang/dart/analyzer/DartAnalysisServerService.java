package com.jetbrains.lang.dart.analyzer;

import com.google.dart.server.AnalysisServer;
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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.concurrency.Semaphore;
import com.intellij.util.net.NetUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DartAnalysisServerService {

  public static final String MIN_SDK_VERSION = "1.7";
  private static final long GET_ERRORS_TIMEOUT = ApplicationManager.getApplication().isUnitTestMode() ? TimeUnit.SECONDS.toMillis(50)
                                                                                                      : TimeUnit.SECONDS.toMillis(5);
  private static final long GET_FIXES_TIMEOUT = TimeUnit.SECONDS.toMillis(1);
  private static final Logger LOG = Logger.getInstance("#com.jetbrains.lang.dart.analyzer.DartAnalysisServerService");

  @Nullable private AnalysisServer myServer;
  @Nullable private String mySdkHome = null;
  private final DartServerRootsHandler myRootsHandler = new DartServerRootsHandler();

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
      LOG.warn("Dart analysis server " + (isFatal ? "FATAL " : "") + "error: " + message + "\n" + stackTrace);

      if (isFatal) {
        stopServer();
      }
    }

    public void serverStatus(AnalysisStatus status) {
    }
  };

  public DartAnalysisServerService() {
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
    if (myServer == null) return;

    if (files == null || files.isEmpty()) return;
    myServer.analysis_updateContent(files);
  }

  public boolean updateRoots(final List<String> includedRoots, final List<String> excludedRoots) {
    if (myServer == null) return false;

    if (LOG.isDebugEnabled()) {
      LOG.debug("analysis_setAnalysisRoots, included:\n" + StringUtil.join(includedRoots, ",\n") +
                "\nexcluded:\n" + StringUtil.join(excludedRoots, ",\n"));
    }

    myServer.analysis_setAnalysisRoots(includedRoots, excludedRoots, null);
    return true;
  }

  @Nullable
  public AnalysisError[] analysis_getErrors(@NotNull final DartAnalysisServerAnnotator.AnnotatorInfo info) {
    if (myServer == null) return null;

    final Ref<AnalysisError[]> resultError = new Ref<AnalysisError[]>();

    final Semaphore semaphore = new Semaphore();
    semaphore.down();

    try {
      final String path = FileUtil.toSystemDependentName(info.myFilePath);
      LOG.debug("analysis_getErrors(" + path + ")");

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
      semaphore.up(); // make sure to unlock semaphore so that computedErrors() can understand when it was unlocked by timeout
    }

    return resultError.get();
  }

  @Nullable
  public List<AnalysisErrorFixes> analysis_getFixes(@NotNull final DartAnalysisServerAnnotator.AnnotatorInfo info, final int offset) {
    if (myServer == null) return null;

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

    final String runtimePath = FileUtil
      .toSystemDependentName((ApplicationManager.getApplication().isUnitTestMode() ? System.getProperty("dart.sdk") : mySdkHome)
                             + "/bin/dart");
    final String analysisServerPath = FileUtil
      .toSystemDependentName((ApplicationManager.getApplication().isUnitTestMode() ? System.getProperty("dart.sdk") : mySdkHome)
                             + "/bin/snapshots/analysis_server.dart.snapshot");

    final DebugPrintStream debugStream = new DebugPrintStream() {
      @Override
      public void println(String str) {
        //System.out.println("debugStream: " + str);
      }
    };

    final int port = NetUtils.tryToFindAvailableSocketPort(10000);

    final StdioServerSocket serverSocket =
      new StdioServerSocket(runtimePath, analysisServerPath, null, debugStream, new String[]{}, false, false, port, false);
    myServer = new RemoteAnalysisServerImpl(serverSocket);

    try {
      myServer.start();
      myServer.analysis_updateOptions(new AnalysisOptions(true, true, true, false, true));
      myServer.addAnalysisServerListener(myListener);
      LOG.info("Server started, see status at http://localhost:" + port + "/status");
    }
    catch (Exception e) {
      LOG.warn("Failed to start Dart analysis server, port=" + port, e);
      myServer = null;
      mySdkHome = null;
      myRootsHandler.reset();
    }
  }

  public boolean serverReadyForRequest(@NotNull final Project project, @NotNull final String sdkHome) {
    if (myServer == null || !sdkHome.equals(mySdkHome)) {
      stopServer();
      startServer(sdkHome);
    }

    if (myServer != null) {
      myRootsHandler.ensureProjectServed(project);
      return true;
    }

    return false;
  }

  private void stopServer() {
    if (myServer != null) {
      LOG.debug("stopping server");
      myServer.server_shutdown();
      myServer = null;
      mySdkHome = null;
      myRootsHandler.reset();
    }
  }
}
