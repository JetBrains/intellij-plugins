package com.jetbrains.lang.dart.analyzer;

import com.google.dart.server.*;
import com.google.dart.server.generated.types.*;
import com.google.dart.server.internal.remote.DebugPrintStream;
import com.google.dart.server.internal.remote.FileReadMode;
import com.google.dart.server.internal.remote.RemoteAnalysisServerImpl;
import com.google.dart.server.internal.remote.StdioServerSocket;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
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
import com.intellij.xml.util.HtmlUtil;
import com.jetbrains.lang.dart.DartFileType;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class DartAnalysisServerService {

  public static final String MIN_SDK_VERSION = "1.9";
  private static final long EDIT_FORMAT_TIMEOUT = TimeUnit.SECONDS.toMillis(3);
  private static final long GET_ERRORS_TIMEOUT = TimeUnit.SECONDS.toMillis(5);
  private static final long GET_ERRORS_LONGER_TIMEOUT = TimeUnit.SECONDS.toMillis(60);
  private static final long GET_FIXES_TIMEOUT = TimeUnit.SECONDS.toMillis(1);
  private static final long GET_VERSION_TIMEOUT = 500;
  private static final Logger LOG = Logger.getInstance("#com.jetbrains.lang.dart.analyzer.DartAnalysisServerService");

  private final Object myLock = new Object(); // Access all fields under this lock. Do not wait for server response under lock.
  @Nullable private AnalysisServer myServer;
  @NotNull private String myServerVersion = "";
  @Nullable private String mySdkHome = null;
  private final DartServerRootsHandler myRootsHandler = new DartServerRootsHandler();
  private final Map<String, Long> myFilePathWithOverlaidContentToTimestamp = new THashMap<String, Long>();

  private final AnalysisServerListener myListener = new AnalysisServerListener() {

    @Override
    public void computedCompletion(String completionId, int replacementOffset, int replacementLength,
                                   List<CompletionSuggestion> completions, boolean isLast) {
    }

    @Override
    public void computedErrors(String file, List<AnalysisError> errors) {
    }

    @Override
    public void computedHighlights(String file, List<HighlightRegion> highlights) {
    }

    @Override
    public void computedLaunchData(String file, String kind, String[] referencedFiles) {
    }

    @Override
    public void computedNavigation(String file, List<NavigationRegion> targets) {
    }

    @Override
    public void computedOccurrences(String file, List<Occurrences> occurrencesArray) {
    }

    @Override
    public void computedOutline(String file, Outline outline) {
    }

    @Override
    public void computedOverrides(String file, List<OverrideMember> overrides) {
    }

    @Override
    public void computedSearchResults(String searchId, List<SearchResult> results, boolean last) {
    }

    @Override
    public void flushedResults(List<String> files) {
    }

    @Override
    public void requestError(final RequestError requestError) {
    }

    @Override
    public void serverConnected() {
    }

    @Override
    public void serverError(boolean isFatal, String message, String stackTrace) {
      if (message == null) message = "<no error message>";
      if (stackTrace == null) stackTrace = "<no stack trace>";
      LOG.warn(
        "Dart analysis server, version " + myServerVersion + ", " + (isFatal ? "FATAL" : "") + "error: " + message + "\n" + stackTrace);

      if (isFatal) {
        onServerStopped();
      }
    }

    @Override
    public void serverStatus(final AnalysisStatus analysisStatus, final PubStatus pubStatus) {
    }
  };

  public static class FormatResult {

    private final List<SourceEdit> myEdits;
    private final int myOffset;
    private final int myLength;

    public FormatResult(final List<SourceEdit> edits, final int selectionOffset, final int selectionLength) {
      myEdits = edits;
      myOffset = selectionOffset;
      myLength = selectionLength;
    }

    public int getLength() {
      return myLength;
    }

    public int getOffset() {
      return myOffset;
    }

    public List<SourceEdit> getEdits() {
      return myEdits;
    }
  }


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

  public void updateFilesContent() {
    //TODO: consider using DocumentListener to collect deltas instead of sending the whole Document.getText() each time
    final Set<String> oldTrackedFiles = new THashSet<String>(myFilePathWithOverlaidContentToTimestamp.keySet());
    final Map<String, Object> filesToUpdate = new THashMap<String, Object>();

    synchronized (myLock) {
      if (myServer == null) return;

      final FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
      for (Document document : fileDocumentManager.getUnsavedDocuments()) {
        final VirtualFile file = fileDocumentManager.getFile(document);
        if (file != null && (file.getFileType() == DartFileType.INSTANCE || HtmlUtil.isHtmlFile(file))) {
          oldTrackedFiles.remove(file.getPath());

          final Long oldTimestamp = myFilePathWithOverlaidContentToTimestamp.get(file.getPath());
          if (oldTimestamp == null || document.getModificationStamp() != oldTimestamp) {
            filesToUpdate.put(FileUtil.toSystemDependentName(file.getPath()), new AddContentOverlay(document.getText()));
            myFilePathWithOverlaidContentToTimestamp.put(file.getPath(), document.getModificationStamp());
          }
        }
      }

      // oldTrackedFiles at this point contains only those files that are not in FileDocumentManager.getUnsavedDocuments() any more
      for (String oldPath : oldTrackedFiles) {
        final Long removed = myFilePathWithOverlaidContentToTimestamp.remove(oldPath);
        LOG.assertTrue(removed != null, oldPath);
        filesToUpdate.put(FileUtil.toSystemDependentName(oldPath), new RemoveContentOverlay());
      }

      if (LOG.isDebugEnabled()) {
        if (!filesToUpdate.isEmpty()) {
          LOG.debug("Sending overlaid content of the following files:\n" + StringUtil.join(filesToUpdate.keySet(), ",\n"));
        }

        if (!oldTrackedFiles.isEmpty()) {
          LOG.debug("Removing overlaid content of the following files:\n" + StringUtil.join(oldTrackedFiles, ",\n"));
        }
      }

      myServer.analysis_updateContent(filesToUpdate);
    }
  }


  public boolean updateRoots(final List<String> includedRoots, final List<String> excludedRoots) {
    synchronized (myLock) {
      if (myServer == null) return false;

      if (LOG.isDebugEnabled()) {
        LOG.debug("analysis_setAnalysisRoots, included:\n" + StringUtil.join(includedRoots, ",\n") +
                  "\nexcluded:\n" + StringUtil.join(excludedRoots, ",\n"));
      }

      myServer.analysis_setAnalysisRoots(includedRoots, excludedRoots, null);
      return true;
    }
  }

  @Nullable
  public AnalysisError[] analysis_getErrors(@NotNull final DartAnalysisServerAnnotator.AnnotatorInfo info) {
    final Ref<AnalysisError[]> resultRef = new Ref<AnalysisError[]>();
    final Semaphore semaphore = new Semaphore();

    try {
      synchronized (myLock) {
        if (myServer == null) return null;

        semaphore.down();

        final String path = FileUtil.toSystemDependentName(info.myFilePath);
        LOG.debug("analysis_getErrors(" + path + ")");

        myServer.analysis_getErrors(path, new GetErrorsConsumer() {
          @Override
          public void computedErrors(final AnalysisError[] errors) {
            if (semaphore.tryUp()) {
              resultRef.set(errors);
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
            LOG.error("Error from analysis_getErrors() for file " +
                      path +
                      ", server version= " +
                      myServerVersion +
                      ", code=" +
                      error.getCode() +
                      ": " +
                      error.getMessage());
          }
        });
      }

      final long timeout = info.isLongerAnalysisTimeout() || ApplicationManager.getApplication().isUnitTestMode()
                           ? GET_ERRORS_LONGER_TIMEOUT
                           : GET_ERRORS_TIMEOUT;
      semaphore.waitFor(timeout);
    }
    finally {
      semaphore.up(); // make sure to unlock semaphore so that computedErrors() can understand when it was unlocked by timeout
    }

    return resultRef.get();
  }

  @Nullable
  public List<AnalysisErrorFixes> analysis_getFixes(@NotNull final DartAnalysisServerAnnotator.AnnotatorInfo info, final int offset) {
    final Ref<List<AnalysisErrorFixes>> resultRef = new Ref<List<AnalysisErrorFixes>>();
    final Semaphore semaphore = new Semaphore();
    final String path = FileUtil.toSystemDependentName(info.myFilePath);

    synchronized (myLock) {
      if (myServer == null) return null;

      semaphore.down();

      myServer.edit_getFixes(path, offset, new GetFixesConsumer() {
        @Override
        public void computedFixes(final List<AnalysisErrorFixes> fixes) {
          resultRef.set(fixes);
          semaphore.up();
        }

        @Override
        public void onError(final RequestError error) {
          semaphore.up();
          LOG.error("Error from edit_getFixes() for file " +
                    path +
                    ", server version= " +
                    myServerVersion +
                    ", code=" +
                    error.getCode() +
                    ": " +
                    error.getMessage());
        }
      });
    }

    final long t0 = System.currentTimeMillis();
    semaphore.waitFor(GET_FIXES_TIMEOUT);

    if (semaphore.tryUp()) {
      LOG.info("edit_getFixes() took too long for file " + path + ": " + (System.currentTimeMillis() - t0) + "ms");
      return null;
    }

    return resultRef.get();
  }

  @Nullable
  public FormatResult edit_format(@NotNull final String path, final int selectionOffset, final int selectionLength) {
    final Ref<FormatResult> resultRef = new Ref<FormatResult>();
    final Semaphore semaphore = new Semaphore();

    synchronized (myLock) {
      if (myServer == null) return null;

      semaphore.down();

      myServer.edit_format(path, selectionOffset, selectionLength, new FormatConsumer() {
        @Override
        public void computedFormat(final List<SourceEdit> edits, final int selectionOffset, final int selectionLength) {
          resultRef.set(new FormatResult(edits, selectionOffset, selectionLength));
          semaphore.up();
        }

        @Override
        public void onError(final RequestError error) {
          semaphore.up();
          LOG.error("Error from edit_format() for file " +
                    path +
                    ", server version= " +
                    myServerVersion +
                    ", code=" +
                    error.getCode() + ": " + error.getMessage());
        }
      });
    }

    final long t0 = System.currentTimeMillis();
    semaphore.waitFor(EDIT_FORMAT_TIMEOUT);

    if (semaphore.tryUp()) {
      LOG.info("edit_format() took too long for file " + path + ": " + (System.currentTimeMillis() - t0) + "ms");
      return null;
    }

    return resultRef.get();
  }

  @NotNull
  private String server_getVersion() {
    final Ref<String> resultRef = new Ref<String>("");
    final Semaphore semaphore = new Semaphore();

    synchronized (myLock) {
      assert myServer != null;

      semaphore.down();

      myServer.server_getVersion(new GetVersionConsumer() {

        @Override
        public void computedVersion(final String version) {
          resultRef.set(version);
          semaphore.up();
        }

        @Override
        public void onError(final RequestError requestError) {
          semaphore.up();
          LOG.warn("Error from server_getVersion(), code=" + requestError.getCode() + ": " + requestError.getMessage());
        }
      });
    }

    final long t0 = System.currentTimeMillis();
    semaphore.waitFor(GET_VERSION_TIMEOUT);

    if (semaphore.tryUp()) {
      LOG.info("server_getVersion() took too long: " + (System.currentTimeMillis() - t0) + "ms");
    }

    return resultRef.get();
  }


  private void startServer(@NotNull final String sdkHome) {
    synchronized (myLock) {
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
        new StdioServerSocket(runtimePath, analysisServerPath, null, debugStream, new String[]{}, false, false, port, true,
                              FileReadMode.NORMALIZE_EOL_ALWAYS);
      myServer = new RemoteAnalysisServerImpl(serverSocket);

      try {
        myServer.start();
        myServer.addAnalysisServerListener(myListener);
        myServerVersion = server_getVersion();
        myServer.analysis_updateOptions(new AnalysisOptions(true, true, true, false, true, false));
        LOG.info("Server started, see status at http://localhost:" + port + "/status");
      }
      catch (Exception e) {
        LOG.warn("Failed to start Dart analysis server, port=" + port, e);
        onServerStopped();
      }
    }
  }

  public boolean serverReadyForRequest(@NotNull final Project project, @NotNull final String sdkHome) {
    synchronized (myLock) {
      if (myServer == null || !sdkHome.equals(mySdkHome) || !myServer.isSocketOpen()) {
        stopServer();
        startServer(sdkHome);
      }

      if (myServer != null) {
        myRootsHandler.ensureProjectServed(project);
        return true;
      }

      return false;
    }
  }

  private void stopServer() {
    synchronized (myLock) {
      if (myServer != null) {
        LOG.debug("stopping server");
        myServer.removeAnalysisServerListener(myListener);
        myServer.server_shutdown();
      }

      onServerStopped();
    }
  }

  private void onServerStopped() {
    synchronized (myLock) {
      myServer = null;
      mySdkHome = null;
      myRootsHandler.reset();
      myFilePathWithOverlaidContentToTimestamp.clear();
    }
  }
}
