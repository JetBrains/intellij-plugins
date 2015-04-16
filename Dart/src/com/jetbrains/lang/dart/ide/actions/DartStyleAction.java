package com.jetbrains.lang.dart.ide.actions;

import com.google.dart.server.generated.types.SourceEdit;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.hint.HintManagerImpl;
import com.intellij.codeInsight.hint.HintUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.ui.LightweightHint;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerAnnotator;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.ide.DartWritingAccessProvider;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkGlobalLibUtil;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class DartStyleAction extends AnAction implements DumbAware {

  private static final Logger LOG = Logger.getInstance(DartStyleAction.class.getName());

  public DartStyleAction() {
    super(DartBundle.message("dart.style.action.name"), DartBundle.message("dart.style.action.description"), DartIcons.Dart_16);
  }

  @Override
  public void actionPerformed(final AnActionEvent event) {
    final Project project = event.getProject();
    if (project == null) {
      return;
    }

    PsiDocumentManager.getInstance(project).commitAllDocuments();
    final Editor editor = event.getData(CommonDataKeys.EDITOR);

    if (editor != null) {
      runDartStyleOverEditor(project, editor);
    }
  }

  @Override
  public void update(final AnActionEvent event) {
    final Presentation presentation = event.getPresentation();
    final Project project = event.getProject();
    if (project == null) {
      presentation.setVisible(false);
      presentation.setEnabled(false);
      return;
    }

    final Editor editor = event.getData(CommonDataKeys.EDITOR);
    if (editor != null) {
      final PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
      // visible for any Dart file, but enabled for applicable only
      presentation.setVisible(psiFile != null && psiFile.getFileType() == DartFileType.INSTANCE);
      presentation.setEnabled(isApplicableFile(psiFile));
      return;
    }

    presentation.setEnabledAndVisible(false);
  }

  private static boolean isApplicableFile(@Nullable final PsiFile psiFile) {
    if (psiFile == null || psiFile.getVirtualFile() == null || psiFile.getFileType() != DartFileType.INSTANCE) return false;

    final Module module = ModuleUtilCore.findModuleForPsiElement(psiFile);
    if (module == null) return false;

    final DartSdk sdk = DartSdk.getDartSdk(module.getProject());
    if (sdk == null || !DartAnalysisServerAnnotator.isDartSDKVersionSufficient(sdk)) return false;

    if (!DartSdkGlobalLibUtil.isDartSdkGlobalLibAttached(module, sdk.getGlobalLibName())) return false;

    if (DartWritingAccessProvider.isInDartSdkOrDartPackagesFolder(psiFile)) return false;

    return true;
  }

  private static void runDartStyleOverEditor(@NotNull final Project project, @NotNull final Editor editor) {
    final PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
    if (!isApplicableFile(psiFile)) return;

    final Document document = editor.getDocument();
    if (!ReadonlyStatusHandler.ensureDocumentWritable(project, document)) return;

    final DartSdk sdk = DartSdk.getDartSdk(project);
    if (sdk == null || !DartAnalysisServerService.getInstance().serverReadyForRequest(project, sdk)) return;

    final Runnable runnable = new Runnable() {
      public void run() {
        final String path = FileUtil.toSystemDependentName(psiFile.getVirtualFile().getPath());
        int caretOffset = editor.getCaretModel().getOffset();

        DartAnalysisServerService.getInstance().updateFilesContent();
        DartAnalysisServerService.FormatResult formatResult =
          DartAnalysisServerService.getInstance().edit_format(path, caretOffset, 0);

        if (formatResult == null) {
          showHintLater(editor, DartBundle.message("dart.style.failed"), true);
          LOG.warn("Unexpected response from edit_format, formatResult is null");
          return;
        }

        final List<SourceEdit> edits = formatResult.getEdits();
        if (edits == null || edits.size() == 0) {
          showHintLater(editor, DartBundle.message("dart.style.already.good"), false);
        }
        else if (edits.size() == 1) {
          document.replaceString(0, document.getTextLength(), edits.get(0).getReplacement());
          editor.getCaretModel().moveToOffset(formatResult.getOffset());
          showHintLater(editor, DartBundle.message("dart.style.success"), false);
        }
        else {
          showHintLater(editor, DartBundle.message("dart.style.failed"), true);
          LOG.warn("Unexpected response from edit_format, formatResult.getEdits().size() = " + edits.size());
        }
      }
    };

    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        CommandProcessor.getInstance().executeCommand(project, runnable, DartBundle.message("dart.style.action.name"), null);
      }
    });
  }

  private static void showHintLater(@NotNull final Editor editor, @NotNull final String text, final boolean error) {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        final JComponent component = error ? HintUtil.createErrorLabel(text)
                                           : HintUtil.createInformationLabel(text);
        final LightweightHint hint = new LightweightHint(component);
        HintManagerImpl.getInstanceImpl().showEditorHint(hint, editor, HintManager.UNDER,
                                                         HintManager.HIDE_BY_ANY_KEY |
                                                         HintManager.HIDE_BY_TEXT_CHANGE |
                                                         HintManager.HIDE_BY_SCROLLING,
                                                         0, false);
      }
    }, ModalityState.NON_MODAL, new Condition() {
      @Override
      public boolean value(Object o) {
        return editor.isDisposed() || !editor.getComponent().isShowing();
      }
    });
  }
}
