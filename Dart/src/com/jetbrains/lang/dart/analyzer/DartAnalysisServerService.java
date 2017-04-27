package com.jetbrains.lang.dart.analyzer;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.dart.server.*;
import com.google.dart.server.generated.AnalysisServer;
import com.google.dart.server.internal.remote.DebugPrintStream;
import com.google.dart.server.internal.remote.RemoteAnalysisServerImpl;
import com.google.dart.server.internal.remote.StdioServerSocket;
import com.google.dart.server.utilities.logging.Logging;
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
import com.intellij.openapi.editor.event.DocumentListener;
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
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.Alarm;
import com.intellij.util.Consumer;
import com.intellij.util.PathUtil;
import com.intellij.util.Processor;
import com.intellij.util.concurrency.QueueProcessor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.UIUtil;
import com.intellij.xml.util.HtmlUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartFileListener;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.DartYamlFileTypeFactory;
import com.jetbrains.lang.dart.assists.DartQuickAssistIntention;
import com.jetbrains.lang.dart.assists.QuickAssistSet;
import com.jetbrains.lang.dart.ide.errorTreeView.DartFeedbackBuilder;
import com.jetbrains.lang.dart.ide.errorTreeView.DartProblemsView;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkLibUtil;
import com.jetbrains.lang.dart.sdk.DartSdkUpdateChecker;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import gnu.trove.TObjectIntHashMap;
import org.dartlang.analysis.server.protocol.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DartAnalysisServerService implements Disposable {

  public static final String MIN_SDK_VERSION = "1.12";

  private static final long UPDATE_FILES_TIMEOUT = 300;

  private static final long CHECK_CANCELLED_PERIOD = 10;
  private static final long SEND_REQUEST_TIMEOUT = TimeUnit.SECONDS.toMillis(1);
  private static final long EDIT_FORMAT_TIMEOUT = TimeUnit.SECONDS.toMillis(3);
  private static final long EDIT_ORGANIZE_DIRECTIVES_TIMEOUT = TimeUnit.MILLISECONDS.toMillis(300);
  private static final long EDIT_SORT_MEMBERS_TIMEOUT = TimeUnit.SECONDS.toMillis(3);
  private static final long GET_HOVER_TIMEOUT = TimeUnit.SECONDS.toMillis(1);
  private static final long GET_NAVIGATION_TIMEOUT = TimeUnit.SECONDS.toMillis(1);
  private static final long GET_ASSISTS_TIMEOUT = TimeUnit.MILLISECONDS.toMillis(100);
  private static final long GET_FIXES_TIMEOUT = TimeUnit.MILLISECONDS.toMillis(100);
  private static final long GET_SUGGESTIONS_TIMEOUT = TimeUnit.SECONDS.toMillis(1);
  private static final long FIND_ELEMENT_REFERENCES_TIMEOUT = TimeUnit.SECONDS.toMillis(1);
  private static final long GET_TYPE_HIERARCHY_TIMEOUT = TimeUnit.SECONDS.toMillis(10);
  private static final long EXECUTION_CREATE_CONTEXT_TIMEOUT = TimeUnit.SECONDS.toMillis(1);
  private static final long EXECUTION_MAP_URI_TIMEOUT = TimeUnit.SECONDS.toMillis(1);
  private static final long ANALYSIS_IN_TESTS_TIMEOUT = TimeUnit.SECONDS.toMillis(10);
  private static final long TESTS_TIMEOUT_COEFF = 10;

  private static final Logger LOG = Logger.getInstance("#com.jetbrains.lang.dart.analyzer.DartAnalysisServerService");
  private static final String STACK_TRACE_MARKER = "#0";
  private static final long MIN_DISRUPTION_TIME = 5000L; // 5 seconds minimum between error report balloons
  private static final int MAX_DISRUPTIONS_PER_SESSION = 20; // Do not annoy the user too many times

  private static final int DEBUG_LOG_CAPACITY = 30;
  private static final int MAX_DEBUG_LOG_LINE_LENGTH = 200; // Saw one line while testing that was > 50k

  @NotNull private final Project myProject;
  private boolean myInitializationOnServerStartupDone = false;

  // Do not wait for server response under lock. Do not take read/write action under lock.
  private final Object myLock = new Object();
  @Nullable private AnalysisServer myServer;
  @Nullable private StdioServerSocket myServerSocket;

  @NotNull private String myServerVersion = "";
  @NotNull private String mySdkVersion = "";
  @Nullable private String mySdkHome = null;

  private final DartServerRootsHandler myRootsHandler;
  private final Map<String, Long> myFilePathWithOverlaidContentToTimestamp = new THashMap<>();
  private final List<String> myVisibleFiles = new ArrayList<>();
  private final Set<Document> myChangedDocuments = new THashSet<>();
  private final Alarm myUpdateFilesAlarm;

  @NotNull private final Queue<CompletionInfo> myCompletionInfos = new LinkedList<>();
  @NotNull private final Queue<SearchResultsSet> mySearchResultSets = new LinkedList<>();

  @NotNull private final DartServerData myServerData;

  private volatile boolean myAnalysisInProgress;
  private volatile boolean myPubListInProgress;
  @NotNull private final Alarm myShowServerProgressAlarm;
  @Nullable private ProgressIndicator myProgressIndicator;
  private final Object myProgressLock = new Object();

  private boolean myHaveShownInitialProgress;
  private boolean mySentAnalysisBusy;

  // files with red squiggles in Project View. This field is also used as a lock to access these 3 collections
  @NotNull private final Set<String> myFilePathsWithErrors = new THashSet<>();
  // how many files with errors are in this folder (recursively)
  @NotNull private final TObjectIntHashMap<String> myFolderPathsWithErrors = new TObjectIntHashMap<>();
  // errors hash is tracked to optimize error notification listener: do not handle equal notifications more than once
  @NotNull private final TObjectIntHashMap<String> myFilePathToErrorsHash = new TObjectIntHashMap<>();

  public long maxMillisToWaitForServerResponse = 0L;
  @NotNull private final InteractiveErrorReporter myErrorReporter = new InteractiveErrorReporter();

  @NotNull private final EvictingQueue<String> myDebugLog = EvictingQueue.create(DEBUG_LOG_CAPACITY);

  public static String getClientId() {
    return ApplicationNamesInfo.getInstance().getFullProductName().replaceAll(" ", "-");
  }

  private static String getClientVersion() {
    return ApplicationInfo.getInstance().getApiVersion();
  }

  private final AnalysisServerListener myAnalysisServerListener = new AnalysisServerListenerAdapter() {

    @Override
    public void computedAnalyzedFiles(List<String> filePaths) {
      configureImportedLibraries(filePaths);
    }

    @Override
    public void computedErrors(@NotNull final String filePathSD, @NotNull final List<AnalysisError> errors) {
      final String fileName = PathUtil.getFileName(filePathSD);

      final ProgressIndicator indicator = myProgressIndicator;
      if (indicator != null) {
        indicator.setText(DartBundle.message("dart.analysis.progress.with.file", fileName));
      }

      final List<AnalysisError> errorsWithoutTodo = errors.isEmpty() ? Collections.emptyList() : new ArrayList<>(errors.size());
      boolean hasSevereProblems = false;

      for (AnalysisError error : errors) {
        if (AnalysisErrorSeverity.ERROR.equals(error.getSeverity()) || AnalysisErrorSeverity.WARNING.equals(error.getSeverity())) {
          hasSevereProblems = true;
        }
        if (!AnalysisErrorType.TODO.equals(error.getType())) {
          errorsWithoutTodo.add(error);
        }
      }

      final String filePathSI = FileUtil.toSystemIndependentName(filePathSD);

      final int oldHash;
      synchronized (myFilePathsWithErrors) {
        // TObjectIntHashMap returns 0 if there's no such entry, it's equivalent to empty error set for this file
        oldHash = myFilePathToErrorsHash.get(filePathSI);
      }

      final int newHash = errorsWithoutTodo.isEmpty() ? 0 : ensureNotZero(errorsWithoutTodo.hashCode());
      // do nothing if errors are the same as were already handled previously
      if (oldHash == newHash && !myServerData.isErrorInfoLost(filePathSI)) return;

      final boolean visible = myVisibleFiles.contains(filePathSD);
      if (myServerData.computedErrors(filePathSI, errorsWithoutTodo, visible)) {
        onErrorsUpdated(filePathSI, errorsWithoutTodo, hasSevereProblems, newHash);
      }
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
    public void flushedResults(@NotNull final List<String> _filePaths) {
      final List<String> filePaths = new ArrayList<>(_filePaths.size());
      for (String path : _filePaths) {
        filePaths.add(FileUtil.toSystemIndependentName(path));
      }

      myServerData.onFlushedResults(filePaths);

      for (String filePath : filePaths) {
        onErrorsUpdated(filePath, AnalysisError.EMPTY_LIST, false, 0);
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
      if (!isFatal && stackTrace.startsWith("#0      checkValidPackageUri (package:package_config/src/util.dart:72)")) {
        return;
      }

      String errorMessage =
        "Dart analysis server, SDK version " + mySdkVersion +
        ", server version " + myServerVersion +
        ", " + (isFatal ? "FATAL " : "") + "error: " + message + "\n" + stackTrace;
      myErrorReporter.report(errorMessage);
    }

    @Override
    public void serverStatus(@Nullable final AnalysisStatus analysisStatus, @Nullable final PubStatus pubStatus) {
      final boolean wasBusy = myAnalysisInProgress || myPubListInProgress;

      if (analysisStatus != null) myAnalysisInProgress = analysisStatus.isAnalyzing();
      if (pubStatus != null) myPubListInProgress = pubStatus.isListingPackageDirs();

      if (!wasBusy && (myAnalysisInProgress || myPubListInProgress)) {
        final Runnable delayedRunnable = () -> {
          if (myAnalysisInProgress || myPubListInProgress) {
            startShowingServerProgress();
          }
        };

        // 50ms delay to minimize blinking in case of consequent start-stop-start-stop-... events that happen with pubStatus events
        // 300ms delay to avoid showing progress for very fast analysis start-stop cycle that happens with analysisStatus events
        final int delay = pubStatus != null && pubStatus.isListingPackageDirs() ? 50 : 300;
        myShowServerProgressAlarm.addRequest(delayedRunnable, delay, ModalityState.any());
      }

      if (!myAnalysisInProgress && !myPubListInProgress) {
        stopShowingServerProgress();
      }
    }
  };

  private static int ensureNotZero(int i) {
    return i == 0 ? Integer.MAX_VALUE : i;
  }

  private void startShowingServerProgress() {
    if (!myHaveShownInitialProgress) {
      myHaveShownInitialProgress = true;

      final Task.Backgroundable task = new Task.Backgroundable(myProject, DartBundle.message("dart.analysis.progress.title"), false) {
        @Override
        public void run(@NotNull final ProgressIndicator indicator) {
          if (DartAnalysisServerService.this.myProject.isDisposed()) return;
          if (!myAnalysisInProgress && !myPubListInProgress) return;

          indicator.setText(DartBundle.message("dart.analysis.progress.title"));

          if (ApplicationManager.getApplication().isDispatchThread()) {
            if (!ApplicationManager.getApplication().isUnitTestMode()) {
              LOG.error("wait() in EDT");
            }
          }
          else {
            try {
              myProgressIndicator = indicator;
              waitWhileServerBusy();
            }
            finally {
              myProgressIndicator = null;
            }
          }
        }
      };

      ProgressManager.getInstance().run(task);
    }

    DartAnalysisServerMessages.sendAnalysisStarted(myProject, true);
    mySentAnalysisBusy = true;
  }

  /**
   * Must use it each time right after reading any offset or length from any class from org.dartlang.analysis.server.protocol package
   */
  public int getConvertedOffset(@Nullable final VirtualFile file, final int originalOffset) {
    if (originalOffset <= 0 || file == null) return originalOffset;
    return myFilePathWithOverlaidContentToTimestamp.containsKey(file.getPath())
           ? originalOffset
           : FileOffsetsManager.getInstance().getConvertedOffset(file, originalOffset);
  }

  /**
   * Must use it right before sending any offsets and lengths to the AnalysisServer
   */
  private int getOriginalOffset(@Nullable final VirtualFile file, final int convertedOffset) {
    if (file == null) return convertedOffset;

    return myFilePathWithOverlaidContentToTimestamp.containsKey(file.getPath())
           ? convertedOffset
           : FileOffsetsManager.getInstance().getOriginalOffset(file, convertedOffset);
  }

  public int[] getConvertedOffsets(@NotNull final VirtualFile file, final int[] _offsets) {
    final int[] offsets = new int[_offsets.length];
    for (int i = 0; i < _offsets.length; i++) {
      offsets[i] = getConvertedOffset(file, _offsets[i]);
    }
    return offsets;
  }

  public int[] getConvertedLengths(@NotNull final VirtualFile file, final int[] _offsets, final int[] _lengths) {
    final int[] offsets = getConvertedOffsets(file, _offsets);
    final int[] lengths = new int[_lengths.length];
    for (int i = 0; i < _lengths.length; i++) {
      lengths[i] = getConvertedOffset(file, _offsets[i] + _lengths[i]) - offsets[i];
    }
    return lengths;
  }

  public static boolean isDartSdkVersionSufficient(@NotNull final DartSdk sdk) {
    return StringUtil.compareVersionNumbers(sdk.getVersion(), MIN_SDK_VERSION) >= 0;
  }

  public void addCompletions(@NotNull final VirtualFile file,
                             @NotNull final String completionId,
                             @NotNull final CompletionSuggestionConsumer consumer) {
    while (true) {
      ProgressManager.checkCanceled();

      synchronized (myCompletionInfos) {
        CompletionInfo completionInfo;
        while ((completionInfo = myCompletionInfos.poll()) != null) {
          if (!completionInfo.myCompletionId.equals(completionId)) continue;
          if (!completionInfo.isLast) continue;

          for (final CompletionSuggestion completion : completionInfo.myCompletions) {
            final int convertedReplacementOffset = getConvertedOffset(file, completionInfo.myOriginalReplacementOffset);
            final int convertedReplacementLength = getConvertedOffset(file, completionInfo.myOriginalReplacementLength);
            consumer.consumeCompletionSuggestion(convertedReplacementOffset, convertedReplacementLength, completion);
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
    DumbService.getInstance(myProject).smartInvokeLater(() -> doConfigureImportedLibraries(myProject, filePaths));
  }

  private static void doConfigureImportedLibraries(@NotNull final Project project, @NotNull final Collection<String> filePaths) {
    final DartSdk sdk = DartSdk.getDartSdk(project);
    if (sdk == null) return;

    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
    final SortedSet<String> folderPaths = new TreeSet<>();
    final Collection<String> rootsToAddToLib = new THashSet<>();

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

    final Processor<? super PsiFileSystemItem> falseProcessor = (Processor<PsiFileSystemItem>)item -> false;

    final Condition<Module> moduleFilter = module -> DartSdkLibUtil.isDartSdkEnabled(module) &&
                                                     !FilenameIndex.processFilesByName(PubspecYamlUtil.PUBSPEC_YAML, false,
                                                                                       falseProcessor, module.getModuleContentScope(),
                                                                                       project, null);

    final DartFileListener.DartLibInfo libInfo = new DartFileListener.DartLibInfo(true);
    libInfo.addRoots(rootsToAddToLib);
    final Library library = DartFileListener.updatePackagesLibraryRoots(project, libInfo);
    DartFileListener.updateDependenciesOnDartPackagesLibrary(project, moduleFilter, library);
  }

  public DartAnalysisServerService(@NotNull final Project project) {
    myProject = project;
    myRootsHandler = new DartServerRootsHandler(project);
    myServerData = new DartServerData(this);
    myUpdateFilesAlarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, project);
    myShowServerProgressAlarm = new Alarm(project);
  }

  private static void setDasLogger() {
    if (Logging.getLogger() != com.google.dart.server.utilities.logging.Logger.NULL) {
      return; // already registered
    }

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
  }

  private void registerFileEditorManagerListener() {
    myProject.getMessageBus().connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
      @Override
      public void fileOpened(@NotNull final FileEditorManager source, @NotNull final VirtualFile file) {
        if (!Registry.is("dart.projects.without.pubspec", false) &&
            (PubspecYamlUtil.PUBSPEC_YAML.equals(file.getName()) || file.getFileType() == DartFileType.INSTANCE)) {
          DartSdkUpdateChecker.mayBeCheckForSdkUpdate(source.getProject());
        }

        updateCurrentFile();

        if (isLocalAnalyzableFile(file)) {
          updateVisibleFiles();
        }
      }

      @Override
      public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        updateCurrentFile();

        if (isLocalAnalyzableFile(event.getOldFile()) || isLocalAnalyzableFile(event.getNewFile())) {
          updateVisibleFiles();
        }
      }

      @Override
      public void fileClosed(@NotNull final FileEditorManager source, @NotNull final VirtualFile file) {
        updateCurrentFile();

        if (isLocalAnalyzableFile(file)) {
          // file could be opened in more than one editor, so this check is needed
          if (FileEditorManager.getInstance(myProject).getSelectedEditor(file) == null) {
            myServerData.onFileClosed(file);
          }

          updateVisibleFiles();
        }
      }
    });
  }

  private void registerDocumentListener() {
    final DocumentListener documentListener = new DocumentAdapter() {
      @Override
      public void beforeDocumentChange(DocumentEvent e) {
        if (myServer == null) return;

        myServerData.onDocumentChanged(e);

        final VirtualFile file = FileDocumentManager.getInstance().getFile(e.getDocument());
        if (isLocalAnalyzableFile(file)) {
          for (VirtualFile fileInEditor : FileEditorManager.getInstance(myProject).getOpenFiles()) {
            if (fileInEditor.equals(file)) {
              synchronized (myLock) {
                myChangedDocuments.add(e.getDocument());
              }
              break;
            }
          }
        }

        myUpdateFilesAlarm.cancelAllRequests();
        myUpdateFilesAlarm.addRequest(DartAnalysisServerService.this::updateFilesContent, UPDATE_FILES_TIMEOUT);
      }
    };

    EditorFactory.getInstance().getEventMulticaster().addDocumentListener(documentListener, myProject);
  }

  @NotNull
  public static DartAnalysisServerService getInstance(@NotNull final Project project) {
    return ServiceManager.getService(project, DartAnalysisServerService.class);
  }

  @NotNull
  public Project getProject() {
    return myProject;
  }

  @Override
  public void dispose() {
    stopServer();
  }

  @NotNull
  public List<DartServerData.DartError> getErrors(@NotNull final VirtualFile file) {
    return myServerData.getErrors(file);
  }

  public List<DartServerData.DartError> getErrors(@NotNull final SearchScope scope) {
    return myServerData.getErrors(scope);
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

  void updateCurrentFile() {
    UIUtil.invokeLaterIfNeeded(() -> {
      final VirtualFile[] files = FileEditorManager.getInstance(myProject).getSelectedFiles();
      if (files.length > 0) {
        DartProblemsView.getInstance(myProject).setCurrentFile(files[0]);
      }
    });
  }

  void updateVisibleFiles() {
    ApplicationManager.getApplication().assertReadAccessAllowed();

    synchronized (myLock) {
      final List<String> newVisibleFiles = new ArrayList<>();

      for (VirtualFile file : FileEditorManager.getInstance(myProject).getSelectedFiles()) {
        if (isLocalAnalyzableFile(file)) {
          newVisibleFiles.add(FileUtil.toSystemDependentName(file.getPath()));
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

  /**
   * Return true if the given file can be analyzed by Dart Analysis Server.
   */
  @Contract("null->false")
  public static boolean isLocalAnalyzableFile(@Nullable final VirtualFile file) {
    if (file != null && file.isInLocalFileSystem()) {
      return file.getFileType() == DartFileType.INSTANCE ||
             HtmlUtil.isHtmlFile(file) ||
             file.getName().equals(DartYamlFileTypeFactory.DOT_ANALYSIS_OPTIONS);
    }
    return false;
  }

  public void updateFilesContent() {
    if (myServer != null) {
      ApplicationManager.getApplication().runReadAction(this::doUpdateFilesContent);
    }
  }

  private void doUpdateFilesContent() {
    // may be use DocumentListener to collect deltas instead of sending the whole Document.getText() each time?

    AnalysisServer server = myServer;
    if (server == null) {
      return;
    }

    myUpdateFilesAlarm.cancelAllRequests();

    final Map<String, Object> filesToUpdate = new THashMap<>();
    ApplicationManager.getApplication().assertReadAccessAllowed();
    synchronized (myLock) {
      final Set<String> oldTrackedFiles = new THashSet<>(myFilePathWithOverlaidContentToTimestamp.keySet());

      final FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();

      // some documents in myChangedDocuments may be updated by external change, suxh as switch branch, that's why we track them,
      // getUnsavedDocuments() is not enough, we must make sure that overlaid content is sent for for myChangedDocuments as well (to trigger DAS notifications)
      final Set<Document> documents = new THashSet<>(myChangedDocuments);
      myChangedDocuments.clear();
      ContainerUtil.addAll(documents, fileDocumentManager.getUnsavedDocuments());

      for (Document document : documents) {
        final VirtualFile file = fileDocumentManager.getFile(document);
        if (isLocalAnalyzableFile(file)) {
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
        final Set<String> overlaid = new THashSet<>(filesToUpdate.keySet());
        for (String removeOverlaid : oldTrackedFiles) {
          overlaid.remove(FileUtil.toSystemDependentName(removeOverlaid));
        }
        if (!overlaid.isEmpty()) {
          LOG.debug("Sending overlaid content: " + StringUtil.join(overlaid, ",\n"));
        }

        if (!oldTrackedFiles.isEmpty()) {
          LOG.debug("Removing overlaid content: " + StringUtil.join(oldTrackedFiles, ",\n"));
        }
      }
    }

    if (!filesToUpdate.isEmpty()) {
      server.analysis_updateContent(filesToUpdate, myServerData::onFilesContentUpdated);
    }
  }

  public boolean updateRoots(@NotNull final List<String> includedRoots, @NotNull final List<String> excludedRoots) {
    AnalysisServer server = myServer;
    if (server == null) {
      return false;
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("analysis_setAnalysisRoots, included:\n" + StringUtil.join(includedRoots, ",\n") +
                "\nexcluded:\n" + StringUtil.join(excludedRoots, ",\n"));
    }

    server.analysis_setAnalysisRoots(includedRoots, excludedRoots, null);
    return true;
  }

  private void onErrorsUpdated(@NotNull final String filePath,
                               @NotNull final List<AnalysisError> errors,
                               final boolean hasSevereProblems,
                               final int errorsHash) {
    updateFilesWithErrorsSet(filePath, hasSevereProblems, errorsHash);
    DartProblemsView.getInstance(myProject).updateErrorsForFile(filePath, errors);
  }

  private void updateFilesWithErrorsSet(@NotNull final String filePath, final boolean hasSevereProblems, final int errorsHash) {
    synchronized (myFilePathsWithErrors) {
      if (errorsHash == 0) {
        // no errors
        myFilePathToErrorsHash.remove(filePath);
      }
      else {
        myFilePathToErrorsHash.put(filePath, errorsHash);
      }

      if (hasSevereProblems) {
        if (myFilePathsWithErrors.add(filePath)) {
          String parentPath = PathUtil.getParentPath(filePath);
          while (!parentPath.isEmpty()) {
            final int count = myFolderPathsWithErrors.get(parentPath); // returns zero if there were no path in the map
            myFolderPathsWithErrors.put(parentPath, count + 1);
            parentPath = PathUtil.getParentPath(parentPath);
          }
        }
      }
      else {
        if (myFilePathsWithErrors.remove(filePath)) {
          String parentPath = PathUtil.getParentPath(filePath);
          while (!parentPath.isEmpty()) {
            final int count = myFolderPathsWithErrors.remove(parentPath); // returns zero if there was no path in the map
            if (count > 1) {
              myFolderPathsWithErrors.put(parentPath, count - 1);
            }
            parentPath = PathUtil.getParentPath(parentPath);
          }
        }
      }
    }
  }

  private void clearAllErrors() {
    synchronized (myFilePathsWithErrors) {
      myFilePathsWithErrors.clear();
      myFilePathToErrorsHash.clear();
      myFolderPathsWithErrors.clear();
    }

    if (!myProject.isDisposed() && myInitializationOnServerStartupDone) {
      DartProblemsView.getInstance(myProject).clearAll();
    }
  }

  @NotNull
  public List<HoverInformation> analysis_getHover(@NotNull final VirtualFile file, final int _offset) {
    final String filePath = FileUtil.toSystemDependentName(file.getPath());
    final List<HoverInformation> result = Lists.newArrayList();

    final AnalysisServer server = myServer;
    if (server == null) {
      return HoverInformation.EMPTY_LIST;
    }

    final CountDownLatch latch = new CountDownLatch(1);
    final int offset = getOriginalOffset(file, _offset);
    server.analysis_getHover(filePath, offset, new GetHoverConsumer() {
      @Override
      public void computedHovers(HoverInformation[] hovers) {
        Collections.addAll(result, hovers);
        latch.countDown();
      }

      @Override
      public void onError(RequestError error) {
        logError("analysis_getHover()", filePath, error);
        latch.countDown();
      }
    });

    awaitForLatchCheckingCanceled(server, latch, GET_HOVER_TIMEOUT);
    return result;
  }

  @Nullable
  public List<DartServerData.DartNavigationRegion> analysis_getNavigation(@NotNull final VirtualFile file,
                                                                          final int _offset,
                                                                          final int length) {
    final String filePath = FileUtil.toSystemDependentName(file.getPath());
    final Ref<List<DartServerData.DartNavigationRegion>> resultRef = Ref.create();

    final AnalysisServer server = myServer;
    if (server == null) {
      return null;
    }

    final CountDownLatch latch = new CountDownLatch(1);
    LOG.debug("analysis_getNavigation(" + filePath + ")");

    final int offset = getOriginalOffset(file, _offset);
    server.analysis_getNavigation(filePath, offset, length, new GetNavigationConsumer() {
      @Override
      public void computedNavigation(final List<NavigationRegion> regions) {
        final List<DartServerData.DartNavigationRegion> dartRegions = new ArrayList<>(regions.size());
        for (NavigationRegion region : regions) {
          if (region.getLength() > 0) {
            dartRegions.add(DartServerData.createDartNavigationRegion(DartAnalysisServerService.this, file, region));
          }
        }

        resultRef.set(dartRegions);
        latch.countDown();
      }

      @Override
      public void onError(final RequestError error) {
        if (RequestErrorCode.GET_NAVIGATION_INVALID_FILE.equals(error.getCode())) {
          LOG.info(getShortErrorMessage("analysis_getNavigation()", filePath, error));
        }
        else {
          logError("analysis_getNavigation()", filePath, error);
        }

        latch.countDown();
      }
    });

    awaitForLatchCheckingCanceled(server, latch, GET_NAVIGATION_TIMEOUT);

    if (latch.getCount() > 0) {
      LOG.info("analysis_getNavigation() took more than " + GET_NAVIGATION_TIMEOUT + "ms for file " + filePath);
    }

    return resultRef.get();
  }

  @NotNull
  public List<SourceChange> edit_getAssists(@NotNull final VirtualFile file, final int _offset, final int _length) {
    final String filePath = FileUtil.toSystemDependentName(file.getPath());
    final List<SourceChange> results = Lists.newArrayList();

    final AnalysisServer server = myServer;
    if (server == null) {
      return results;
    }

    final CountDownLatch latch = new CountDownLatch(1);
    final int offset = getOriginalOffset(file, _offset);
    final int length = getOriginalOffset(file, _offset + _length) - offset;
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

  public void diagnostic_getServerPort(GetServerPortConsumer consumer) {
    final AnalysisServer server = myServer;
    if (server == null) {
      consumer.onError(new RequestError(ExtendedRequestErrorCode.INVALID_SERVER_RESPONSE,
                                        "The analysis server is not running.", null));
    }
    else {
      server.diagnostic_getServerPort(consumer);
    }
  }

  /**
   * If server responds in less than <code>GET_FIXES_TIMEOUT</code> then this method can be considered synchronous: when exiting this method
   * <code>consumer</code> is already notified. Otherwise this method is async.
   */
  public void askForFixesAndWaitABitIfReceivedQuickly(@NotNull final VirtualFile file,
                                                      final int _offset,
                                                      @NotNull final Consumer<List<AnalysisErrorFixes>> consumer) {
    final String filePath = FileUtil.toSystemDependentName(file.getPath());

    final AnalysisServer server = myServer;
    if (server == null) return;

    final CountDownLatch latch = new CountDownLatch(1);
    final int offset = getOriginalOffset(file, _offset);
    server.edit_getFixes(filePath, offset, new GetFixesConsumer() {
      @Override
      public void computedFixes(final List<AnalysisErrorFixes> fixes) {
        consumer.consume(fixes);
        latch.countDown();
      }

      @Override
      public void onError(final RequestError error) {
        logError("edit_getFixes()", filePath, error);
        latch.countDown();
      }
    });

    awaitForLatchCheckingCanceled(server, latch, GET_FIXES_TIMEOUT);
  }

  public void search_findElementReferences(@NotNull final VirtualFile file,
                                           final int _offset,
                                           @NotNull final Consumer<SearchResult> consumer) {
    final String filePath = FileUtil.toSystemDependentName(file.getPath());
    final Ref<String> searchIdRef = new Ref<>();

    final AnalysisServer server = myServer;
    if (server == null) return;

    final CountDownLatch latch = new CountDownLatch(1);
    final int offset = getOriginalOffset(file, _offset);
    server.search_findElementReferences(filePath, offset, true, new FindElementReferencesConsumer() {
      @Override
      public void computedElementReferences(String searchId, Element element) {
        searchIdRef.set(searchId);
        latch.countDown();
      }

      @Override
      public void onError(RequestError error) {
        LOG.info(getShortErrorMessage("search_findElementReferences()", filePath, error));
        latch.countDown();
      }
    });

    awaitForLatchCheckingCanceled(server, latch, FIND_ELEMENT_REFERENCES_TIMEOUT);

    if (latch.getCount() > 0) {
      LOG.info("search_findElementReferences() took too long for " + filePath + "@" + offset);
      return;
    }

    final String searchId = searchIdRef.get();
    if (searchId == null) {
      return;
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
  public List<TypeHierarchyItem> search_getTypeHierarchy(@NotNull final VirtualFile file, final int _offset, final boolean superOnly) {
    final String filePath = FileUtil.toSystemDependentName(file.getPath());
    final List<TypeHierarchyItem> results = Lists.newArrayList();

    final AnalysisServer server = myServer;
    if (server == null) {
      return results;
    }

    final CountDownLatch latch = new CountDownLatch(1);
    final int offset = getOriginalOffset(file, _offset);
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
  public String completion_getSuggestions(@NotNull final VirtualFile file, final int _offset) {
    final String filePath = FileUtil.toSystemDependentName(file.getPath());
    final Ref<String> resultRef = new Ref<>();

    final AnalysisServer server = myServer;
    if (server == null) {
      return null;
    }

    final CountDownLatch latch = new CountDownLatch(1);
    final int offset = getOriginalOffset(file, _offset);
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
  public FormatResult edit_format(@NotNull final VirtualFile file,
                                  final int _selectionOffset,
                                  final int _selectionLength,
                                  final int lineLength) {
    final String filePath = FileUtil.toSystemDependentName(file.getPath());
    final Ref<FormatResult> resultRef = new Ref<>();

    final AnalysisServer server = myServer;
    if (server == null) return null;

    final CountDownLatch latch = new CountDownLatch(1);
    final int selectionOffset = getOriginalOffset(file, _selectionOffset);
    final int selectionLength = getOriginalOffset(file, _selectionOffset + _selectionLength) - selectionOffset;
    server.edit_format(filePath, selectionOffset, selectionLength, lineLength, new FormatConsumer() {
      @Override
      public void computedFormat(final List<SourceEdit> edits, final int selectionOffset, final int selectionLength) {
        resultRef.set(new FormatResult(edits, selectionOffset, selectionLength));
        latch.countDown();
      }

      @Override
      public void onError(final RequestError error) {
        if (RequestErrorCode.FORMAT_WITH_ERRORS.equals(error.getCode()) || RequestErrorCode.FORMAT_INVALID_FILE.equals(error.getCode())) {
          LOG.info(getShortErrorMessage("edit_format()", filePath, error));
        }
        else {
          logError("edit_format()", filePath, error);
        }

        latch.countDown();
      }
    });

    awaitForLatchCheckingCanceled(server, latch, EDIT_FORMAT_TIMEOUT);

    if (latch.getCount() > 0) {
      LOG.info("edit_format() took too long for file " + filePath);
    }

    return resultRef.get();
  }

  public boolean edit_getRefactoring(String kind,
                                     VirtualFile file,
                                     int _offset,
                                     int _length,
                                     boolean validateOnly,
                                     RefactoringOptions options,
                                     GetRefactoringConsumer consumer) {
    final String filePath = FileUtil.toSystemDependentName(file.getPath());

    final AnalysisServer server = myServer;
    if (server == null) return false;

    final int offset = getOriginalOffset(file, _offset);
    final int length = getOriginalOffset(file, _offset + _length) - offset;
    server.edit_getRefactoring(kind, filePath, offset, length, validateOnly, options, consumer);
    return true;
  }

  @Nullable
  public SourceFileEdit edit_organizeDirectives(@NotNull final String _filePath) {
    final String filePath = FileUtil.toSystemDependentName(_filePath);
    final Ref<SourceFileEdit> resultRef = new Ref<>();

    final AnalysisServer server = myServer;
    if (server == null) return null;

    final CountDownLatch latch = new CountDownLatch(1);

    server.edit_organizeDirectives(filePath, new OrganizeDirectivesConsumer() {
      @Override
      public void computedEdit(final SourceFileEdit edit) {
        resultRef.set(edit);
        latch.countDown();
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

        latch.countDown();
      }
    });

    awaitForLatchCheckingCanceled(server, latch, EDIT_ORGANIZE_DIRECTIVES_TIMEOUT);

    if (latch.getCount() > 0) {
      LOG.info("edit_organizeDirectives() took too long for file " + filePath);
    }

    return resultRef.get();
  }

  @Nullable
  public SourceFileEdit edit_sortMembers(@NotNull final String _filePath) {
    final String filePath = FileUtil.toSystemDependentName(_filePath);
    final Ref<SourceFileEdit> resultRef = new Ref<>();

    final AnalysisServer server = myServer;
    if (server == null) return null;

    final CountDownLatch latch = new CountDownLatch(1);
    server.edit_sortMembers(filePath, new SortMembersConsumer() {
      @Override
      public void computedEdit(final SourceFileEdit edit) {
        resultRef.set(edit);
        latch.countDown();
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

        latch.countDown();
      }
    });

    awaitForLatchCheckingCanceled(server, latch, EDIT_SORT_MEMBERS_TIMEOUT);

    if (latch.getCount() > 0) {
      LOG.info("edit_sortMembers() took too long for file " + filePath);
    }

    return resultRef.get();
  }

  public void analysis_reanalyze() {
    final AnalysisServer server = myServer;
    if (server == null) return;

    server.analysis_reanalyze(null);

    ApplicationManager.getApplication().invokeLater(this::clearAllErrors, ModalityState.NON_MODAL);
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

      final Map<String, List<String>> subscriptions = new THashMap<>();
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

  @Nullable
  public String execution_createContext(@NotNull final String _filePath) {
    final String filePath = FileUtil.toSystemDependentName(_filePath);
    final Ref<String> resultRef = new Ref<>();

    final AnalysisServer server = myServer;
    if (server == null) return null;

    final CountDownLatch latch = new CountDownLatch(1);
    server.execution_createContext(filePath, new CreateContextConsumer() {
      @Override
      public void computedExecutionContext(final String contextId) {
        resultRef.set(contextId);
        latch.countDown();
      }

      @Override
      public void onError(final RequestError error) {
        logError("execution_createContext()", filePath, error);
        latch.countDown();
      }
    });

    awaitForLatchCheckingCanceled(server, latch, EXECUTION_CREATE_CONTEXT_TIMEOUT);

    if (latch.getCount() > 0) {
      LOG.info("execution_createContext() took too long for file " + filePath);
    }

    return resultRef.get();
  }

  public void execution_deleteContext(@NotNull final String contextId) {
    final AnalysisServer server = myServer;
    if (server != null) {
      server.execution_deleteContext(contextId);
    }
  }

  @Nullable
  public String execution_mapUri(@NotNull final String _id, @Nullable final String _filePath, @Nullable final String _uri) {
    // From the Dart Analysis Server Spec:
    // Exactly one of the file and uri fields must be provided. If both fields are provided, then an error of type INVALID_PARAMETER will
    // be generated. Similarly, if neither field is provided, then an error of type INVALID_PARAMETER will be generated.
    if ((_filePath == null && _uri == null) || (_filePath != null && _uri != null)) {
      LOG.error("One of _filePath and _uri must be non-null.");
      return null;
    }

    final String filePath = _filePath != null ? FileUtil.toSystemDependentName(_filePath) : null;
    final Ref<String> resultRef = new Ref<>();

    final AnalysisServer server = myServer;
    if (server == null) return null;

    final CountDownLatch latch = new CountDownLatch(1);
    server.execution_mapUri(_id, filePath, _uri, new MapUriConsumer() {
      @Override
      public void computedFileOrUri(final String file, final String uri) {
        if (uri != null) {
          resultRef.set(uri);
        }
        else {
          resultRef.set(file);
        }
        latch.countDown();
      }

      @Override
      public void onError(final RequestError error) {
        LOG.warn(
          "execution_mapUri(" + _id + ", " + filePath + ", " + _uri + ") returned error " + error.getCode() + ": " + error.getMessage());
        latch.countDown();
      }
    });

    awaitForLatchCheckingCanceled(server, latch, EXECUTION_MAP_URI_TIMEOUT);

    if (latch.getCount() > 0) {
      LOG.info("execution_mapUri() took too long for contextID " + _id + " and file or uri " + (filePath != null ? filePath : _uri));
      return null;
    }

    if (_uri != null && !resultRef.isNull()) {
      return FileUtil.toSystemIndependentName(resultRef.get());
    }

    return resultRef.get();
  }

  private void startServer(@NotNull final DartSdk sdk) {
    synchronized (myLock) {
      mySdkHome = sdk.getHomePath();

      final String runtimePath = FileUtil.toSystemDependentName(mySdkHome + "/bin/dart");

      String analysisServerPath = FileUtil.toSystemDependentName(mySdkHome + "/bin/snapshots/analysis_server.dart.snapshot");
      analysisServerPath = System.getProperty("dart.server.path", analysisServerPath);

      final DebugPrintStream debugStream = str -> {
        str = str.substring(0, Math.min(str.length(), MAX_DEBUG_LOG_LINE_LENGTH));
        synchronized (myDebugLog) {
          myDebugLog.add(str);
        }
      };

      String vmArgsRaw;
      try {
        vmArgsRaw = Registry.stringValue("dart.server.vm.options");
      }
      catch (MissingResourceException e) {
        vmArgsRaw = "";
      }

      String serverArgsRaw = "";
      serverArgsRaw += " --useAnalysisHighlight2";
      //serverArgsRaw += " --file-read-mode=normalize-eol-always";
      try {
        serverArgsRaw += " " + Registry.stringValue("dart.server.additional.arguments");
      }
      catch (MissingResourceException e) {
        // NOP
      }

      myServerSocket =
        new StdioServerSocket(runtimePath, StringUtil.split(vmArgsRaw, " "), analysisServerPath, StringUtil.split(serverArgsRaw, " "),
                              debugStream);
      myServerSocket.setClientId(getClientId());
      myServerSocket.setClientVersion(getClientVersion());

      final AnalysisServer startedServer = new RemoteAnalysisServerImpl(myServerSocket);

      try {
        startedServer.start();
        startedServer.server_setSubscriptions(Collections.singletonList(ServerService.STATUS));
        if (Registry.is("dart.projects.without.pubspec", false)) {
          startedServer.analysis_setGeneralSubscriptions(Collections.singletonList(GeneralAnalysisService.ANALYZED_FILES));
        }

        if (!myInitializationOnServerStartupDone) {
          myInitializationOnServerStartupDone = true;

          registerFileEditorManagerListener();
          registerDocumentListener();
          setDasLogger();
          registerQuickAssistIntentions();
        }

        startedServer.addAnalysisServerListener(myAnalysisServerListener);

        myHaveShownInitialProgress = false;
        startedServer.addStatusListener(isAlive -> {
          if (!isAlive) {
            synchronized (myLock) {
              if (startedServer == myServer) {
                stopServer();
              }
            }
          }
        });

        mySdkVersion = sdk.getVersion();

        startedServer.analysis_updateOptions(new AnalysisOptions(true, true, true, true, true, false, true, false));

        myServer = startedServer;
      }
      catch (Exception e) {
        LOG.warn("Failed to start Dart analysis server", e);
        stopServer();
      }
    }
  }

  public boolean isServerProcessActive() {
    synchronized (myLock) {
      return myServer != null && myServer.isSocketOpen();
    }
  }

  public boolean isServerResponsive() {
    if (maxMillisToWaitForServerResponse == 0L) return true; // UI has not finished initialization yet.
    if (!(myAnalysisInProgress || myPubListInProgress)) return true;
    long responseMillis, requestMillis;
    synchronized (myLock) {
      if (myServer == null) return false;
      responseMillis = myServer.getLastResponseMillis();
      requestMillis = myServer.getLastRequestMillis();
    }
    if (responseMillis == 0L) return true; // Allow UI to start in good state even if it becomes unknown later.
    if (requestMillis <= responseMillis) return true;
    long delta = System.currentTimeMillis() - requestMillis;
    return delta > maxMillisToWaitForServerResponse;
  }

  public boolean serverReadyForRequest(@NotNull final Project project) {
    final DartSdk sdk = DartSdk.getDartSdk(project);
    if (sdk == null || !isDartSdkVersionSufficient(sdk)) {
      stopServer();
      return false;
    }

    ApplicationManager.getApplication().assertReadAccessAllowed();
    synchronized (myLock) {
      if (myServer == null || !sdk.getHomePath().equals(mySdkHome) || !sdk.getVersion().equals(mySdkVersion) || !myServer.isSocketOpen()) {
        stopServer();
        startServer(sdk);

        if (myServer != null) {
          myRootsHandler.ensureProjectServed();
        }
      }

      return myServer != null;
    }
  }

  public void restartServer() {
    stopServer();
    serverReadyForRequest(myProject);
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

      stopShowingServerProgress();
      myUpdateFilesAlarm.cancelAllRequests();

      myServerSocket = null;
      myServer = null;
      mySdkHome = null;
      myFilePathWithOverlaidContentToTimestamp.clear();
      myVisibleFiles.clear();
      myChangedDocuments.clear();
      myServerData.clearData();
      myRootsHandler.reset();

      if (myProject.isOpen() && !myProject.isDisposed()) {
        ApplicationManager.getApplication().invokeLater(this::clearAllErrors, ModalityState.NON_MODAL, myProject.getDisposed());
      }
    }
  }

  public void waitForAnalysisToComplete_TESTS_ONLY(@NotNull final VirtualFile file) {
    assert ApplicationManager.getApplication().isUnitTestMode();

    final AnalysisServer server = myServer;
    if (server == null) return;

    final CountDownLatch latch = new CountDownLatch(1);
    server.analysis_getErrors(FileUtil.toSystemDependentName(file.getPath()), new GetErrorsConsumer() {
      @Override
      public void computedErrors(AnalysisError[] errors) {
        latch.countDown();
      }

      @Override
      public void onError(RequestError requestError) {
        latch.countDown();
        LOG.error(requestError.getMessage());
      }
    });

    awaitForLatchCheckingCanceled(server, latch, ANALYSIS_IN_TESTS_TIMEOUT / TESTS_TIMEOUT_COEFF);
    assert latch.getCount() == 0 : "Analysis did't complete in " + ANALYSIS_IN_TESTS_TIMEOUT + "ms.";
  }

  private void waitWhileServerBusy() {
    try {
      synchronized (myProgressLock) {
        while (myAnalysisInProgress || myPubListInProgress) {
          myProgressLock.wait();
        }
      }
    }
    catch (InterruptedException e) {/* unlucky */}
  }

  private void stopShowingServerProgress() {
    myShowServerProgressAlarm.cancelAllRequests();

    synchronized (myProgressLock) {
      myAnalysisInProgress = false;
      myPubListInProgress = false;
      myProgressLock.notifyAll();

      if (mySentAnalysisBusy) {
        mySentAnalysisBusy = false;
        DartAnalysisServerMessages.sendAnalysisStarted(myProject, false);
      }
    }
  }

  public boolean isFileWithErrors(@NotNull final VirtualFile file) {
    synchronized (myFilePathsWithErrors) {
      return file.isDirectory() ? myFolderPathsWithErrors.get(file.getPath()) > 0 : myFilePathsWithErrors.contains(file.getPath());
    }
  }

  public int getFilePathsWithErrorsHash() {
    synchronized (myFilePathsWithErrors) {
      return myFilePathsWithErrors.hashCode();
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

  private static boolean awaitForLatchCheckingCanceled(@NotNull final AnalysisServer server,
                                                       @NotNull final CountDownLatch latch,
                                                       long timeoutInMillis) {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      timeoutInMillis *= TESTS_TIMEOUT_COEFF;
    }

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

  public interface CompletionSuggestionConsumer {
    void consumeCompletionSuggestion(final int replacementOffset,
                                     final int replacementLength,
                                     final @NotNull CompletionSuggestion completionSuggestion);
  }

  private static class CompletionInfo {
    @NotNull private final String myCompletionId;
    /**
     * must be converted before any usage
     */
    private final int myOriginalReplacementOffset;
    /**
     * must be converted before any usage
     */
    private final int myOriginalReplacementLength;
    @NotNull private final List<CompletionSuggestion> myCompletions;
    private final boolean isLast;

    public CompletionInfo(@NotNull final String completionId,
                          int replacementOffset,
                          int originalReplacementLength,
                          @NotNull final List<CompletionSuggestion> completions,
                          boolean isLast) {
      this.myCompletionId = completionId;
      this.myOriginalReplacementOffset = replacementOffset;
      this.myOriginalReplacementLength = originalReplacementLength;
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

  /**
   * Ask the user to report an error in the analysis server, subject to these constraints:
   * - The same message is not reported twice in a row
   * - The user is not interrupted too often
   */
  private class InteractiveErrorReporter {

    @NotNull private QueueProcessor<Runnable> myErrorReporter = QueueProcessor.createRunnableQueueProcessor();
    private long myPreviousTime;
    @NotNull private String myPreviousMessage = "";
    private int myDisruptionCount = 0;

    public void report(@NotNull String errorMessage) {
      if (myDisruptionCount > MAX_DISRUPTIONS_PER_SESSION) return;
      long timeStamp = System.currentTimeMillis();
      if (timeStamp - myPreviousTime < MIN_DISRUPTION_TIME) {
        if (messageDiffers(errorMessage)) {
          LOG.warn(errorMessage);
          if (myDisruptionCount > 0) {
            myDisruptionCount++; // The red flashing icon is somewhat disruptive, but we only count if the user has already been queried.
          }
        }
        return;
      }
      myPreviousTime = timeStamp;
      if (messageDiffers(errorMessage)) {
        String debugLog = debugLogContent();
        myErrorReporter.add(() -> {
          DartFeedbackBuilder builder = DartFeedbackBuilder.getFeedbackBuilder();
          myDisruptionCount++;
          builder.showNotification(DartBundle.message("dart.analysis.server.error"), myProject, errorMessage, debugLog);
        });
      }
      myPreviousMessage = errorMessage;
    }

    private boolean messageDiffers(@NotNull String errorMessage) {
      int prevIdx = myPreviousMessage.indexOf(STACK_TRACE_MARKER);
      if (prevIdx < 0) return !errorMessage.equals(myPreviousMessage);
      int errIdx = errorMessage.indexOf(STACK_TRACE_MARKER);
      if (errIdx < 0) return !errorMessage.equals(myPreviousMessage);
      // Compare Dart stack traces
      return !errorMessage.substring(errIdx).equals(myPreviousMessage.substring(prevIdx));
    }

    private String debugLogContent() {
      StringBuilder log = new StringBuilder();
      log.append("```\n");
      synchronized (myDebugLog) {
        for (String s : myDebugLog) {
          log.append(s).append('\n');
        }
      }
      log.append("```\n");
      return log.toString();
    }
  }
}
