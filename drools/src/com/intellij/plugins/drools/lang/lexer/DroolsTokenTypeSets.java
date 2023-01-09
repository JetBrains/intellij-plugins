// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.lexer;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.*;

public interface DroolsTokenTypeSets {
  IElementType SINGLE_LINE_COMMENT = new DroolsElementType("SINGLE_LINE_COMMENT");
  IElementType SINGLE_LINE_COMMENT_DEPR = new DroolsElementType("SINGLE_LINE_COMMENT_DEPR");     // # - deprecated style
  IElementType MULTI_LINE_COMMENT = new DroolsElementType("MULTI_LINE_COMMENT");
  IElementType NEW_LINE = new DroolsElementType("NEW_LINE");

  TokenSet STRINGS = TokenSet.create(STRING_LITERAL, STRING_ID);
  TokenSet BOOLEANS = TokenSet.create(TRUE, FALSE);
  TokenSet CHUNK_START_TOKENS = TokenSet.create(TIMER, DURATION, INIT, ACTION, REVERSE, RESULT, OP_AT);
  TokenSet BLOCK_START_TOKENS = TokenSet.create(FUNCTION);

  TokenSet COMMENTS = TokenSet.create(SINGLE_LINE_COMMENT, SINGLE_LINE_COMMENT_DEPR, MULTI_LINE_COMMENT);
  TokenSet KEYWORDS = TokenSet.create(RULE, IMPORT, PACKAGE, ENTRY_POINT, EXTENDS, WHEN, THEN, TEMPLATE, QUERY, DECLARE, FUNCTION, GLOBAL, END, WINDOW);
  TokenSet PRIMITIVE_TYPES = TokenSet.create(VOID, BOOLEAN, BYTE, CHAR,SHORT, INT,LONG, FLOAT, DOUBLE);

  TokenSet KEYWORD_ATTRS =
    TokenSet.create(SALIENCE, ENABLED, NO_LOOP, AUTO_FOCUS, LOCK_ON_ACTIVE, RETRACT, AGENDA_GROUP, ACTIVATION_GROUP, RULEFLOW_GROUP,
                    DATE_EFFECTIVE, DATE_EXPIRES, DIALECT, DURATION, ATTRIBUTES,TIMER,CALENDARS);
  TokenSet KEYWORD_OPS = TokenSet
    .create(THIS, NOT, IN, OR, AND, EXISTS, FORALL, ACCUMULATE, COLLECT, FROM, ACTION, REVERSE, RESULT, EVAL, OVER, INIT, MODIFY, UPDATE, INSERT,
            INSERT_LOGICAL, IF,  BREAK, DO);

  TokenSet OPERATORS = TokenSet.create(MEMBEROF, CONTAINS, SOUNDSLIKE, MATCHES, IS_A);
  TokenSet OPERATIONS = TokenSet.create(OP_PLUS, OP_MINUS, OP_MUL, OP_DIV, OP_REMAINDER, OP_AT, OP_ASSIGN, OP_PLUS_ASSIGN, OP_MINUS_ASSIGN,
                                        OP_MUL_ASSIGN, OP_DIV_ASSIGN, OP_BIT_AND_ASSIGN, OP_BIT_OR_ASSIGN, OP_BIT_XOR_ASSIGN, OP_REMAINDER_ASSIGN, OP_SL_ASSIGN,
                                        OP_SR_ASSIGN, OP_BSR_ASSIGN, OP_EQ, OP_NOT_EQ, OP_NOT, OP_COMPLEMENT, OP_PLUS_PLUS, OP_MINUS_MINUS,
                                        OP_COND_OR, OP_COND_AND, OP_BIT_OR, OP_BIT_AND, OP_BIT_XOR, OP_LESS, OP_LESS_OR_EQUAL, OP_GREATER, OP_GREATER_OR_EQUAL);
}

