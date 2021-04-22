/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.ide.editing;

import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.codeInsight.editorActions.BackspaceHandlerDelegate;
import com.intellij.codeInsight.highlighting.BraceMatcher;
import com.intellij.codeInsight.highlighting.BraceMatchingUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.psi.PsiFile;
import com.intellij.protobuf.lang.psi.PbFile;
import com.intellij.protobuf.lang.psi.PbTextFile;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link BackspaceHandlerDelegate} that handles deletion of autoinserted '>' characters when the
 * leading '<' is deleted. Equivalent to how deleting a '{' after then '}' is autoinserted works.
 */
public class ProtoBackspaceHandler extends BackspaceHandlerDelegate {

  @Override
  public void beforeCharDeleted(char c, @NotNull PsiFile file, @NotNull Editor editor) {}

  @Override
  public boolean charDeleted(char c, @NotNull PsiFile file, @NotNull Editor editor) {
    // This logic is mostly just copied from BackspaceHandler#handleBackspace(...)
    if ((c == '<')
        && CodeInsightSettings.getInstance().AUTOINSERT_PAIR_BRACKET
        && (file instanceof PbFile || file instanceof PbTextFile)) {
      int offset = editor.getCaretModel().getOffset();
      if (offset >= editor.getDocument().getTextLength()) {
        return false;
      }
      CharSequence chars = editor.getDocument().getCharsSequence();
      char nextChar = chars.charAt(offset);
      if (nextChar != '>') {
        return false;
      }

      HighlighterIterator iterator = ((EditorEx) editor).getHighlighter().createIterator(offset);
      BraceMatcher braceMatcher = BraceMatchingUtil.getBraceMatcher(file.getFileType(), iterator);
      if (!braceMatcher.isLBraceToken(iterator, chars, file.getFileType())
          && !braceMatcher.isRBraceToken(iterator, chars, file.getFileType())) {
        return false;
      }

      int rparenOffset =
          BraceMatchingUtil.findRightmostRParen(
              iterator, iterator.getTokenType(), chars, file.getFileType());
      if (rparenOffset >= 0) {
        iterator = ((EditorEx) editor).getHighlighter().createIterator(rparenOffset);
        boolean matched = BraceMatchingUtil.matchBrace(chars, file.getFileType(), iterator, false);
        if (matched) {
          return false;
        }
      }

      editor.getDocument().deleteString(offset, offset + 1);
      return true;
    }
    return false;
  }
}
