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
