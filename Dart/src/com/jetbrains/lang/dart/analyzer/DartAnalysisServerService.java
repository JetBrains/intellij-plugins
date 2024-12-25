// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.analyzer;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.dart.server.*;
import com.google.dart.server.generated.AnalysisServer;
import com.google.dart.server.internal.remote.DebugPrintStream;
import com.google.dart.server.internal.remote.RemoteAnalysisServerImpl;
import com.google.dart.server.internal.remote.StdioServerSocket;
import com.google.dart.server.utilities.logging.Logging;
import com.google.gson.JsonObject;
import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.impl.FileOffsetsManager;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.*;
import com.intellij.util.Consumer;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.io.URLUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.assists.DartQuickAssistIntention;
import com.jetbrains.lang.dart.assists.DartQuickAssistIntentionListener;
import com.jetbrains.lang.dart.fixes.DartQuickFix;
import com.jetbrains.lang.dart.fixes.DartQuickFixListener;
import com.jetbrains.lang.dart.ide.actions.DartPubActionBase;
import com.jetbrains.lang.dart.ide.completion.DartCompletionTimerExtension;
import com.jetbrains.lang.dart.ide.errorTreeView.DartProblemsView;
import com.jetbrains.lang.dart.ide.template.postfix.DartPostfixTemplateProvider;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkUpdateChecker;
import com.jetbrains.lang.dart.sdk.DartSdkUtil;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import kotlinx.coroutines.CoroutineScope;
import org.dartlang.analysis.server.protocol.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class DartAnalysisServerService implements Disposable {
  public static final String MIN_SDK_VERSION = "1.12";
  private static final String MIN_MOVE_FILE_SDK_VERSION = "2.3.2";
  private static final String COMPLETION_2_SERVER_VERSION = "1.33";

  // Webdev works going back to 2.6.0, future minimum version listed in the pubspec.yaml, link below, won't mean that 2.6.0 aren't
  // supported.
  // https://github.com/dart-lang/webdev/blob/master/webdev/pubspec.yaml#L11
  public static final String MIN_WEBDEV_SDK_VERSION = "2.6.0";

  // As of the Dart SDK version 2.8.0, the file .dart_tool/package_config.json is preferred over the .packages file.
  // https://github.com/dart-lang/sdk/issues/48272
  public static final String MIN_PACKAGE_CONFIG_JSON_SDK_VERSION = "2.8.0";

  // The dart cli command provides a language server command, `dart language-server`, which
  // should be used going forward instead of `dart .../analysis_server.dart.snapshot`.
  public static final String MIN_DART_LANG_SERVER_SDK_VERSION = "2.16.0";

  // See "supportsUris"
  // https://htmlpreview.github.io/?https://github.com/dart-lang/sdk/blob/main/pkg/analysis_server/doc/api.html#request_server.setClientCapabilities
  public static final String MIN_FILE_URI_SDK_VERSION = "3.4.0";

  private static final long UPDATE_FILES_TIMEOUT = 300;

  private static final long CHECK_CANCELLED_PERIOD = 10;
  private static final long SEND_REQUEST_TIMEOUT = TimeUnit.SECONDS.toMillis(1);
  private static final long EDIT_FORMAT_TIMEOUT = TimeUnit.SECONDS.toMillis(3);
  private static final long EDIT_ORGANIZE_DIRECTIVES_TIMEOUT = TimeUnit.MILLISECONDS.toMillis(300);
  private static final long EDIT_SORT_MEMBERS_TIMEOUT = TimeUnit.SECONDS.toMillis(3);
  private static final long GET_HOVER_TIMEOUT = TimeUnit.SECONDS.toMillis(1);
  private static final long GET_NAVIGATION_TIMEOUT = TimeUnit.SECONDS.toMillis(1);
  private static final long GET_ASSISTS_TIMEOUT_EDT = TimeUnit.MILLISECONDS.toMillis(100);
  private static final long GET_ASSISTS_TIMEOUT = TimeUnit.MILLISECONDS.toMillis(1000);
  private static final long GET_FIXES_TIMEOUT_EDT = TimeUnit.MILLISECONDS.toMillis(100);
  private static final long GET_FIXES_TIMEOUT = TimeUnit.MILLISECONDS.toMillis(1000);
  private static final long IMPORTED_ELEMENTS_TIMEOUT = TimeUnit.MILLISECONDS.toMillis(100);
  private static final long POSTFIX_COMPLETION_TIMEOUT = TimeUnit.MILLISECONDS.toMillis(100);
  private static final long POSTFIX_INITIALIZATION_TIMEOUT = TimeUnit.MILLISECONDS.toMillis(5000);
  private static final long STATEMENT_COMPLETION_TIMEOUT = TimeUnit.MILLISECONDS.toMillis(100);
  private static final long GET_SUGGESTIONS_TIMEOUT = TimeUnit.SECONDS.toMillis(5);
  private static final long GET_SUGGESTION_DETAILS_TIMEOUT = TimeUnit.MILLISECONDS.toMillis(100);
  private static final long GET_SUGGESTION_DETAILS2_TIMEOUT = TimeUnit.MILLISECONDS.toMillis(100);
  private static final long FIND_ELEMENT_REFERENCES_TIMEOUT = TimeUnit.SECONDS.toMillis(1);
  private static final long GET_TYPE_HIERARCHY_TIMEOUT = TimeUnit.SECONDS.toMillis(10);
  private static final long EXECUTION_CREATE_CONTEXT_TIMEOUT = TimeUnit.SECONDS.toMillis(1);
  private static final long EXECUTION_MAP_URI_TIMEOUT = TimeUnit.SECONDS.toMillis(1);
  private static final long ANALYSIS_IN_TESTS_TIMEOUT = TimeUnit.SECONDS.toMillis(10);
  private static final long LSP_MESSAGE_TEXT_DOCUMENT_CONTENT_TIMEOUT = TimeUnit.SECONDS.toMillis(50);
  private static final long TESTS_TIMEOUT_COEFF = 10;

  private static final Logger LOG = Logger.getInstance(DartAnalysisServerService.class);

  private static final int DEBUG_LOG_CAPACITY = 30;
  private static final int MAX_DEBUG_LOG_LINE_LENGTH = 200; // Saw one line while testing that was > 50k

  private final @NotNull Project myProject;
  private final @NotNull CoroutineScope myServiceScope;
  private boolean myInitializationOnServerStartupDone;
  private boolean mySubscribeToServerLog;

  // Do not wait for server response under lock. Do not take read/write action under lock.
  private final Object myLock = new Object();
  private @Nullable RemoteAnalysisServerImpl myServer;
  private @Nullable StdioServerSocket myServerSocket;

  private @NotNull String myServerVersion = "";
  private @NotNull String mySdkVersion = "";
  private @Nullable String mySdkHome;

  private final DartServerRootsHandler myRootsHandler;
  private final Map<String, Long> myFilePathWithOverlaidContentToTimestamp = Collections.synchronizedMap(new HashMap<>());
  private final List<String> myVisibleFileUris = new ArrayList<>();
  private final Set<Document> myChangedDocuments = new HashSet<>();
  private final Alarm myUpdateFilesAlarm;

  private final @NotNull Queue<CompletionInfo> myCompletionInfos = new LinkedList<>();
  private final @NotNull Queue<SearchResultsSet> mySearchResultSets = new LinkedList<>();

  private final @NotNull DartServerData myServerData;

  private volatile boolean myAnalysisInProgress;
  private volatile boolean myPubListInProgress;
  private final @NotNull Alarm myShowServerProgressAlarm;
  private final @NotNull DartAnalysisServerErrorHandler myServerErrorHandler;
  private @Nullable ProgressIndicator myProgressIndicator;
  private final Object myProgressLock = new Object();

  private boolean myHaveShownInitialProgress;
  private boolean mySentAnalysisBusy;

  // files with red squiggles in Project View. This field is also used as a lock to access these 3 collections
  private final @NotNull Set<String> myFilePathsWithErrors = new HashSet<>();
  // how many files with errors are in this folder (recursively)
  private final @NotNull Object2IntMap<String> myFolderPathsWithErrors = new Object2IntOpenHashMap<>();
  // errors hash is tracked to optimize error notification listener: do not handle equal notifications more than once
  private final @NotNull Object2IntMap<String> myFilePathToErrorsHash = new Object2IntOpenHashMap<>();

  private final @NotNull EvictingQueue<String> myDebugLog = EvictingQueue.create(DEBUG_LOG_CAPACITY);

  private boolean myDisposed;
  private final @NotNull Condition<?> myDisposedCondition = o -> myDisposed;

  public static String getClientId() {
    return ApplicationNamesInfo.getInstance().getFullProductName().replace(' ', '-');
  }

  private static String getClientVersion() {
    return ApplicationInfo.getInstance().getApiVersion();
  }

  private final @NotNull List<AnalysisServerListener> myAdditionalServerListeners = new SmartList<>();
  private final @NotNull List<RequestListener> myRequestListeners = new SmartList<>();
  private final @NotNull List<ResponseListener> myResponseListeners = new SmartList<>();
  private final @NotNull List<DartQuickAssistIntentionListener> myQuickAssistIntentionListeners = new SmartList<>();
  private final @NotNull List<DartQuickFixListener> myQuickFixListeners = new SmartList<>();

  private final AnalysisServerListener myAnalysisServerListener = new AnalysisServerListenerAdapter() {
    @Override
    public void computedAvailableSuggestions(@NotNull List<AvailableSuggestionSet> changed, int @NotNull [] removed) {
      myServerData.computedAvailableSuggestions(changed, removed);
    }

    @Override
    public void computedExistingImports(@NotNull String filePathSD, @NotNull Map<String, Map<String, Set<String>>> existingImports) {
      myServerData.computedExistingImports(filePathSD, existingImports);
    }

    @Override
    public void computedErrors(@NotNull String filePathOrUri, @NotNull List<AnalysisError> errors) {
      DartFileInfo fileInfo = DartFileInfoKt.getDartFileInfo(myProject, filePathOrUri);

      final ProgressIndicator indicator = myProgressIndicator;
      if (indicator != null && fileInfo instanceof DartLocalFileInfo localFileInfo) {
        String fileName = PathUtil.getFileName(localFileInfo.getFilePath());
        indicator.setText(DartBundle.message("dart.analysis.progress.with.file", fileName));
      }

      final List<AnalysisError> errorsWithoutTodo = errors.isEmpty() ? Collections.emptyList() : new ArrayList<>(errors.size());
      boolean hasSevereProblems = false;

      for (AnalysisError error : errors) {
        if (AnalysisErrorSeverity.ERROR.equals(error.getSeverity())) {
          hasSevereProblems = true;
        }
        if (!AnalysisErrorType.TODO.equals(error.getType())) {
          errorsWithoutTodo.add(error);
        }
      }

      int newHash = errorsWithoutTodo.isEmpty() ? 0 : ensureNotZero(errorsWithoutTodo.hashCode());

      if (fileInfo instanceof DartLocalFileInfo localFileInfo) {
        int oldHash;
        synchronized (myFilePathsWithErrors) {
          // TObjectIntHashMap returns 0 if there's no such entry, it's equivalent to empty error set for this file
          oldHash = myFilePathToErrorsHash.getInt(localFileInfo.getFilePath());
        }

        // do nothing if errors are the same as were already handled previously
        if (oldHash == newHash && myServerData.isErrorInfoUpToDate(localFileInfo)) return;
      }

      boolean restartHighlighting =
        fileInfo instanceof DartLocalFileInfo localFileInfo && myVisibleFileUris.contains(getLocalFileUri(localFileInfo.getFilePath()))
        ||
        fileInfo instanceof DartNotLocalFileInfo notLocalFileInfo && myVisibleFileUris.contains(notLocalFileInfo.getFileUri());

      if (myServerData.computedErrors(fileInfo, errorsWithoutTodo, restartHighlighting)) {
        if (fileInfo instanceof DartLocalFileInfo localFileInfo) {
          onErrorsUpdated(localFileInfo, errorsWithoutTodo, hasSevereProblems, newHash);
        }
      }
    }

    @Override
    public void computedHighlights(@NotNull String filePathOrUri, @NotNull List<HighlightRegion> regions) {
      DartFileInfo fileInfo = DartFileInfoKt.getDartFileInfo(myProject, filePathOrUri);
      myServerData.computedHighlights(fileInfo, regions);
    }

    @Override
    public void computedClosingLabels(@NotNull String filePathOrUri, @NotNull List<ClosingLabel> labels) {
      DartFileInfo fileInfo = DartFileInfoKt.getDartFileInfo(myProject, filePathOrUri);
      myServerData.computedClosingLabels(fileInfo, labels);
    }

    @Override
    public void computedImplemented(@NotNull String filePathOrUri,
                                    @NotNull List<ImplementedClass> implementedClasses,
                                    @NotNull List<ImplementedMember> implementedMembers) {
      DartFileInfo fileInfo = DartFileInfoKt.getDartFileInfo(myProject, filePathOrUri);
      myServerData.computedImplemented(fileInfo, implementedClasses, implementedMembers);
    }

    @Override
    public void computedNavigation(@NotNull String filePathOrUri, @NotNull List<NavigationRegion> regions) {
      DartFileInfo fileInfo = DartFileInfoKt.getDartFileInfo(myProject, filePathOrUri);
      myServerData.computedNavigation(fileInfo, regions);
    }

    @Override
    public void computedOverrides(@NotNull String filePathOrUri, @NotNull List<OverrideMember> overrides) {
      DartFileInfo fileInfo = DartFileInfoKt.getDartFileInfo(myProject, filePathOrUri);
      myServerData.computedOverrides(fileInfo, overrides);
    }

    @Override
    public void computedOutline(@NotNull String filePathOrUri, @NotNull Outline outline) {
      DartFileInfo fileInfo = DartFileInfoKt.getDartFileInfo(myProject, filePathOrUri);
      myServerData.computedOutline(fileInfo, outline);
    }

    @Override
    public void flushedResults(@NotNull List<String> filePathsOrUris) {
      List<DartFileInfo> fileInfos = ContainerUtil.map(filePathsOrUris, pathOrUri -> DartFileInfoKt.getDartFileInfo(myProject, pathOrUri));

      myServerData.onFlushedResults(fileInfos);

      for (DartFileInfo fileInfo : fileInfos) {
        if (fileInfo instanceof DartLocalFileInfo localFileInfo) {
          onErrorsUpdated(localFileInfo, AnalysisError.EMPTY_LIST, false, 0);
        }
      }
    }

    @Override
    public void computedCompletion(final @NotNull String completionId,
                                   final int replacementOffset,
                                   final int replacementLength,
                                   final @NotNull List<CompletionSuggestion> completions,
                                   final @NotNull List<IncludedSuggestionSet> includedSuggestionSets,
                                   final @NotNull List<String> includedElementKinds,
                                   final @NotNull List<IncludedSuggestionRelevanceTag> includedSuggestionRelevanceTags,
                                   final boolean isLast,
                                   final @Nullable String libraryFilePathSD) {
      synchronized (myCompletionInfos) {
        myCompletionInfos.add(new CompletionInfo(completionId, replacementOffset, replacementLength, completions, includedSuggestionSets,
                                                 includedElementKinds, includedSuggestionRelevanceTags, isLast, libraryFilePathSD));
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
      // completion_setSubscriptions() are handled here instead of in startServer() as the server version isn't known until this
      // serverConnected() call.
      if (myServer != null && !shouldUseCompletion2()) {
        myServer.completion_setSubscriptions(List.of(CompletionService.AVAILABLE_SUGGESTION_SETS));
      }
    }

    @Override
    public void requestError(RequestError error) {
      if (RequestErrorCode.SERVER_ERROR.equals(error.getCode())) {
        serverError(false, error.getMessage(), error.getStackTrace());
      }
      else {
        LOG.info(getShortErrorMessage("unknown", null, error));
      }
    }

    @Override
    public void serverError(boolean isFatal, @Nullable String message, @NonNls @Nullable String stackTrace) {
      if (message == null) {
        message = DartBundle.message("issue.occurred.with.analysis.server");
      }
      if (!isFatal &&
          stackTrace != null &&
          stackTrace.startsWith("#0      checkValidPackageUri (package:package_config/src/util.dart:72)")) {
        return;
      }

      String sdkVersion = mySdkVersion.isEmpty() ? null : mySdkVersion;
      StringBuilder debugLog = new StringBuilder();
      synchronized (myDebugLog) {
        for (String s : myDebugLog) {
          debugLog.append(s).append('\n');
        }
      }

      myServerErrorHandler.handleError(message, stackTrace, isFatal, sdkVersion, debugLog.isEmpty() ? null : debugLog.toString());
    }

    @Override
    public void serverStatus(final @Nullable AnalysisStatus analysisStatus, final @Nullable PubStatus pubStatus) {
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

    @Override
    public void lspTextDocumentContentDidChange(@NotNull String fileUri) {
      flushedResults(List.of(fileUri));
      myServerData.textDocumentContentDidChange(fileUri);
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
        public void run(final @NotNull ProgressIndicator indicator) {
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
  public int getConvertedOffset(final @Nullable VirtualFile file, final int originalOffset) {
    if (originalOffset <= 0 || file == null || !file.isInLocalFileSystem()) return originalOffset;
    return myFilePathWithOverlaidContentToTimestamp.containsKey(file.getPath())
           ? originalOffset
           : FileOffsetsManager.getInstance().getConvertedOffset(file, originalOffset);
  }

  /**
   * Must use it right before sending any offsets and lengths to the AnalysisServer
   */
  public int getOriginalOffset(final @Nullable VirtualFile file, final int convertedOffset) {
    if (file == null || !file.isInLocalFileSystem()) return convertedOffset;

    return myFilePathWithOverlaidContentToTimestamp.containsKey(file.getPath())
           ? convertedOffset
           : FileOffsetsManager.getInstance().getOriginalOffset(file, convertedOffset);
  }

  public int[] getConvertedOffsets(final @NotNull VirtualFile file, final int[] _offsets) {
    final int[] offsets = new int[_offsets.length];
    for (int i = 0; i < _offsets.length; i++) {
      offsets[i] = getConvertedOffset(file, _offsets[i]);
    }
    return offsets;
  }

  public int[] getConvertedLengths(final @NotNull VirtualFile file, final int[] _offsets, final int[] _lengths) {
    final int[] offsets = getConvertedOffsets(file, _offsets);
    final int[] lengths = new int[_lengths.length];
    for (int i = 0; i < _lengths.length; i++) {
      lengths[i] = getConvertedOffset(file, _offsets[i] + _lengths[i]) - offsets[i];
    }
    return lengths;
  }

  public static boolean isDartSdkVersionSufficient(final @NotNull DartSdk sdk) {
    return StringUtil.compareVersionNumbers(sdk.getVersion(), MIN_SDK_VERSION) >= 0;
  }

  public static boolean isDartSdkVersionSufficientForMoveFileRefactoring(final @NotNull DartSdk sdk) {
    return StringUtil.compareVersionNumbers(sdk.getVersion(), MIN_MOVE_FILE_SDK_VERSION) >= 0;
  }

  public static boolean isDartSdkVersionSufficientForWebdev(final @NotNull DartSdk sdk) {
    return StringUtil.compareVersionNumbers(sdk.getVersion(), MIN_WEBDEV_SDK_VERSION) >= 0;
  }

  public static boolean isDartSdkVersionSufficientForPackageConfigJson(final @NotNull DartSdk sdk) {
    return StringUtil.compareVersionNumbers(sdk.getVersion(), MIN_PACKAGE_CONFIG_JSON_SDK_VERSION) >= 0;
  }

  public static boolean isDartSdkVersionSufficientForDartLangServer(final @NotNull DartSdk sdk) {
    return StringUtil.compareVersionNumbers(sdk.getVersion(), MIN_DART_LANG_SERVER_SDK_VERSION) >= 0;
  }

  public static boolean isDartSdkVersionSufficientForFileUri(@NotNull String sdkVersion) {
    return Registry.is("dart.macros.support", false) && StringUtil.compareVersionNumbers(sdkVersion, MIN_FILE_URI_SDK_VERSION) >= 0;
  }

  public boolean shouldUseCompletion2() {
    return StringUtil.compareVersionNumbers(getServerVersion(), COMPLETION_2_SERVER_VERSION) >= 0;
  }

  public void addCompletions(final @NotNull VirtualFile file,
                             final @NotNull String completionId,
                             final @NotNull CompletionSuggestionConsumer consumer,
                             final @NotNull CompletionLibraryRefConsumer libraryRefConsumer) {
    while (true) {
      ProgressManager.checkCanceled();

      synchronized (myCompletionInfos) {
        CompletionInfo completionInfo;
        while ((completionInfo = myCompletionInfos.poll()) != null) {
          if (!completionInfo.myCompletionId.equals(completionId)) continue;
          if (!completionInfo.isLast) continue;

          for (final CompletionSuggestion completion : completionInfo.myCompletions) {
            final int convertedReplacementOffset = getConvertedOffset(file, completionInfo.myOriginalReplacementOffset);
            consumer.consumeCompletionSuggestion(convertedReplacementOffset, completionInfo.myReplacementLength, completion);
          }

          final Set<String> includedKinds = Sets.newHashSet(completionInfo.myIncludedElementKinds);
          final Map<String, IncludedSuggestionRelevanceTag> includedRelevanceTags = new HashMap<>();
          for (IncludedSuggestionRelevanceTag includedRelevanceTag : completionInfo.myIncludedSuggestionRelevanceTags) {
            includedRelevanceTags.put(includedRelevanceTag.getTag(), includedRelevanceTag);
          }
          for (final IncludedSuggestionSet includedSet : completionInfo.myIncludedSuggestionSets) {
            libraryRefConsumer.consumeLibraryRef(includedSet, includedKinds, includedRelevanceTags, completionInfo.myLibraryFilePathSD);
          }

          for (DartCompletionTimerExtension extension : DartCompletionTimerExtension.getExtensions()) {
            extension.dartCompletionEnd();
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
    private final @Nullable List<SourceEdit> myEdits;
    private final int myOffset;
    private final int myLength;

    public FormatResult(final @Nullable List<SourceEdit> edits, final int selectionOffset, final int selectionLength) {
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

    public @Nullable List<SourceEdit> getEdits() {
      return myEdits;
    }
  }

  public DartAnalysisServerService(@NotNull Project project, @NotNull CoroutineScope serviceScope) {
    myProject = project;
    myServiceScope = serviceScope;
    myRootsHandler = new DartServerRootsHandler(project);
    myServerData = new DartServerData(this);
    myUpdateFilesAlarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, this);
    myShowServerProgressAlarm = new Alarm(this);
    myServerErrorHandler = new DartAnalysisServerErrorHandler(project);

    DartClosingLabelManager.getInstance().addListener(this::handleClosingLabelPreferenceChanged, this);
  }

  public @NotNull CoroutineScope getServiceScope() {
    return myServiceScope;
  }

  @SuppressWarnings("unused") // for Flutter plugin
  public void addAnalysisServerListener(final @NotNull AnalysisServerListener serverListener) {
    if (!myAdditionalServerListeners.contains(serverListener)) {
      myAdditionalServerListeners.add(serverListener);
      if (myServer != null && isServerProcessActive()) {
        myServer.addAnalysisServerListener(serverListener);
      }
    }
  }

  @SuppressWarnings("unused") // for Flutter plugin
  public void removeAnalysisServerListener(final @NotNull AnalysisServerListener serverListener) {
    myAdditionalServerListeners.remove(serverListener);
    if (myServer != null) {
      myServer.removeAnalysisServerListener(serverListener);
    }
  }

  @SuppressWarnings("unused") // for Flutter plugin
  public void addRequestListener(final @NotNull RequestListener requestListener) {
    if (!myRequestListeners.contains(requestListener)) {
      myRequestListeners.add(requestListener);
      if (myServer != null && isServerProcessActive()) {
        myServer.addRequestListener(requestListener);
      }
    }
  }

  @SuppressWarnings("unused") // for Flutter plugin
  public void removeRequestListener(final @NotNull RequestListener requestListener) {
    myRequestListeners.remove(requestListener);
    if (myServer != null) {
      myServer.removeRequestListener(requestListener);
    }
  }

  @SuppressWarnings("unused") // for Flutter plugin
  public void addResponseListener(final @NotNull ResponseListener responseListener) {
    if (!myResponseListeners.contains(responseListener)) {
      myResponseListeners.add(responseListener);
      if (myServer != null && isServerProcessActive()) {
        myServer.addResponseListener(responseListener);
      }
    }
  }

  @SuppressWarnings("unused") // for Flutter plugin
  public void removeResponseListener(final @NotNull ResponseListener responseListener) {
    myResponseListeners.remove(responseListener);
    if (myServer != null) {
      myServer.removeResponseListener(responseListener);
    }
  }

  @SuppressWarnings("unused") // for Flutter plugin
  public void addQuickAssistIntentionListener(@NotNull DartQuickAssistIntentionListener listener) {
    if (!myQuickAssistIntentionListeners.contains(listener)) {
      myQuickAssistIntentionListeners.add(listener);
    }
  }

  @SuppressWarnings("unused") // for Flutter plugin
  public void removeQuickAssistIntentionListener(@NotNull DartQuickAssistIntentionListener listener) {
    myQuickAssistIntentionListeners.remove(listener);
  }

  public void fireBeforeQuickAssistIntentionInvoked(@NotNull DartQuickAssistIntention intention,
                                                    @NotNull Editor editor,
                                                    @NotNull PsiFile file) {
    try {
      myQuickAssistIntentionListeners.forEach(listener -> listener.beforeQuickAssistIntentionInvoked(intention, editor, file));
    }
    catch (Throwable t) {
      LOG.error(t);
    }
  }

  @SuppressWarnings("unused") // for Flutter plugin
  public void addQuickFixListener(@NotNull DartQuickFixListener listener) {
    if (!myQuickFixListeners.contains(listener)) {
      myQuickFixListeners.add(listener);
    }
  }

  @SuppressWarnings("unused") // for Flutter plugin
  public void removeQuickFixListener(@NotNull DartQuickFixListener listener) {
    myQuickFixListeners.remove(listener);
  }

  public void fireBeforeQuickFixInvoked(@NotNull DartQuickFix fix, @NotNull Editor editor, @NotNull PsiFile file) {
    try {
      myQuickFixListeners.forEach(listener -> listener.beforeQuickFixInvoked(fix, editor, file));
    }
    catch (Throwable t) {
      LOG.error(t);
    }
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
      public void fileOpened(final @NotNull FileEditorManager source, final @NotNull VirtualFile file) {
        if (PubspecYamlUtil.PUBSPEC_YAML.equals(file.getName()) ||
            FileTypeRegistry.getInstance().isFileOfType(file, DartFileType.INSTANCE)) {
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
      public void fileClosed(final @NotNull FileEditorManager source, final @NotNull VirtualFile file) {
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
    final DocumentListener documentListener = new DocumentListener() {
      @Override
      public void beforeDocumentChange(@NotNull DocumentEvent e) {
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

    EditorFactory.getInstance().getEventMulticaster().addDocumentListener(documentListener, this);
  }

  public static @NotNull DartAnalysisServerService getInstance(final @NotNull Project project) {
    return project.getService(DartAnalysisServerService.class);
  }

  public @NotNull String getSdkVersion() {
    return mySdkVersion;
  }

  public @NotNull String getServerVersion() {
    return myServerVersion;
  }

  public @NotNull Project getProject() {
    return myProject;
  }

  @Override
  public void dispose() {
    myDisposed = true;
    stopServer();
  }

  public @NotNull Condition<?> getDisposedCondition() {
    return myDisposedCondition;
  }

  private void handleClosingLabelPreferenceChanged() {
    analysis_setSubscriptions();
  }

  public @Nullable AvailableSuggestionSet getAvailableSuggestionSet(int id) {
    return myServerData.getAvailableSuggestionSet(id);
  }

  public @Nullable Map<String, Map<String, Set<String>>> getExistingImports(@Nullable String filePathSD) {
    return myServerData.getExistingImports(filePathSD);
  }

  public @NotNull List<DartServerData.DartError> getErrors(final @NotNull VirtualFile file) {
    return myServerData.getErrors(file);
  }

  public List<DartServerData.DartError> getErrors(final @NotNull SearchScope scope) {
    return myServerData.getErrors(scope);
  }

  public @NotNull List<DartServerData.DartHighlightRegion> getHighlight(final @NotNull VirtualFile file) {
    return myServerData.getHighlight(file);
  }

  public @NotNull List<DartServerData.DartNavigationRegion> getNavigation(final @NotNull VirtualFile file) {
    return myServerData.getNavigation(file);
  }

  public @NotNull List<DartServerData.DartOverrideMember> getOverrideMembers(final @NotNull VirtualFile file) {
    return myServerData.getOverrideMembers(file);
  }

  public @NotNull List<DartServerData.DartRegion> getImplementedClasses(final @NotNull VirtualFile file) {
    return myServerData.getImplementedClasses(file);
  }

  public @NotNull List<DartServerData.DartRegion> getImplementedMembers(final @NotNull VirtualFile file) {
    return myServerData.getImplementedMembers(file);
  }

  @Contract("null -> null")
  public @Nullable Outline getOutline(final @Nullable VirtualFile file) {
    if (file == null) return null;
    return myServerData.getOutline(file);
  }

  @Nullable
  VirtualFile getNotLocalVirtualFile(@NotNull String fileUri) {
    return myServerData.getNotLocalVirtualFile(fileUri);
  }

  void updateCurrentFile() {
    ModalityUiUtil.invokeLaterIfNeeded(ModalityState.nonModal(), myDisposedCondition,
                                       () -> DartProblemsView.getInstance(myProject).setCurrentFile(getCurrentOpenFile())
    );
  }

  public boolean isInIncludedRoots(final @Nullable VirtualFile vFile) {
    return myRootsHandler.isInIncludedRoots(vFile);
  }

  private @Nullable VirtualFile getCurrentOpenFile() {
    final VirtualFile[] files = FileEditorManager.getInstance(myProject).getSelectedFiles();
    if (files.length > 0 && files[0].isInLocalFileSystem()) {
      return files[0];
    }
    return null;
  }

  public void updateVisibleFiles() {
    ApplicationManager.getApplication().assertReadAccessAllowed();

    synchronized (myLock) {
      final List<String> newVisibleFileUris = new ArrayList<>();

      for (VirtualFile file : FileEditorManager.getInstance(myProject).getSelectedFiles()) {
        if (isLocalAnalyzableFile(file)) {
          newVisibleFileUris.add(getFileUri(file));
        }
      }

      if (!Comparing.haveEqualElements(myVisibleFileUris, newVisibleFileUris)) {
        myVisibleFileUris.clear();
        myVisibleFileUris.addAll(newVisibleFileUris);
        analysis_setPriorityFiles();
        analysis_setSubscriptions();
      }
    }
  }

  /**
   * Return true if the given file can be analyzed by Dart Analysis Server.
   */
  @Contract("null->false")
  public static boolean isLocalAnalyzableFile(final @Nullable VirtualFile file) {
    if (file == null) return false;

    return file.getUserData(DartFileInfoKt.DART_NOT_LOCAL_FILE_URI_KEY) != null ||
           file.isInLocalFileSystem() && isFileNameRespectedByAnalysisServer(file.getName());
  }

  public static boolean isFileNameRespectedByAnalysisServer(@NotNull String _fileName) {
    // see https://github.com/dart-lang/sdk/blob/master/pkg/analyzer/lib/src/generated/engine.dart (class AnalysisEngine)
    // and AbstractAnalysisServer.analyzableFilePatterns
    @NonNls String fileName = _fileName.toLowerCase(Locale.US);
    return fileName.endsWith(".dart") ||
           fileName.endsWith(".htm") ||
           fileName.endsWith(".html") ||
           fileName.equals("analysis_options.yaml") ||
           fileName.equals("pubspec.yaml") ||
           fileName.equals("fix_data.yaml") ||
           fileName.equals("androidmanifest.xml");
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

    final Map<String, Object> fileUriToContentOverlay = new HashMap<>();
    final Set<String> filePathsToRemoveContentOverlay;

    ApplicationManager.getApplication().assertReadAccessAllowed();

    synchronized (myLock) {
      final Set<String> oldTrackedFilePaths = new HashSet<>(myFilePathWithOverlaidContentToTimestamp.keySet());

      final FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();

      // some documents in myChangedDocuments may be updated by external change, such as switch branch, that's why we track them,
      // getUnsavedDocuments() is not enough, we must make sure that overlaid content is sent for myChangedDocuments as well (to trigger DAS notifications)
      final Set<Document> documents = new HashSet<>(myChangedDocuments);
      myChangedDocuments.clear();
      ContainerUtil.addAll(documents, fileDocumentManager.getUnsavedDocuments());

      for (Document document : documents) {
        final VirtualFile file = fileDocumentManager.getFile(document);
        if (isLocalAnalyzableFile(file) && file.isInLocalFileSystem()) {
          oldTrackedFilePaths.remove(file.getPath());

          final Long oldTimestamp = myFilePathWithOverlaidContentToTimestamp.get(file.getPath());
          if (oldTimestamp == null || document.getModificationStamp() != oldTimestamp) {
            fileUriToContentOverlay.put(getLocalFileUri(file.getPath()), new AddContentOverlay(document.getText()));
            myFilePathWithOverlaidContentToTimestamp.put(file.getPath(), document.getModificationStamp());
          }
        }
      }

      // oldTrackedFilePaths at this point contains only those files that are not in FileDocumentManager.getUnsavedDocuments() anymore
      filePathsToRemoveContentOverlay = Collections.unmodifiableSet(oldTrackedFilePaths);
      for (String oldPath : filePathsToRemoveContentOverlay) {
        if (myFilePathWithOverlaidContentToTimestamp.get(oldPath) != null) {
          fileUriToContentOverlay.put(getLocalFileUri(oldPath), new RemoveContentOverlay());
        }
      }

      if (LOG.isDebugEnabled()) {
        final Set<String> overlaidFileUris = new HashSet<>(fileUriToContentOverlay.keySet());
        for (String filePathToRemoveContentOverlay : filePathsToRemoveContentOverlay) {
          overlaidFileUris.remove(getLocalFileUri(filePathToRemoveContentOverlay));
        }
        if (!overlaidFileUris.isEmpty()) {
          LOG.debug("Sending overlaid content: " + StringUtil.join(overlaidFileUris, ",\n"));
        }

        if (!filePathsToRemoveContentOverlay.isEmpty()) {
          LOG.debug("Removing overlaid content: " + StringUtil.join(filePathsToRemoveContentOverlay, ",\n"));
        }
      }
    }

    if (!fileUriToContentOverlay.isEmpty()) {
      server.analysis_updateContent(fileUriToContentOverlay, () -> {
        synchronized (myFilePathWithOverlaidContentToTimestamp) {
          filePathsToRemoveContentOverlay.forEach(myFilePathWithOverlaidContentToTimestamp::remove);
        }
        myServerData.onFilesContentUpdated();
      });
    }
  }

  public void ensureAnalysisRootsUpToDate() {
    myRootsHandler.scheduleDartRootsUpdate(null);
  }

  boolean setAnalysisRoots(@NotNull List<String> includedRootPaths, @NotNull List<String> excludedRootPaths) {
    AnalysisServer server = myServer;
    if (server == null) {
      return false;
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("analysis_setAnalysisRoots, included:\n" + StringUtil.join(includedRootPaths, ",\n") +
                "\nexcluded:\n" + StringUtil.join(excludedRootPaths, ",\n"));
    }

    List<String> includedRootUris = ContainerUtil.map(includedRootPaths, this::getLocalFileUri);
    List<String> excludedRootUris = ContainerUtil.map(excludedRootPaths, this::getLocalFileUri);
    server.analysis_setAnalysisRoots(includedRootUris, excludedRootUris, null);
    return true;
  }

  private void onErrorsUpdated(@NotNull DartLocalFileInfo localFileInfo,
                               @NotNull List<? extends AnalysisError> errors,
                               boolean hasSevereProblems,
                               int errorsHash) {
    String filePath = localFileInfo.getFilePath();
    updateFilesWithErrorsSet(filePath, hasSevereProblems, errorsHash);
    DartProblemsView.getInstance(myProject).updateErrorsForFile(filePath, errors);
  }

  private void updateFilesWithErrorsSet(final @NotNull String filePath, final boolean hasSevereProblems, final int errorsHash) {
    synchronized (myFilePathsWithErrors) {
      if (errorsHash == 0) {
        // no errors
        myFilePathToErrorsHash.removeInt(filePath);
      }
      else {
        myFilePathToErrorsHash.put(filePath, errorsHash);
      }

      if (hasSevereProblems) {
        if (myFilePathsWithErrors.add(filePath)) {
          String parentPath = PathUtil.getParentPath(filePath);
          while (!parentPath.isEmpty()) {
            final int count = myFolderPathsWithErrors.getInt(parentPath); // returns zero if there were no path in the map
            myFolderPathsWithErrors.put(parentPath, count + 1);
            parentPath = PathUtil.getParentPath(parentPath);
          }
        }
      }
      else {
        if (myFilePathsWithErrors.remove(filePath)) {
          String parentPath = PathUtil.getParentPath(filePath);
          while (!parentPath.isEmpty()) {
            final int count = myFolderPathsWithErrors.removeInt(parentPath); // returns zero if there was no path in the map
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

    if (myInitializationOnServerStartupDone) {
      DartProblemsView.getInstance(myProject).clearAll();
    }
  }

  public @NotNull List<HoverInformation> analysis_getHover(final @NotNull VirtualFile file, final int _offset) {
    final AnalysisServer server = myServer;
    if (server == null) {
      return HoverInformation.EMPTY_LIST;
    }

    final String fileUri = getFileUri(file);
    final List<HoverInformation> result = new ArrayList<>();

    final CountDownLatch latch = new CountDownLatch(1);
    final int offset = getOriginalOffset(file, _offset);
    server.analysis_getHover(fileUri, offset, new GetHoverConsumer() {
      @Override
      public void computedHovers(HoverInformation[] hovers) {
        Collections.addAll(result, hovers);
        latch.countDown();
      }

      @Override
      public void onError(RequestError error) {
        logError("analysis_getHover()", fileUri, error);
        latch.countDown();
      }
    });

    awaitForLatchCheckingCanceled(server, latch, GET_HOVER_TIMEOUT);

    if (latch.getCount() > 0) {
      logTookTooLongMessage("analysis_getHover", GET_HOVER_TIMEOUT, fileUri);
    }
    return result;
  }

  public @Nullable List<DartServerData.DartNavigationRegion> analysis_getNavigation(final @NotNull VirtualFile file,
                                                                                    final int _offset,
                                                                                    final int length) {
    final AnalysisServer server = myServer;
    if (server == null) {
      return null;
    }

    final String fileUri = getFileUri(file);
    final Ref<List<DartServerData.DartNavigationRegion>> resultRef = Ref.create();

    final CountDownLatch latch = new CountDownLatch(1);
    LOG.debug("analysis_getNavigation(" + fileUri + ")");

    final int offset = getOriginalOffset(file, _offset);
    server.analysis_getNavigation(fileUri, offset, length, new GetNavigationConsumer() {
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
          LOG.info(getShortErrorMessage("analysis_getNavigation()", fileUri, error));
        }
        else {
          logError("analysis_getNavigation()", fileUri, error);
        }

        latch.countDown();
      }
    });

    awaitForLatchCheckingCanceled(server, latch, GET_NAVIGATION_TIMEOUT);

    if (latch.getCount() > 0) {
      logTookTooLongMessage("analysis_getNavigation", GET_NAVIGATION_TIMEOUT, fileUri);
    }

    return resultRef.get();
  }

  public @NotNull List<SourceChange> edit_getAssists(final @NotNull VirtualFile file, final int _offset, final int _length) {
    if (!file.isInLocalFileSystem()) return Collections.emptyList();

    final AnalysisServer server = myServer;
    if (server == null) {
      return Collections.emptyList();
    }

    final String fileUri = getLocalFileUri(file.getPath());
    final List<SourceChange> results = new ArrayList<>();
    final CountDownLatch latch = new CountDownLatch(1);
    final int offset = getOriginalOffset(file, _offset);
    final int length = getOriginalOffset(file, _offset + _length) - offset;
    server.edit_getAssists(fileUri, offset, length, new GetAssistsConsumer() {
      @Override
      public void computedSourceChanges(List<SourceChange> sourceChanges) {
        results.addAll(sourceChanges);
        latch.countDown();
      }

      @Override
      public void onError(final RequestError error) {
        logError("edit_getAssists()", fileUri, error);
        latch.countDown();
      }
    });

    final long timeout = ApplicationManager.getApplication().isDispatchThread() ? GET_ASSISTS_TIMEOUT_EDT : GET_ASSISTS_TIMEOUT;

    awaitForLatchCheckingCanceled(server, latch, timeout);

    if (latch.getCount() > 0) {
      logTookTooLongMessage("edit_getAssists", timeout, fileUri);
    }
    return results;
  }

  public boolean edit_isPostfixCompletionApplicable(VirtualFile file, int _offset, String key) {
    if (!file.isInLocalFileSystem()) return false;

    final AnalysisServer server = myServer;
    if (server == null) {
      return false;
    }

    final String fileUri = getLocalFileUri(file.getPath());
    final Ref<Boolean> resultRef = Ref.create(false);
    final CountDownLatch latch = new CountDownLatch(1);
    final int offset = getOriginalOffset(file, _offset);
    server.edit_isPostfixCompletionApplicable(fileUri, key, offset, new IsPostfixCompletionApplicableConsumer() {
      @Override
      public void isPostfixCompletionApplicable(Boolean value) {
        resultRef.set(value);
        latch.countDown();
      }
    });

    awaitForLatchCheckingCanceled(server, latch, POSTFIX_COMPLETION_TIMEOUT);

    if (latch.getCount() > 0) {
      logTookTooLongMessage("edit_isPostfixCompletionApplicable", POSTFIX_COMPLETION_TIMEOUT, fileUri);
    }
    return resultRef.get();
  }

  public PostfixTemplateDescriptor @Nullable [] edit_listPostfixCompletionTemplates() {
    final AnalysisServer server = myServer;
    if (server == null) {
      return null;
    }

    if (StringUtil.compareVersionNumbers(mySdkVersion, "1.25") < 0) {
      return PostfixTemplateDescriptor.EMPTY_ARRAY;
    }

    final Ref<PostfixTemplateDescriptor[]> resultRef = Ref.create();
    final CountDownLatch latch = new CountDownLatch(1);
    server.edit_listPostfixCompletionTemplates(new ListPostfixCompletionTemplatesConsumer() {
      @Override
      public void postfixCompletionTemplates(PostfixTemplateDescriptor[] templates) {
        resultRef.set(templates);
        latch.countDown();
      }

      @Override
      public void onError(RequestError error) {
        if (!RequestErrorCode.UNKNOWN_REQUEST.equals(error.getCode())) {
          logError("edit_listPostfixCompletionTemplates()", null, error);
        }
        latch.countDown();
      }
    });

    awaitForLatchCheckingCanceled(server, latch, POSTFIX_INITIALIZATION_TIMEOUT);

    if (latch.getCount() > 0) {
      logTookTooLongMessage("edit_listPostfixCompletionTemplates", POSTFIX_INITIALIZATION_TIMEOUT, null);
    }

    return resultRef.get();
  }

  public @Nullable SourceChange edit_getPostfixCompletion(final @NotNull VirtualFile file, final int _offset, final String key) {
    if (!file.isInLocalFileSystem()) return null;

    final AnalysisServer server = myServer;
    if (server == null) {
      return null;
    }

    final String fileUri = getLocalFileUri(file.getPath());
    final Ref<SourceChange> resultRef = Ref.create();
    final CountDownLatch latch = new CountDownLatch(1);
    final int offset = getOriginalOffset(file, _offset);
    server.edit_getPostfixCompletion(fileUri, key, offset, new GetPostfixCompletionConsumer() {
      @Override
      public void computedSourceChange(SourceChange sourceChange) {
        resultRef.set(sourceChange);
        latch.countDown();
      }

      @Override
      public void onError(RequestError error) {
        logError("edit_getPostfixCompletion()", fileUri, error);
        latch.countDown();
      }
    });

    awaitForLatchCheckingCanceled(server, latch, POSTFIX_COMPLETION_TIMEOUT);

    if (latch.getCount() > 0) {
      logTookTooLongMessage("edit_getPostfixCompletion", POSTFIX_COMPLETION_TIMEOUT, fileUri);
    }
    return resultRef.get();
  }

  public @Nullable SourceChange edit_getStatementCompletion(final @NotNull VirtualFile file, final int _offset) {
    if (!file.isInLocalFileSystem()) return null;

    final AnalysisServer server = myServer;
    if (server == null) {
      return null;
    }

    final String fileUri = getLocalFileUri(file.getPath());
    final Ref<SourceChange> resultRef = Ref.create();
    final CountDownLatch latch = new CountDownLatch(1);
    final int offset = getOriginalOffset(file, _offset);
    server.edit_getStatementCompletion(fileUri, offset, new GetStatementCompletionConsumer() {
      @Override
      public void computedSourceChange(SourceChange sourceChange) {
        resultRef.set(sourceChange);
        latch.countDown();
      }

      @Override
      public void onError(RequestError error) {
        latch.countDown();
        logError("edit_getStatementCompletion()", fileUri, error);
      }
    });

    awaitForLatchCheckingCanceled(server, latch, STATEMENT_COMPLETION_TIMEOUT);

    if (latch.getCount() > 0) {
      logTookTooLongMessage("edit_getStatementCompletion", STATEMENT_COMPLETION_TIMEOUT, fileUri);
    }
    return resultRef.get();
  }

  public void diagnostic_getServerPort(GetServerPortConsumer consumer) {
    final AnalysisServer server = myServer;
    if (server == null) {
      consumer.onError(new RequestError(ExtendedRequestErrorCode.INVALID_SERVER_RESPONSE,
                                        DartBundle.message("analysis.server.not.running"), null));
    }
    else {
      server.diagnostic_getServerPort(consumer);
    }
  }

  /**
   * If server responds in less than {@code GET_FIXES_TIMEOUT_EDT} / {@code GET_FIXES_TIMEOUT} then this method can be considered synchronous: when exiting this method
   * {@code consumer} is already notified. Otherwise, this method is async.
   */
  public void askForFixesAndWaitABitIfReceivedQuickly(final @NotNull VirtualFile file,
                                                      final int _offset,
                                                      final @NotNull Consumer<? super List<AnalysisErrorFixes>> consumer) {
    if (!file.isInLocalFileSystem()) {
      consumer.consume(Collections.emptyList());
      return;
    }

    final AnalysisServer server = myServer;
    if (server == null) {
      consumer.consume(Collections.emptyList());
      return;
    }

    final String fileUri = getLocalFileUri(file.getPath());
    final CountDownLatch latch = new CountDownLatch(1);
    final int offset = getOriginalOffset(file, _offset);
    server.edit_getFixes(fileUri, offset, new GetFixesConsumer() {
      @Override
      public void computedFixes(final List<AnalysisErrorFixes> fixes) {
        consumer.consume(fixes);
        latch.countDown();
      }

      @Override
      public void onError(final RequestError error) {
        logError("edit_getFixes()", fileUri, error);
        latch.countDown();
      }
    });

    final long timeout = ApplicationManager.getApplication().isDispatchThread() ? GET_FIXES_TIMEOUT_EDT : GET_FIXES_TIMEOUT;

    awaitForLatchCheckingCanceled(server, latch, timeout);

    if (latch.getCount() > 0) {
      logTookTooLongMessage("edit_getFixes", timeout, fileUri);
    }
  }

  public void search_findElementReferences(final @NotNull VirtualFile file,
                                           final int _offset,
                                           final @NotNull Consumer<? super SearchResult> consumer) {
    final AnalysisServer server = myServer;
    if (server == null) {
      return;
    }

    final String fileUri = getFileUri(file);
    final Ref<String> searchIdRef = new Ref<>();

    final CountDownLatch latch = new CountDownLatch(1);
    final int offset = getOriginalOffset(file, _offset);
    server.search_findElementReferences(fileUri, offset, true, new FindElementReferencesConsumer() {
      @Override
      public void computedElementReferences(String searchId, Element element) {
        searchIdRef.set(searchId);
        latch.countDown();
      }

      @Override
      public void onError(RequestError error) {
        LOG.info(getShortErrorMessage("search_findElementReferences()", fileUri, error));
        latch.countDown();
      }
    });

    awaitForLatchCheckingCanceled(server, latch, FIND_ELEMENT_REFERENCES_TIMEOUT);

    if (latch.getCount() > 0) {
      logTookTooLongMessage("search_findElementReferences", FIND_ELEMENT_REFERENCES_TIMEOUT, fileUri + "@" + offset);
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

  public @NotNull List<TypeHierarchyItem> search_getTypeHierarchy(final @NotNull VirtualFile file, final int _offset, final boolean superOnly) {
    final List<TypeHierarchyItem> results = new ArrayList<>();
    final AnalysisServer server = myServer;
    if (server == null) {
      return results;
    }

    final String fileUri = getFileUri(file);
    final CountDownLatch latch = new CountDownLatch(1);
    final int offset = getOriginalOffset(file, _offset);
    server.search_getTypeHierarchy(fileUri, offset, superOnly, new GetTypeHierarchyConsumer() {
      @Override
      public void computedHierarchy(List<TypeHierarchyItem> hierarchyItems) {
        results.addAll(hierarchyItems);
        latch.countDown();
      }

      @Override
      public void onError(RequestError error) {
        logError("search_getTypeHierarchy()", fileUri, error);
        latch.countDown();
      }
    });

    awaitForLatchCheckingCanceled(server, latch, GET_TYPE_HIERARCHY_TIMEOUT);

    if (latch.getCount() > 0) {
      logTookTooLongMessage("search_getTypeHierarchy", GET_TYPE_HIERARCHY_TIMEOUT, fileUri);
    }
    return results;
  }

  public @Nullable Pair<String, SourceChange> completion_getSuggestionDetails(@NotNull VirtualFile file,
                                                                              int id,
                                                                              String label,
                                                                              int _offset) {
    if (!file.isInLocalFileSystem()) return null;

    final AnalysisServer server = myServer;
    if (server == null) {
      return null;
    }

    final String fileUri = getLocalFileUri(file.getPath());
    final Ref<Pair<String, SourceChange>> resultRef = new Ref<>();
    final CountDownLatch latch = new CountDownLatch(1);
    final int offset = getOriginalOffset(file, _offset);
    server.completion_getSuggestionDetails(fileUri, id, label, offset, new GetSuggestionDetailsConsumer() {
      @Override
      public void computedDetails(String completion, SourceChange change) {
        resultRef.set(new Pair<>(completion, change));
        latch.countDown();
      }

      @Override
      public void onError(RequestError requestError) {
        latch.countDown();
      }
    });

    awaitForLatchCheckingCanceled(server, latch, GET_SUGGESTION_DETAILS_TIMEOUT);

    if (latch.getCount() > 0) {
      logTookTooLongMessage("completion_getSuggestionDetails", GET_SUGGESTION_DETAILS_TIMEOUT, fileUri);
    }
    return resultRef.get();
  }

  public @Nullable Pair<String, SourceChange> completion_getSuggestionDetails2(@NotNull VirtualFile file,
                                                                               int _offset,
                                                                               @NotNull String completion,
                                                                               @NotNull String libraryUri) {
    if (!file.isInLocalFileSystem()) return null;

    final AnalysisServer server = myServer;
    if (server == null) {
      return null;
    }

    final String fileUri = getLocalFileUri(file.getPath());
    final Ref<Pair<String, SourceChange>> resultRef = new Ref<>();
    final CountDownLatch latch = new CountDownLatch(1);
    final int offset = getOriginalOffset(file, _offset);
    server.completion_getSuggestionDetails2(fileUri, offset, completion, libraryUri, new GetSuggestionDetailsConsumer2() {
      @Override
      public void computedDetails(String completion, SourceChange change) {
        resultRef.set(new Pair<>(completion, change));
        latch.countDown();
      }

      @Override
      public void onError(RequestError requestError) {
        latch.countDown();
      }
    });

    awaitForLatchCheckingCanceled(server, latch, GET_SUGGESTION_DETAILS2_TIMEOUT);

    if (latch.getCount() > 0) {
      logTookTooLongMessage("completion_getSuggestionDetails2", GET_SUGGESTION_DETAILS2_TIMEOUT, fileUri);
    }
    return resultRef.get();
  }


  public @Nullable String completion_getSuggestions(final @NotNull VirtualFile file, final int _offset) {
    if (!file.isInLocalFileSystem()) return null;

    final AnalysisServer server = myServer;
    if (server == null) {
      return null;
    }

    for (DartCompletionTimerExtension extension : DartCompletionTimerExtension.getExtensions()) {
      extension.dartCompletionStart();
    }

    final String fileUri = getLocalFileUri(file.getPath());
    final Ref<String> resultRef = new Ref<>();
    final CountDownLatch latch = new CountDownLatch(1);
    final int offset = getOriginalOffset(file, _offset);
    server.completion_getSuggestions(fileUri, offset, new GetSuggestionsConsumer() {
      @Override
      public void computedCompletionId(final @NotNull String completionId) {
        resultRef.set(completionId);
        latch.countDown();
      }

      @Override
      public void onError(final @NotNull RequestError error) {
        for (DartCompletionTimerExtension extension : DartCompletionTimerExtension.getExtensions()) {
          extension.dartCompletionError(StringUtil.notNullize(error.getCode()), StringUtil.notNullize(error.getMessage()),
                                        StringUtil.notNullize(error.getStackTrace()));
        }
        // Not a problem. Happens if a file is outside the project, or server is just not ready yet.
        latch.countDown();
      }
    });

    awaitForLatchCheckingCanceled(server, latch, GET_SUGGESTIONS_TIMEOUT);

    if (latch.getCount() > 0) {
      logTookTooLongMessage("completion_getSuggestions", GET_SUGGESTIONS_TIMEOUT, fileUri);
    }

    return resultRef.get();
  }

  public @Nullable CompletionInfo2 completion_getSuggestions2(final @NotNull VirtualFile file,
                                                              final int _offset,
                                                              final int maxResults,
                                                              final String completionMode,
                                                              final int invocationCount) {
    if (!file.isInLocalFileSystem()) return null;

    final AnalysisServer server = myServer;
    if (server == null) {
      return null;
    }

    for (DartCompletionTimerExtension extension : DartCompletionTimerExtension.getExtensions()) {
      extension.dartCompletionStart();
    }

    final String fileUri = getLocalFileUri(file.getPath());
    final Ref<CompletionInfo2> resultRef = new Ref<>();
    final CountDownLatch latch = new CountDownLatch(1);
    final int offset = getOriginalOffset(file, _offset);

    final String completionCaseMatchingMode;
    final int caseSensitiveSetting = CodeInsightSettings.getInstance().getCompletionCaseSensitive();
    if (caseSensitiveSetting == CodeInsightSettings.FIRST_LETTER) {
      completionCaseMatchingMode = CompletionCaseMatchingMode.FIRST_CHAR;
    }
    else if (caseSensitiveSetting == CodeInsightSettings.NONE) {
      completionCaseMatchingMode = CompletionCaseMatchingMode.NONE;
    }
    else {
      completionCaseMatchingMode = CompletionCaseMatchingMode.ALL_CHARS;
    }

    server.completion_getSuggestions2(fileUri, offset, maxResults, completionCaseMatchingMode, completionMode, invocationCount, -1,
                                      new GetSuggestionsConsumer2() {
                                        @Override
                                        public void computedSuggestions(int replacementOffset,
                                                                        int replacementLength,
                                                                        List<CompletionSuggestion> suggestions,
                                                                        boolean isIncomplete) {
                                          resultRef.set(
                                            new CompletionInfo2(replacementOffset, replacementLength, suggestions, isIncomplete));
                                          latch.countDown();

                                          for (DartCompletionTimerExtension extension : DartCompletionTimerExtension.getExtensions()) {
                                            extension.dartCompletionEnd();
                                          }
                                        }

                                        @Override
                                        public void onError(final @NotNull RequestError error) {
                                          // Not a problem. Happens if a file is outside the project, or server is just not ready yet.
                                          latch.countDown();

                                          for (DartCompletionTimerExtension extension : DartCompletionTimerExtension.getExtensions()) {
                                            extension.dartCompletionError(StringUtil.notNullize(error.getCode()),
                                                                          StringUtil.notNullize(error.getMessage()),
                                                                          StringUtil.notNullize(error.getStackTrace()));
                                          }
                                        }
                                      });

    awaitForLatchCheckingCanceled(server, latch, GET_SUGGESTIONS_TIMEOUT);

    if (latch.getCount() > 0) {
      logTookTooLongMessage("completion_getSuggestions2", GET_SUGGESTIONS_TIMEOUT, fileUri);

      for (DartCompletionTimerExtension extension : DartCompletionTimerExtension.getExtensions()) {
        extension.dartCompletionEnd();
      }
    }

    return resultRef.get();
  }

  public @Nullable FormatResult edit_format(final @NotNull VirtualFile file,
                                            final int _selectionOffset,
                                            final int _selectionLength,
                                            final int lineLength) {
    if (!file.isInLocalFileSystem()) return null;

    final AnalysisServer server = myServer;
    if (server == null) {
      return null;
    }

    final String fileUri = getLocalFileUri(file.getPath());
    final Ref<FormatResult> resultRef = new Ref<>();

    final CountDownLatch latch = new CountDownLatch(1);
    final int selectionOffset = getOriginalOffset(file, _selectionOffset);
    final int selectionLength = getOriginalOffset(file, _selectionOffset + _selectionLength) - selectionOffset;
    server.edit_format(fileUri, selectionOffset, selectionLength, lineLength, new FormatConsumer() {
      @Override
      public void computedFormat(final List<SourceEdit> edits, final int selectionOffset, final int selectionLength) {
        resultRef.set(new FormatResult(edits, selectionOffset, selectionLength));
        latch.countDown();
      }

      @Override
      public void onError(final RequestError error) {
        if (RequestErrorCode.FORMAT_WITH_ERRORS.equals(error.getCode()) || RequestErrorCode.FORMAT_INVALID_FILE.equals(error.getCode())) {
          LOG.info(getShortErrorMessage("edit_format()", fileUri, error));
        }
        else {
          logError("edit_format()", fileUri, error);
        }

        latch.countDown();
      }
    });

    awaitForLatchCheckingCanceled(server, latch, EDIT_FORMAT_TIMEOUT);

    if (latch.getCount() > 0) {
      logTookTooLongMessage("edit_format", EDIT_FORMAT_TIMEOUT, fileUri);
    }

    return resultRef.get();
  }

  public @Nullable List<ImportedElements> analysis_getImportedElements(final @NotNull VirtualFile file,
                                                                       final int _selectionOffset,
                                                                       final int _selectionLength) {
    final AnalysisServer server = myServer;
    if (server == null || StringUtil.compareVersionNumbers(mySdkVersion, "1.25") < 0) {
      return null;
    }

    final String fileUri = getFileUri(file);
    final Ref<List<ImportedElements>> resultRef = new Ref<>();
    final CountDownLatch latch = new CountDownLatch(1);
    final int selectionOffset = getOriginalOffset(file, _selectionOffset);
    final int selectionLength = getOriginalOffset(file, _selectionOffset + _selectionLength) - selectionOffset;
    server.analysis_getImportedElements(fileUri, selectionOffset, selectionLength, new GetImportedElementsConsumer() {
      @Override
      public void computedImportedElements(final List<ImportedElements> importedElements) {
        resultRef.set(importedElements);
        latch.countDown();
      }

      @Override
      public void onError(final RequestError error) {
        if (!RequestErrorCode.GET_IMPORTED_ELEMENTS_INVALID_FILE.equals(error.getCode())) {
          logError("analysis_getImportedElements()", fileUri, error);
        }

        latch.countDown();
      }
    });

    awaitForLatchCheckingCanceled(server, latch, IMPORTED_ELEMENTS_TIMEOUT);

    if (latch.getCount() > 0) {
      logTookTooLongMessage("analysis_getImportedElements", IMPORTED_ELEMENTS_TIMEOUT, fileUri);
    }

    return resultRef.get();
  }

  public @Nullable SourceFileEdit edit_importElements(final @NotNull VirtualFile file,
                                                      final @NotNull List<ImportedElements> importedElements,
                                                      final int _offset) {
    final AnalysisServer server = myServer;
    if (server == null || StringUtil.compareVersionNumbers(mySdkVersion, "1.25") < 0) {
      return null;
    }

    final String fileUri = getFileUri(file);
    final Ref<SourceFileEdit> resultRef = new Ref<>();
    final CountDownLatch latch = new CountDownLatch(1);
    final int offset = getOriginalOffset(file, _offset);
    server.edit_importElements(fileUri, importedElements, offset, new ImportElementsConsumer() {
      @Override
      public void computedImportedElements(final SourceFileEdit edit) {
        resultRef.set(edit);
        latch.countDown();
      }

      @Override
      public void onError(final RequestError error) {
        if (!RequestErrorCode.IMPORT_ELEMENTS_INVALID_FILE.equals(error.getCode())) {
          logError("edit_importElements()", fileUri, error);
        }

        latch.countDown();
      }
    });

    awaitForLatchCheckingCanceled(server, latch, IMPORTED_ELEMENTS_TIMEOUT);

    if (latch.getCount() > 0) {
      logTookTooLongMessage("edit_importElements", IMPORTED_ELEMENTS_TIMEOUT, fileUri);
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
    if (!file.isInLocalFileSystem()) return false;

    final AnalysisServer server = myServer;
    if (server == null) {
      return false;
    }

    final String fileUri = getLocalFileUri(file.getPath());
    final int offset = getOriginalOffset(file, _offset);
    final int length = getOriginalOffset(file, _offset + _length) - offset;
    server.edit_getRefactoring(kind, fileUri, offset, length, validateOnly, options, consumer);
    return true;
  }

  public @Nullable SourceFileEdit edit_organizeDirectives(@NotNull String filePath) {
    final AnalysisServer server = myServer;
    if (server == null) {
      return null;
    }

    final String fileUri = getLocalFileUri(filePath);
    final Ref<SourceFileEdit> resultRef = new Ref<>();
    final CountDownLatch latch = new CountDownLatch(1);
    server.edit_organizeDirectives(fileUri, new OrganizeDirectivesConsumer() {
      @Override
      public void computedEdit(final SourceFileEdit edit) {
        resultRef.set(edit);
        latch.countDown();
      }

      @Override
      public void onError(final RequestError error) {
        if (RequestErrorCode.ORGANIZE_DIRECTIVES_ERROR.equals(error.getCode())) {
          LOG.info(getShortErrorMessage("edit_organizeDirectives()", fileUri, error));
        }
        else {
          logError("edit_organizeDirectives()", fileUri, error);
        }

        latch.countDown();
      }
    });

    awaitForLatchCheckingCanceled(server, latch, EDIT_ORGANIZE_DIRECTIVES_TIMEOUT);

    if (latch.getCount() > 0) {
      logTookTooLongMessage("edit_organizeDirectives", EDIT_ORGANIZE_DIRECTIVES_TIMEOUT, fileUri);
    }

    return resultRef.get();
  }

  public @Nullable SourceFileEdit edit_sortMembers(@NotNull String filePath) {
    final AnalysisServer server = myServer;
    if (server == null) {
      return null;
    }

    final String fileUri = getLocalFileUri(filePath);
    final Ref<SourceFileEdit> resultRef = new Ref<>();

    final CountDownLatch latch = new CountDownLatch(1);
    server.edit_sortMembers(fileUri, new SortMembersConsumer() {
      @Override
      public void computedEdit(final SourceFileEdit edit) {
        resultRef.set(edit);
        latch.countDown();
      }

      @Override
      public void onError(final RequestError error) {
        if (RequestErrorCode.SORT_MEMBERS_PARSE_ERRORS.equals(error.getCode()) ||
            RequestErrorCode.SORT_MEMBERS_INVALID_FILE.equals(error.getCode())) {
          LOG.info(getShortErrorMessage("edit_sortMembers()", fileUri, error));
        }
        else {
          logError("edit_sortMembers()", fileUri, error);
        }

        latch.countDown();
      }
    });

    awaitForLatchCheckingCanceled(server, latch, EDIT_SORT_MEMBERS_TIMEOUT);

    if (latch.getCount() > 0) {
      logTookTooLongMessage("edit_sortMembers", EDIT_SORT_MEMBERS_TIMEOUT, fileUri);
    }

    return resultRef.get();
  }

  public void analysis_reanalyze() {
    final AnalysisServer server = myServer;
    if (server == null) {
      return;
    }

    server.analysis_reanalyze();

    ApplicationManager.getApplication().invokeLater(this::clearAllErrors, ModalityState.nonModal(), myDisposedCondition);
  }

  private void analysis_setPriorityFiles() {
    synchronized (myLock) {
      if (myServer == null) return;

      if (LOG.isDebugEnabled()) {
        LOG.debug("analysis_setPriorityFiles, files:\n" + StringUtil.join(myVisibleFileUris, ",\n"));
      }

      myServer.analysis_setPriorityFiles(myVisibleFileUris);
    }
  }

  private void analysis_setSubscriptions() {
    synchronized (myLock) {
      if (myServer == null) return;

      final Map<String, List<String>> subscriptions = new HashMap<>();
      subscriptions.put(AnalysisService.HIGHLIGHTS, myVisibleFileUris);
      subscriptions.put(AnalysisService.NAVIGATION, myVisibleFileUris);
      subscriptions.put(AnalysisService.OVERRIDES, myVisibleFileUris);
      subscriptions.put(AnalysisService.OUTLINE, myVisibleFileUris);
      if (StringUtil.compareVersionNumbers(mySdkVersion, "1.13") >= 0) {
        subscriptions.put(AnalysisService.IMPLEMENTED, myVisibleFileUris);
      }
      if (DartClosingLabelManager.getInstance().getShowClosingLabels()
          && StringUtil.compareVersionNumbers(mySdkVersion, "1.25.0") >= 0) {
        subscriptions.put(AnalysisService.CLOSING_LABELS, myVisibleFileUris);
      }

      if (LOG.isDebugEnabled()) {
        LOG.debug("analysis_setSubscriptions, subscriptions:\n" + subscriptions);
      }

      myServer.analysis_setSubscriptions(subscriptions);
    }
  }

  public @Nullable String execution_createContext(@NotNull String filePath) {
    final AnalysisServer server = myServer;
    if (server == null) {
      return null;
    }

    final String fileUri = getLocalFileUri(filePath);
    final Ref<String> resultRef = new Ref<>();
    final CountDownLatch latch = new CountDownLatch(1);
    server.execution_createContext(fileUri, new CreateContextConsumer() {
      @Override
      public void computedExecutionContext(final String contextId) {
        resultRef.set(contextId);
        latch.countDown();
      }

      @Override
      public void onError(final RequestError error) {
        logError("execution_createContext()", fileUri, error);
        latch.countDown();
      }
    });

    awaitForLatchCheckingCanceled(server, latch, EXECUTION_CREATE_CONTEXT_TIMEOUT);

    if (latch.getCount() > 0) {
      logTookTooLongMessage("execution_createContext", EXECUTION_CREATE_CONTEXT_TIMEOUT, fileUri);
    }
    return resultRef.get();
  }

  public void execution_deleteContext(final @NotNull String contextId) {
    final AnalysisServer server = myServer;
    if (server != null) {
      server.execution_deleteContext(contextId);
    }
  }

  public @Nullable Pair<List<CompletionSuggestion>, List<RuntimeCompletionExpression>> execution_getSuggestions(@NotNull String code,
                                                                                                                int offset,
                                                                                                                @NotNull VirtualFile contextFile,
                                                                                                                int contextOffset,
                                                                                                                @NotNull List<RuntimeCompletionVariable> variables,
                                                                                                                @NotNull List<RuntimeCompletionExpression> expressions) {
    final AnalysisServer server = myServer;
    if (server == null) {
      return new Pair<>(new ArrayList<>(), new ArrayList<>());
    }

    final String contextFileUri = getFileUri(contextFile);
    final CountDownLatch latch = new CountDownLatch(1);
    final Ref<Pair<List<CompletionSuggestion>, List<RuntimeCompletionExpression>>> refResult = Ref.create();
    server.execution_getSuggestions(
      code, offset,
      contextFileUri, contextOffset,
      variables, expressions,
      new GetRuntimeCompletionConsumer() {
        @Override
        public void computedResult(List<CompletionSuggestion> suggestions, List<RuntimeCompletionExpression> expressions) {
          refResult.set(new Pair<>(suggestions, expressions));
          latch.countDown();
        }

        @Override
        public void onError(RequestError error) {
          latch.countDown();
          if (!RequestErrorCode.UNKNOWN_REQUEST.equals(error.getCode())) {
            logError("execution_getSuggestions()", contextFileUri, error);
          }
        }
      });

    awaitForLatchCheckingCanceled(server, latch, GET_SUGGESTIONS_TIMEOUT);

    if (latch.getCount() > 0) {
      logTookTooLongMessage("execution_getSuggestions", GET_SUGGESTIONS_TIMEOUT, contextFileUri);
    }
    return refResult.get();
  }

  public @Nullable String execution_mapUri(@NotNull String _id, @NotNull VirtualFile _file) {
    return execution_mapUri(_id, getFileUri(_file), null);
  }

  public @Nullable String execution_mapUri(@NotNull String _id, @NotNull String _executionContextUri) {
    return execution_mapUri(_id, null, _executionContextUri);
  }

  /**
   * @deprecated use {@link #execution_mapUri(String, VirtualFile)} or {@link #execution_mapUri(String, String)}
   */
  @Deprecated
  public @Nullable String execution_mapUri(@NotNull String _id, @Nullable String _filePathOrUri, @Nullable String _executionContextUri) {
    final AnalysisServer server = myServer;
    if (server == null) {
      return null;
    }

    // From the Dart Analysis Server Spec:
    // Exactly one of the file and uri fields must be provided. If both fields are provided, then an error of type INVALID_PARAMETER will
    // be generated. Similarly, if neither field is provided, then an error of type INVALID_PARAMETER will be generated.
    if ((_filePathOrUri == null && _executionContextUri == null) || (_filePathOrUri != null && _executionContextUri != null)) {
      LOG.error("execution_mapUri - one of _filePathOrUri and _executionContextUri must be non-null.");
      return null;
    }

    if (_filePathOrUri != null && !_filePathOrUri.contains("://")) {
      _filePathOrUri = FileUtil.toSystemDependentName(_filePathOrUri);
    }

    final Ref<String> resultRef = new Ref<>();

    final CountDownLatch latch = new CountDownLatch(1);
    server.execution_mapUri(_id, _filePathOrUri, _executionContextUri, new MapUriConsumer() {
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
        latch.countDown();
      }
    });

    awaitForLatchCheckingCanceled(server, latch, EXECUTION_MAP_URI_TIMEOUT);

    if (latch.getCount() > 0) {
      logTookTooLongMessage("execution_mapUri", EXECUTION_MAP_URI_TIMEOUT, _filePathOrUri != null ? _filePathOrUri : _executionContextUri);
      return null;
    }

    if (_executionContextUri != null && !resultRef.isNull()) {
      return FileUtil.toSystemIndependentName(resultRef.get());
    }

    return resultRef.get();
  }

  // LSP over Legacy Dart Analysis Server protocols
  public @Nullable String lspMessage_dart_textDocumentContent(@NotNull String fileUri) {
    RemoteAnalysisServerImpl server = myServer;
    if (server == null) {
      return null;
    }

    Ref<String> resultRef = new Ref<>();
    CountDownLatch latch = new CountDownLatch(1);
    server.lspMessage_dart_textDocumentContent(fileUri, new DartLspTextDocumentContentConsumer() {
      @Override
      public void computedDocumentContents(@NotNull String contents) {
        resultRef.set(StringUtil.convertLineSeparators(contents));
        latch.countDown();
      }

      @Override
      public void onError(RequestError error) {
        logError("lspMessage_dart_textDocumentContent()", fileUri, error);
        latch.countDown();
      }
    });

    awaitForLatchCheckingCanceled(server, latch, LSP_MESSAGE_TEXT_DOCUMENT_CONTENT_TIMEOUT);

    if (latch.getCount() > 0) {
      logTookTooLongMessage("lspMessage_dart_textDocumentContent", LSP_MESSAGE_TEXT_DOCUMENT_CONTENT_TIMEOUT, fileUri);
    }
    return resultRef.get();
  }

  private void startServer(final @NotNull DartSdk sdk) {
    if (DartPubActionBase.isInProgress()) return; // DartPubActionBase will start the server itself when finished

    synchronized (myLock) {
      mySdkHome = sdk.getHomePath();

      final String runtimePath = FileUtil.toSystemDependentName(DartSdkUtil.getDartExePath(sdk));

      // If true, then the DAS will be started via `dart language-server`, instead of `dart .../analysis_server.dart.snapshot`
      final boolean useDartLangServerCall = isDartSdkVersionSufficientForDartLangServer(sdk);

      String analysisServerPath = FileUtil.toSystemDependentName(mySdkHome + "/bin/snapshots/analysis_server.dart.snapshot");
      analysisServerPath = System.getProperty("dart.server.path", analysisServerPath);

      String dasStartupErrorMessage = "";
      final File runtimePathFile = new File(runtimePath);
      final File dasSnapshotFile = new File(analysisServerPath);
      if (!runtimePathFile.exists()) {
        dasStartupErrorMessage = DartBundle.message("dart.vm.file.does.not.exist.at.0", runtimePath);
      }
      else if (!useDartLangServerCall && !dasSnapshotFile.exists()) {
        dasStartupErrorMessage = DartBundle.message("analysis.server.snapshot.file.does.not.exist.at.0", analysisServerPath);
      }
      else if (!runtimePathFile.canExecute()) {
        dasStartupErrorMessage = DartBundle.message("dart.vm.file.is.not.executable.at.0", runtimePath);
      }
      else if (!useDartLangServerCall && !dasSnapshotFile.canRead()) {
        dasStartupErrorMessage = DartBundle.message("analysis.server.snapshot.file.is.not.readable.at.0", analysisServerPath);
      }

      if (!dasStartupErrorMessage.isEmpty()) {
        LOG.warn("Failed to start Dart analysis server: " + dasStartupErrorMessage);
        stopServer();
        return;
      }

      // To use a Dart Analysis Server locally, uncomment the line below and replace the `localDartSdkPath` value with your path on disk.
      // When configuring the Dart Plugin, set the Dart SDK to your locally built Dart SDK.
      // Directions here on getting the Dart SDK sources: https://github.com/dart-lang/sdk/wiki/Building
      //
      //final String localDartSdkPath = ".../dart-sdk/sdk/";
      //analysisServerPath =
      //  FileUtil.toSystemDependentName(localDartSdkPath + "pkg/analysis_server/bin/server.dart");

      final DebugPrintStream debugStream = str -> {
        str = StringUtil.first(str, MAX_DEBUG_LOG_LINE_LENGTH, true);
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

      @NonNls String serverArgsRaw;
      if (useDartLangServerCall) {
        serverArgsRaw = "--protocol=analyzer";
      }
      else {
        // Note that as of Dart 2.12.0 the '--useAnalysisHighlight2' flag is ignored (and is the
        // default highlighting mode). We still want to pass it in for earlier SDKs.
        serverArgsRaw = "--useAnalysisHighlight2";
      }

      try {
        serverArgsRaw += " " + Registry.stringValue("dart.server.additional.arguments");
      }
      catch (MissingResourceException e) {
        // NOP
      }

      String firstArgument = useDartLangServerCall ? "language-server" : analysisServerPath;
      myServerSocket =
        new StdioServerSocket(runtimePath, StringUtil.split(vmArgsRaw, " "), firstArgument, StringUtil.split(serverArgsRaw, " "),
                              debugStream);
      myServerSocket.setClientId(getClientId());
      myServerSocket.setClientVersion(getClientVersion());

      final RemoteAnalysisServerImpl startedServer = new DartAnalysisServerImpl(myProject, myServerSocket);

      try {
        startedServer.start();
        server_setSubscriptions(startedServer);

        if (!myInitializationOnServerStartupDone) {
          myInitializationOnServerStartupDone = true;

          registerFileEditorManagerListener();
          registerDocumentListener();
          setDasLogger();
        }

        startedServer.addAnalysisServerListener(myAnalysisServerListener);
        for (AnalysisServerListener listener : myAdditionalServerListeners) {
          startedServer.addAnalysisServerListener(listener);
        }
        for (RequestListener listener : myRequestListeners) {
          startedServer.addRequestListener(listener);
        }
        for (ResponseListener listener : myResponseListeners) {
          startedServer.addResponseListener(listener);
        }

        myHaveShownInitialProgress = false;
        startedServer.addStatusListener(isAlive -> {
          if (!isAlive) {
            synchronized (myLock) {
              if (startedServer == myServer) {
                // Show a notification on the dart analysis tool window.
                ApplicationManager.getApplication().invokeLater(
                  () -> {
                    final DartProblemsView problemsView = DartProblemsView.getInstance(myProject);
                    problemsView.showErrorNotificationTerse(DartBundle.message("analysis.server.terminated"));
                  },
                  ModalityState.nonModal(),
                  myDisposedCondition
                );

                stopServer();
              }
            }
          }
        });

        mySdkVersion = sdk.getVersion();

        startedServer.analysis_updateOptions(new AnalysisOptions(true, true, true, true, false, true, false));
        boolean supportsUris = isDartSdkVersionSufficientForFileUri(mySdkVersion);
        startedServer.server_setClientCapabilities(List.of("openUrlRequest", "showMessageRequest"), supportsUris);

        myServer = startedServer;

        // Clear any dart view notifications.
        ApplicationManager.getApplication().invokeLater(
          () -> {
            final DartProblemsView problemsView = DartProblemsView.getInstance(myProject);
            problemsView.clearNotifications();
          },
          ModalityState.nonModal(),
          myDisposedCondition
        );

        // This must be done after myServer is set, and should be done each time the server starts.
        registerPostfixCompletionTemplates();
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

  public boolean serverReadyForRequest() {
    final DartSdk sdk = DartSdk.getDartSdk(myProject);
    if (sdk == null || !isDartSdkVersionSufficient(sdk)) {
      stopServer();
      return false;
    }

    ApplicationManager.getApplication().assertReadAccessAllowed();
    synchronized (myLock) {
      if (myServer == null ||
          !sdk.getHomePath().equals(mySdkHome) ||
          !sdk.getVersion().equals(mySdkVersion) ||
          !myServer.isSocketOpen()) {
        stopServer();
        DartProblemsView.getInstance(myProject).setInitialCurrentFileBeforeServerStart(getCurrentOpenFile());
        startServer(sdk);

        if (myServer != null) {
          myRootsHandler.onServerStarted();
        }
      }

      return myServer != null;
    }
  }

  public void restartServer() {
    stopServer();
    serverReadyForRequest();
  }

  void stopServer() {
    synchronized (myLock) {
      if (myServer != null) {
        LOG.debug("stopping server");
        myServer.removeAnalysisServerListener(myAnalysisServerListener);
        for (AnalysisServerListener listener : myAdditionalServerListeners) {
          myServer.removeAnalysisServerListener(listener);
        }
        for (RequestListener listener : myRequestListeners) {
          myServer.removeRequestListener(listener);
        }
        for (ResponseListener listener : myResponseListeners) {
          myServer.removeResponseListener(listener);
        }

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
      mySdkVersion = "";
      myServerVersion = "";
      myFilePathWithOverlaidContentToTimestamp.clear();
      myVisibleFileUris.clear();
      myChangedDocuments.clear();
      myServerData.clearData();
      myRootsHandler.onServerStopped();

      if (myProject.isOpen() && !myProject.isDisposed()) {
        ApplicationManager.getApplication().invokeLater(this::clearAllErrors, ModalityState.nonModal(), myDisposedCondition);
      }
    }
  }

  public void waitForAnalysisToComplete_TESTS_ONLY(final @NotNull VirtualFile file) {
    assert ApplicationManager.getApplication().isUnitTestMode();

    long startTime = System.currentTimeMillis();
    while (isServerProcessActive() && !myServerData.hasAllData_TESTS_ONLY(file)) {
      if (System.currentTimeMillis() > startTime + ANALYSIS_IN_TESTS_TIMEOUT) return;
      TimeoutUtil.sleep(100);
    }
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

  public boolean isFileWithErrors(final @NotNull VirtualFile file) {
    synchronized (myFilePathsWithErrors) {
      return file.isDirectory() ? myFolderPathsWithErrors.getInt(file.getPath()) > 0 : myFilePathsWithErrors.contains(file.getPath());
    }
  }

  public int getFilePathsWithErrorsHash() {
    synchronized (myFilePathsWithErrors) {
      return myFilePathsWithErrors.hashCode();
    }
  }

  private void logError(final @NonNls @NotNull String methodName, final @Nullable String filePath, final @NotNull RequestError error) {
    if (RequestErrorCode.FILE_NOT_ANALYZED.equals(error.getCode())) {
      LOG.info(getShortErrorMessage(methodName, filePath, error));
      return;
    }

    final String trace = error.getStackTrace();
    final String partialTrace = trace == null || trace.isEmpty() ? "" : trace.substring(0, Math.min(trace.length(), 1000));
    final String message = getShortErrorMessage(methodName, filePath, error) + "\n" + partialTrace + "...";
    LOG.error(message);
  }

  private @NonNls @NotNull String getShortErrorMessage(@NonNls @NotNull String methodName, @Nullable String filePath, @NotNull RequestError error) {
    return "Error from " + methodName +
           (filePath == null ? "" : (", file = " + filePath)) +
           ", SDK version = " + mySdkVersion +
           ", server version = " + myServerVersion +
           ", error code = " + error.getCode() + ": " + error.getMessage();
  }

  private void logTookTooLongMessage(final @NonNls @NotNull String methodName, final long timeout, @Nullable String filePath) {
    @NonNls StringBuilder builder = new StringBuilder();
    builder.append(methodName).append("() took longer than ").append(timeout).append("ms");
    if (filePath != null) {
      builder.append(", for file ").append(filePath);
    }
    builder.append(", Dart SDK version: ").append(mySdkVersion);
    LOG.info(builder.toString());
  }

  private static boolean awaitForLatchCheckingCanceled(final @NotNull AnalysisServer server,
                                                       final @NotNull CountDownLatch latch,
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

  private void registerPostfixCompletionTemplates() {
    ApplicationManager.getApplication().executeOnPooledThread(() -> DartPostfixTemplateProvider.initializeTemplates(this));
  }

  public interface CompletionSuggestionConsumer {
    void consumeCompletionSuggestion(final int replacementOffset,
                                     final int replacementLength,
                                     final @NotNull CompletionSuggestion completionSuggestion);
  }

  public interface CompletionLibraryRefConsumer {
    void consumeLibraryRef(@NotNull IncludedSuggestionSet includedSet,
                           @NotNull Set<String> includedKinds,
                           @NotNull Map<String, IncludedSuggestionRelevanceTag> includedRelevanceTags,
                           @Nullable String libraryFilePathSD);
  }

  private static class CompletionInfo {
    private final @NotNull String myCompletionId;
    /**
     * must be converted before any usage
     */
    private final int myOriginalReplacementOffset;
    private final int myReplacementLength;
    private final @NotNull List<CompletionSuggestion> myCompletions;
    private final @NotNull List<IncludedSuggestionSet> myIncludedSuggestionSets;
    private final @NotNull List<String> myIncludedElementKinds;
    private final @NotNull List<IncludedSuggestionRelevanceTag> myIncludedSuggestionRelevanceTags;
    private final boolean isLast;
    private final @Nullable String myLibraryFilePathSD;

    CompletionInfo(final @NotNull String completionId,
                   int replacementOffset,
                   int replacementLength,
                   final @NotNull List<CompletionSuggestion> completions,
                   final @NotNull List<IncludedSuggestionSet> includedSuggestionSets,
                   final @NotNull List<String> includedElementKinds,
                   final @NotNull List<IncludedSuggestionRelevanceTag> includedSuggestionRelevanceTags,
                   boolean isLast,
                   @Nullable String libraryFilePathSD) {
      this.myCompletionId = completionId;
      this.myOriginalReplacementOffset = replacementOffset;
      this.myReplacementLength = replacementLength;
      this.myCompletions = completions;
      this.myIncludedSuggestionSets = includedSuggestionSets;
      this.myIncludedElementKinds = includedElementKinds;
      this.myIncludedSuggestionRelevanceTags = includedSuggestionRelevanceTags;
      this.isLast = isLast;
      this.myLibraryFilePathSD = libraryFilePathSD;
    }
  }

  public static class CompletionInfo2 {
    public final int myReplacementOffset;
    public final int myReplacementLength;
    public final @NotNull List<CompletionSuggestion> mySuggestions;
    public final boolean myIsIncomplete;

    CompletionInfo2(int replacementOffset,
                    int replacementLength,
                    @NotNull List<CompletionSuggestion> suggestions,
                    boolean isIncomplete) {
      myReplacementOffset = replacementOffset;
      myReplacementLength = replacementLength;
      mySuggestions = suggestions;
      myIsIncomplete = isIncomplete;
    }
  }

  /**
   * A set of {@link SearchResult}s.
   */
  private static class SearchResultsSet {
    final @NotNull String id;
    final @NotNull List<SearchResult> results;
    final boolean isLast;

    SearchResultsSet(@NotNull String id, @NotNull List<SearchResult> results, boolean isLast) {
      this.id = id;
      this.results = results;
      this.isLast = isLast;
    }
  }

  public void addOutlineListener(final @NotNull DartServerData.OutlineListener listener) {
    myServerData.addOutlineListener(listener);
  }

  public void removeOutlineListener(final @NotNull DartServerData.OutlineListener listener) {
    myServerData.removeOutlineListener(listener);
  }

  /**
   * Generate and return a unique {@link String} id to be used to sent requests.
   */
  @SuppressWarnings("unused") // for Flutter plugin
  public String generateUniqueId() {
    final RemoteAnalysisServerImpl server = myServer;
    if (server == null) {
      return null;
    }
    return server.generateUniqueId();
  }

  /**
   * Send the request for which the client that does not expect a response.
   */
  @SuppressWarnings("unused") // for Flutter plugin
  public void sendRequest(String id, JsonObject request) {
    final RemoteAnalysisServerImpl server = myServer;
    if (server != null) {
      server.sendRequestToServer(id, request);
    }
  }

  /**
   * Send the request and associate it with the passed {@link com.google.dart.server.Consumer}.
   */
  @SuppressWarnings("unused") // for Flutter plugin
  public void sendRequestToServer(String id, JsonObject request, com.google.dart.server.Consumer consumer) {
    final RemoteAnalysisServerImpl server = myServer;
    if (server != null) {
      server.sendRequestToServer(id, request, consumer);
    }
  }

  /**
   * Subscribe for verbose analysis server `server.log` notifications.
   */
  @SuppressWarnings("unused") // for Flutter plugin
  public void setServerLogSubscription(boolean subscribeToLog) {
    if (mySubscribeToServerLog != subscribeToLog) {
      mySubscribeToServerLog = subscribeToLog;
      server_setSubscriptions(myServer);
    }
  }

  private void server_setSubscriptions(@Nullable AnalysisServer server) {
    if (server != null) {
      server.server_setSubscriptions(mySubscribeToServerLog ? Arrays.asList(ServerService.STATUS, ServerService.LOG)
                                                            : Collections.singletonList(ServerService.STATUS));
    }
  }

  /**
   * Returns a string, which the
   * <a href="https://htmlpreview.github.io/?https://github.com/dart-lang/sdk/blob/main/pkg/analysis_server/doc/api.html#type_FilePath">Analysis Server API specification</a>
   * defines as `FilePath`:
   * <ul>
   * <li>for SDK version 3.3 and older, it's an absolute file path with OS-dependent slashes
   * <li>for SDK version 3.4 and newer, it's a URI, thanks to the `supportsUris` capability defined in the spec
   * </ul>
   */
  public String getFileUri(@NotNull VirtualFile file) {
    if (!isDartSdkVersionSufficientForFileUri(mySdkVersion)) {
      // prior to Dart SDK 3.4, the protocol required file paths instead of URIs
      return FileUtil.toSystemDependentName(file.getPath());
    }

    String fileUri = file.getUserData(DartFileInfoKt.DART_NOT_LOCAL_FILE_URI_KEY);
    return fileUri != null ? fileUri : getLocalFileUri(file.getPath());
  }

  /**
   * Prefer {@link #getFileUri(VirtualFile)}.
   * Use this method only if the corresponding `VirtualFile` is not available at the call site,
   * and you are sure that this is a local file path.
   *
   * @apiNote URI calculation is similar to {@link com.intellij.platform.lsp.api.LspServerDescriptor#getFileUri(VirtualFile)}
   * @see #getFileUri(VirtualFile)
   */
  public String getLocalFileUri(@NotNull String localFilePath) {
    if (!isDartSdkVersionSufficientForFileUri(mySdkVersion)) {
      // prior to Dart SDK 3.4, the protocol required file paths instead of URIs
      return FileUtil.toSystemDependentName(localFilePath);
    }

    String escapedPath = URLUtil.encodePath(FileUtil.toSystemIndependentName(localFilePath));
    String url = VirtualFileManager.constructUrl(URLUtil.FILE_PROTOCOL, escapedPath);
    URI uri = VfsUtil.toUri(url);
    return uri != null ? uri.toString() : url;
  }
}
