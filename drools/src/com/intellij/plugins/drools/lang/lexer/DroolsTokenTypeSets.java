// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.lexer;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.ACCUMULATE;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.ACTION;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.ACTIVATION_GROUP;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.AGENDA_GROUP;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.AND;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.ATTRIBUTES;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.AUTO_FOCUS;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.BOOLEAN;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.BREAK;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.BYTE;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.CALENDARS;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.CHAR;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.COLLECT;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.CONTAINS;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.DATE_EFFECTIVE;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.DATE_EXPIRES;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.DECLARE;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.DIALECT;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.DO;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.DOUBLE;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.DURATION;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.ENABLED;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.END;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.ENTRY_POINT;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.ENUM;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.EVAL;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.EXISTS;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.EXTENDS;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.FALSE;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.FLOAT;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.FORALL;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.FROM;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.FUNCTION;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.GLOBAL;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.IF;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.IMPORT;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.IN;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.INIT;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.INSERT;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.INSERT_LOGICAL;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.INT;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.IS_A;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.LOCK_ON_ACTIVE;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.LONG;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.MATCHES;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.MEMBEROF;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.MODIFY;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.NOT;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.NO_LOOP;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.OP_ASSIGN;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.OP_AT;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.OP_BIT_AND;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.OP_BIT_AND_ASSIGN;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.OP_BIT_OR;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.OP_BIT_OR_ASSIGN;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.OP_BIT_XOR;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.OP_BIT_XOR_ASSIGN;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.OP_BSR_ASSIGN;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.OP_COMPLEMENT;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.OP_COND_AND;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.OP_COND_OR;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.OP_DIV;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.OP_DIV_ASSIGN;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.OP_EQ;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.OP_GREATER;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.OP_GREATER_OR_EQUAL;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.OP_LESS;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.OP_LESS_OR_EQUAL;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.OP_MINUS;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.OP_MINUS_ASSIGN;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.OP_MINUS_MINUS;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.OP_MUL;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.OP_MUL_ASSIGN;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.OP_NOT;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.OP_NOT_EQ;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.OP_PLUS;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.OP_PLUS_ASSIGN;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.OP_PLUS_PLUS;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.OP_REMAINDER;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.OP_REMAINDER_ASSIGN;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.OP_SL_ASSIGN;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.OP_SR_ASSIGN;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.OR;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.OVER;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.PACKAGE;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.QUERY;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.RESULT;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.RETRACT;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.REVERSE;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.RULE;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.RULEFLOW_GROUP;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.SALIENCE;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.SHORT;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.SOUNDSLIKE;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.STATIC;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.STRING_ID;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.STRING_LITERAL;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.TEMPLATE;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.THEN;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.THIS;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.TIMER;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.TRUE;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.UNIT;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.UPDATE;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.VOID;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.WHEN;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.WINDOW;

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
  TokenSet KEYWORDS =
    TokenSet.create(RULE, IMPORT, PACKAGE, ENTRY_POINT, EXTENDS, WHEN, THEN, TEMPLATE, QUERY, DECLARE, FUNCTION, GLOBAL, END, WINDOW, UNIT,
                    ENUM, STATIC);
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

