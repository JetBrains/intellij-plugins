package com.jetbrains.lang.dart.ide.actions;

import com.google.dart.server.generated.types.SourceEdit;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtilBase;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.sdk.DartSdk;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DartStyleAction extends AnAction implements DumbAware {

  private final static String DART_STYLE_COMMAND_NAME = "Reformat with Dart Style";

  public DartStyleAction() {
    super("Dart Style", "", DartIcons.Dart_16);
  }

  @Override
  public void actionPerformed(final AnActionEvent event) {
    final DataContext dataContext = event.getDataContext();
    final Project project = CommonDataKeys.PROJECT.getData(dataContext);
    if (project == null) {
      return;
    }

    PsiDocumentManager.getInstance(project).commitAllDocuments();
    final Editor editor = CommonDataKeys.EDITOR.getData(dataContext);

    if (editor != null) {
      runDartStyleOverEditor(project, editor);
    }
  }

  @Override
  public void update(final AnActionEvent event) {
    super.update(event);
    final Presentation presentation = event.getPresentation();
    final DataContext dataContext = event.getDataContext();
    final Project project = CommonDataKeys.PROJECT.getData(dataContext);
    if (project == null) {
      presentation.setVisible(false);
      presentation.setEnabled(false);
      return;
    }

    presentation.setVisible(FileTypeIndex.containsFileOfType(DartFileType.INSTANCE, GlobalSearchScope.projectScope(project)));

    final Editor editor = CommonDataKeys.EDITOR.getData(dataContext);

    if (editor != null) {
      PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
      if (file == null || file.getVirtualFile() == null || file.getFileType() != DartFileType.INSTANCE) {
        presentation.setEnabled(false);
        return;
      }
      else {
        presentation.setEnabled(true);
        return;
      }
    }
    presentation.setEnabled(false);
  }

  private static void runDartStyleOverEditor(@NotNull final Project project, @NotNull final Editor editor) {
    final PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);
    if (psiFile == null) return;

    final Runnable runnable = new Runnable() {
      public void run() {
        final Document document = editor.getDocument();
        final String path = FileUtil.toSystemDependentName(psiFile.getVirtualFile().getPath());
        int caretOffset = editor.getCaretModel().getOffset();

        DartAnalysisServerService.getInstance().updateFilesContent();
        DartAnalysisServerService.FormatResult formatResult =
          DartAnalysisServerService.getInstance().edit_format(path, caretOffset, 0);

        if (formatResult != null && formatResult.getEdits() != null && formatResult.getEdits().size() == 1) {
          for (SourceEdit edit : formatResult.getEdits()) {
            document.replaceString(0, document.getTextLength(), edit.getReplacement());
            editor.getCaretModel().moveToOffset(formatResult.getOffset());
          }
        }
      }
    };
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        CommandProcessor.getInstance().executeCommand(project, runnable, DART_STYLE_COMMAND_NAME, null);
      }
    });
  }
}
