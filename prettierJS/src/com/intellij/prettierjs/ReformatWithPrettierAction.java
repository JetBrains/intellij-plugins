// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs;

import com.intellij.codeInsight.actions.FileTreeIterator;
import com.intellij.codeInsight.actions.FormatChangedTextUtil;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.hint.HintManagerImpl;
import com.intellij.codeInsight.hint.HintUtil;
import com.intellij.codeStyle.AbstractConvertLineSeparatorsAction;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.javascript.nodejs.interpreter.NodeInterpreterUtil;
import com.intellij.javascript.nodejs.npm.NpmManager;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.linter.JSLinterGuesser;
import com.intellij.lang.javascript.linter.JSLinterUtil;
import com.intellij.lang.javascript.linter.JsqtProcessOutputViewer;
import com.intellij.lang.javascript.modules.InstallNodeLocalDependenciesAction;
import com.intellij.lang.javascript.service.JSLanguageServiceUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.util.CaretVisualPositionKeeper;
import com.intellij.openapi.fileEditor.FileDocumentManager;
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
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.text.SemVer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.util.*;

public class ReformatWithPrettierAction extends AnAction implements DumbAware {
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
    NodePackage nodePackage = PrettierConfiguration.getInstance(project).getPackage();
    e.getPresentation().setEnabledAndVisible(nodePackage != null && !nodePackage.isEmptyPath()
                                             && isAcceptableFileContext(e));
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
    PrettierConfiguration configuration = PrettierConfiguration.getInstance(project);
    try {
      NodeInterpreterUtil.getValidInterpreterOrThrow(configuration.getInterpreterRef().resolve(project));
    }
    catch (ExecutionException e1) {
      myErrorHandler.showError(project, editor, PrettierBundle.message("error.invalid.interpreter"),
                () -> editSettings(project));
      return;
    }

    NodePackage nodePackage = configuration.getPackage();
    if (nodePackage.isEmptyPath()) {
      myErrorHandler.showError(project, editor, PrettierBundle.message("error.no.valid.package"),
                () -> editSettings(project));
      return;
    }
    if (!nodePackage.isValid()) {
      String message = PrettierBundle.message("error.package.is.not.installed",
                                              NpmManager.getInstance(project).getNpmInstallPresentableText());
      myErrorHandler.showError(project, editor, message, () -> installPackage(project));
      return;
    }
    SemVer nodePackageVersion = nodePackage.getVersion();
    if (nodePackageVersion != null && nodePackageVersion.compareTo(PrettierUtil.MIN_VERSION) < 0) {
      myErrorHandler.showError(project, editor,
                               PrettierBundle.message("error.unsupported.version", PrettierUtil.MIN_VERSION.getRawVersion()), null);
      return;
    }

    if (editor != null) {
      processFileInEditor(project, editor, nodePackage);
    }
    else {
      VirtualFile[] virtualFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
      if (!ArrayUtil.isEmpty(virtualFiles)) {
        processVirtualFiles(project, Arrays.asList(virtualFiles), nodePackage);
      }
    }
  }

  private void processFileInEditor(@NotNull Project project, @NotNull Editor editor, @NotNull NodePackage nodePackage) {
    PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
    if (file == null) {
      return;
    }
    VirtualFile vFile = file.getVirtualFile();
    if (ReadonlyStatusHandler.getInstance(project).ensureFilesWritable(Collections.singletonList(vFile))
      .hasReadonlyFiles()) {
      return;
    }

    TextRange range = editor.getSelectionModel().hasSelection()
                      ? new TextRange(editor.getSelectionModel().getSelectionStart(), editor.getSelectionModel().getSelectionEnd()) : null;
    ensureConfigsSaved(Collections.singletonList(vFile), project);
    PrettierLanguageService service = PrettierLanguageService.getInstance(file.getProject());
    ThrowableComputable<PrettierLanguageService.FormatResult, RuntimeException> computable =
      () -> ReadAction.compute(() -> performRequestForFile(project, nodePackage, service, file, range));
    PrettierLanguageService.FormatResult result = ProgressManager
      .getInstance()
      .runProcessWithProgressSynchronously(computable, PrettierBundle.message("progress.title"), true, project);
    // timed out. show notification?
    if (result == null) {
      return;
    }
    if (!StringUtil.isEmpty(result.error)) {
      myErrorHandler.showErrorWithDetails(project, editor,
                                          PrettierBundle.message("error.while.reformatting.message"), result.error);
    }
    else if (result.unsupported) {
      myErrorHandler.showError(project, editor, PrettierBundle.message("not.supported.file", file.getName()), null);
    }
    else if (result.ignored) {
      showHintLater(editor, "Prettier: " + PrettierBundle.message("file.was.ignored", file.getName()), false, null);
    }
    else {
      Document document = editor.getDocument();
      CaretVisualPositionKeeper caretVisualPositionKeeper = new CaretVisualPositionKeeper(document);
      CharSequence textBefore = document.getImmutableCharSequence();
      String newContent = result.result;
      /*
       * This checks only the first line break, but given that we don't handle mixed line separators,
       * this is enough to detect if separators were changed by the external process
       */
      LineSeparator newLineSeparator = StringUtil.detectSeparators(newContent);
      String newDocumentContent = StringUtil.convertLineSeparators(newContent);

      Ref<Boolean> lineSeparatorUpdated = new Ref<>(Boolean.FALSE);
      runWriteCommandAction(project, () -> {
        if (!StringUtil.equals(textBefore, newContent)) {
          document.setText(newDocumentContent);
          caretVisualPositionKeeper.restoreOriginalLocation(true);
        }
        lineSeparatorUpdated.set(setDetectedLineSeparator(project, vFile, newLineSeparator));
      });
      showHintLater(editor, buildNotificationMessage(document, textBefore, lineSeparatorUpdated.get()), false, null);
    }
  }

  private static void ensureConfigsSaved(@NotNull List<VirtualFile> virtualFiles, @NotNull Project project) {
    FileDocumentManager documentManager = FileDocumentManager.getInstance();
    for (VirtualFile config : PrettierUtil.lookupPossibleConfigFiles(virtualFiles, project)) {
      Document document = documentManager.getCachedDocument(config);
      if (document != null && documentManager.isDocumentUnsaved(document)) {
        documentManager.saveDocument(document);
      }
    }
  }

  private void processVirtualFiles(@NotNull Project project,
                                   @NotNull List<VirtualFile> virtualFiles,
                                   @NotNull NodePackage nodePackage) {
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
      processFileIterator(project, new FileTreeIterator(psiDirectory), nodePackage, false);
    }
    else {
      processFileIterator(project, new FileTreeIterator(PsiUtilCore.toPsiFiles(psiManager, virtualFiles)),
                          nodePackage, true);
    }
  }

  private void processFileIterator(@NotNull Project project,
                                   @NotNull final FileTreeIterator fileIterator,
                                   @NotNull NodePackage nodePackage,
                                   boolean reportSkippedFiles) {
    PrettierLanguageService service = PrettierLanguageService.getInstance(project);
    Map<PsiFile, PrettierLanguageService.FormatResult> results = executeUnderProgress(project, indicator -> ReadAction.compute(() -> {
      Map<PsiFile, PrettierLanguageService.FormatResult> reformattedResults = new HashMap<>();

      while (fileIterator.hasNext()) {
        PsiFile currentFile = fileIterator.next();
        indicator.setText("Processing " + currentFile.getName());
        PrettierLanguageService.FormatResult result = performRequestForFile(project, nodePackage, service, currentFile, null);
        // timed out. show notification?
        if (result == null) {
          continue;
        }
        if (result.unsupported && reportSkippedFiles) {
          PrettierLanguageService.FormatResult errorResult = PrettierLanguageService.FormatResult
            .error(PrettierBundle.message("not.supported.file", currentFile.getName()));
          reformattedResults.put(currentFile,errorResult); 
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
    }));

    runWriteCommandAction(project, () -> {
      for (Map.Entry<PsiFile, PrettierLanguageService.FormatResult> entry : results.entrySet()) {
        VirtualFile virtualFile = entry.getKey().getVirtualFile();
        if (virtualFile == null) {
          continue;
        }
        Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
        PrettierLanguageService.FormatResult result = entry.getValue();
        if (document != null && StringUtil.isEmpty(result.error) && !result.ignored) {
          CharSequence textBefore = document.getCharsSequence();
          LineSeparator newlineSeparator = StringUtil.detectSeparators(result.result);
          String newContent = StringUtil.convertLineSeparators(result.result);
          if (!StringUtil.equals(textBefore, newContent)) {
            document.setText(newContent);
          }
          setDetectedLineSeparator(project, virtualFile, newlineSeparator);
        }
      }
    });
    List<String> errors = ContainerUtil.mapNotNull(results.entrySet(), t -> t.getValue().error);
    if (errors.size() > 0) {
      myErrorHandler.showErrorWithDetails(project, null,
                                          "Failed to reformat " + errors.size() + " files<br><a href=''>Details</a>",
                                          StringUtil.join(errors, "\n"));
    }
  }

  @Nullable
  private static PrettierLanguageService.FormatResult performRequestForFile(@NotNull Project project,
                                                                            @NotNull NodePackage nodePackage,
                                                                            @NotNull PrettierLanguageService service,
                                                                            @NotNull PsiFile currentFile,
                                                                            @Nullable TextRange range) {
    ApplicationManager.getApplication().assertReadAccessAllowed();
    VirtualFile currentVFile = currentFile.getVirtualFile();
    String filePath = currentVFile.getPath();
    String text = JSLinterUtil.convertLineSeparatorsToFileOriginal(project, currentFile.getText(), currentVFile).toString();
    VirtualFile ignoreVFile = PrettierUtil.findIgnoreFile(currentVFile, project);
    String ignoreFilePath = ignoreVFile != null ? ignoreVFile.getPath() : null;
    return JSLanguageServiceUtil.awaitFuture(service.format(filePath, ignoreFilePath, text, nodePackage, range));
  }

  private static <T> T executeUnderProgress(@NotNull Project project, @NotNull NullableFunction<ProgressIndicator, T> handler) {
    return ProgressManager
      .getInstance()
      .runProcessWithProgressSynchronously(() -> handler.fun(ProgressManager.getInstance().getProgressIndicator()),
                                           PrettierBundle.message("progress.title"), true, project);
  }

  private static void runWriteCommandAction(@NotNull Project project, @NotNull Runnable runnable) {
    WriteCommandAction.runWriteCommandAction(project, PrettierBundle.message("command.name"), null, runnable);
  }

  private static String buildNotificationMessage(@NotNull Document document, @NotNull CharSequence textBefore, boolean lineSeparatorsUpdated) {
    int number = FormatChangedTextUtil.getInstance().calculateChangedLinesNumber(document, textBefore);
    return "Prettier: " + (number > 0 ? "Reformatted " + number + " lines"
                                      : lineSeparatorsUpdated
                                        ? "Line endings were updated" : "No lines changed. Content is already properly formatted");
  }

  private static void showHintLater(@NotNull Editor editor,
                                    @NotNull String text,
                                    boolean isError,
                                    @Nullable HyperlinkListener hyperlinkListener) {
    ApplicationManager.getApplication().invokeLater(() -> {
      final JComponent component = isError ? HintUtil.createErrorLabel(text, hyperlinkListener, null, null)
                                           : HintUtil.createInformationLabel(text, hyperlinkListener, null, null);
      final LightweightHint hint = new LightweightHint(component);
      HintManagerImpl.getInstanceImpl()
                     .showEditorHint(hint, editor, HintManager.UNDER, HintManager.HIDE_BY_ANY_KEY | HintManager.HIDE_BY_TEXT_CHANGE |
                                                                      HintManager.HIDE_BY_SCROLLING, 0, false);
    }, ModalityState.NON_MODAL, o -> editor.isDisposed() || !editor.getComponent().isShowing());
  }

  private static void installPackage(@NotNull Project project) {
    final VirtualFile packageJson = project.getBaseDir().findChild(PackageJsonUtil.FILE_NAME);
    if (packageJson != null) {
      InstallNodeLocalDependenciesAction.runAndShowConsole(project, packageJson);
    }
  }

  private static void showErrorDetails(@NotNull Project project, @NotNull String text) {
    ProcessOutput output = new ProcessOutput();
    output.appendStderr(text);
    JsqtProcessOutputViewer.show(project, PrettierUtil.PACKAGE_NAME, PrettierUtil.ICON, null, null, output);
  }

  private static void editSettings(@NotNull Project project) {
    new PrettierConfigurable(project).showEditDialog();
  }

  /**
   * @returns true if line separator was updated
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
                   @NotNull String text,
                   @Nullable Runnable onLinkClick);

    default void showErrorWithDetails(@NotNull Project project,
                                      @Nullable Editor editor,
                                      @NotNull String text,
                                      @NotNull String details) {
      showError(project, editor, text, () -> showErrorDetails(project, details));
    }
  }

  private static class DefaultErrorHandler implements ErrorHandler {
    @Override
    public void showError(@NotNull Project project, @Nullable Editor editor, @NotNull String text, @Nullable Runnable onLinkClick) {
      if (editor != null) {
        showHintLater(editor, "Prettier: " + text, true, toHyperLinkListener(onLinkClick));
      }
      else {
        showErrorNotification(project, text, toNotificationListener(onLinkClick));
      }
    }

    private static void showErrorNotification(@NotNull Project project,
                                              @NotNull String text,
                                              @Nullable NotificationListener notificationListener) {
      JSLinterGuesser.NOTIFICATION_GROUP
        .createNotification("Prettier", text, NotificationType.ERROR, notificationListener)
        .notify(project);
    }

    @Nullable
    private static NotificationListener toNotificationListener(@Nullable Runnable runnable) {
      if (runnable == null) return null;
      return new NotificationListener.Adapter() {
        @Override
        protected void hyperlinkActivated(@NotNull Notification notification, @NotNull HyperlinkEvent e) {
          runnable.run();
        }
      };
    }

    @Nullable
    private static HyperlinkListener toHyperLinkListener(@Nullable Runnable runnable) {
      if (runnable == null) return null;
      return new HyperlinkAdapter() {
        @Override
        protected void hyperlinkActivated(HyperlinkEvent e) {
          runnable.run();
        }
      };
    }
  }
}
