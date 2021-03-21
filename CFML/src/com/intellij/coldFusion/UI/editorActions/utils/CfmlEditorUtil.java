// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.UI.editorActions.utils;

import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;

public final class CfmlEditorUtil {
  public static int countSharpsBalance(Editor editor) {
    int sharpsCounter = 0;
    // count balance
    HighlighterIterator iterator = editor.getHighlighter().createIterator(0);
    while (!iterator.atEnd()) {
      if (iterator.getTokenType() == CfscriptTokenTypes.OPENSHARP ||
          iterator.getTokenType() == CfmlTokenTypes.START_EXPRESSION) {
        sharpsCounter++;
      }
      else if (iterator.getTokenType() == CfscriptTokenTypes.CLOSESHARP ||
               iterator.getTokenType() == CfmlTokenTypes.END_EXPRESSION) {
        sharpsCounter--;
      }
      iterator.advance();
    }
    return sharpsCounter;
  }
}
