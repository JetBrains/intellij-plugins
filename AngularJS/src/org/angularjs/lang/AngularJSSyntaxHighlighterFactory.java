// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.lang;

import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.highlighting.JSHighlighter;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SingleLazyInstanceSyntaxHighlighterFactory;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.psi.tree.IElementType;
import gnu.trove.THashMap;
import org.angularjs.lang.lexer.AngularJSLexer;
import org.angularjs.lang.lexer.AngularJSTokenTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSSyntaxHighlighterFactory extends SingleLazyInstanceSyntaxHighlighterFactory {
  @Override
  @NotNull
  protected SyntaxHighlighter createHighlighter() {
    return new AngularJSSyntaxHighlighter();
  }

  private static class AngularJSSyntaxHighlighter extends JSHighlighter {
    private final Map<IElementType, TextAttributesKey> myKeysMap = new THashMap<>();

    public AngularJSSyntaxHighlighter() {
      super(AngularJSLanguage.INSTANCE.getOptionHolder(), false);
      myKeysMap.put(JSTokenTypes.AS_KEYWORD, DefaultLanguageHighlighterColors.KEYWORD);
      myKeysMap.put(AngularJSTokenTypes.TRACK_BY_KEYWORD, DefaultLanguageHighlighterColors.KEYWORD);
    }

    @Override
    @NotNull
    public TextAttributesKey[] getTokenHighlights(final IElementType tokenType) {
      if (myKeysMap.containsKey(tokenType)) {
        return pack(myKeysMap.get(tokenType));
      }
      return super.getTokenHighlights(tokenType);
    }

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
      return new AngularJSLexer();
    }
  }
}
