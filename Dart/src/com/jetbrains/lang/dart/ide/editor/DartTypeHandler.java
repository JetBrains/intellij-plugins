package com.jetbrains.lang.dart.ide.editor;

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.psi.DartPsiCompositeElement;
import com.jetbrains.lang.dart.psi.DartType;
import com.jetbrains.lang.dart.util.UsefulPsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public class DartTypeHandler extends TypedHandlerDelegate {
  private boolean myAfterTypeOrComponentName = false;
  private boolean myAfterDollar = false;


  @Override
  public Result beforeCharTyped(char c,
                                Project project,
                                Editor editor,
                                PsiFile file,
                                FileType fileType) {
    if (c == '<') {
      myAfterTypeOrComponentName = checkAfterTypeOrComponentName(file, editor.getCaretModel().getOffset());
    }
    if (c == '{') {
      myAfterDollar = checkAfterDollarInString(file, editor.getCaretModel().getOffset());
    }
    return super.beforeCharTyped(c, project, editor, file, fileType);
  }

  private static boolean checkAfterTypeOrComponentName(PsiFile file, int offset) {
    PsiElement at = file.findElementAt(offset - 1);
    PsiElement toCheck = UsefulPsiTreeUtil.getPrevSiblingSkipWhiteSpacesAndComments(at, false);
    return PsiTreeUtil.getParentOfType(toCheck, DartType.class, DartComponentName.class) != null;
  }

  private static boolean checkAfterDollarInString(PsiFile file, int offset) {
    PsiElement at = file.findElementAt(offset - 1);
    final String text = at != null ? at.getText() : "";
    return text.endsWith("$") && PsiTreeUtil.getParentOfType(at, DartPsiCompositeElement.class) != null;
  }

  @Override
  public Result charTyped(char c, Project project, Editor editor, @NotNull PsiFile file) {
    String textToInsert = null;
    if (c == '<' && myAfterTypeOrComponentName) {
      myAfterTypeOrComponentName = false;
      textToInsert = ">";
    }
    else if (c == '{' && myAfterDollar) {
      myAfterDollar = false;
      textToInsert = "}";
    }
    if (textToInsert != null) {
      int offset = editor.getCaretModel().getOffset();
      if (offset >= 0) {
        editor.getDocument().insertString(offset, textToInsert);
        editor.getCaretModel().moveToOffset(offset);
        return Result.STOP;
      }
    }
    return super.charTyped(c, project, editor, file);
  }
}
