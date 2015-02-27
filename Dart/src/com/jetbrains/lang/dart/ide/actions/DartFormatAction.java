package com.jetbrains.lang.dart.ide.actions;

import com.google.dart.server.generated.types.SourceEdit;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilBase;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import icons.DartIcons;


public class DartFormatAction extends EditorAction {
  public DartFormatAction() {
    super(new Handler());
    super.getTemplatePresentation().setIcon(DartIcons.Dart_file);
    setInjectedContext(true);
  }

  private static class Handler extends EditorWriteActionHandler {
    public Handler() {
      super(true);
    }

    @Override
    public void executeWriteAction(Editor editor, Caret caret, DataContext dataContext) {

      int start = editor.getDocument().getTextLength();
      int begin = editor.getCaretModel().getOffset();

      Project project = CommonDataKeys.PROJECT.getData(dataContext);
      if (project == null) return;
      PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);
      if (psiFile == null) return;

      DartAnalysisServerService.getInstance().updateFilesContent();
      DartAnalysisServerService.FormatResult formatResult =
        DartAnalysisServerService.getInstance().edit_format(psiFile.getVirtualFile().getPath(), 0, 1);

      if (formatResult != null && formatResult.getEdits() != null && !formatResult.getEdits().isEmpty()) {
        for (SourceEdit edit : formatResult.getEdits()) {
          editor.getDocument().replaceString(0, editor.getDocument().getTextLength(), edit.getReplacement());
          int end = editor.getDocument().getTextLength();
          editor.getCaretModel().moveToOffset(begin + end - start);
          //this is not a really solid, it works only in some cases
        }
      }
    }
  }
}
