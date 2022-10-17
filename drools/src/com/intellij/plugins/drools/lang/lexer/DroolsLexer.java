// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.lexer;

import com.intellij.lexer.Lexer;
import com.intellij.lexer.LexerPosition;
import com.intellij.lexer.LookAheadLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.*;
import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;

public class DroolsLexer extends LookAheadLexer {

  public DroolsLexer() {
    super(new DroolsFlexLexer());
  }

  @Override
  protected void lookAhead(final @NotNull Lexer baseLexer) {
    IElementType tokenType = baseLexer.getTokenType();

    if (DroolsTokenTypeSets.CHUNK_START_TOKENS.contains(tokenType)) {
      processCodeChunks(baseLexer);
    }
    else if (DroolsTokenTypeSets.BLOCK_START_TOKENS.contains(tokenType)) {
      processBlockExpressions(baseLexer);
    }
    else if (tokenType == THEN) {
      advanceLexer(baseLexer);
      processRhsStatements(baseLexer);
    }
    else {
      super.lookAhead(baseLexer);
    }
  }

  private void processRhsStatements(Lexer baseLexer) {
    addAllTokens(baseLexer, TokenSet.create(WHITE_SPACE, SEMICOLON));
    if (baseLexer.getTokenType() == LBRACKET) {
      advanceLexer(baseLexer);
      if (skipTokensWithBraces(baseLexer, LBRACKET, RBRACKET, true)) {
        advanceLexer(baseLexer);
        return;
      }
    }

    IElementType currentTokenType = baseLexer.getTokenType();
    if (currentTokenType == null) return;
    if (currentTokenType == END) {
      advanceLexer(baseLexer);
      return;
    }

    TokenSet modifyTokenSet = TokenSet.create(MODIFY, RETRACT, UPDATE, INSERT_LOGICAL, INSERT);
    if (modifyTokenSet.contains(currentTokenType)) {
      processModifyStatement(baseLexer);
    } else if (baseLexer.getTokenType() == DroolsTokenTypeSets.SINGLE_LINE_COMMENT_DEPR) {
      advanceLexer(baseLexer);
    } else if (currentTokenType == THEN) {
      advanceLexer(baseLexer);
      processRhsStatements(baseLexer);
    } else {
      TokenSet interruptSet = TokenSet.create(THEN, END, MODIFY, RETRACT, UPDATE, INSERT_LOGICAL, INSERT, DroolsTokenTypeSets.SINGLE_LINE_COMMENT_DEPR);
      if (skipTokens(baseLexer, interruptSet)) {
        IElementType nextTokenType = baseLexer.getTokenType();
        if (nextTokenType == null) {
          advanceAs(baseLexer, BAD_CHARACTER);
        }
        else {
          addToken(baseLexer.getTokenStart(), JAVA_STATEMENT);
        }
      }
    }
    processRhsStatements(baseLexer);
  }

  private void processModifyStatement(Lexer baseLexer) {
    IElementType tokenType = baseLexer.getTokenType();
    if (tokenType == RETRACT || tokenType == UPDATE || tokenType == INSERT || tokenType == INSERT_LOGICAL) {
      advanceLexer(baseLexer);
      addWhiteSpaces(baseLexer);
      if (baseLexer.getTokenType() == LPAREN) {
        advanceLexer(baseLexer);
        if (skipTokensWithBraces(baseLexer, LPAREN, RPAREN, true)) {
          advanceLexer(baseLexer);
          return;
        }
      }
    }
    else if (tokenType == MODIFY) {
      advanceLexer(baseLexer);
      addWhiteSpaces(baseLexer);
      while (baseLexer.getTokenType() != null) {
        advanceLexer(baseLexer);
        if (LBRACE == baseLexer.getTokenType()) {
          advanceLexer(baseLexer);
          if (RBRACE == baseLexer.getTokenType() || skipTokensWithBraces(baseLexer, LBRACE, RBRACE, true)) {
            advanceLexer(baseLexer);
          }
          return;
        }
      }
    }
    super.lookAhead(baseLexer);
  }

  private void processCodeChunks(final Lexer baseLexer) {
    if (baseLexer.getTokenType() == OP_AT) {
      // process annotations
      advanceLexer(baseLexer);
      if (baseLexer.getTokenType() == JAVA_IDENTIFIER) {
        processChunkBlock(baseLexer);
      }
    } else {
      processChunkBlock(baseLexer);
    }
  }

  private void processChunkBlock(Lexer baseLexer) {
    advanceLexer(baseLexer);
    addWhiteSpaces(baseLexer);
    if (baseLexer.getTokenType() == LPAREN) {
      advanceLexer(baseLexer);
      if (skipTokensWithBraces(baseLexer, LPAREN, RPAREN)) {
        //if (baseLexer.getTokenType() != null) {
        addToken(baseLexer.getTokenStart(), CHUNK_BLOCK);
        advanceLexer(baseLexer);
        //}
      }
    }
    else {
      super.lookAhead(baseLexer);
    }
  }

  private void processBlockExpressions(final Lexer baseLexer) {
    advanceLexer(baseLexer);
    while (true) {
      IElementType tokenType = baseLexer.getTokenType();
      if (tokenType == null) {
        advanceLexer(baseLexer);
        break;
      }
      if (LBRACE == tokenType) {
        LexerPosition currentPosition = baseLexer.getCurrentPosition();
        baseLexer.advance();
        if (skipTokensWithBraces(baseLexer, LBRACE, RBRACE) || baseLexer.getTokenType() == RBRACE) {
          advanceAs(baseLexer, BLOCK_EXPRESSION);
        } else {
          baseLexer.restore(currentPosition);
          advanceLexer(baseLexer);
        }
        break;
      }
      else {
        advanceLexer(baseLexer);
      }
    }
  }

  private void addWhiteSpaces(Lexer baseLexer) {
    addAllTokens(baseLexer, TokenSet.create(WHITE_SPACE));
  }

  private void addAllTokens(Lexer baseLexer, TokenSet set) {
    while (set.contains(baseLexer.getTokenType())) {
      advanceLexer(baseLexer);
    }
  }

  protected static boolean currentOrSkipTokens(Lexer baseLexer, TokenSet until) {
    return until.contains(baseLexer.getTokenType()) || skipTokens(baseLexer, until);
  }

  protected static boolean skipTokens(Lexer baseLexer, TokenSet until) {
    boolean skipped = false;
    while (true) {
      IElementType tokenType = baseLexer.getTokenType();
      if (tokenType == null || until.contains(tokenType)) {
        return skipped;
      }
      skipped = true;
      baseLexer.advance();
    }
  }

  protected boolean skipTokensWithBraces(Lexer baseLexer, IElementType lBrace, IElementType rBrace) {
    return skipTokensWithBraces(baseLexer, lBrace, rBrace, false);
  }

  protected boolean skipTokensWithBraces(Lexer baseLexer, IElementType lBrace, IElementType rBrace, boolean addSkippedTokens) {
    boolean skipped = false;
    TokenSet until = TokenSet.create(lBrace, rBrace);
    int lBraces = 0;
    while (true) {
      IElementType tokenType = baseLexer.getTokenType();
      if (tokenType == null) {
        return skipped;
      }
      if (until.contains(tokenType)) {
        if (baseLexer.getTokenType() == lBrace) {
          lBraces++;
        }
        else if (lBraces == 0) {
          return skipped;
        }
        else {
          lBraces--;
        }
      }
      skipped = true;

      if (addSkippedTokens) {
        advanceLexer(baseLexer);
      }
      else {
        baseLexer.advance();
      }
    }
  }
}
