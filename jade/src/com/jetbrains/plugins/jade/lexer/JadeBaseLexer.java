// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.lexer;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.MergeFunction;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.jetbrains.plugins.jade.JadeLanguage;
import com.jetbrains.plugins.jade.psi.JadeTokenTypes;

class JadeBaseLexer extends MergingLexerAdapter {

  private static final TokenSet TOKENS_TO_MERGE = TokenSet.create(JadeTokenTypes.COMMENT,
                                                                  JadeTokenTypes.UNBUF_COMMENT,
                                                                  JadeTokenTypes.EOL,
                                                                  JadeTokenTypes.ATTRIBUTE_VALUE,
                                                                  JadeTokenTypes.IDENTIFIER,
                                                                  JadeTokenTypes.JS_CODE_BLOCK,
                                                                  JadeTokenTypes.JS_EXPR,
                                                                  JadeTokenTypes.JS_EACH_EXPR,
                                                                  JadeTokenTypes.STYLE_BLOCK,
                                                                  JadeTokenTypes.FILE_PATH,
                                                                  JadeTokenTypes.JS_CODE_BLOCK_PATCHED,
                                                                  JadeTokenTypes.FILTER_CODE,
                                                                  JadeTokenTypes.JS_MIXIN_PARAMS,
                                                                  JadeTokenTypes.JS_META_CODE);
  public static final MergeFunction MERGE_FUNCTION = new MergeFunction() {
    @Override
    public IElementType merge(IElementType type, Lexer originalLexer) {
      if (type != JadeTokenTypes.EOL) {
        return type;
      }

      if (originalLexer.getTokenType() != JadeTokenTypes.INDENT) {
        return JadeTokenTypes.EOL;
      }

      //concat eol & ident
      //in token [eol], token [ident] -> token [ident]
      //because of IndentHelperImpl.getIndent() expect that indent has format '\n+indent'
      //otherwise returned value always will be '0'
      while (true) {
        final IElementType tokenType = originalLexer.getTokenType();
        if (!(tokenType == JadeTokenTypes.INDENT)) break;
        originalLexer.advance();
      }
      return JadeTokenTypes.INDENT;
    }
  };

  JadeBaseLexer(final CodeStyleSettings codeStyleSettings, final int explicitTabSize) {
    super(new MergingLexerAdapter(getFlexAdapter(codeStyleSettings, explicitTabSize), null) {
      @Override
      public MergeFunction getMergeFunction() {
        return MERGE_FUNCTION;
      }
    }, TOKENS_TO_MERGE);
  }

  private static FlexAdapter getFlexAdapter(final CodeStyleSettings codeStyleSettings,
                                            final int explicitTabSize) {
    return new FlexAdapter(new _JadeLexer(null) {
      {
        resetInternal(getTabSize());
      }

      @Override
      public void reset(final CharSequence buffer, final int start, final int end, final int initialState) {
        super.reset(buffer, start, end, initialState);
        resetInternal(getTabSize());
      }

      private int getTabSize() {
        if (explicitTabSize > 0) {
          return explicitTabSize;
        }

        if (codeStyleSettings != null) {
          final CommonCodeStyleSettings settings = codeStyleSettings.getCommonSettings(JadeLanguage.INSTANCE);
          final CommonCodeStyleSettings.IndentOptions options = settings.getIndentOptions();
          if (options != null) {
            return options.TAB_SIZE;
          }
        }

        return 2;
      }
    });
  }
}
