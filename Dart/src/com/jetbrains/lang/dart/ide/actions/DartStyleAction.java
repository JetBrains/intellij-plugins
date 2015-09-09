package com.jetbrains.lang.dart.ide.actions;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import gnu.trove.THashMap;
import icons.DartIcons;
import org.dartlang.analysis.server.protocol.SourceEdit;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class DartStyleAction extends AbstractDartFileProcessingAction {

  private static final Logger LOG = Logger.getInstance(DartStyleAction.class.getName());

  public DartStyleAction() {
    super(DartBundle.message("dart.style.action.name"), DartBundle.message("dart.style.action.description"), DartIcons.Dart_16);
  }

  @NotNull
  @Override
  protected String getActionTextForEditor() {
    return DartBundle.message("dart.style.action.name");
  }

  @NotNull
  @Override
  protected String getActionTextForFiles() {
    return DartBundle.message("dart.style.action.name.ellipsis"); // because with dialog
  }

  protected void runOverEditor(@NotNull final Project project, @NotNull final Editor editor, @NotNull final PsiFile psiFile) {
    final Document document = editor.getDocument();
    if (!ReadonlyStatusHandler.ensureDocumentWritable(project, document)) return;

    final Runnable runnable = new Runnable() {
      public void run() {
        final String path = psiFile.getVirtualFile().getPath();
        final int caretOffset = editor.getCaretModel().getOffset();
        final int lineLength = getRightMargin(project);

        DartAnalysisServerService.getInstance().updateFilesContent();
        DartAnalysisServerService.FormatResult formatResult =
          DartAnalysisServerService.getInstance().edit_format(path, caretOffset, 0, lineLength);

        if (formatResult == null) {
          showHintLater(editor, DartBundle.message("dart.style.hint.failed"), true);
          LOG.warn("Unexpected response from edit_format, formatResult is null");
          return;
        }

        final List<SourceEdit> edits = formatResult.getEdits();
        if (edits == null || edits.size() == 0) {
          showHintLater(editor, DartBundle.message("dart.style.hint.already.good"), false);
        }
        else if (edits.size() == 1) {
          document.replaceString(0, document.getTextLength(), edits.get(0).getReplacement());
          editor.getCaretModel().moveToOffset(formatResult.getOffset());
          showHintLater(editor, DartBundle.message("dart.style.hint.success"), false);
        }
        else {
          showHintLater(editor, DartBundle.message("dart.style.hint.failed"), true);
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

  protected void runOverFiles(@NotNull final Project project, @NotNull final List<VirtualFile> dartFiles) {
    if (dartFiles.isEmpty()) {
      Messages.showInfoMessage(project, DartBundle.message("dart.style.files.no.dart.files"), DartBundle.message("dart.style.action.name"));
      return;
    }

    if (Messages.showOkCancelDialog(project, DartBundle.message("dart.style.files.dialog.question", dartFiles.size()),
                                    DartBundle.message("dart.style.action.name"), null) != Messages.OK) {
      return;
    }

    final Map<VirtualFile, String> fileToNewContentMap = new THashMap<VirtualFile, String>();
    final int lineLength = getRightMargin(project);

    final Runnable runnable = new Runnable() {
      public void run() {
        double fraction = 0.0;
        for (final VirtualFile virtualFile : dartFiles) {
          fraction += 1.0;
          final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
          if (indicator != null) {
            indicator.checkCanceled();
            indicator.setFraction(fraction / dartFiles.size());
            indicator.setText2(FileUtil.toSystemDependentName(virtualFile.getPath()));
          }

          final String path = virtualFile.getPath();
          final DartAnalysisServerService.FormatResult formatResult =
            DartAnalysisServerService.getInstance().edit_format(path, 0, 0, lineLength);
          if (formatResult != null && formatResult.getEdits() != null && formatResult.getEdits().size() == 1) {
            fileToNewContentMap.put(virtualFile, formatResult.getEdits().get(0).getReplacement());
          }
        }
      }
    };

    DartAnalysisServerService.getInstance().updateFilesContent();

    final boolean ok = ApplicationManagerEx.getApplicationEx()
      .runProcessWithProgressSynchronously(runnable, DartBundle.message("dart.style.action.name"), true, project);

    if (ok) {
      final Runnable onSuccessRunnable = new Runnable() {
        @Override
        public void run() {
          CommandProcessor.getInstance().markCurrentCommandAsGlobal(project);

          for (Map.Entry<VirtualFile, String> entry : fileToNewContentMap.entrySet()) {
            final VirtualFile file = entry.getKey();
            final Document document = FileDocumentManager.getInstance().getDocument(file);
            final String newContent = entry.getValue();

            if (document != null && newContent != null) {
              document.setText(newContent);
            }
          }
        }
      };

      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        @Override
        public void run() {
          CommandProcessor.getInstance().executeCommand(project, onSuccessRunnable, DartBundle.message("dart.style.action.name"), null);
        }
      });
    }
  }

  private static int getRightMargin(@NotNull Project project) {
    return CodeStyleSettingsManager.getSettings(project).getCommonSettings(DartLanguage.INSTANCE).RIGHT_MARGIN;
  }
}
