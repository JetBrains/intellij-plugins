package com.jetbrains.lang.dart.ide.editor;

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
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
    int offset = editor.getCaretModel().getOffset();
    if (c == '<') {
      myAfterTypeOrComponentName = checkAfterTypeOrComponentName(file, offset);
    }
    else if (c == '{') {
      myAfterDollar = checkAfterDollarInString(file, offset);
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
    return text.endsWith("$") && isDartContext(at);
  }

  private static boolean isDartContext(PsiElement at) {
    return PsiTreeUtil.getParentOfType(at, DartPsiCompositeElement.class) != null;
  }

  @Override
  public Result charTyped(char c, Project project, @NotNull Editor editor, @NotNull PsiFile file) {
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
      EditorModificationUtil.insertStringAtCaret(editor, textToInsert, false, 0);
      return Result.STOP;
    }
    return super.charTyped(c, project, editor, file);
  }
}
