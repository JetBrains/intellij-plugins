package com.intellij.coldFusion.UI.editorActions.surroundWith;

import com.intellij.lang.surroundWith.Surrounder;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Nadya.Zabrodina
 * Date: 11/28/11
 */
public class CfmlSharpSurrounder implements Surrounder {

  @Override
  public String getTemplateDescription() {
    return "#expr#";
  }

  @Override
  public boolean isApplicable(@NotNull final PsiElement[] elements) {
    return true;
  }

  @Override
  public TextRange surroundElements(@NotNull Project project, @NotNull Editor editor, @NotNull PsiElement[] elements) {
    return surroundSelection(editor);
  }

  @NotNull
  protected static TextRange surroundSelection(@NotNull Editor editor) {
    int start = editor.getSelectionModel().getSelectionStart();
    int end = editor.getSelectionModel().getSelectionEnd();
    editor.getDocument().replaceString(start,
                                       end,
                                       "#" + editor.getSelectionModel().getSelectedText() + "#");
    return TextRange.create(start, end + 2);
  }
}

