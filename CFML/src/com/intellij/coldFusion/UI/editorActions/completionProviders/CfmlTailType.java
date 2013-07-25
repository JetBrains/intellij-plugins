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
package com.intellij.coldFusion.UI.editorActions.completionProviders;

import com.intellij.codeInsight.TailType;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;

/**
 * @author vnikolaenko
 */
public class CfmlTailType extends TailType {
  public static final TailType PARENTHS = new CfmlTailType();

  public int processTail(Editor editor, int tailOffset) {
    HighlighterIterator iterator = ((EditorEx)editor).getHighlighter().createIterator(tailOffset);
    if (iterator.getTokenType() != CfscriptTokenTypes.L_BRACKET) {
      editor.getDocument().insertString(tailOffset, "()");
    }
    return moveCaret(editor, tailOffset, 1);
  }
}
