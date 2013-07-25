package com.intellij.coldFusion.UI.editorActions.utils;

import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;

/**
 * Created by IntelliJ IDEA.
 * User: vnikolaenko
 * Date: 15.12.2008
 * Time: 16:19:24
 * To change this template use File | Settings | File Templates.
 */
public class CfmlEditorUtil {
  public static int countSharpsBalance(Editor editor) {
    int sharpsCounter = 0;
    // count balance
    HighlighterIterator iterator = ((EditorEx)editor).getHighlighter().createIterator(0);
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
