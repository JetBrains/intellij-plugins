// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.highlighting;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.psi.StringEscapesTokenTypes;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

public class ActionScriptEditorHighlighterTest extends BasePlatformTestCase {

  @Override
  protected @NotNull String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("lexer/");
  }

  public void testEditorHighlighting() {
    myFixture.configureByFile("1.js2");
    EditorHighlighter highlighter = HighlighterFactory.createHighlighter(getProject(), myFixture.getFile().getVirtualFile());
    highlighter.setText(myFixture.getEditor().getDocument().getText());
    HighlighterIterator iterator = highlighter.createIterator(0);
    assertEquals(JSTokenTypes.PACKAGE_KEYWORD, iterator.getTokenType());

    //                10        20
    //      01 2345 6789 01 234567890
    String text = "'\\xFF\\111\\v\\0'";
    highlighter.setText(text);
    iterator = highlighter.createIterator(3);
    assertEquals(StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN, iterator.getTokenType());
    iterator = highlighter.createIterator(6);
    assertEquals(StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN, iterator.getTokenType());
    iterator = highlighter.createIterator(10);
    assertEquals(StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN, iterator.getTokenType());
    iterator = highlighter.createIterator(11);
    assertEquals(StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN, iterator.getTokenType());

    //                10        20
    //      012345678901234567890
    text = "str.replace(/[/][*]/g,\"txt\");";
    highlighter.setText(text);
    iterator = highlighter.createIterator(15);
    assertEquals(JSTokenTypes.REGEXP_LITERAL, iterator.getTokenType());

    text = " ";
    highlighter.setText(text);
    iterator = highlighter.createIterator(0);
    assertEquals(JSTokenTypes.WHITE_SPACE, iterator.getTokenType());
  }
}
