package com.jetbrains.lang.dart.analyzer;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.dart.server.*;
import com.google.dart.server.generated.AnalysisServer;
import com.google.dart.server.internal.remote.DebugPrintStream;
import com.google.dart.server.internal.remote.FileReadMode;
import com.google.dart.server.internal.remote.RemoteAnalysisServerImpl;
import com.google.dart.server.internal.remote.StdioServerSocket;
import com.google.dart.server.utilities.logging.Logging;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.intention.IntentionManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.util.*;
import com.intellij.util.Consumer;
import com.intellij.util.concurrency.Semaphore;
import com.intellij.util.net.NetUtils;
import com.intellij.xml.util.HtmlUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartFileListener;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.assists.DartQuickAssistIntention;
import com.jetbrains.lang.dart.assists.QuickAssistSet;
import com.jetbrains.lang.dart.ide.errorTreeView.DartProblemsViewImpl;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkGlobalLibUtil;
import com.jetbrains.lang.dart.sdk.DartSdkUpdateChecker;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.dartlang.analysis.server.protocol.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.ide.PooledThreadExecutor;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class DartAnalysisServerService {

  public static final String MIN_SDK_VERSION = "1.12";

  private static final long CHECK_CANCELLED_PERIOD = 10;
  private static final long SEND_REQUEST_TIMEOUT = TimeUnit.SECONDS.toMillis(1);
  private static final long EDIT_FORMAT_TIMEOUT = TimeUnit.SECONDS.toMillis(3);
  private static final long EDIT_ORGANIZE_DIRECTIVES_TIMEOUT = TimeUnit.SECONDS.toMillis(3);
  private static final long EDIT_SORT_MEMBERS_TIMEOUT = TimeUnit.SECONDS.toMillis(3);
  private static final long GET_ERRORS_TIMEOUT = TimeUnit.SECONDS.toMillis(5);
  private static final long GET_ERRORS_LONGER_TIMEOUT = TimeUnit.SECONDS.toMillis(60);
  private static final long GET_ASSISTS_TIMEOUT = TimeUnit.SECONDS.toMillis(1);
  private static final long GET_FIXES_TIMEOUT = TimeUnit.SECONDS.toMillis(10);
  private static final long GET_SUGGESTIONS_TIMEOUT = TimeUnit.SECONDS.toMillis(1);
  private static final long FIND_ELEMENT_REFERENCES_TIMEOUT = TimeUnit.SECONDS.toMillis(1);
  private static final long GET_TYPE_HIERARCHY_TIMEOUT = TimeUnit.SECONDS.toMillis(10);
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
  private final List<String> myVisibleFiles = new ArrayList<String>();

  private final Set<Document> myUpdatedDocuments = Sets.newHashSet();
  private Runnable myUpdateFilesContextRunner = null;

  @NotNull private final Queue<CompletionInfo> myCompletionInfos = new LinkedList<CompletionInfo>();
  @NotNull private final Queue<SearchResultsSet> mySearchResultSets = new LinkedList<SearchResultsSet>();

  @NotNull private final DartServerData myServerData = new DartServerData(myRootsHandler);

  @NotNull private final AtomicBoolean myServerBusy = new AtomicBoolean(false);
  @NotNull private final Alarm myShowServerProgressAlarm = new Alarm();

  private final AnalysisServerListener myAnalysisServerListener = new AnalysisServerListenerAdapter() {

    @Override
    public void computedAnalyzedFiles(List<String> filePaths) {
      configureImportedLibraries(filePaths);
    }

    @Override
    public void computedErrors(@NotNull final String filePath, @NotNull final List<AnalysisError> errors) {
      updateProblemsView(DartProblemsViewImpl.createGroupName(filePath), errors);
    }

    @Override
    public void computedHighlights(@NotNull final String filePath, @NotNull final List<HighlightRegion> regions) {
      myServerData.computedHighlights(FileUtil.toSystemIndependentName(filePath), regions);
    }

    @Override
    public void computedImplemented(String _filePath,
                                    List<ImplementedClass> implementedClasses,
                                    List<ImplementedMember> implementedMembers) {
      myServerData.computedImplemented(FileUtil.toSystemIndependentName(_filePath), implementedClasses, implementedMembers);
    }

    @Override
    public void computedNavigation(@NotNull final String _filePath, @NotNull final List<NavigationRegion> regions) {
      myServerData.computedNavigation(FileUtil.toSystemIndependentName(_filePath), regions);
    }

    @Override
    public void computedOverrides(@NotNull final String _filePath, @NotNull final List<OverrideMember> overrides) {
      myServerData.computedOverrides(FileUtil.toSystemIndependentName(_filePath), overrides);
    }

    @Override
    public void flushedResults(List<String> filePaths) {
      myServerData.onFlushedResults(filePaths);

      for (String filePath : filePaths) {
        updateProblemsView(DartProblemsViewImpl.createGroupName(filePath), AnalysisError.EMPTY_LIST);
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
    public void computedSearchResults(String searchId, List<SearchResult> results, boolean last) {
      synchronized (mySearchResultSets) {
        mySearchResultSets.add(new SearchResultsSet(searchId, results, last));
        mySearchResultSets.notifyAll();
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
        stopServer();
      }
    }

    @Override
    public void serverStatus(@Nullable final AnalysisStatus analysisStatus, @Nullable final PubStatus pubStatus) {
      if (analysisStatus != null && analysisStatus.isAnalyzing() || pubStatus != null && pubStatus.isListingPackageDirs()) {
        if (myServerBusy.compareAndSet(false, true)) {
          for (final Project project : myRootsHandler.getTrackedProjects()) {
            final Runnable delayedRunnable = new Runnable() {
              public void run() {
                if (project.isDisposed() || !myServerBusy.get()) return;

                final Task.Backgroundable task =
                  new Task.Backgroundable(project, DartBundle.message("dart.analysis.progress.title"), false) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                      waitWhileServerBusy();
                    }
                  };

                ProgressManager.getInstance().run(task);
              }
            };

            // 50ms delay to minimize blinking in case of consequent start-stop-start-stop-... events that happen with pubStatus events
            // 300ms delay to avoid showing progress for very fast analysis start-stop cycle that happens with analysisStatus events
            final int delay = pubStatus != null && pubStatus.isListingPackageDirs() ? 50 : 300;
            myShowServerProgressAlarm.addRequest(delayedRunnable, delay, ModalityState.any());
          }
        }
      }
      else {
        stopShowingServerProgress();
      }
    }
  };

  private DocumentAdapter myDocumentListener = new DocumentAdapter() {
    @Override
    public void beforeDocumentChange(DocumentEvent e) {
      myServerData.onDocumentChanged(e);
      synchronized (myLock) {
        final Document document = e.getDocument();
        myUpdatedDocuments.add(document);
        scheduleUpdateFilesContent();
      }
    }
  };

  private void scheduleUpdateFilesContent() {
    synchronized (myLock) {
      if (myUpdateFilesContextRunner != null) {
        return;
      }
      myUpdateFilesContextRunner = new Runnable() {
        @Override
        public void run() {
          final Set<Document> updatedDocuments = Sets.newHashSet();
          // Grab updated documents, wait for more.
          while (true) {
            Uninterruptibles.sleepUninterruptibly(100, TimeUnit.MILLISECONDS);
            synchronized (myLock) {
              if (myUpdatedDocuments.isEmpty()) {
                myUpdateFilesContextRunner = null;
                break;
              }
              updatedDocuments.addAll(myUpdatedDocuments);
              myUpdatedDocuments.clear();
            }
          }
          // Enough waiting, let's send the documents to the server.
          updateFilesContent(updatedDocuments);
        }
      };
      PooledThreadExecutor.INSTANCE.execute(myUpdateFilesContextRunner);
    }
  }

  void addDocumentListener() {
    // by design document listener must not be already registered, next line is for the safety only
    EditorFactory.getInstance().getEventMulticaster().removeDocumentListener(myDocumentListener);
    EditorFactory.getInstance().getEventMulticaster().addDocumentListener(myDocumentListener);
  }

  void removeDocumentListener() {
    EditorFactory.getInstance().getEventMulticaster().removeDocumentListener(myDocumentListener);
  }

  public static boolean isDartSdkVersionSufficient(@NotNull final DartSdk sdk) {
    return StringUtil.compareVersionNumbers(sdk.getVersion(), MIN_SDK_VERSION) >= 0;
  }

  public void addCompletions(@NotNull final String completionId, @NotNull final Consumer<CompletionSuggestion> consumer) {
    while (true) {
      ProgressManager.checkCanceled();

      synchronized (myCompletionInfos) {
        CompletionInfo completionInfo;
        while ((completionInfo = myCompletionInfos.poll()) != null) {
          if (!completionInfo.myCompletionId.equals(completionId)) continue;
          if (!completionInfo.isLast) continue;

          for (final CompletionSuggestion completion : completionInfo.myCompletions) {
            consumer.consume(completion);
          }
          return;
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

    public FormatResult(@Nullable final List<SourceEdit> edits, final int selectionOffset, final int selectionLength) {
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

  private void configureImportedLibraries(@NotNull final Collection<String> filePaths) {
    final Set<Project> projects = myRootsHandler.getTrackedProjects();
    if (projects.size() != 1) return; // no idea how to map files from filePaths to several open projects

    final Project project = projects.iterator().next();
    DumbService.getInstance(project).smartInvokeLater(new Runnable() {
      @Override
      public void run() {
        doConfigureImportedLibraries(project, filePaths);
      }
    });
  }

  private static void doConfigureImportedLibraries(@NotNull final Project project, @NotNull final Collection<String> filePaths) {
    final DartSdk sdk = DartSdk.getDartSdk(project);
    if (sdk == null) return;

    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
    final SortedSet<String> folderPaths = new TreeSet<String>();
    final Collection<String> rootsToAddToLib = new THashSet<String>();

    for (final String path : filePaths) {
      if (path != null) {
        folderPaths.add(PathUtil.getParentPath(FileUtil.toSystemIndependentName(path)));
      }
    }

    outer:
    for (final String path : folderPaths) {
      final VirtualFile vFile = LocalFileSystem.getInstance().findFileByPath(path);
      if (!path.startsWith(sdk.getHomePath() + "/") && (vFile == null || !fileIndex.isInContent(vFile))) {
        for (String configuredPath : rootsToAddToLib) {
          if (path.startsWith(configuredPath + "/")) {
            continue outer; // folderPaths is sorted so subfolders go after parent folder
          }
        }
        rootsToAddToLib.add(path);
      }
    }

    final Processor<? super PsiFileSystemItem> falseProcessor = new Processor<PsiFileSystemItem>() {
      @Override
      public boolean process(final PsiFileSystemItem item) {
        return false;
      }
    };

    final Condition<Module> moduleFilter = new Condition<Module>() {
      @Override
      public boolean value(final Module module) {
        return DartSdkGlobalLibUtil.isDartSdkEnabled(module) &&
               !FilenameIndex.processFilesByName(PubspecYamlUtil.PUBSPEC_YAML, false,
                                                 falseProcessor, module.getModuleContentScope(), project, null);
      }
    };

    final DartFileListener.DartLibInfo libInfo = new DartFileListener.DartLibInfo(true);
    libInfo.addRoots(rootsToAddToLib);
    final Library library = DartFileListener.updatePackagesLibraryRoots(project, libInfo);
    DartFileListener.updateDependenciesOnDartPackagesLibrary(project, moduleFilter, library);
  }

  public DartAnalysisServerService() {
    Disposer.register(ApplicationManager.getApplication(), new Disposable() {
      public void dispose() {
        stopServer();
      }
    });

    Logging.setLogger(new com.google.dart.server.utilities.logging.Logger() {
      @Override
      public void logError(String message) {
        LOG.error(message);
      }

      @Override
      public void logError(String message, Throwable exception) {
        LOG.error(message, exception);
      }

      @Override
      public void logInformation(String message) {
        LOG.debug(message);
      }

      @Override
      public void logInformation(String message, Throwable exception) {
        LOG.debug(message, exception);
      }
    });

    ApplicationManager.getApplication().getMessageBus().connect()
      .subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
        @Override
        public void fileOpened(@NotNull final FileEditorManager source, @NotNull final VirtualFile file) {
          if (!Registry.is("dart.projects.without.pubspec", false) &&
              (PubspecYamlUtil.PUBSPEC_YAML.equals(file.getName()) || file.getFileType() == DartFileType.INSTANCE)) {
            DartSdkUpdateChecker.mayBeCheckForSdkUpdate(source.getProject());
          }

          if (isLocalDartOrHtmlFile(file)) {
            updateVisibleFiles();
          }
        }

        @Override
        public void selectionChanged(@NotNull FileEditorManagerEvent event) {
          if (isLocalDartOrHtmlFile(event.getOldFile()) || isLocalDartOrHtmlFile(event.getNewFile())) {
            updateVisibleFiles();
          }
        }

        @Override
        public void fileClosed(@NotNull final FileEditorManager source, @NotNull final VirtualFile file) {
          if (isLocalDartOrHtmlFile(file)) {
            // file could be opened in more than one editor, so this check is needed
            for (Project project : myRootsHandler.getTrackedProjects()) {
              if (FileEditorManager.getInstance(project).getSelectedEditor(file) == null) {
                myServerData.onFileClosed(file);
                break;
              }
            }

            updateVisibleFiles();
          }
        }
      });

    registerQuickAssistIntentions();
  }

  @NotNull
  public static DartAnalysisServerService getInstance() {
    return ServiceManager.getService(DartAnalysisServerService.class);
  }

  @NotNull
  public List<DartServerData.DartHighlightRegion> getHighlight(@NotNull final VirtualFile file) {
    return myServerData.getHighlight(file);
  }

  @NotNull
  public List<DartServerData.DartNavigationRegion> getNavigation(@NotNull final VirtualFile file) {
    return myServerData.getNavigation(file);
  }

  @NotNull
  public List<DartServerData.DartOverrideMember> getOverrideMembers(@NotNull final VirtualFile file) {
    return myServerData.getOverrideMembers(file);
  }

  @NotNull
  public List<DartServerData.DartRegion> getImplementedClasses(@NotNull final VirtualFile file) {
    return myServerData.getImplementedClasses(file);
  }

  @NotNull
  public List<DartServerData.DartRegion> getImplementedMembers(@NotNull final VirtualFile file) {
    return myServerData.getImplementedMembers(file);
  }

  @SuppressWarnings("NestedSynchronizedStatement")
  void updateVisibleFiles() {
    synchronized (myLock) {
      final List<String> newVisibleFiles = new ArrayList<String>();

      for (Project project : myRootsHandler.getTrackedProjects()) {
        for (VirtualFile file : FileEditorManager.getInstance(project).getSelectedFiles()) {
          if (file.isInLocalFileSystem() && isLocalDartOrHtmlFile(file)) {
            newVisibleFiles.add(FileUtil.toSystemDependentName(file.getPath()));
          }
        }
      }

      if (!Comparing.haveEqualElements(myVisibleFiles, newVisibleFiles)) {
        myVisibleFiles.clear();
        myVisibleFiles.addAll(newVisibleFiles);
        analysis_setPriorityFiles();
        analysis_setSubscriptions();
      }
    }
  }

  @Contract("null->false")
  public static boolean isLocalDartOrHtmlFile(@Nullable final VirtualFile file) {
    return file != null && file.isInLocalFileSystem() && (file.getFileType() == DartFileType.INSTANCE || HtmlUtil.isHtmlFile(file));
  }

  public void updateFilesContent() {
    updateFilesContent(Collections.<Document>emptySet());
  }

  public void updateFilesContent(@NotNull final Set<Document> additionalUpdatedDocuments) {
    // may be use DocumentListener to collect deltas instead of sending the whole Document.getText() each time?

    AnalysisServer server = myServer;
    if (server == null) {
      return;
    }

    final Map<String, Object> filesToUpdate = new THashMap<String, Object>();
    synchronized (myLock) {
      final Set<String> oldTrackedFiles = new THashSet<String>(myFilePathWithOverlaidContentToTimestamp.keySet());

      final FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();

      // Prepare all updated documents.
      final Set<Document> updatedDocuments = Sets.newHashSet();
      Collections.addAll(updatedDocuments, fileDocumentManager.getUnsavedDocuments());
      updatedDocuments.addAll(additionalUpdatedDocuments);

      for (Document document : updatedDocuments) {
        final VirtualFile file = fileDocumentManager.getFile(document);
        if (isLocalDartOrHtmlFile(file)) {
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
    }

    if (!filesToUpdate.isEmpty()) {
      server.analysis_updateContent(filesToUpdate, new UpdateContentConsumer() {
        @Override
        public void onResponse() {
          myServerData.onFilesContentUpdated();
        }
      });
    }
  }

  public boolean updateRoots(@NotNull final List<String> includedRoots,
                             @NotNull final List<String> excludedRoots,
                             @Nullable final Map<String, String> packageRoots) {
    AnalysisServer server = myServer;
    if (server == null) {
      return false;
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("analysis_setAnalysisRoots, included:\n" + StringUtil.join(includedRoots, ",\n") +
                "\nexcluded:\n" + StringUtil.join(excludedRoots, ",\n"));
    }

    server.analysis_setAnalysisRoots(includedRoots, excludedRoots, packageRoots);
    return true;
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
  public AnalysisError[] analysis_getErrors(@NotNull final DartServerErrorsAnnotator.AnnotatorInfo info) {
    final String filePath = FileUtil.toSystemDependentName(info.myFilePath);

    final Ref<AnalysisError[]> resultRef = new Ref<AnalysisError[]>();
    final Semaphore semaphore = new Semaphore();

    try {
      synchronized (myLock) {
        if (myServer == null) return null;

        semaphore.down();

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
            if (RequestErrorCode.GET_ERRORS_INVALID_FILE.equals(error.getCode())) {
              LOG.info(getShortErrorMessage("analysis_getErrors()", filePath, error));
            }
            else {
              logError("analysis_getErrors()", filePath, error);
            }

            semaphore.up();
          }
        };

        myServer.analysis_getErrors(filePath, consumer);
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

  @NotNull
  public List<SourceChange> edit_getAssists(@NotNull final String _filePath, final int offset, final int length) {
    final String filePath = FileUtil.toSystemDependentName(_filePath);
    final List<SourceChange> results = Lists.newArrayList();

    final AnalysisServer server = myServer;
    if (server == null) {
      return results;
    }

    final CountDownLatch latch = new CountDownLatch(1);
    server.edit_getAssists(filePath, offset, length, new GetAssistsConsumer() {
      @Override
      public void computedSourceChanges(List<SourceChange> sourceChanges) {
        results.addAll(sourceChanges);
        latch.countDown();
      }

      @Override
      public void onError(final RequestError error) {
        logError("edit_getAssists()", filePath, error);
        latch.countDown();
      }
    });

    awaitForLatchCheckingCanceled(server, latch, GET_ASSISTS_TIMEOUT);
    return results;
  }

  @Nullable
  public List<AnalysisErrorFixes> edit_getFixes(@NotNull final String _filePath, final int offset) {
    final String filePath = FileUtil.toSystemDependentName(_filePath);
    final Ref<List<AnalysisErrorFixes>> resultRef = new Ref<List<AnalysisErrorFixes>>();
    final Semaphore semaphore = new Semaphore();

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

      myServer.edit_getFixes(filePath, offset, consumer);
    }

    final long t0 = System.currentTimeMillis();
    semaphore.waitFor(GET_FIXES_TIMEOUT);

    if (semaphore.tryUp()) {
      LOG.info("edit_getFixes() took too long for file " + filePath + ": " + (System.currentTimeMillis() - t0) + "ms");
      return null;
    }

    return resultRef.get();
  }

  public void search_findElementReferences(@NotNull final String _filePath,
                                           final int offset,
                                           @NotNull final Consumer<SearchResult> consumer) {
    final String filePath = FileUtil.toSystemDependentName(_filePath);

    final String searchId;
    synchronized (myLock) {
      if (myServer == null) return;
      final AnalysisServer server = myServer;

      final Ref<String> searchIdRef = new Ref<String>();
      final Semaphore semaphore = new Semaphore();

      semaphore.down();

      server.search_findElementReferences(filePath, offset, true, new FindElementReferencesConsumer() {
        @Override
        public void computedElementReferences(String searchId, Element element) {
          searchIdRef.set(searchId);
          semaphore.up();
        }

        @Override
        public void onError(RequestError requestError) {
          semaphore.up();
        }
      });

      final long t0 = System.currentTimeMillis();
      semaphore.waitFor(FIND_ELEMENT_REFERENCES_TIMEOUT);

      if (semaphore.tryUp()) {
        LOG.info("search_findElementReferences() took too long for file " +
                 filePath +
                 "@" +
                 offset +
                 ": " +
                 (System.currentTimeMillis() - t0) +
                 "ms");
        return;
      }

      searchId = searchIdRef.get();
      if (searchId == null) {
        return;
      }
    }

    while (true) {
      ProgressManager.checkCanceled();
      synchronized (mySearchResultSets) {
        SearchResultsSet resultSet;
        // process already received results
        while ((resultSet = mySearchResultSets.poll()) != null) {
          if (!resultSet.id.equals(searchId)) continue;
          for (final SearchResult searchResult : resultSet.results) {
            consumer.consume(searchResult);
          }
          if (resultSet.isLast) return;
        }
        // wait for more results
        try {
          mySearchResultSets.wait(CHECK_CANCELLED_PERIOD);
        }
        catch (InterruptedException e) {
          return;
        }
      }
    }
  }

  @NotNull
  public List<TypeHierarchyItem> search_getTypeHierarchy(@NotNull final VirtualFile file, final int offset, final boolean superOnly) {
    final String filePath = FileUtil.toSystemDependentName(file.getPath());
    final List<TypeHierarchyItem> results = Lists.newArrayList();

    final AnalysisServer server = myServer;
    if (server == null) {
      return results;
    }

    final CountDownLatch latch = new CountDownLatch(1);
    server.search_getTypeHierarchy(filePath, offset, superOnly, new GetTypeHierarchyConsumer() {
      @Override
      public void computedHierarchy(List<TypeHierarchyItem> hierarchyItems) {
        results.addAll(hierarchyItems);
        latch.countDown();
      }

      @Override
      public void onError(RequestError error) {
        logError("search_getTypeHierarchy()", filePath, error);
        latch.countDown();
      }
    });

    awaitForLatchCheckingCanceled(server, latch, GET_TYPE_HIERARCHY_TIMEOUT);
    return results;
  }

  @Nullable
  public String completion_getSuggestions(@NotNull final String _filePath, final int offset) {
    final String filePath = FileUtil.toSystemDependentName(_filePath);
    final Ref<String> resultRef = new Ref<String>();

    final AnalysisServer server = myServer;
    if (server == null) {
      return null;
    }

    final CountDownLatch latch = new CountDownLatch(1);
    server.completion_getSuggestions(filePath, offset, new GetSuggestionsConsumer() {
      @Override
      public void computedCompletionId(@NotNull final String completionId) {
        resultRef.set(completionId);
        latch.countDown();
      }

      @Override
      public void onError(@NotNull final RequestError error) {
        // Not a problem. Happens if a file is outside of the project, or server is just not ready yet.
        latch.countDown();
      }
    });

    awaitForLatchCheckingCanceled(server, latch, GET_SUGGESTIONS_TIMEOUT);
    return resultRef.get();
  }

  @Nullable
  public FormatResult edit_format(@NotNull final String _filePath,
                                  final int selectionOffset,
                                  final int selectionLength,
                                  final int lineLength) {
    final String filePath = FileUtil.toSystemDependentName(_filePath);

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
          if (RequestErrorCode.FORMAT_WITH_ERRORS.equals(error.getCode()) || RequestErrorCode.FORMAT_INVALID_FILE.equals(error.getCode())) {
            LOG.info(getShortErrorMessage("edit_format()", filePath, error));
          }
          else {
            logError("edit_format()", filePath, error);
          }

          semaphore.up();
        }
      };

      myServer.edit_format(filePath, selectionOffset, selectionLength, lineLength, consumer);
    }

    final long t0 = System.currentTimeMillis();
    semaphore.waitFor(EDIT_FORMAT_TIMEOUT);

    if (semaphore.tryUp()) {
      LOG.info("edit_format() took too long for file " + filePath + ": " + (System.currentTimeMillis() - t0) + "ms");
      return null;
    }

    return resultRef.get();
  }

  public boolean edit_getRefactoring(String kind,
                                     String _filePath,
                                     int offset,
                                     int length,
                                     boolean validateOnly,
                                     RefactoringOptions options,
                                     GetRefactoringConsumer consumer) {
    final String filePath = FileUtil.toSystemDependentName(_filePath);

    synchronized (myLock) {
      if (myServer == null) return false;
      myServer.edit_getRefactoring(kind, filePath, offset, length, validateOnly, options, consumer);
      return true;
    }
  }

  @Nullable
  public SourceFileEdit edit_organizeDirectives(@NotNull final String _filePath) {
    final String filePath = FileUtil.toSystemDependentName(_filePath);

    final Ref<SourceFileEdit> resultRef = new Ref<SourceFileEdit>();
    final Semaphore semaphore = new Semaphore();

    synchronized (myLock) {
      if (myServer == null) return null;

      semaphore.down();

      final OrganizeDirectivesConsumer consumer = new OrganizeDirectivesConsumer() {
        @Override
        public void computedEdit(final SourceFileEdit edit) {
          resultRef.set(edit);
          semaphore.up();
        }

        @Override
        public void onError(final RequestError error) {
          if (RequestErrorCode.FILE_NOT_ANALYZED.equals(error.getCode()) ||
              RequestErrorCode.ORGANIZE_DIRECTIVES_ERROR.equals(error.getCode())) {
            LOG.info(getShortErrorMessage("edit_organizeDirectives()", filePath, error));
          }
          else {
            logError("edit_organizeDirectives()", filePath, error);
          }

          semaphore.up();
        }
      };

      myServer.edit_organizeDirectives(filePath, consumer);
    }

    final long t0 = System.currentTimeMillis();
    semaphore.waitFor(EDIT_ORGANIZE_DIRECTIVES_TIMEOUT);

    if (semaphore.tryUp()) {
      LOG.info("edit_organizeDirectives() took too long for file " + filePath + ": " + (System.currentTimeMillis() - t0) + "ms");
      return null;
    }

    return resultRef.get();
  }

  @Nullable
  public SourceFileEdit edit_sortMembers(@NotNull final String _filePath) {
    final String filePath = FileUtil.toSystemDependentName(_filePath);

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
          if (RequestErrorCode.SORT_MEMBERS_PARSE_ERRORS.equals(error.getCode()) ||
              RequestErrorCode.SORT_MEMBERS_INVALID_FILE.equals(error.getCode())) {
            LOG.info(getShortErrorMessage("edit_sortMembers()", filePath, error));
          }
          else {
            logError("edit_sortMembers()", filePath, error);
          }

          semaphore.up();
        }
      };

      myServer.edit_sortMembers(filePath, consumer);
    }

    final long t0 = System.currentTimeMillis();
    semaphore.waitFor(EDIT_SORT_MEMBERS_TIMEOUT);

    if (semaphore.tryUp()) {
      LOG.info("edit_sortMembers() took too long for file " + filePath + ": " + (System.currentTimeMillis() - t0) + "ms");
      return null;
    }

    return resultRef.get();
  }

  public void analysis_reanalyze(@Nullable final List<String> roots) {
    synchronized (myLock) {
      if (myServer == null) return;

      String rootsStr = roots != null ? StringUtil.join(roots, ",\n") : "all roots";
      LOG.debug("analysis_reanalyze, roots: " + rootsStr);

      myServer.analysis_reanalyze(roots);
    }
  }

  private void analysis_setPriorityFiles() {
    synchronized (myLock) {
      if (myServer == null) return;

      if (LOG.isDebugEnabled()) {
        LOG.debug("analysis_setPriorityFiles, files:\n" + StringUtil.join(myVisibleFiles, ",\n"));
      }

      myServer.analysis_setPriorityFiles(myVisibleFiles);
    }
  }

  private void analysis_setSubscriptions() {
    synchronized (myLock) {
      if (myServer == null) return;

      final Map<String, List<String>> subscriptions = new THashMap<String, List<String>>();
      subscriptions.put(AnalysisService.HIGHLIGHTS, myVisibleFiles);
      subscriptions.put(AnalysisService.NAVIGATION, myVisibleFiles);
      subscriptions.put(AnalysisService.OVERRIDES, myVisibleFiles);
      if (StringUtil.compareVersionNumbers(mySdkVersion, "1.13") >= 0) {
        subscriptions.put(AnalysisService.IMPLEMENTED, myVisibleFiles);
      }

      if (LOG.isDebugEnabled()) {
        LOG.debug("analysis_setSubscriptions, subscriptions:\n" + subscriptions);
      }

      myServer.analysis_setSubscriptions(subscriptions);
    }
  }

  private void startServer(@NotNull final DartSdk sdk) {
    synchronized (myLock) {
      mySdkHome = sdk.getHomePath();

      final String runtimePath = FileUtil.toSystemDependentName(mySdkHome + "/bin/dart");

      String analysisServerPath = FileUtil.toSystemDependentName(mySdkHome + "/bin/snapshots/analysis_server.dart.snapshot");
      analysisServerPath = System.getProperty("dart.server.path", analysisServerPath);

      final DebugPrintStream debugStream = new DebugPrintStream() {
        @Override
        public void println(String str) {
          //System.out.println("debugStream: " + str);
        }
      };

      final int port = NetUtils.tryToFindAvailableSocketPort(10000);

      String argsRaw;
      try {
        argsRaw = Registry.stringValue("dart.server.additional.arguments");
      }
      catch (MissingResourceException e) {
        argsRaw = "";
      }
      argsRaw += " --useAnalysisHighlight2";

      myServerSocket =
        new StdioServerSocket(runtimePath, analysisServerPath, null, debugStream, ArrayUtil.toStringArray(StringUtil.split(argsRaw, " ")),
                              false, false, port, false, FileReadMode.NORMALIZE_EOL_ALWAYS);
      myServerSocket.setClientId(ApplicationNamesInfo.getInstance().getFullProductName().replace(' ', '_'));
      myServerSocket.setClientVersion(ApplicationInfo.getInstance().getApiVersion());
      myServer = new RemoteAnalysisServerImpl(myServerSocket);

      try {
        myServer.start();
        myServer.server_setSubscriptions(SERVER_SUBSCRIPTIONS);
        if (Registry.is("dart.projects.without.pubspec", false)) {
          myServer.analysis_setGeneralSubscriptions(Collections.singletonList(GeneralAnalysisService.ANALYZED_FILES));
        }
        myServer.addAnalysisServerListener(myAnalysisServerListener);
        mySdkVersion = sdk.getVersion();

        myServer.analysis_updateOptions(new AnalysisOptions(true, true, true, true, true, false, true, false));

        LOG.info("Server started, see status at http://localhost:" + port + "/status");
      }
      catch (Exception e) {
        LOG.warn("Failed to start Dart analysis server, port=" + port, e);
        stopServer();
      }
    }
  }

  public boolean serverReadyForRequest(@NotNull final Project project) {
    final DartSdk sdk = DartSdk.getDartSdk(project);
    if (sdk == null || !isDartSdkVersionSufficient(sdk)) {
      stopServer();
      return false;
    }

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

  void stopServer() {
    synchronized (myLock) {
      if (myServer != null) {
        LOG.debug("stopping server");
        myServer.removeAnalysisServerListener(myAnalysisServerListener);

        myServer.server_shutdown();

        long startTime = System.currentTimeMillis();
        while (myServerSocket != null && myServerSocket.isOpen()) {
          if (System.currentTimeMillis() - startTime > SEND_REQUEST_TIMEOUT) {
            myServerSocket.stop();
            break;
          }
          Uninterruptibles.sleepUninterruptibly(CHECK_CANCELLED_PERIOD, TimeUnit.MILLISECONDS);
        }
      }

      onServerStopped();
    }
  }

  private void onServerStopped() {
    stopShowingServerProgress();

    synchronized (myLock) {
      myServerSocket = null;
      myServer = null;
      mySdkHome = null;
      myFilePathWithOverlaidContentToTimestamp.clear();
      myVisibleFiles.clear();
      myServerData.clearData();

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

  public void waitWhileServerBusy_TESTS_ONLY() {
    assert ApplicationManager.getApplication().isUnitTestMode();
    waitWhileServerBusy();
  }

  private void waitWhileServerBusy() {
    if (myServerBusy.get()) {
      try {
        synchronized (myServerBusy) {
          if (myServerBusy.get()) {
            //noinspection WaitNotInLoop
            myServerBusy.wait();
          }
        }
      }
      catch (InterruptedException e) {/* unlucky */}
    }
  }

  private void stopShowingServerProgress() {
    myShowServerProgressAlarm.cancelAllRequests();

    synchronized (myServerBusy) {
      myServerBusy.set(false);
      myServerBusy.notifyAll();
    }
  }

  private void logError(@NotNull final String methodName, @Nullable final String filePath, @NotNull final RequestError error) {
    final String trace = error.getStackTrace();
    final String partialTrace = trace == null || trace.isEmpty() ? "" : trace.substring(0, Math.min(trace.length(), 1000));
    final String message = getShortErrorMessage(methodName, filePath, error) + "\n" + partialTrace + "...";
    LOG.error(message);
  }

  private String getShortErrorMessage(@NotNull String methodName, @Nullable String filePath, @NotNull RequestError error) {
    return "Error from " + methodName +
           (filePath == null ? "" : (", file = " + filePath)) +
           ", SDK version = " + mySdkVersion +
           ", server version = " + myServerVersion +
           ", error code = " + error.getCode() + ": " + error.getMessage();
  }

  private static boolean awaitForLatchCheckingCanceled(AnalysisServer server, CountDownLatch latch, long timeoutInMillis) {
    long startTime = System.currentTimeMillis();
    while (true) {
      ProgressManager.checkCanceled();
      if (!server.isSocketOpen()) {
        return false;
      }
      if (timeoutInMillis != -1 && System.currentTimeMillis() > startTime + timeoutInMillis) {
        return false;
      }
      if (Uninterruptibles.awaitUninterruptibly(latch, CHECK_CANCELLED_PERIOD, TimeUnit.MILLISECONDS)) {
        return true;
      }
    }
  }

  /**
   * see {@link DartQuickAssistIntention}
   */
  private static void registerQuickAssistIntentions() {
    final IntentionManager intentionManager = IntentionManager.getInstance();
    final QuickAssistSet quickAssistSet = new QuickAssistSet();
    for (int i = 0; i < 20; i++) {
      final DartQuickAssistIntention intention = new DartQuickAssistIntention(quickAssistSet, i);
      intentionManager.addAction(intention);
    }
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
                          @NotNull final List<CompletionSuggestion> completions,
                          boolean isLast) {
      this.myCompletionId = completionId;
      this.myReplacementOffset = replacementOffset;
      this.myReplacementLength = replacementLength;
      this.myCompletions = completions;
      this.isLast = isLast;
    }
  }

  /**
   * A set of {@link SearchResult}s.
   */
  private static class SearchResultsSet {
    @NotNull final String id;
    @NotNull final List<SearchResult> results;
    final boolean isLast;

    public SearchResultsSet(@NotNull String id, @NotNull List<SearchResult> results, boolean isLast) {
      this.id = id;
      this.results = results;
      this.isLast = isLast;
    }
  }
}
