// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.lexer;

import com.intellij.embedding.LazyDelegateLexer;
import com.intellij.lexer.LayeredLexer;
import com.intellij.lexer.Lexer;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.plugins.jade.psi.JadeTokenTypes;

public class JadeHighlightingLexer extends LayeredLexer {
  public JadeHighlightingLexer(CodeStyleSettings codeStyleSettings) {
    super(new JadeLexer(codeStyleSettings));

    registerSelfStoppingLayer(new MyRecursiveDelegate(new JSMetaCodeLexer(), codeStyleSettings),
                              new IElementType[]{JadeTokenTypes.JS_META_CODE},
                              IElementType.EMPTY_ARRAY);
  }

  private static class MyRecursiveDelegate extends LayeredLexer {

    MyRecursiveDelegate(Lexer delegate, final CodeStyleSettings codeStyleSettings) {
      super(delegate);

      registerSelfStoppingLayer(new LazyDelegateLexer() {
                                  @Override
                                  protected Lexer createDelegate() {
                                    return new JadeHighlightingLexer(codeStyleSettings);
                                  }
                                }, new IElementType[]{JadeTokenTypes.JADE_EMBEDDED_CONTENT},
                                IElementType.EMPTY_ARRAY);
    }
  }
}
