package com.jetbrains.lang.dart.analyzer;

import com.google.dart.server.*;
import com.google.dart.server.generated.AnalysisServer;
import com.google.dart.server.generated.types.*;
import com.google.dart.server.internal.remote.DebugPrintStream;
import com.google.dart.server.internal.remote.FileReadMode;
import com.google.dart.server.internal.remote.RemoteAnalysisServerImpl;
import com.google.dart.server.internal.remote.StdioServerSocket;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerAdapter;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.concurrency.Semaphore;
import com.intellij.util.net.NetUtils;
import com.intellij.xml.util.HtmlUtil;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.ide.errorTreeView.DartProblemsViewImpl;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.resolve.DartResolver;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkUpdateChecker;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class DartAnalysisServerService {

  public static final String MIN_SDK_VERSION = "1.9";

  private static final long CHECK_CANCELLED_PERIOD = 100;
  private static final long SEND_REQUEST_TIMEOUT = TimeUnit.SECONDS.toMillis(1);
  private static final long EDIT_FORMAT_TIMEOUT = TimeUnit.SECONDS.toMillis(3);
  private static final long EDIT_SORT_MEMBERS_TIMEOUT = TimeUnit.SECONDS.toMillis(3);
  private static final long GET_ERRORS_TIMEOUT = TimeUnit.SECONDS.toMillis(5);
  private static final long GET_ERRORS_LONGER_TIMEOUT = TimeUnit.SECONDS.toMillis(60);
  private static final long GET_FIXES_TIMEOUT = TimeUnit.SECONDS.toMillis(10);
  private static final long GET_SUGGESTIONS_TIMEOUT = TimeUnit.SECONDS.toMillis(1);
  private static final long GET_LIBRARY_DEPENDENCIES_TIMEOUT = TimeUnit.MINUTES.toMillis(5);
  private static final List<String> SERVER_SUBSCRIPTIONS = Collections.singletonList(ServerService.STATUS);
  private static final Logger LOG = Logger.getInstance("#com.jetbrains.lang.dart.analyzer.DartAnalysisServerService");

  private final Object myLock = new Object(); // Access all fields under this lock. Do not wait for server response under lock.
  @Nullable private AnalysisServer myServer;
  @Nullable private StdioServerSocket myServerSocket;

  @NotNull private String myServerVersion = "";
  @NotNull private String mySdkVersion = "";
  @Nullable private String mySdkHome = null;
  private final DartServerRootsHandler myRootsHandler = new DartServerRootsHandler();
  private final Map<String, Long> myFilePathWithOverlaidContentToTimestamp = new THashMap<String, Long>();
  private final List<String> myPriorityFiles = new ArrayList<String>();

  @NotNull private final Queue<CompletionInfo> myCompletionInfos = new LinkedList<CompletionInfo>();
  @NotNull private final Map<String, List<NavigationRegion>> myNavigationData = new THashMap<String, List<NavigationRegion>>();

  private final AnalysisServerListener myAnalysisServerListener = new AnalysisServerListenerAdapter() {

    @Override
    public void computedErrors(@NotNull final String file, @NotNull final List<AnalysisError> errors) {
      updateProblemsView(DartProblemsViewImpl.createGroupName(file), errors);
    }

    @Override
    public void computedNavigation(String file, List<NavigationRegion> targets) {
      if (DartResolver.USE_SERVER) {
        myNavigationData.put(FileUtil.toSystemIndependentName(file), targets);
        final VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(file);
        if (virtualFile != null) {
          Project[] projects = ProjectManager.getInstance().getOpenProjects();
          for (final Project project : projects) {
            ResolveCache.getInstance(project).clearCache(true);
            DaemonCodeAnalyzer.getInstance(project).restart();
          }
        }
      }
    }

    @Override
    public void flushedResults(List<String> files) {
      for (String file : files) {
        updateProblemsView(DartProblemsViewImpl.createGroupName(file), AnalysisError.EMPTY_LIST);
      }
    }

    @Override
    public void computedCompletion(@NotNull final String completionId,
                                   final int replacementOffset,
                                   final int replacementLength,
                                   @NotNull final List<CompletionSuggestion> completions,
                                   final boolean isLast) {
      synchronized (myCompletionInfos) {
        myCompletionInfos.add(new CompletionInfo(completionId, replacementOffset, replacementLength, completions, isLast));
        myCompletionInfos.notifyAll();
      }
    }

    @Override
    public void serverConnected(@Nullable String version) {
      myServerVersion = version != null ? version : "";
    }

    @Override
    public void serverError(boolean isFatal, @Nullable String message, @Nullable String stackTrace) {
      if (message == null) message = "<no error message>";
      if (stackTrace == null) stackTrace = "<no stack trace>";
      LOG.error("Dart analysis server, SDK version " + mySdkVersion +
                ", server version " + myServerVersion +
                ", " + (isFatal ? "FATAL " : "") + "error: " + message + "\n" + stackTrace);

      if (isFatal) {
        onServerStopped();
      }
    }

    @Override
    public void serverStatus(@Nullable final AnalysisStatus analysisStatus, @Nullable final PubStatus pubStatus) {
      ApplicationManager.getApplication().runReadAction(new Runnable() {
        @Override
        public void run() {
          for (final Project project : myRootsHandler.getTrackedProjects()) {
            if (project.isDisposed()) continue;
            DartProblemsViewImpl.getInstance(project).setProgress(analysisStatus, pubStatus);
          }
        }
      });
    }
  };

  public void addCompletions(@NotNull final Project project,
                             @NotNull final String completionId,
                             @NotNull final CompletionResultSet resultSet) {
    while (true) {
      ProgressManager.checkCanceled();

      synchronized (myCompletionInfos) {
        // todo in case of several AnalysisServerListenerAdapter.computedCompletion() invocations for one completion (only the last
        // invocation has isLast==true) it looks like each next List<CompletionSuggestion> also contains all items that were already
        // given in previous invocations. If it is by design we should optimize cycle and handle only the last in queue.

        CompletionInfo completionInfo;
        while ((completionInfo = myCompletionInfos.poll()) != null) {
          if (!completionInfo.myCompletionId.equals(completionId)) continue;

          for (final CompletionSuggestion completion : completionInfo.myCompletions) {
            final ResolveResult psiElementFinder = new ResolveResult() {
              @Nullable
              @Override
              public PsiElement getElement() {
                final Element element = completion.getElement();
                // todo for some reason Analysis Server doesn't provide location for local vars, fields and other local elements
                final Location location = element == null ? null : element.getLocation();
                final String filePath = location == null ? null : FileUtil.toSystemIndependentName(location.getFile());
                // todo this path may point to Pub global cache, should point to local packages/FooPackage/... instead
                final VirtualFile vFile = filePath == null ? null : LocalFileSystem.getInstance().findFileByPath(filePath);
                final PsiFile psiFile = vFile == null ? null : PsiManager.getInstance(project).findFile(vFile);
                final PsiElement elementAtOffset = psiFile == null ? null : psiFile.findElementAt(location.getOffset());
                return PsiTreeUtil.getParentOfType(elementAtOffset, DartComponentName.class);
              }

              @Override
              public boolean isValidResult() {
                return true;
              }
            };

            final LookupElement lookup = LookupElementBuilder.create(psiElementFinder, completion.getCompletion());
            // todo: lookup presentable text (e.g. signature for functions, etc.)
            //       respect priority (may be also emphasize using bold font), see CompletionWeigher
            //       tail text
            //       type text
            //       icon
            //       strikeout for deprecated
            //       quick doc preview (already works if corresponding PsiElement is correctly found by psiElementFinder)
            //       quick definition view (already works (but uses DartDocumentationProvider, not info from server) if corresponding PsiElement is correctly found by psiElementFinder)
            //       jump to item declaration (already works if corresponding PsiElement is correctly found by psiElementFinder)
            //       what to insert except identifier (e.g. parentheses, import directive, update show/hide, etc.)
            //       caret placement after insertion

            resultSet.consume(lookup);
          }

          if (completionInfo.isLast) return;
        }

        try {
          myCompletionInfos.wait(CHECK_CANCELLED_PERIOD);
        }
        catch (InterruptedException e) {
          return;
        }
      }
    }
  }

  public static class FormatResult {
    @Nullable private final List<SourceEdit> myEdits;
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

    @Nullable
    public List<SourceEdit> getEdits() {
      return myEdits;
    }
  }

  public static class LibraryDependenciesResult {
    @Nullable final String[] libraries;

    @Nullable final Map<String, Map<String, List<String>>> packageMap;

    public LibraryDependenciesResult(@Nullable final String[] libraries,
                                     @Nullable final Map<String, Map<String, List<String>>> packageMap) {
      this.libraries = libraries;
      this.packageMap = packageMap;
    }

    @Nullable
    public String[] getLibraries() {
      return libraries;
    }

    @Nullable
    public Map<String, Map<String, List<String>>> getPackageMap() {
      return packageMap;
    }
  }

  public DartAnalysisServerService() {
    Disposer.register(ApplicationManager.getApplication(), new Disposable() {
      public void dispose() {
        stopServer();
      }
    });

    ApplicationManager.getApplication().getMessageBus().connect()
      .subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerAdapter() {
        @Override
        public void fileOpened(@NotNull final FileEditorManager source, @NotNull final VirtualFile file) {
          if (PubspecYamlUtil.PUBSPEC_YAML.equals(file.getName()) || file.getFileType() == DartFileType.INSTANCE) {
            DartSdkUpdateChecker.mayBeCheckForSdkUpdate(source.getProject());
          }
        }

        @Override
        public void fileClosed(@NotNull final FileEditorManager source, @NotNull final VirtualFile file) {
          if (isDartOrHtmlFile(file)) {
            removePriorityFile(file);
          }
        }
      });
  }

  @NotNull
  public static DartAnalysisServerService getInstance() {
    return ServiceManager.getService(DartAnalysisServerService.class);
  }

  /**
   * Returns {@link NavigationRegion}s for the given file.
   * Empty if no regions.
   *
   * @param file
   */
  @NotNull
  public List<NavigationRegion> getNavigation(@NotNull final VirtualFile file) {
    List<NavigationRegion> sourceRegions = myNavigationData.get(file.getPath());
    if (sourceRegions == null) {
      return NavigationRegion.EMPTY_LIST;
    }
    return sourceRegions;
  }

  void addPriorityFile(@NotNull final VirtualFile file) {
    synchronized (myLock) {
      final String path = FileUtil.toSystemDependentName(file.getPath());
      if (!myPriorityFiles.contains(path)) {
        myPriorityFiles.add(path);
        analysis_setPriorityFiles();
        analysis_setSubscriptions();
      }
    }
  }

  private void removePriorityFile(@NotNull final VirtualFile file) {
    synchronized (myLock) {
      final String path = FileUtil.toSystemDependentName(file.getPath());
      if (myPriorityFiles.remove(path)) {
        analysis_setPriorityFiles();
        analysis_setSubscriptions();
      }
    }
  }

  private static boolean isDartOrHtmlFile(@NotNull final VirtualFile file) {
    return file.getFileType() == DartFileType.INSTANCE || HtmlUtil.isHtmlFile(file);
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
        if (file != null && isDartOrHtmlFile(file)) {
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

      if (!filesToUpdate.isEmpty()) {
        final UpdateContentConsumer consumer = new UpdateContentConsumer() {
          @Override
          public void onResponse() {
          }
        };

        final AnalysisServer server = myServer;
        final boolean ok = runInPooledThreadAndWait(new Runnable() {
          @Override
          public void run() {
            server.analysis_updateContent(filesToUpdate, consumer);
          }
        }, "analysis_updateContent(" + StringUtil.join(filesToUpdate.keySet(), ", ") + ")", SEND_REQUEST_TIMEOUT);

        if (!ok) {
          stopServer();
          //noinspection UnnecessaryReturnStatement
          return;
        }
      }
    }
  }

  public boolean updateRoots(@NotNull final List<String> includedRoots,
                             @NotNull final List<String> excludedRoots,
                             @Nullable final Map<String, String> packageRoots) {
    synchronized (myLock) {
      if (myServer == null) return false;

      if (LOG.isDebugEnabled()) {
        LOG.debug("analysis_setAnalysisRoots, included:\n" + StringUtil.join(includedRoots, ",\n") +
                  "\nexcluded:\n" + StringUtil.join(excludedRoots, ",\n"));
      }

      final AnalysisServer server = myServer;
      final String runnableInfo = "analysis_setAnalysisRoots(" + StringUtil.join(includedRoots, ", ") + "; " +
                                  StringUtil.join(excludedRoots, ", ") + ")";
      final boolean ok = runInPooledThreadAndWait(new Runnable() {
        @Override
        public void run() {
          server.analysis_setAnalysisRoots(includedRoots, excludedRoots, packageRoots);
        }
      }, runnableInfo, SEND_REQUEST_TIMEOUT);

      if (!ok) {
        stopServer();
        return false;
      }

      return true;
    }
  }

  private void updateProblemsView(@NotNull final String filePath, @NotNull final List<AnalysisError> errors) {
    ApplicationManager.getApplication().runReadAction(new Runnable() {
      @Override
      public void run() {
        final VirtualFile vFile = LocalFileSystem.getInstance().findFileByPath(filePath);

        for (final Project project : myRootsHandler.getTrackedProjects()) {
          if (project.isDisposed()) continue;

          if (vFile != null && ProjectRootManager.getInstance(project).getFileIndex().isInContent(vFile)) {
            DartProblemsViewImpl.getInstance(project).updateErrorsForFile(vFile, errors);
          }
          else {
            DartProblemsViewImpl.getInstance(project).removeErrorsForFile(filePath);
          }
        }
      }
    });
  }

  @Nullable
  public AnalysisError[] analysis_getErrors(@NotNull final DartAnalysisServerAnnotator.AnnotatorInfo info) {
    final Ref<AnalysisError[]> resultRef = new Ref<AnalysisError[]>();
    final Semaphore semaphore = new Semaphore();

    try {
      synchronized (myLock) {
        if (myServer == null) return null;

        semaphore.down();

        final String filePath = FileUtil.toSystemDependentName(info.myFilePath);

        LOG.debug("analysis_getErrors(" + filePath + ")");

        final GetErrorsConsumer consumer = new GetErrorsConsumer() {
          @Override
          public void computedErrors(final AnalysisError[] errors) {
            if (semaphore.tryUp()) {
              resultRef.set(errors);
            }
            else {
              // semaphore unlocked by timeout, schedule to highlight the file again
              LOG.info("analysis_getErrors() took too long for file " + filePath + ", restarting daemon");

              ApplicationManager.getApplication().runReadAction(new Runnable() {
                @Override
                public void run() {
                  final VirtualFile vFile =
                    info.myProject.isDisposed() ? null : LocalFileSystem.getInstance().findFileByPath(info.myFilePath);
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
            logError("analysis_getErrors()", filePath, error);
            semaphore.up();
          }
        };

        final AnalysisServer server = myServer;
        final boolean ok = runInPooledThreadAndWait(new Runnable() {
          @Override
          public void run() {
            server.analysis_getErrors(filePath, consumer);
          }
        }, "analysis_getErrors(" + filePath + ")", SEND_REQUEST_TIMEOUT);

        if (!ok) {
          stopServer();
          return null;
        }
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
  public LibraryDependenciesResult analysis_getLibraryDependencies() {
    final Ref<LibraryDependenciesResult> resultRef = new Ref<LibraryDependenciesResult>();
    final Semaphore semaphore = new Semaphore();

    try {
      synchronized (myLock) {
        if (myServer == null) return null;

        semaphore.down();

        LOG.debug("analysis_getLibraryDependencies()");

        final GetLibraryDependenciesConsumer consumer = new GetLibraryDependenciesConsumer() {
          @Override
          public void computedDependencies(@Nullable final String[] libraries,
                                           @Nullable final Map<String, Map<String, List<String>>> packageMap) {
            resultRef.set(new LibraryDependenciesResult(libraries, packageMap));
            semaphore.up();
          }

          @Override
          public void onError(final RequestError error) {
            logError("analysis_getLibraryDependencies()", null, error);
            semaphore.up();
          }
        };

        final AnalysisServer server = myServer;
        final boolean ok = runInPooledThreadAndWait(new Runnable() {
          @Override
          public void run() {
            server.analysis_getLibraryDependencies(consumer);
          }
        }, "analysis_getLibraryDependencies()", SEND_REQUEST_TIMEOUT);

        if (!ok) {
          stopServer();
          return null;
        }
      }

      semaphore.waitFor(GET_LIBRARY_DEPENDENCIES_TIMEOUT);
    }
    finally {
      semaphore.up(); // make sure to unlock semaphore so that computedDependencies() can understand when it was unlocked by timeout
    }

    return resultRef.get();
  }

  @Nullable
  public List<AnalysisErrorFixes> edit_getFixes(@NotNull final String _filePath, final int offset) {
    final Ref<List<AnalysisErrorFixes>> resultRef = new Ref<List<AnalysisErrorFixes>>();
    final Semaphore semaphore = new Semaphore();
    final String filePath = FileUtil.toSystemDependentName(_filePath);

    synchronized (myLock) {
      if (myServer == null) return null;

      semaphore.down();

      final GetFixesConsumer consumer = new GetFixesConsumer() {
        @Override
        public void computedFixes(final List<AnalysisErrorFixes> fixes) {
          resultRef.set(fixes);
          semaphore.up();
        }

        @Override
        public void onError(final RequestError error) {
          logError("edit_getFixes()", filePath, error);
          semaphore.up();
        }
      };

      final AnalysisServer server = myServer;
      final boolean ok = runInPooledThreadAndWait(new Runnable() {
        @Override
        public void run() {
          server.edit_getFixes(filePath, offset, consumer);
        }
      }, "edit_getFixes(" + filePath + ", " + offset + ")", SEND_REQUEST_TIMEOUT);

      if (!ok) {
        stopServer();
        return null;
      }
    }

    final long t0 = System.currentTimeMillis();
    semaphore.waitFor(GET_FIXES_TIMEOUT);

    if (semaphore.tryUp()) {
      LOG.info("edit_getFixes() took too long for file " + filePath + ": " + (System.currentTimeMillis() - t0) + "ms");
      return null;
    }

    return resultRef.get();
  }

  @Nullable
  public String completion_getSuggestions(@NotNull final String filePath, final int offset) {
    final Ref<String> resultRef = new Ref<String>();
    final Semaphore semaphore = new Semaphore();

    synchronized (myLock) {
      if (myServer == null) return null;

      semaphore.down();

      final GetSuggestionsConsumer consumer = new GetSuggestionsConsumer() {
        @Override
        public void computedCompletionId(@NotNull final String completionId) {
          resultRef.set(completionId);
          semaphore.up();
        }

        @Override
        public void onError(@NotNull final RequestError error) {
          // Not a problem. Happens if a file is outside of the project, or server is just not ready yet.
          semaphore.up();
        }
      };

      final AnalysisServer server = myServer;
      final boolean ok = runInPooledThreadAndWait(new Runnable() {
        @Override
        public void run() {
          server.completion_getSuggestions(filePath, offset, consumer);
        }
      }, "completion_getSuggestions(" + filePath + ", " + offset + ")", SEND_REQUEST_TIMEOUT);

      if (!ok) {
        stopServer();
        return null;
      }
    }

    final long t0 = System.currentTimeMillis();
    semaphore.waitFor(GET_SUGGESTIONS_TIMEOUT);

    if (semaphore.tryUp()) {
      LOG.info("completion_getSuggestions() took too long for file " + filePath + ": " + (System.currentTimeMillis() - t0) + "ms");
      return null;
    }

    return resultRef.get();
  }

  @Nullable
  public FormatResult edit_format(@NotNull final String filePath, final int selectionOffset, final int selectionLength) {
    final Ref<FormatResult> resultRef = new Ref<FormatResult>();
    final Semaphore semaphore = new Semaphore();

    synchronized (myLock) {
      if (myServer == null) return null;

      semaphore.down();

      final FormatConsumer consumer = new FormatConsumer() {
        @Override
        public void computedFormat(final List<SourceEdit> edits, final int selectionOffset, final int selectionLength) {
          resultRef.set(new FormatResult(edits, selectionOffset, selectionLength));
          semaphore.up();
        }

        @Override
        public void onError(final RequestError error) {
          if (!"FORMAT_WITH_ERRORS".equals(error.getCode())) {
            logError("edit_format()", filePath, error);
          }
          semaphore.up();
        }
      };

      final AnalysisServer server = myServer;
      final boolean ok = runInPooledThreadAndWait(new Runnable() {
        @Override
        public void run() {
          server.edit_format(filePath, selectionOffset, selectionLength, consumer);
        }
      }, "edit_format(" + filePath + ", " + selectionOffset + ", " + selectionLength + ")", SEND_REQUEST_TIMEOUT);

      if (!ok) {
        stopServer();
        return null;
      }
    }

    final long t0 = System.currentTimeMillis();
    semaphore.waitFor(EDIT_FORMAT_TIMEOUT);

    if (semaphore.tryUp()) {
      LOG.info("edit_format() took too long for file " + filePath + ": " + (System.currentTimeMillis() - t0) + "ms");
      return null;
    }

    return resultRef.get();
  }

  @Nullable
  public SourceFileEdit edit_sortMembers(@NotNull final String filePath) {
    final Ref<SourceFileEdit> resultRef = new Ref<SourceFileEdit>();
    final Semaphore semaphore = new Semaphore();

    synchronized (myLock) {
      if (myServer == null) return null;

      semaphore.down();

      final SortMembersConsumer consumer = new SortMembersConsumer() {
        @Override
        public void computedEdit(final SourceFileEdit edit) {
          resultRef.set(edit);
          semaphore.up();
        }

        @Override
        public void onError(final RequestError error) {
          logError("edit_sortMembers()", filePath, error);
          semaphore.up();
        }
      };

      final AnalysisServer server = myServer;
      final boolean ok = runInPooledThreadAndWait(new Runnable() {
        @Override
        public void run() {
          server.edit_sortMembers(filePath, consumer);
        }
      }, "edit_sortMembers(" + filePath + ")", SEND_REQUEST_TIMEOUT);

      if (!ok) {
        stopServer();
        return null;
      }
    }

    final long t0 = System.currentTimeMillis();
    semaphore.waitFor(EDIT_SORT_MEMBERS_TIMEOUT);

    if (semaphore.tryUp()) {
      LOG.info("edit_sortMembers() took too long for file " + filePath + ": " + (System.currentTimeMillis() - t0) + "ms");
      return null;
    }

    return resultRef.get();
  }

  private boolean analysis_setPriorityFiles() {
    synchronized (myLock) {
      if (myServer == null) return false;

      if (LOG.isDebugEnabled()) {
        LOG.debug("analysis_setPriorityFiles, files:\n" + StringUtil.join(myPriorityFiles, ",\n"));
      }

      final AnalysisServer server = myServer;
      final boolean ok = runInPooledThreadAndWait(new Runnable() {
        @Override
        public void run() {
          server.analysis_setPriorityFiles(myPriorityFiles);
        }
      }, "analysis_setPriorityFiles(" + StringUtil.join(myPriorityFiles, ", ") + ")", SEND_REQUEST_TIMEOUT);

      if (!ok) {
        stopServer();
        return false;
      }

      return true;
    }
  }

  private boolean analysis_setSubscriptions() {
    synchronized (myLock) {
      if (myServer == null) return false;

      final Map<String, List<String>> subscriptions = new THashMap<String, List<String>>();
      subscriptions.put(AnalysisService.NAVIGATION, myPriorityFiles);

      if (LOG.isDebugEnabled()) {
        LOG.debug("analysis_setSubscriptions, subscriptions:\n" + subscriptions);
      }

      final AnalysisServer server = myServer;
      final boolean ok = runInPooledThreadAndWait(new Runnable() {
        @Override
        public void run() {
          server.analysis_setSubscriptions(subscriptions);
        }
      }, "analysis_setSubscriptions(" + subscriptions + ")", SEND_REQUEST_TIMEOUT);

      if (!ok) {
        stopServer();
        return false;
      }

      return true;
    }
  }

  private void startServer(@NotNull final DartSdk sdk) {
    synchronized (myLock) {
      mySdkHome = sdk.getHomePath();

      final String testSdkHome = System.getProperty("dart.sdk");
      if (ApplicationManager.getApplication().isUnitTestMode() && testSdkHome == null) return;

      final String runtimePath =
        FileUtil.toSystemDependentName((ApplicationManager.getApplication().isUnitTestMode() ? testSdkHome : mySdkHome) + "/bin/dart");
      final String analysisServerPath = FileUtil.toSystemDependentName(
        (ApplicationManager.getApplication().isUnitTestMode() ? testSdkHome : mySdkHome) + "/bin/snapshots/analysis_server.dart.snapshot");

      final DebugPrintStream debugStream = new DebugPrintStream() {
        @Override
        public void println(String str) {
          //System.out.println("debugStream: " + str);
        }
      };

      final int port = NetUtils.tryToFindAvailableSocketPort(10000);

      myServerSocket = new StdioServerSocket(runtimePath, analysisServerPath, null, debugStream, new String[]{}, false, false, port, false,
                                             FileReadMode.NORMALIZE_EOL_ALWAYS);
      myServerSocket.setClientId(ApplicationNamesInfo.getInstance().getFullProductName().replace(' ', '_'));
      myServerSocket.setClientVersion(ApplicationInfo.getInstance().getApiVersion());
      myServer = new RemoteAnalysisServerImpl(myServerSocket);

      try {
        myServer.start();
        myServer.server_setSubscriptions(SERVER_SUBSCRIPTIONS);
        myServer.addAnalysisServerListener(myAnalysisServerListener);
        mySdkVersion = sdk.getVersion();

        final AnalysisServer server = myServer;
        final boolean ok = runInPooledThreadAndWait(new Runnable() {
          @Override
          public void run() {
            server.analysis_updateOptions(new AnalysisOptions(true, true, true, false, false, true, false));
          }
        }, "analysis_updateOptions(true, true, true, false, false, true, false)", SEND_REQUEST_TIMEOUT);

        if (!ok) {
          stopServer();
          return;
        }

        LOG.info("Server started, see status at http://localhost:" + port + "/status");
      }
      catch (Exception e) {
        LOG.warn("Failed to start Dart analysis server, port=" + port, e);
        stopServer();
      }
    }
  }

  public boolean serverReadyForRequest(@NotNull final Project project, @NotNull final DartSdk sdk) {
    synchronized (myLock) {
      if (myServer == null || !sdk.getHomePath().equals(mySdkHome) || !sdk.getVersion().equals(mySdkVersion) || !myServer.isSocketOpen()) {
        stopServer();
        startServer(sdk);
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
        myServer.removeAnalysisServerListener(myAnalysisServerListener);

        final AnalysisServer server = myServer;
        final boolean ok = runInPooledThreadAndWait(new Runnable() {
          @Override
          public void run() {
            server.server_shutdown();
          }
        }, "server_shutdown()", SEND_REQUEST_TIMEOUT);

        if (!ok) {
          if (myServerSocket != null) {
            myServerSocket.stop();
          }
        }
      }

      onServerStopped();
    }
  }

  private void onServerStopped() {
    synchronized (myLock) {
      myServerSocket = null;
      myServer = null;
      mySdkHome = null;
      myFilePathWithOverlaidContentToTimestamp.clear();

      ApplicationManager.getApplication().runReadAction(new Runnable() {
        @Override
        public void run() {
          for (final Project project : myRootsHandler.getTrackedProjects()) {
            if (!project.isDisposed()) {
              DartProblemsViewImpl.getInstance(project).clearAll();
            }
          }
        }
      });

      myRootsHandler.reset();
    }
  }

  private void logError(@NotNull final String methodName, @Nullable final String filePath, @NotNull final RequestError error) {
    final String trace = error.getStackTrace();
    final String partialTrace = trace == null || trace.isEmpty() ? "" : trace.substring(0, Math.min(trace.length(), 1000));
    LOG.error("Error from " + methodName +
              (filePath == null ? "" : (", file = " + filePath)) +
              ", SDK version = " + mySdkVersion +
              ", server version = " + myServerVersion +
              ", error code = " + error.getCode() + ": " + error.getMessage() +
              "\n" + partialTrace + "...");
  }

  private static boolean runInPooledThreadAndWait(@NotNull final Runnable runnable,
                                                  @NotNull final String runnableInfo,
                                                  final long timeout) {
    final Ref<RuntimeException> exceptionRef = new Ref<RuntimeException>();

    final Semaphore semaphore = new Semaphore();
    semaphore.down();

    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      @Override
      public void run() {
        try {
          runnable.run();
        }
        catch (RuntimeException e) {
          exceptionRef.set(e);
        }

        semaphore.up();
      }
    });

    semaphore.waitFor(timeout);

    if (!exceptionRef.isNull()) {
      LOG.error(runnableInfo, exceptionRef.get());
      return false;
    }

    if (semaphore.tryUp()) {
      // runnable is still not complete
      LOG.error("Operation didn't finish in " + timeout + " ms: " + runnableInfo);
      return false;
    }

    return true;
  }

  private static class CompletionInfo {
    @NotNull final String myCompletionId;
    final int myReplacementOffset;
    final int myReplacementLength;
    @NotNull final List<CompletionSuggestion> myCompletions;
    final boolean isLast;

    public CompletionInfo(@NotNull final String completionId,
                          final int replacementOffset,
                          final int replacementLength,
                          @NotNull final List<CompletionSuggestion> completions, boolean isLast) {
      this.myCompletionId = completionId;
      this.myReplacementOffset = replacementOffset;
      this.myReplacementLength = replacementLength;
      this.myCompletions = completions;
      this.isLast = isLast;
    }
  }
}
