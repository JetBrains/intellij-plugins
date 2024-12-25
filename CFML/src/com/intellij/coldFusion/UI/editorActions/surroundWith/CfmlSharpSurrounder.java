// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.UI.editorActions.surroundWith;

import com.intellij.lang.surroundWith.Surrounder;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class CfmlSharpSurrounder implements Surrounder {

  @Override
  public @NonNls String getTemplateDescription() {
    return "#expr#";
  }

  @Override
  public boolean isApplicable(final PsiElement @NotNull [] elements) {
    return true;
  }

  @Override
  public TextRange surroundElements(@NotNull Project project, @NotNull Editor editor, PsiElement @NotNull [] elements) {
    return surroundSelection(editor);
  }

  protected static @NotNull TextRange surroundSelection(@NotNull Editor editor) {
    int start = editor.getSelectionModel().getSelectionStart();
    int end = editor.getSelectionModel().getSelectionEnd();
    editor.getDocument().replaceString(start,
                                       end,
                                       "#" + editor.getSelectionModel().getSelectedText() + "#");
    return TextRange.create(start, end + 2);
  }
}

