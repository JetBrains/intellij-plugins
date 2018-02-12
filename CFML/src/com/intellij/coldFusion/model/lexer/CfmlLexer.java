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
package com.intellij.coldFusion.model.lexer;

import com.intellij.coldFusion.model.parsers.CfmlElementTypes;
import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.openapi.project.Project;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Lera Nikolaenko
 */
public class CfmlLexer extends MergingLexerAdapter {
  private static final int LEXER_STATE_LIMIT = 0xFFFF;
  private Lexer myCfscriptLexer = null;
  private int myStartPosition = 0;
  private final Project myProject;
  private final _CfmlLexer.CfmlLexerConfiguration myConfiguration;

  private static final TokenSet TOKENS_TO_MERGE =
    TokenSet.create(CfmlTokenTypes.COMMENT,
                    CfmlTokenTypes.WHITE_SPACE,
                    CfmlTokenTypes.SCRIPT_EXPRESSION, CfmlElementTypes.TEMPLATE_TEXT);

  public CfmlLexer(boolean highlightingMode, Project project) {
    super(new FlexAdapter(new _CfmlLexer(project)), TOKENS_TO_MERGE);
    myProject = project;
    myConfiguration = ((_CfmlLexer)((FlexAdapter)getDelegate()).getFlex()).myCurrentConfiguration;
  }

  @Override
  public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
    myCfscriptLexer = null;
    super.start(buffer, startOffset, endOffset, initialState);
  }

  @Override
  public int getState() {
    if (myCfscriptLexer != null) {
      int modifiedState = (myCfscriptLexer.getState() << 16) + LEXER_STATE_LIMIT;
      assert modifiedState != 0;
      return modifiedState;
    }
    int state = doGetState();
    assert state >= 0 && state < LEXER_STATE_LIMIT;
    return state;
  }

  private int doGetState() {
    return super.getState() + myConfiguration.getExtraState();
  }

  @Override
  public void advance() {
    if (myCfscriptLexer != null) {
      myCfscriptLexer.advance();
      if (myCfscriptLexer.getTokenType() == null) {
        myCfscriptLexer = null;
      }
    }
    else {
      super.advance();
    }
  }

  @Override
  public IElementType getTokenType() {

    if (myCfscriptLexer != null) {
      return myCfscriptLexer.getTokenType();
    }
    if (super.getTokenType() == CfmlElementTypes.CF_SCRIPT ||
        super.getTokenType() == CfmlTokenTypes.SCRIPT_EXPRESSION) {
      final int startPosition = super.getTokenStart();
      myStartPosition = startPosition;
      int endPosition = super.getTokenEnd();
      while (super.getTokenType() == CfmlTokenTypes.SCRIPT_EXPRESSION ||
             super.getTokenType() == CfmlElementTypes.CF_SCRIPT) {
        endPosition = super.getTokenEnd();
        super.advance();
      }
      myCfscriptLexer = new CfscriptLexer(myProject);
      myCfscriptLexer.start(super.getBufferSequence().subSequence(startPosition, endPosition),
                            0, endPosition - startPosition, myCfscriptLexer.getState());
      return myCfscriptLexer.getTokenType();
    }
    return super.getTokenType();
  }

  @Override
  public int getTokenStart() {
    if (myCfscriptLexer != null) {
      return myCfscriptLexer.getTokenStart() + myStartPosition;
    }
    return super.getTokenStart();
  }

  @Override
  public int getTokenEnd() {
    if (myCfscriptLexer != null) {
      return myCfscriptLexer.getTokenEnd() + myStartPosition;
    }
    return super.getTokenEnd();
  }
}
