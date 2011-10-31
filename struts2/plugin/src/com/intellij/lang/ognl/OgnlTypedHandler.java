/*
 * Copyright 2011 The authors
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

package com.intellij.lang.ognl;

import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Special "completions" when typing characters.
 *
 * @author Yann C&eacute;bron
 */
public class OgnlTypedHandler extends TypedHandlerDelegate {

  @Override
  public Result charTyped(final char c, final Project project, final Editor editor, @NotNull final PsiFile file) {
    if (!CodeInsightSettings.getInstance().AUTOINSERT_PAIR_BRACKET) {
      return Result.CONTINUE;
    }

    if (file.getFileType() != OgnlFileType.INSTANCE) {
      PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
      final int offset = editor.getCaretModel().getOffset();
      final PsiElement elementAtCursor = InjectedLanguageUtil.findElementAtNoCommit(file, offset);

      if (elementAtCursor.getLanguage() != OgnlLanguage.INSTANCE) {
        return Result.CONTINUE;
      }
    }

    if (handleExpressionPrefix(c, editor)) {
      return Result.STOP;
    }

    if (handleOpeningBrace(c, editor)) {
      return Result.STOP;
    }

    return Result.CONTINUE;
  }

  /**
   * Autocomplete "%{" to "%{}".
   *
   * @param c      Typed character.
   * @param editor Current editor.
   * @return {@code true} if handled.
   */
  private boolean handleExpressionPrefix(final char c, final Editor editor) {
    if (c != '{') {
      return false;
    }

    final int offset = editor.getCaretModel().getOffset();
    final CharSequence before = editor.getDocument().getCharsSequence().subSequence(offset - 2, offset);
    if (!OgnlLanguage.EXPRESSION_PREFIX.equals(before.toString())) {
      return false;
    }

    editor.getDocument().insertString(editor.getCaretModel().getOffset(), OgnlLanguage.EXPRESSION_SUFFIX);
    return true;
  }

  /**
   * Autocomplete "{" to "{}".
   *
   * @param c      Typed character.
   * @param editor Current editor.
   * @return {@code true} if handled.
   */

  private boolean handleOpeningBrace(final char c, final Editor editor) {
    if (c != '{') {
      return false;
    }

    editor.getDocument().insertString(editor.getCaretModel().getOffset(), "}");
    return true;
  }

}