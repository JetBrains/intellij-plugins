// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.UI.editorActions.completionProviders;

import com.intellij.codeInsight.TailType;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import org.jetbrains.annotations.NotNull;

public class CfmlTailType extends TailType {
  public static final TailType PARENTHS = new CfmlTailType();

  @Override
  public int processTail(@NotNull Editor editor, int tailOffset) {
    HighlighterIterator iterator = editor.getHighlighter().createIterator(tailOffset);
    if (iterator.getTokenType() != CfscriptTokenTypes.L_BRACKET) {
      editor.getDocument().insertString(tailOffset, "()");
    }
    return moveCaret(editor, tailOffset, 1);
  }
}
