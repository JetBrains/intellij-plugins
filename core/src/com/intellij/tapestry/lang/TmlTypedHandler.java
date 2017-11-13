package com.intellij.tapestry.lang;

import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.tapestry.psi.TelTokenTypes;
import org.jetbrains.annotations.NotNull;

public class TmlTypedHandler extends TypedHandlerDelegate {
  @NotNull
  @Override
  public Result charTyped(char c, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
    if (file.getFileType() != TmlFileType.INSTANCE || c != '{') return Result.CONTINUE;
    if(!CodeInsightSettings.getInstance().AUTOINSERT_PAIR_BRACKET) return Result.CONTINUE;
    PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
    final int offset = editor.getCaretModel().getOffset();
    final PsiElement elementAt = file.findElementAt(offset - 1);

    if (elementAt != null) {
      PsiElement parent = elementAt.getParent();
      final int index = offset - 2;
      
      if (index >= 0) {
        final CharSequence charSequence = editor.getDocument().getCharsSequence();

        if(charSequence.length() > index) {

        if (charSequence.charAt(index) == '$' && parent != null && parent.getNode().getElementType() != TelTokenTypes.TAP5_EL_HOLDER) {
          editor.getDocument().insertString(offset, "}");
          return Result.STOP;
        }
        }
      }
    }

    return Result.CONTINUE;
  }
}
