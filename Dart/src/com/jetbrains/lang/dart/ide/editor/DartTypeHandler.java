package com.jetbrains.lang.dart.ide.editor;

import com.intellij.codeInsight.editorActions.TypedHandler;
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.psi.*;
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
      TypedHandler.commitDocumentIfCurrentCaretIsNotTheFirstOne(editor, project);
      myAfterTypeOrComponentName = checkAfterTypeOrComponentName(file, offset);
    }
    else if (c == '{') {
      TypedHandler.commitDocumentIfCurrentCaretIsNotTheFirstOne(editor, project);
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
    return StringUtil.endsWithChar(text, '$') && isDartContext(at);
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
    } else if (c == ':') {
      if (autoIndentCase(editor, project, file)) {
        return Result.STOP;
      }
    }
    return super.charTyped(c, project, editor, file);
  }

  private static boolean autoIndentCase(Editor editor, Project project, PsiFile file) {
    int offset = editor.getCaretModel().getOffset();
    PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
    PsiElement currElement = file.findElementAt(offset - 1);
    if (currElement != null) {
      PsiElement parent = currElement.getParent();
      if (parent != null && parent instanceof DartSwitchCase || parent instanceof DartDefaultCase) {
        CodeStyleManager.getInstance(project).adjustLineIndent(file, parent.getTextOffset());
        return true;
      }
    }
    return false;
  }
}
