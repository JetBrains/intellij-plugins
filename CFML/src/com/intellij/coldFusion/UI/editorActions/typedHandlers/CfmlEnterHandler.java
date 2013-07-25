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
package com.intellij.coldFusion.UI.editorActions.typedHandlers;

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter;
import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: vnikolaenko
 * Date: 15.12.2008
 * Time: 14:43:37
 * To change this template use File | Settings | File Templates.
 */
public class CfmlEnterHandler extends EnterHandlerDelegateAdapter {
  public Result preprocessEnter(@NotNull final PsiFile file,
                                @NotNull final Editor editor,
                                @NotNull final Ref<Integer> caretOffset,
                                @NotNull final Ref<Integer> caretAdvance,
                                @NotNull final DataContext dataContext,
                                final EditorActionHandler originalHandler) {
    if (file.getLanguage() != CfmlLanguage.INSTANCE) {
      return Result.Continue;
    }
    if (file instanceof CfmlFile && isBetweenCfmlTags(file, editor, caretOffset.get())) {
      originalHandler.execute(editor, dataContext);
      return Result.DefaultForceIndent;
    }
    else if (isAfterAndBeforeCurlyBracket(editor, caretOffset.get())) {
      originalHandler.execute(editor, dataContext);
      return Result.DefaultForceIndent;
    }
    return Result.Continue;
  }

  private boolean isAfterAndBeforeCurlyBracket(Editor editor, int offset) {
    CharSequence chars = editor.getDocument().getCharsSequence();
    return offset > 0 && chars.charAt(offset - 1) == '{' && offset < chars.length() && chars.charAt(offset) == '}';
  }

  private static boolean isBetweenCfmlTags(PsiFile file, Editor editor, int offset) {
    if (offset == 0) return false;
    CharSequence chars = editor.getDocument().getCharsSequence();
    if (chars.charAt(offset - 1) != '>') return false;

    EditorHighlighter highlighter = ((EditorEx)editor).getHighlighter();
    HighlighterIterator iterator = highlighter.createIterator(offset - 1);
    if (iterator.getTokenType() != CfmlTokenTypes.R_ANGLEBRACKET) return false;
    iterator.retreat();

    int retrieveCount = 1;
    while (!iterator.atEnd()) {
      final IElementType tokenType = iterator.getTokenType();
      if (tokenType == CfmlTokenTypes.LSLASH_ANGLEBRACKET) return false;
      if (tokenType == CfmlTokenTypes.OPENER) break;
      ++retrieveCount;
      iterator.retreat();
    }
    for (int i = 0; i < retrieveCount; ++i) iterator.advance();
    iterator.advance();
    return !iterator.atEnd() && iterator.getTokenType() == CfmlTokenTypes.LSLASH_ANGLEBRACKET;
  }
}

