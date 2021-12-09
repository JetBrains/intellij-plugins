/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
  @NonNls
  public String getTemplateDescription() {
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

