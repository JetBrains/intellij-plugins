package com.jetbrains.lang.dart.ide.editor;

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.psi.DartType;
import com.jetbrains.lang.dart.util.UsefulPsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public class DartTypeHandler extends TypedHandlerDelegate {
  private boolean myAfterTypeOrComponentName = false;

  @Override
  public Result beforeCharTyped(char c,
                                Project project,
                                Editor editor,
                                PsiFile file,
                                FileType fileType) {
    if (c == '<') {
      myAfterTypeOrComponentName = checkAfterTypeOrComponentName(file, editor.getCaretModel().getOffset());
    }
    return super.beforeCharTyped(c, project, editor, file, fileType);
  }

  private static boolean checkAfterTypeOrComponentName(PsiFile file, int offset) {
    PsiElement at = file.findElementAt(offset - 1);
    PsiElement toCheck = UsefulPsiTreeUtil.getPrevSiblingSkipWhiteSpacesAndComments(at, false);
    return PsiTreeUtil.getParentOfType(toCheck, DartType.class, DartComponentName.class) != null;
  }

  @Override
  public Result charTyped(char c, Project project, Editor editor, @NotNull PsiFile file) {
    if (c == '<' && myAfterTypeOrComponentName) {
      myAfterTypeOrComponentName = false;
      int offset = editor.getCaretModel().getOffset();
      if (offset >= 0) {
        editor.getDocument().insertString(offset, ">");
        editor.getCaretModel().moveToOffset(offset);
        return Result.STOP;
      }
    }
    return super.charTyped(c, project, editor, file);
  }
}
