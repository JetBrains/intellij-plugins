package com.intellij.prettierjs;

import com.intellij.codeInsight.actions.FormatChangedTextUtil;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.hint.HintManagerImpl;
import com.intellij.codeInsight.hint.HintUtil;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.ide.DataManager;
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
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.util.CaretVisualPositionKeeper;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.options.ex.Settings;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.css.CssFile;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.ui.LightweightHint;
import com.intellij.util.text.SemVer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class ReformatWithPrettierAction extends AnAction implements DumbAware {
  private static final int REQUEST_TIMEOUT = 3000;

  @Override
  public void update(AnActionEvent e) {
    PsiFile file = e.getData(CommonDataKeys.PSI_FILE);

    Project project = e.getProject();
    e.getPresentation().setVisible(project != null && PrettierUtil.isEnabled());
    e.getPresentation().setEnabled(isAcceptableFile(file));
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    Project project = e.getProject();
    if (project == null) {
      return;
    }
    PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
    Editor editor = e.getData(CommonDataKeys.EDITOR);
    if (editor == null || file == null) {
      return;
    }
    if (!PrettierUtil.isEnabled()) {
      return;
    }

    PrettierLanguageService service = PrettierLanguageService.getInstance(project);
    TextRange range = editor.getSelectionModel().hasSelection() ? new TextRange(editor.getSelectionModel().getSelectionStart(),
                                                                                editor.getSelectionModel().getSelectionEnd()) : null;
    NodePackage nodePackage = PrettierConfiguration.getInstance(project).getOrDetectNodePackage();
    if (nodePackage == null || nodePackage.isEmptyPath()) {
      showHintLater(editor, PrettierBundle.message("error.no.valid.package"), true,
                    toHyperLinkListener(() -> editSettings(project, editor)));
      return;
    }
    if (!nodePackage.isValid()) {
      showHintLater(editor, PrettierBundle.message("error.package.is.not.installed"), true,
                    toHyperLinkListener(() -> installPackage(project)));
      return;
    }
    SemVer nodePackageVersion = nodePackage.getVersion();
    if (nodePackageVersion != null && !nodePackageVersion.isGreaterOrEqualThan(1, 7, 0)) {
      showHintLater(editor, PrettierBundle.message("error.unsupported.version"), true, null);
      return;
    }
    Document document = editor.getDocument();
    CharSequence textBefore = document.getImmutableCharSequence();
    CaretVisualPositionKeeper caretVisualPositionKeeper = new CaretVisualPositionKeeper(document);
    FileDocumentManager.getInstance().saveAllDocuments();
    PrettierLanguageService.FormatResult result = ProgressManager
      .getInstance()
      .runProcessWithProgressSynchronously(
        () -> JSLanguageServiceUtil.awaitFuture(service.format(file, nodePackage, range), REQUEST_TIMEOUT),
        PrettierBundle.message("progress.title"), true, project);
    if (result == null) {
      return;
    }
    if (!StringUtil.isEmpty(result.error)) {
      showHintLater(editor, PrettierBundle.message("error.while.reformatting.message"), true, toHyperLinkListener(() -> {
        ProcessOutput output = new ProcessOutput();
        output.appendStderr(result.error);
        JsqtProcessOutputViewer.show(project, PrettierUtil.PACKAGE_NAME, PrettierUtil.ICON, null, null, output);
      }));
    }
    else {
      WriteCommandAction.runWriteCommandAction(project, () -> document.replaceString(0, document.getTextLength(), result.result));
      caretVisualPositionKeeper.restoreOriginalLocation(true);
      showHintLater(editor, buildNotificationMessage(document, textBefore), false, null);
    }
  }

  private static String buildNotificationMessage(@NotNull Document document, @NotNull CharSequence textBefore) {
    int number = FormatChangedTextUtil.getInstance().calculateChangedLinesNumber(document, textBefore);
    return "Prettier: " + (number > 0 ? "Reformatted " + number + " lines" : "No lines changed. Content is already properly formatted");
  }

  private static void showHintLater(@NotNull Editor editor, String text, boolean isError, @Nullable HyperlinkListener hyperlinkListener) {
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

  private static void editSettings(@NotNull Project project, @NotNull Editor editor) {
    DataContext dataContext = DataManager.getInstance().getDataContext(editor.getComponent());
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
