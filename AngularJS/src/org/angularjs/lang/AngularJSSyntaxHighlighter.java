// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.lang;

import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.highlighting.JSHighlighter;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.tree.IElementType;
import org.angularjs.lang.lexer.AngularJSLexer;
import org.angularjs.lang.lexer.AngularJSTokenTypes;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class AngularJSSyntaxHighlighter extends JSHighlighter {
  private final Map<IElementType, TextAttributesKey> myKeysMap = new HashMap<>();

  AngularJSSyntaxHighlighter() {
    super(AngularJSLanguage.INSTANCE.getOptionHolder(), false);
    myKeysMap.put(JSTokenTypes.AS_KEYWORD, DefaultLanguageHighlighterColors.KEYWORD);
    myKeysMap.put(AngularJSTokenTypes.TRACK_BY_KEYWORD, DefaultLanguageHighlighterColors.KEYWORD);
  }

  @Override
  public TextAttributesKey @NotNull [] getTokenHighlights(final IElementType tokenType) {
    if (myKeysMap.containsKey(tokenType)) {
      return pack(myKeysMap.get(tokenType));
    }
    return super.getTokenHighlights(tokenType);
  }

  @Override
  public @NotNull Lexer getHighlightingLexer() {
    return new AngularJSLexer();
  }
}
