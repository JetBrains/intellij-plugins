// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.intellij.lang.ognl.highlight;

import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.psi.StringEscapesTokenTypes;
import com.intellij.testFramework.LightPlatformTestCase;
import org.jetbrains.annotations.NotNull;

public class OgnlHighlightingLexerTest extends LightPlatformTestCase {

  public void testStringLiteralStringEscapeToken() {
    EditorHighlighter highlighter = createHighlighter("\"string\\u2008\"");

    HighlighterIterator it = highlighter.createIterator(7);
    assertEquals(StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN, it.getTokenType());
  }

  public void testCharacterLiteralStringEscapeToken() {
    EditorHighlighter highlighter = createHighlighter("'\\u2008'");

    HighlighterIterator it = highlighter.createIterator(1);
    assertEquals(StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN, it.getTokenType());
  }

  @NotNull
  private static EditorHighlighter createHighlighter(String text) {
    EditorHighlighter highlighter =
      HighlighterFactory.createHighlighter(new OgnlHighlighter(), EditorColorsManager.getInstance().getGlobalScheme());
    highlighter.setText(text);
    return highlighter;
  }
}
