package com.intellij.prettierjs;

import com.intellij.codeInsight.actions.FileTreeIterator;
import com.intellij.codeInsight.actions.FormatChangedTextUtil;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.hint.HintManagerImpl;
import com.intellij.codeInsight.hint.HintUtil;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.DialectOptionHolder;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.linter.JSLinterGuesser;
import com.intellij.lang.javascript.linter.JsqtProcessOutputViewer;
import com.intellij.lang.javascript.modules.InstallNodeLocalDependenciesAction;
import com.intellij.lang.javascript.psi.JSFile;
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
import com.intellij.openapi.util.TextRange;
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
import com.intellij.util.NullableFunction;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.text.SemVer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReformatWithPrettierAction extends AnAction implements DumbAware {
  private static final int REQUEST_TIMEOUT = 3000;
  private final ErrorHandler myErrorHandler;

  public ReformatWithPrettierAction(ErrorHandler errorHandler) {
    myErrorHandler = errorHandler;
  }

  public ReformatWithPrettierAction() {
    this(ErrorHandler.DEFAULT);
  }

  @Override
  public void update(AnActionEvent e) {
    Project project = e.getProject();
    if (project == null) {
      e.getPresentation().setEnabledAndVisible(false);
      return;
    }
    NodePackage nodePackage = PrettierConfiguration.getInstance(project).getPackage();
    e.getPresentation().setEnabledAndVisible(PrettierUtil.isEnabled()
                                             && nodePackage != null && !nodePackage.isEmptyPath()
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
  public void actionPerformed(AnActionEvent e) {
    Project project = e.getProject();
    if (project == null) {
      return;
    }
    Editor editor = e.getData(CommonDataKeys.EDITOR);
    if (!PrettierUtil.isEnabled()) {
      return;
    }
    PrettierConfiguration configuration = PrettierConfiguration.getInstance(project);
    NodeJsInterpreter interpreter = configuration.getInterpreterRef().resolve(project);
    try {
      NodeJsLocalInterpreter.castAndValidate(interpreter);
    }
    catch (ExecutionException e1) {
      myErrorHandler.showError(project, editor, PrettierBundle.message("error.invalid.interpreter"),
                () -> editSettings(project));
      return;
    }

    NodePackage nodePackage = configuration.getPackage();
    if (nodePackage == null || nodePackage.isEmptyPath()) {
      myErrorHandler.showError(project, editor, PrettierBundle.message("error.no.valid.package"),
                () -> editSettings(project));
      return;
    }
    if (!nodePackage.isValid()) {
      myErrorHandler.showError(project, editor, PrettierBundle.message("error.package.is.not.installed"),
                () -> installPackage(project));
      return;
    }
    SemVer nodePackageVersion = nodePackage.getVersion();
    if (nodePackageVersion != null && !nodePackageVersion.isGreaterOrEqualThan(1, 7, 0)) {
      myErrorHandler.showError(project, editor, PrettierBundle.message("error.unsupported.version"), null);
      return;
    }

    if (editor != null) {
      processFileInEditor(project, editor, nodePackage);
    }
    else {
      VirtualFile[] virtualFiles = CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(e.getDataContext());
      if (!ArrayUtil.isEmpty(virtualFiles)) {
        processVirtualFiles(project, virtualFiles, nodePackage);
      }  
    }
  }

  private void processFileInEditor(@NotNull Project project, @NotNull Editor editor, @NotNull NodePackage nodePackage) {
    PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
    if (file == null) {
      return;
    }
    ReadonlyStatusHandler.OperationStatus readonlyStatus = ReadonlyStatusHandler.getInstance(project)
                                                                                .ensureFilesWritable(file.getVirtualFile());
    if (readonlyStatus.hasReadonlyFiles()) {
      return;
    }

    TextRange range = editor.getSelectionModel().hasSelection()
                      ? new TextRange(editor.getSelectionModel().getSelectionStart(), editor.getSelectionModel().getSelectionEnd()) : null;
    Document document = editor.getDocument();
    CharSequence textBefore = document.getImmutableCharSequence();
    CaretVisualPositionKeeper caretVisualPositionKeeper = new CaretVisualPositionKeeper(document);
    FileDocumentManager.getInstance().saveAllDocuments();
    PrettierLanguageService.FormatResult result = ProgressManager
      .getInstance()
      .runProcessWithProgressSynchronously(() -> {
                                             if (!isAcceptableFile(file, nodePackage)) {
                                               myErrorHandler.showError(project, editor, PrettierBundle.message("not.supported.file", file.getName()), null);
                                               return null;
                                             }
                                             return processFileWithService(file, nodePackage, range);
                                           },
                                           PrettierBundle.message("progress.title"), true, project);
    if (result == null) {
      return;
    }
    if (!StringUtil.isEmpty(result.error)) {
      myErrorHandler.showErrorWithDetails(project, editor,
                                          PrettierBundle.message("error.while.reformatting.message"), result.error);
    }
    else {
      if (!StringUtil.equals(textBefore, result.result)) {
        runWriteCommandAction(project, () -> document.setText(result.result));
        caretVisualPositionKeeper.restoreOriginalLocation(true);
      }
      showHintLater(editor, buildNotificationMessage(document, textBefore), false, null);
    }
  }

  private void processVirtualFiles(@NotNull Project project,
                                          @NotNull VirtualFile[] virtualFiles,
                                          @NotNull NodePackage nodePackage) {
    ReadonlyStatusHandler.OperationStatus readonlyStatus = ReadonlyStatusHandler.getInstance(project).ensureFilesWritable(virtualFiles);
    if (readonlyStatus.hasReadonlyFiles()) {
      return;
    }
    PsiManager psiManager = PsiManager.getInstance(project);
    if (virtualFiles.length == 1 && virtualFiles[0].isDirectory()) {
      PsiDirectory psiDirectory = psiManager.findDirectory(virtualFiles[0]);
      if (psiDirectory == null) {
        return;
      }
      processFileIterator(project, new FileTreeIterator(psiDirectory), nodePackage, false);
    }
    else {
      processFileIterator(project, new FileTreeIterator(PsiUtilCore.toPsiFiles(psiManager, Arrays.asList(virtualFiles))), 
                          nodePackage, true);
    }
  }

  private void processFileIterator(@NotNull Project project,
                                   @NotNull final FileTreeIterator fileIterator,
                                   @NotNull NodePackage nodePackage, 
                                   boolean reportUnsupported) {
    Map<PsiFile, PrettierLanguageService.FormatResult> results = executeUnderProgress(project, indicator -> {
      Map<PsiFile, PrettierLanguageService.FormatResult> reformattedResults = new HashMap<>();

      while (fileIterator.hasNext()) {
        PsiFile currentFile = ReadAction.compute(() -> fileIterator.next());
        if (!isAcceptableFile(currentFile, nodePackage)) {
          if (reportUnsupported) {
            reformattedResults.put(currentFile,
                                   PrettierLanguageService.FormatResult.error(PrettierBundle.message("not.supported.file", currentFile.getName())));
          }
          continue;
        }
        indicator.setText("Processing " + currentFile.getName());
        PrettierLanguageService.FormatResult result = ReadAction.compute(() -> processFileWithService(currentFile, nodePackage, null));
        if (result == null) {
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
        Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
        if (document != null && StringUtil.isEmpty(entry.getValue().error)) {
          CharSequence textBefore = document.getCharsSequence();
          if (!StringUtil.equals(textBefore, entry.getValue().result)) {
            document.setText(entry.getValue().result);
          }
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
  private static PrettierLanguageService.FormatResult processFileWithService(@NotNull PsiFile currentFile,
                                                                             @NotNull NodePackage nodePackage,
                                                                             @Nullable TextRange range) {
    PrettierLanguageService service = PrettierLanguageService.getInstance(currentFile.getProject());
    return JSLanguageServiceUtil.awaitFuture(service.format(currentFile, nodePackage, range), REQUEST_TIMEOUT);
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

  private static String buildNotificationMessage(@NotNull Document document, @NotNull CharSequence textBefore) {
    int number = FormatChangedTextUtil.getInstance().calculateChangedLinesNumber(document, textBefore);
    return "Prettier: " + (number > 0 ? "Reformatted " + number + " lines" : "No lines changed. Content is already properly formatted");
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

  private static boolean isAcceptableFile(@Nullable PsiFile file, @NotNull NodePackage nodePackage) {
    if (file == null || (!file.isPhysical())) {
      return false;
    }
    return isDetectedAcceptableFile(file.getVirtualFile(), file.getProject(), nodePackage) || isDefaultAcceptableFile(file);
  }

  private static boolean isDefaultAcceptableFile(@Nullable PsiFile file) {
    if (file instanceof JSFile) {
      DialectOptionHolder optionHolder = DialectDetector.dialectOfElement(file);
      return optionHolder == null || (!optionHolder.isCoffeeScript && !optionHolder.isECMA4);
    }
    return false;
  }

  private static boolean isDetectedAcceptableFile(@Nullable VirtualFile virtualFile,
                                                  @NotNull Project project,
                                                  @NotNull NodePackage nodePackage) {
    if (virtualFile == null) {
      return false;
    }
    PrettierLanguageServiceImpl languageService = PrettierLanguageService.getInstance(project);
    PrettierLanguageService.SupportedFilesInfo supportedFiles = JSLanguageServiceUtil.awaitFuture(languageService.getSupportedFiles(nodePackage), REQUEST_TIMEOUT);
    if (supportedFiles != null) {
      
      String nameWithoutExtension = virtualFile.getNameWithoutExtension();
      if (StringUtil.isEmpty(nameWithoutExtension)) {
        nameWithoutExtension = virtualFile.getName();
      }
      return supportedFiles.fileNames.contains(nameWithoutExtension)
             || supportedFiles.extensions.contains(virtualFile.getExtension());
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
