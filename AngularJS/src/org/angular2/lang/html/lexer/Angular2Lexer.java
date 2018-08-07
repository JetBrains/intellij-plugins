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
package org.angular2.lang.html.lexer;

import com.intellij.lexer.*;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.xml.XmlTokenType;
import org.jetbrains.annotations.NotNull;

public class Angular2Lexer extends HtmlLexer {

  private static final TokenSet TOKENS_TO_MERGE =
    TokenSet.create(XmlTokenType.XML_COMMENT_CHARACTERS, XmlTokenType.XML_WHITE_SPACE, XmlTokenType.XML_REAL_WHITE_SPACE,
                    XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN, XmlTokenType.XML_DATA_CHARACTERS, XmlTokenType.XML_TAG_CHARACTERS);

  private static final TokenSet EXPANSION_TOKENS =
    TokenSet.create(XmlTokenType.XML_COMMA, Angular2TokenTypes.NG_LBRACE, Angular2TokenTypes.NG_RBRACE);

  private static final TokenSet INTERPOLATION_CONTENT_TOKENS =
    TokenSet.orSet(TokenSet.create(XmlTokenType.XML_REAL_WHITE_SPACE, XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN,
                                   XmlTokenType.XML_DATA_CHARACTERS),
                   EXPANSION_TOKENS);

  public Angular2Lexer(boolean tokenizeExpansionForms,
                       Pair<String, String> interpolationConfig) {
    super(new MyMergingLexer(
      new FlexAdapter(new _HtmlNgLexer()), tokenizeExpansionForms, interpolationConfig), true);
  }

  private static class MyMergingLexer extends MergingLexerAdapterBase {

    private final boolean myTokenizeExpansionForms;
    private final Pair<String, String> myInterpolationConfig;

    private int myInterpolationScanningState;

    public MyMergingLexer(Lexer original, boolean tokenizeExpansionForms,
                          Pair<String, String> interpolationConfig) {
      super(original);
      myTokenizeExpansionForms = tokenizeExpansionForms;
      myInterpolationConfig = interpolationConfig == null ? Pair.create("{{", "}}") : interpolationConfig;
    }

    @Override
    public MergeFunction getMergeFunction() {
      return this::merge;
    }

    @Override
    public void restore(@NotNull LexerPosition position) {
      super.restore(((MyLexerPosition)position).getOriginal());
      myInterpolationScanningState = ((MyLexerPosition)position).getInterpolationScanningState();
    }

    @NotNull
    @Override
    public LexerPosition getCurrentPosition() {
      return new MyLexerPosition(super.getCurrentPosition(), myInterpolationScanningState);
    }

    private IElementType merge(IElementType type, Lexer originalLexer) {
      if (INTERPOLATION_CONTENT_TOKENS.contains(type)) {
        switch (myInterpolationScanningState) {
          case 0:
            if (tryConsumeInterpolationBoundary(myInterpolationConfig.first)) {
              myInterpolationScanningState = 1;
              return Angular2TokenTypes.NG_INTERPOLATION_START;
            }
            break;
          case 1:
            if (tryConsumeInterpolationContent()) {
              myInterpolationScanningState = 2;
              return Angular2TokenTypes.NG_INTERPOLATION_CONTENT;
            }
          case 2:
            myInterpolationScanningState = 0;
            if (tryConsumeInterpolationBoundary(myInterpolationConfig.second)) {
              return Angular2TokenTypes.NG_INTERPOLATION_END;
            }
            break;
        }
      }
      else {
        myInterpolationScanningState = 0;
      }
      type = convertType(type);
      if (!TOKENS_TO_MERGE.contains(type)) {
        return type;
      }
      while (true) {
        final IElementType tokenType = convertType(originalLexer.getTokenType());
        if (tokenType != type
            || (INTERPOLATION_CONTENT_TOKENS.contains(tokenType)
                && inBuffer(myInterpolationConfig.first, 0))) {
          break;
        }
        originalLexer.advance();
      }
      return type;
    }

    private IElementType convertType(IElementType tokenType) {
      return !myTokenizeExpansionForms && EXPANSION_TOKENS.contains(tokenType) ?
             XmlTokenType.XML_DATA_CHARACTERS : tokenType;
    }

    private boolean tryConsumeInterpolationBoundary(String boundary) {
      if (inBuffer(boundary, -1)) {
        final Lexer original = getOriginal();
        int interpolationCharsToConsume = boundary.length() - 1;
        while (interpolationCharsToConsume > 0) {
          original.advance();
          interpolationCharsToConsume--;
        }
        return true;
      }
      return false;
    }

    private boolean tryConsumeInterpolationContent() {
      if (inBuffer(myInterpolationConfig.second, -1)) {
        return false;
      }
      final Lexer originalLexer = getOriginal();
      while (true) {
        final IElementType tokenType = originalLexer.getTokenType();
        if (!INTERPOLATION_CONTENT_TOKENS.contains(tokenType)
            || inBuffer(myInterpolationConfig.second, 0)) {
          break;
        }
        originalLexer.advance();
      }
      return true;
    }

    private boolean inBuffer(String text, int offset) {
      final Lexer original = getOriginal();
      final int tokenPos = original.getTokenStart() + offset;
      return tokenPos >= 0
             && original.getBufferSequence()
                        .subSequence(tokenPos, Math.min(tokenPos + text.length(), original.getBufferEnd()))
                        .equals(text);
    }
  }

  private static class MyLexerPosition implements LexerPosition {

    private final LexerPosition myOriginal;
    private final int myInterpolationScanningState;

    public MyLexerPosition(LexerPosition original, int interpolationScanningState) {
      myOriginal = original;
      myInterpolationScanningState = interpolationScanningState;
    }

    public int getInterpolationScanningState() {
      return myInterpolationScanningState;
    }

    public LexerPosition getOriginal() {
      return myOriginal;
    }

    @Override
    public int getOffset() {
      return myOriginal.getOffset();
    }

    @Override
    public int getState() {
      return myOriginal.getState();
    }
  }
}
