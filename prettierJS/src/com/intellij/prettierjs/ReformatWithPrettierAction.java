package com.intellij.prettierjs;

import com.intellij.CommonBundle;
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
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.linter.JsqtProcessOutputViewer;
import com.intellij.lang.javascript.modules.InstallNodeLocalDependenciesAction;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.service.JSLanguageServiceUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.util.CaretVisualPositionKeeper;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.options.ex.Settings;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.css.CssFile;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.ui.LightweightHint;
import com.intellij.util.ArrayUtil;
import com.intellij.util.NullableFunction;
import com.intellij.util.text.SemVer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ReformatWithPrettierAction extends AnAction implements DumbAware {
  private static final int REQUEST_TIMEOUT = 3000;

  @Override
  public void update(AnActionEvent e) {
    Project project = e.getProject();
    e.getPresentation().setVisible(project != null && PrettierUtil.isEnabled());
    Editor editor = e.getData(CommonDataKeys.EDITOR);
    boolean isEnabled = (editor != null && isAcceptableFile(e.getData(CommonDataKeys.PSI_FILE)))
                        || (editor == null && !(ArrayUtil.isEmpty(e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY))));
    e.getPresentation().setEnabled(isEnabled);
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
    NodeJsInterpreter interpreter = configuration.getOrDetectInterpreterRef().resolve(project);
    try {
      NodeJsLocalInterpreter.castAndValidate(interpreter);
    }
    catch (ExecutionException e1) {
      showHintLater(project, editor, PrettierBundle.message("error.invalid.interpreter"), true,
                    toHyperLinkListener(() -> editSettings(project, e.getDataContext())));
      return;
    }

    NodePackage nodePackage = configuration.getOrDetectNodePackage();
    if (nodePackage == null || nodePackage.isEmptyPath()) {
      showHintLater(project, editor, PrettierBundle.message("error.no.valid.package"), true,
                    toHyperLinkListener(() -> editSettings(project, e.getDataContext())));
      return;
    }
    if (!nodePackage.isValid()) {
      showHintLater(project, editor, PrettierBundle.message("error.package.is.not.installed"), true,
                    toHyperLinkListener(() -> installPackage(project)));
      return;
    }
    SemVer nodePackageVersion = nodePackage.getVersion();
    if (nodePackageVersion != null && !nodePackageVersion.isGreaterOrEqualThan(1, 7, 0)) {
      showHintLater(project, editor, PrettierBundle.message("error.unsupported.version"), true, null);
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

  private static void processFileInEditor(@NotNull Project project, @NotNull Editor editor, @NotNull NodePackage nodePackage) {
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
      .runProcessWithProgressSynchronously(() -> processFileWithService(file, nodePackage, range),
                                           PrettierBundle.message("progress.title"), true, project);
    if (result == null) {
      return;
    }
    if (!StringUtil.isEmpty(result.error)) {
      showHintLater(project, editor, PrettierBundle.message("error.while.reformatting.message"), true, toHyperLinkListener(() -> {
        ProcessOutput output = new ProcessOutput();
        output.appendStderr(result.error);
        JsqtProcessOutputViewer.show(project, PrettierUtil.PACKAGE_NAME, PrettierUtil.ICON, null, null, output);
      }));
    }
    else {
      runWriteCommandAction(project, () -> document.replaceString(0, document.getTextLength(), result.result));
      caretVisualPositionKeeper.restoreOriginalLocation(true);
      showHintLater(project, editor, buildNotificationMessage(document, textBefore), false, null);
    }
  }

  private static void processVirtualFiles(@NotNull Project project,
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
      processFileIterator(project, new FileTreeIterator(psiDirectory), nodePackage);
    }
    else {
      processFileIterator(project, new FileTreeIterator(PsiUtilCore.toPsiFiles(psiManager, Arrays.asList(virtualFiles))), nodePackage);
    }
  }

  private static void processFileIterator(@NotNull Project project,
                                          @NotNull final FileTreeIterator fileIterator,
                                          @NotNull NodePackage nodePackage) {
    Map<PsiFile, String> fileToFormattedText = executeUnderProgress(project, indicator -> {
      Map<PsiFile, String> reformattedResults = new HashMap<>();

      while (fileIterator.hasNext()) {
        PsiFile currentFile = fileIterator.next();
        if (!isAcceptableFile(currentFile)) {
          continue;
        }
        indicator.setText("Processing " + currentFile.getName());
        PrettierLanguageService.FormatResult result = ReadAction.compute(() -> processFileWithService(currentFile, nodePackage, null));
        if (result == null) {
          continue;
        }
        if (!StringUtil.isEmpty(result.error)) {
          return null;
        }
        reformattedResults.put(currentFile, result.result);
      }
      return reformattedResults;
    });
    if (fileToFormattedText == null) {
      return;
    }
    runWriteCommandAction(project, () -> {
      for (Map.Entry<PsiFile, String> entry : fileToFormattedText.entrySet()) {
        VirtualFile virtualFile = entry.getKey().getVirtualFile();
        if (virtualFile == null) {
          continue;
        }
        Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
        if (document == null) {
          continue;
        }
        document.setText(entry.getValue());
      }
    });
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

  private static void showHintLater(@NotNull Project project,
                                    @Nullable Editor editor,
                                    String text,
                                    boolean isError,
                                    @Nullable HyperlinkListener hyperlinkListener) {
    ApplicationManager.getApplication().invokeLater(() -> {
      if (editor == null) {
        if (isError) {
          Messages.showMessageDialog(project, text, CommonBundle.getWarningTitle(), Messages.getWarningIcon());
        }
        return;
      }
      final JComponent component = isError ? HintUtil.createErrorLabel(text, hyperlinkListener, null, null)
                                           : HintUtil.createInformationLabel(text, hyperlinkListener, null, null);
      final LightweightHint hint = new LightweightHint(component);
      HintManagerImpl.getInstanceImpl()
                     .showEditorHint(hint, editor, HintManager.UNDER, HintManager.HIDE_BY_ANY_KEY | HintManager.HIDE_BY_TEXT_CHANGE |
                                                                      HintManager.HIDE_BY_SCROLLING, 0, false);
    }, ModalityState.NON_MODAL, o -> editor != null && (editor.isDisposed() || !editor.getComponent().isShowing()));
  }

  private static void installPackage(@NotNull Project project) {
    final VirtualFile packageJson = project.getBaseDir().findChild(PackageJsonUtil.FILE_NAME);
    if (packageJson != null) {
      InstallNodeLocalDependenciesAction.runAndShowConsole(project, packageJson);
    }
  }

  private static void editSettings(@NotNull Project project, @NotNull DataContext dataContext) {
    PrettierConfigurable configurable = new PrettierConfigurable(project);
    Settings settings = Settings.KEY.getData(dataContext);
    if (settings == null) {
      configurable.showEditDialog();
    }
    else {
      settings.select(settings.find(configurable.getId()));
    }
  }

  private static boolean isAcceptableFile(@Nullable PsiFile file) {
    return file != null && file.isPhysical() && file instanceof JSFile || file instanceof CssFile;
  }

  private static HyperlinkListener toHyperLinkListener(Runnable runnable) {
    return new HyperlinkAdapter() {
      @Override
      protected void hyperlinkActivated(HyperlinkEvent e) {
        runnable.run();
      }
    };
  }
}
