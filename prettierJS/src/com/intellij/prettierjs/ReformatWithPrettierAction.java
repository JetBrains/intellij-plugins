// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs;

import com.intellij.codeInsight.actions.FileTreeIterator;
import com.intellij.codeInsight.actions.VcsFacade;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.hint.HintManagerImpl;
import com.intellij.codeInsight.hint.HintUtil;
import com.intellij.codeStyle.AbstractConvertLineSeparatorsAction;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.javascript.nodejs.interpreter.NodeInterpreterUtil;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef;
import com.intellij.javascript.nodejs.npm.InstallNodeLocalDependenciesAction;
import com.intellij.javascript.nodejs.npm.NpmManager;
import com.intellij.javascript.nodejs.settings.NodeSettingsConfigurable;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.linter.JSLinterGuesser;
import com.intellij.lang.javascript.linter.JsqtProcessOutputViewer;
import com.intellij.lang.javascript.service.JSLanguageServiceUtil;
import com.intellij.lang.javascript.service.protocol.LocalFilePath;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.util.EditorScrollingPositionKeeper;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.ui.LightweightHint;
import com.intellij.util.ArrayUtil;
import com.intellij.util.LineSeparator;
import com.intellij.util.NullableFunction;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.text.SemVer;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ReformatWithPrettierAction extends AnAction implements DumbAware {
  private static final @NotNull Logger LOG = Logger.getInstance(ReformatWithPrettierAction.class);
  private static final long EDT_TIMEOUT_MS = 2000;

  private final ErrorHandler myErrorHandler;

  public ReformatWithPrettierAction(@NotNull ErrorHandler errorHandler) {
    myErrorHandler = errorHandler;
  }

  public ReformatWithPrettierAction() {
    this(ErrorHandler.DEFAULT);
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    if (project == null) {
      e.getPresentation().setEnabledAndVisible(false);
      return;
    }
    var psiFile = e.getData(CommonDataKeys.PSI_FILE);
    NodePackage nodePackage = PrettierConfiguration.getInstance(project).getPackage(psiFile);
    e.getPresentation().setEnabledAndVisible(!nodePackage.isEmptyPath() && isAcceptableFileContext(e));
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  private static boolean isAcceptableFileContext(@NotNull AnActionEvent e) {
    Editor editor = e.getData(CommonDataKeys.EDITOR);
    if (editor != null) {
      return true;
    }
    VirtualFile[] virtualFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
    return !ArrayUtil.isEmpty(virtualFiles);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    if (project == null) {
      return;
    }
    Editor editor = e.getData(CommonDataKeys.EDITOR);
    if (editor != null) {
      processFileInEditor(project, editor, myErrorHandler, null);
    }
    else {
      VirtualFile[] virtualFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
      if (!ArrayUtil.isEmpty(virtualFiles)) {
        processVirtualFiles(project, Arrays.asList(virtualFiles), myErrorHandler);
      }
    }
  }

  static boolean checkNodeAndPackage(@NotNull PsiFile psiFile, @Nullable Editor editor, @NotNull ErrorHandler errorHandler) {
    Project project = psiFile.getProject();
    NodeJsInterpreterRef interpreterRef = NodeJsInterpreterRef.createProjectRef();
    NodePackage nodePackage = PrettierConfiguration.getInstance(project).getPackage(psiFile);

    NodeJsInterpreter nodeJsInterpreter;
    try {
      nodeJsInterpreter = NodeInterpreterUtil.getValidInterpreterOrThrow(interpreterRef.resolve(project));
    }
    catch (ExecutionException e1) {
      errorHandler.showError(project, editor, PrettierBundle.message("error.invalid.interpreter"),
                             () -> NodeSettingsConfigurable.showSettingsDialog(project));
      return false;
    }

    if (nodePackage.isEmptyPath()) {
      errorHandler.showError(project, editor, PrettierBundle.message("error.no.valid.package"),
                             () -> editSettings(project));
      return false;
    }
    if (!nodePackage.isValid(project, nodeJsInterpreter)) {
      String message = PrettierBundle.message("error.package.is.not.installed",
                                              NpmManager.getInstance(project).getNpmInstallPresentableText());
      errorHandler.showError(project, editor, message, () -> installPackage(project));
      return false;
    }
    SemVer nodePackageVersion = nodePackage.getVersion(project);
    if (nodePackageVersion != null && nodePackageVersion.compareTo(PrettierUtil.MIN_VERSION) < 0) {
      errorHandler.showError(project, editor,
                             PrettierBundle.message("error.unsupported.version", PrettierUtil.MIN_VERSION.getRawVersion()), null);
      return false;
    }

    return true;
  }

  public static boolean isAvailable(@NotNull Project project, @NotNull Editor editor, @NotNull ErrorHandler errorHandler) {
    PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
    if (file == null) {
      return false;
    }
    return checkNodeAndPackage(file, editor, errorHandler);
  }

  public static void processFileInEditor(@NotNull Project project,
                                         @NotNull Editor editor,
                                         @NotNull ErrorHandler errorHandler,
                                         @Nullable TextRange targetRange) {
    if (!isAvailable(project, editor, errorHandler)) return;
    PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
    if (file == null) {
      return;
    }
    if (!checkNodeAndPackage(file, editor, errorHandler)) return;

    VirtualFile vFile = file.getVirtualFile();
    if (ReadonlyStatusHandler.getInstance(project).ensureFilesWritable(Collections.singletonList(vFile))
      .hasReadonlyFiles()) {
      return;
    }

    final TextRange range = (targetRange != null)
                            ? targetRange
                            : (editor.getSelectionModel().hasSelection())
                              ? new TextRange(editor.getSelectionModel().getSelectionStart(), editor.getSelectionModel().getSelectionEnd())
                              : null;

    ensureConfigsSaved(Collections.singletonList(vFile), project);
    ThrowableComputable<PrettierLanguageService.FormatResult, RuntimeException> computable = () -> performRequestForFile(file, range);
    PrettierLanguageService.FormatResult result = ProgressManager
      .getInstance()
      .runProcessWithProgressSynchronously(computable, PrettierBundle.message("progress.title"), true, project);
    // timed out. show notification?
    if (result == null) {
      return;
    }
    if (!StringUtil.isEmpty(result.error)) {
      errorHandler.showErrorWithDetails(project, editor,
                                        PrettierBundle.message("error.while.reformatting.message"), result.error);
    }
    else if (result.unsupported) {
      errorHandler.showError(project, editor, PrettierBundle.message("not.supported.file", file.getName()), null);
    }
    else if (result.ignored) {
      showHintLater(editor, PrettierBundle.message("file.was.ignored.hint", file.getName()), false, null);
    }
    else {
      Document document = editor.getDocument();
      CharSequence textBefore = document.getImmutableCharSequence();
      String newContent = result.result;
      /*
       * This checks only the first line break, but given that we don't handle mixed line separators,
       * this is enough to detect if separators were changed by the external process
       */
      LineSeparator newLineSeparator = StringUtil.detectSeparators(newContent);
      String newDocumentContent = StringUtil.convertLineSeparators(newContent);

      Ref<Boolean> lineSeparatorUpdated = new Ref<>(Boolean.FALSE);
      EditorScrollingPositionKeeper.perform(editor, true, () -> {
        runWriteCommandAction(project, () -> {
          if (!StringUtil.equals(textBefore, newContent)) {
            document.setText(newDocumentContent);
          }
          lineSeparatorUpdated.set(setDetectedLineSeparator(project, vFile, newLineSeparator));
        });
      });

      showHintLater(editor, buildNotificationMessage(document, textBefore, lineSeparatorUpdated.get()), false, null);
    }
  }

  static TextRange processFileAsPostFormatProcessor(@NotNull PsiFile file, @NotNull TextRange range) {
    // PostFormatProcessors are invoked in EDT under rite action. So we can't show progress and need to block for a while waiting for the result.
    LOG.assertTrue(ApplicationManager.getApplication().isWriteAccessAllowed());

    Project project = file.getProject();

    if (!checkNodeAndPackage(file, null, NOOP_ERROR_HANDLER)) {
      return range;
    }

    VirtualFile vFile = file.getVirtualFile();
    ensureConfigsSaved(Collections.singletonList(vFile), project);
    PrettierLanguageService.FormatResult result = performRequestForFile(file, range);
    if (result != null) {
      int delta = applyFormatResult(project, vFile, result);
      if (delta < 0 && range.getLength() < Math.abs(delta)) {
        return TextRange.from(range.getStartOffset(), 0);
      }
      return range.grown(delta);
    }
    return range;
  }

  static void ensureConfigsSaved(@NotNull List<VirtualFile> virtualFiles, @NotNull Project project) {
    FileDocumentManager documentManager = FileDocumentManager.getInstance();
    for (VirtualFile config : PrettierUtil.lookupPossibleConfigFiles(virtualFiles, project)) {
      Document document = documentManager.getCachedDocument(config);
      if (document != null && documentManager.isDocumentUnsaved(document)) {
        documentManager.saveDocument(document);
      }
    }
  }

  public static void processVirtualFiles(@NotNull Project project,
                                         @NotNull List<VirtualFile> virtualFiles,
                                         @NotNull ErrorHandler errorHandler) {
    ReadonlyStatusHandler.OperationStatus readonlyStatus = ReadonlyStatusHandler.getInstance(project).ensureFilesWritable(virtualFiles);
    if (readonlyStatus.hasReadonlyFiles()) {
      return;
    }
    ensureConfigsSaved(virtualFiles, project);
    PsiManager psiManager = PsiManager.getInstance(project);
    if (virtualFiles.size() == 1 && virtualFiles.get(0).isDirectory()) {
      PsiDirectory psiDirectory = psiManager.findDirectory(virtualFiles.get(0));
      if (psiDirectory == null) {
        return;
      }
      processFileIterator(project, new FileTreeIterator(psiDirectory), false, errorHandler);
    }
    else {
      processFileIterator(project, new FileTreeIterator(PsiUtilCore.toPsiFiles(psiManager, virtualFiles)), true, errorHandler);
    }
  }

  private static void processFileIterator(@NotNull Project project,
                                          @NotNull final FileTreeIterator fileIterator,
                                          boolean reportSkippedFiles,
                                          @NotNull ErrorHandler errorHandler) {
    Map<PsiFile, PrettierLanguageService.FormatResult> results = executeUnderProgress(project, indicator -> {
      Map<PsiFile, PrettierLanguageService.FormatResult> reformattedResults = new HashMap<>();

      List<PsiFile> files = new SmartList<>();
      ReadAction.run(() -> {
        while (fileIterator.hasNext()) {
          files.add(fileIterator.next());
        }
      });

      for (PsiFile currentFile : files) {
        indicator.setText(PrettierBundle.message("processing.0.progress", currentFile.getName()));
        if (!checkNodeAndPackage(currentFile, null, errorHandler)) {
          return Collections.emptyMap();
        }

        PrettierLanguageService.FormatResult result = performRequestForFile(currentFile, null);
        // timed out. show notification?
        if (result == null) {
          continue;
        }
        if (result.unsupported && reportSkippedFiles) {
          PrettierLanguageService.FormatResult errorResult = PrettierLanguageService.FormatResult
            .error(PrettierBundle.message("not.supported.file", currentFile.getName()));
          reformattedResults.put(currentFile, errorResult);
        }
        if (result.ignored) {
          PrettierLanguageService.FormatResult errorResult =
            PrettierLanguageService.FormatResult.error(PrettierBundle.message("file.was.ignored", currentFile.getName()));
          reformattedResults.put(currentFile, errorResult);
          continue;
        }
        reformattedResults.put(currentFile, result);
      }
      return reformattedResults;
    });

    runWriteCommandAction(project, () -> {
      for (Map.Entry<PsiFile, PrettierLanguageService.FormatResult> entry : results.entrySet()) {
        VirtualFile virtualFile = entry.getKey().getVirtualFile();
        if (virtualFile == null) {
          continue;
        }
        PrettierLanguageService.FormatResult result = entry.getValue();
        applyFormatResult(project, virtualFile, result);
      }
    });
    List<String> errors = ContainerUtil.mapNotNull(results.entrySet(), t -> t.getValue().error);
    if (!errors.isEmpty()) {
      errorHandler.showErrorWithDetails(project, null,
                                        PrettierBundle.message("failed.to.reformat.0.files", errors.size()),
                                        StringUtil.join(errors, "\n"));
    }
  }

  /**
   * @param result (new text length) - (old text length)
   */
  static int applyFormatResult(@NotNull Project project,
                               @NotNull VirtualFile virtualFile,
                               @NotNull PrettierLanguageService.FormatResult result) {
    Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
    int delta = 0;
    if (document != null && StringUtil.isEmpty(result.error) && !result.ignored && !result.unsupported) {
      CharSequence textBefore = document.getCharsSequence();
      LineSeparator newlineSeparator = StringUtil.detectSeparators(result.result);
      String newContent = StringUtil.convertLineSeparators(result.result);
      if (!StringUtil.equals(textBefore, newContent)) {
        int lengthBefore = textBefore.length();
        document.setText(newContent);
        delta = newContent.length() - lengthBefore;
      }
      setDetectedLineSeparator(project, virtualFile, newlineSeparator);
    }
    return delta;
  }

  static @Nullable PrettierLanguageService.FormatResult performRequestForFile(@NotNull PsiFile currentFile, @Nullable TextRange range) {
    boolean edt = ApplicationManager.getApplication().isDispatchThread();
    if (!edt && ApplicationManager.getApplication().isReadAccessAllowed()) {
      LOG.error("JSLanguageServiceUtil.awaitFuture() under read action may cause deadlock");
    }

    Project project = currentFile.getProject();
    Ref<String> text = Ref.create();
    Ref<String> filePath = Ref.create();
    Ref<String> ignoreFilePath = Ref.create();
    Ref<TextRange> rangeForRequest = Ref.create(range);

    ReadAction.run(() -> {
      if (!currentFile.isValid()) return;

      VirtualFile currentVFile = currentFile.getVirtualFile();
      filePath.set(LocalFilePath.asLocalFilePath(currentVFile.toNioPath()));

      // PsiFile might be not committed at this point, take text from document
      Document document = PsiDocumentManager.getInstance(project).getDocument(currentFile);
      if (document == null) return;

      CharSequence content = document.getImmutableCharSequence();

      if (range != null && range.getStartOffset() == 0 && range.getLength() == content.length()) {
        // Prettier may remove trailing line break in Vue (WEB-56144, https://github.com/prettier/prettier/issues/13399).
        // It's safer not pass a range when there's no need to.
        rangeForRequest.set(null);
      }

      text.set(JSLanguageServiceUtil.convertLineSeparatorsToFileOriginal(project, content, currentVFile).toString());

      VirtualFile ignoreVFile = PrettierUtil.findIgnoreFile(currentVFile, project);
      if (ignoreVFile != null) {
        ignoreFilePath.set(ignoreVFile.getPath());
      }
    });

    if (text.isNull()) {
      return PrettierLanguageService.FormatResult.UNSUPPORTED;
    }

    NodePackage nodePackage = PrettierConfiguration.getInstance(project).getPackage(currentFile);
    PrettierLanguageService service = PrettierLanguageService.getInstance(project, currentFile.getVirtualFile(), nodePackage);

    CompletableFuture<PrettierLanguageService.FormatResult> formatFuture =
      service.format(filePath.get(), ignoreFilePath.get(), text.get(), nodePackage, rangeForRequest.get());
    long timeout = edt ? EDT_TIMEOUT_MS : JSLanguageServiceUtil.getTimeout();
    return JSLanguageServiceUtil.awaitFuture(formatFuture, timeout, JSLanguageServiceUtil.QUOTA_MILLS, null, true, null, edt);
  }

  private static <T> T executeUnderProgress(@NotNull Project project, @NotNull NullableFunction<ProgressIndicator, T> handler) {
    return ProgressManager
      .getInstance()
      .runProcessWithProgressSynchronously(() -> handler.fun(ProgressManager.getInstance().getProgressIndicator()),
                                           PrettierBundle.message("progress.title"), true, project);
  }

  private static void runWriteCommandAction(@NotNull Project project, @NotNull Runnable runnable) {
    WriteCommandAction.runWriteCommandAction(project, PrettierBundle.message("reformat.with.prettier.command.name"), null, runnable);
  }

  private static @NotNull @Nls String buildNotificationMessage(@NotNull Document document,
                                                               @NotNull CharSequence textBefore,
                                                               boolean lineSeparatorsUpdated) {
    int number = VcsFacade.getInstance().calculateChangedLinesNumber(document, textBefore);
    if (number == 0) {
      return lineSeparatorsUpdated ? PrettierBundle.message("line.endings.were.updated")
                                   : PrettierBundle.message("no.lines.changed");
    }
    return PrettierBundle.message("formatted.0.lines", number);
  }

  private static void showHintLater(@NotNull Editor editor,
                                    @NotNull @Nls String text,
                                    boolean isError,
                                    @Nullable HyperlinkListener hyperlinkListener) {
    ApplicationManager.getApplication().invokeLater(() -> {
      final JComponent component = isError ? HintUtil.createErrorLabel(text, hyperlinkListener, null)
                                           : HintUtil.createInformationLabel(text, hyperlinkListener, null, null);
      final LightweightHint hint = new LightweightHint(component);
      HintManagerImpl.getInstanceImpl()
        .showEditorHint(hint, editor, HintManager.UNDER, HintManager.HIDE_BY_ANY_KEY | HintManager.HIDE_BY_TEXT_CHANGE |
                                                         HintManager.HIDE_BY_SCROLLING, 0, false);
    }, ModalityState.nonModal(), o -> editor.isDisposed() || !editor.getComponent().isShowing());
  }

  private static void installPackage(@NotNull Project project) {
    final VirtualFile packageJson = PackageJsonUtil.findChildPackageJsonFile(project.getBaseDir());
    if (packageJson != null) {
      InstallNodeLocalDependenciesAction.runAndShowConsole(project, packageJson);
    }
  }

  private static void showErrorDetails(@NotNull Project project, @NotNull String text) {
    ProcessOutput output = new ProcessOutput();
    output.appendStderr(text);
    JsqtProcessOutputViewer
      .show(project, PrettierBundle.message("prettier.formatter.notification.title"), PrettierUtil.ICON, null, null, output);
  }

  private static void editSettings(@NotNull Project project) {
    ShowSettingsUtil.getInstance().editConfigurable(project, new PrettierConfigurable(project));
  }

  /**
   * @return true if line separator was updated
   */
  private static boolean setDetectedLineSeparator(@NotNull Project project,
                                                  @NotNull VirtualFile vFile,
                                                  @Nullable LineSeparator newSeparator) {
    if (newSeparator != null) {
      String newSeparatorString = newSeparator.getSeparatorString();
      if (!StringUtil.equals(vFile.getDetectedLineSeparator(), newSeparatorString)) {
        AbstractConvertLineSeparatorsAction.changeLineSeparators(project, vFile, newSeparatorString);
        return true;
      }
    }
    return false;
  }

  public interface ErrorHandler {
    ErrorHandler DEFAULT = new DefaultErrorHandler();

    void showError(@NotNull Project project,
                   @Nullable Editor editor,
                   @NotNull @Nls String text,
                   @Nullable Runnable onLinkClick);

    default void showErrorWithDetails(@NotNull Project project,
                                      @Nullable Editor editor,
                                      @NotNull @Nls String text,
                                      @NotNull String details) {
      showError(project, editor, text, () -> showErrorDetails(project, details));
    }
  }

  static final ReformatWithPrettierAction.ErrorHandler NOOP_ERROR_HANDLER = new ReformatWithPrettierAction.ErrorHandler() {
    @Override
    public void showError(@NotNull Project project, @Nullable Editor editor, @NotNull String text, @Nullable Runnable onLinkClick) {
      // No need to show any notification in case of 'Prettier on save' failure.
      // Most likely the file is simply not syntactically valid at the moment.
    }
  };

  private static class DefaultErrorHandler implements ErrorHandler {
    @Override
    public void showError(@NotNull Project project, @Nullable Editor editor, @NotNull @Nls String text, @Nullable Runnable onLinkClick) {
      if (editor != null) {
        HyperlinkListener listener = onLinkClick == null ? null : new HyperlinkAdapter() {
          @Override
          protected void hyperlinkActivated(@NotNull HyperlinkEvent e) {
            onLinkClick.run();
          }
        };
        showHintLater(editor, PrettierBundle.message("prettier.formatter.hint.0", text), true, listener);
      }
      else {
        Notification notification =
          JSLinterGuesser.NOTIFICATION_GROUP.createNotification(PrettierBundle.message("prettier.formatter.notification.title"), text,
                                                                NotificationType.ERROR);
        if (onLinkClick != null) {
          notification.setListener(new NotificationListener.Adapter() {
            @Override
            protected void hyperlinkActivated(@NotNull Notification notification1, @NotNull HyperlinkEvent e) {
              onLinkClick.run();
            }
          });
        }
        notification.notify(project);
      }
    }
  }
}
