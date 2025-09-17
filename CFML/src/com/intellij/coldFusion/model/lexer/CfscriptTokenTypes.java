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

import com.intellij.coldFusion.model.psi.CfmlElementType;
import com.intellij.coldFusion.model.psi.CfmlOperatorTokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

import static com.intellij.coldFusion.model.psi.CfmlExpressionTypeCalculator.*;

/**
 * Created by Lera Nikolaenko
 */
public interface CfscriptTokenTypes {
  IElementType COMMENT = new CfmlElementType("COMMENT");
  IElementType WHITE_SPACE = new CfmlElementType("WHITE_SPACE");
  IElementType L_BRACKET = new CfmlElementType("L_BRACKET"); // (
  IElementType R_BRACKET = new CfmlElementType("R_BRACKET"); // )
  IElementType L_SQUAREBRACKET = new CfmlElementType("L_SQUAREBRACKET"); // [
  IElementType R_SQUAREBRACKET = new CfmlElementType("R_SQUAREBRACKET"); // ]
  IElementType L_CURLYBRACKET = new CfmlElementType("L_CURLYBRACKET"); // {
  IElementType R_CURLYBRACKET = new CfmlElementType("R_CURLYBRACKET"); // }
  IElementType SEMICOLON = new CfmlElementType("SEMICOLON"); // ;

  IElementType QUESTION = new CfmlElementType("QUESTION");
  IElementType ELVIS = new CfmlOperatorTokenType("ELVIS", PLUS_CALCULATOR);
  IElementType COMMA = new CfmlElementType("COMMA");
  IElementType POINT = new CfmlElementType("POINT");
  IElementType DOTDOT = new CfmlElementType("DOTDOT");

  /* arithmetic operators */
  /* boolean logic operators */
  IElementType NOT = new CfmlOperatorTokenType("NOT", BOOLEAN_CALCULATOR);
  IElementType OR = new CfmlOperatorTokenType("OR", BOOLEAN_CALCULATOR);
  IElementType AND = new CfmlOperatorTokenType("AND", BOOLEAN_CALCULATOR);

  /* boolean literal logic operators */
  IElementType NOT_L = new CfmlOperatorTokenType("NOT_L", BOOLEAN_CALCULATOR);
  IElementType AND_L = new CfmlOperatorTokenType("AND_L", BOOLEAN_CALCULATOR);
  IElementType OR_L = new CfmlOperatorTokenType("OR_L", BOOLEAN_CALCULATOR);
  IElementType NOT_XOR_L = new CfmlOperatorTokenType("NOT_XOR_L", BOOLEAN_CALCULATOR);
  IElementType XOR_L = new CfmlOperatorTokenType("XOR_L", BOOLEAN_CALCULATOR);
  IElementType IMP_L = new CfmlOperatorTokenType("IMP_L", BOOLEAN_CALCULATOR);

  /* boolean literal comparison operators */
  IElementType EQ_L = new CfmlOperatorTokenType("EQ_L", BOOLEAN_CALCULATOR);
  IElementType NEQ_L = new CfmlOperatorTokenType("NEQ_L", BOOLEAN_CALCULATOR);
  IElementType CONTAINS_L = new CfmlOperatorTokenType("CONTAINS_L", BOOLEAN_CALCULATOR);
  IElementType NOT_CONTAINS_L = new CfmlOperatorTokenType("NOT_CONTAINS_L", BOOLEAN_CALCULATOR);
  IElementType GT_L = new CfmlOperatorTokenType("GT_L", BOOLEAN_CALCULATOR);
  IElementType LT_L = new CfmlOperatorTokenType("LT_L", BOOLEAN_CALCULATOR);
  IElementType GE_L = new CfmlOperatorTokenType("GE_L", BOOLEAN_CALCULATOR);
  IElementType LE_L = new CfmlOperatorTokenType("LE_L", BOOLEAN_CALCULATOR);

  /* boolean comparison operators */
  IElementType EQEQ = new CfmlOperatorTokenType("EQEQ", BOOLEAN_CALCULATOR);
  IElementType NEQ = new CfmlOperatorTokenType("NEQ", BOOLEAN_CALCULATOR);
  IElementType LT = new CfmlOperatorTokenType("LT", BOOLEAN_CALCULATOR);
  IElementType LTE = new CfmlOperatorTokenType("LTE", BOOLEAN_CALCULATOR);
  IElementType GT = new CfmlOperatorTokenType("GT", BOOLEAN_CALCULATOR);
  IElementType GTE = new CfmlOperatorTokenType("GTE", BOOLEAN_CALCULATOR);

  /* string operator */
  IElementType CONCAT = new CfmlOperatorTokenType("CONCAT", CONCATINATION_CALCULATOR); // &
  IElementType CONCAT_EQ = new CfmlOperatorTokenType("CONCAT_EQ", CONCATINATION_CALCULATOR); // &
  /* numeric operators */
  IElementType ADD = new CfmlOperatorTokenType("ADD", PLUS_CALCULATOR);
  IElementType ADD_EQ = new CfmlOperatorTokenType("ADD_EQ", PLUS_CALCULATOR);
  IElementType MINUS = new CfmlOperatorTokenType("MINUS", MINUS_CALCULATOR);
  IElementType MINUS_EQ = new CfmlOperatorTokenType("MINUS_EQ", MINUS_CALCULATOR);
  IElementType MUL = new CfmlOperatorTokenType("MUL", MULTIPLICATIVE_CALCULATOR);
  IElementType MUL_EQ = new CfmlOperatorTokenType("MUL_EQ", MULTIPLICATIVE_CALCULATOR);
  IElementType DEV = new CfmlOperatorTokenType("DEV", MULTIPLICATIVE_CALCULATOR);
  IElementType DEV_EQ = new CfmlOperatorTokenType("DEV_EQ", MULTIPLICATIVE_CALCULATOR);
  IElementType MOD_L = new CfmlOperatorTokenType("MOD_L", MINUS_CALCULATOR);

  IElementType INC = new CfmlElementType("INC");
  IElementType DEC = new CfmlElementType("DEC");

  IElementType MOD = new CfmlElementType("MOD");
  IElementType INT_DEV = new CfmlElementType("INT_DEV");
  IElementType POW = new CfmlOperatorTokenType("POW", MULTIPLICATIVE_CALCULATOR);

  IElementType IN_L = new CfmlElementType("IN_L");

  // IElementType ASSIGN = new CfmlElementType("ASSIGN");

  /* literals */
  IElementType INTEGER = new CfmlElementType("INTEGER");
  IElementType DOUBLE = new CfmlElementType("DOUBLE");

  /*
  IElementType SINGLE_QUOTE = new CfmlElementType("SINGLE_QUOTE");
  IElementType DOUBLE_QUOTE = new CfmlElementType("DOUBLE_QUOTE");
  IElementType SINGLE_QUOTE_CLOSER = new CfmlElementType("SINGLE_QUOTE_CLOSER");
  IElementType DOUBLE_QUOTE_CLOSER = new CfmlElementType("DOUBLE_QUOTE_CLOSER");
  */
  IElementType BOOLEAN = new CfmlElementType("BOOLEAN");

  IElementType OPENSHARP = new CfmlElementType("OPENSHARP");
  IElementType CLOSESHARP = new CfmlElementType("CLOSESHARP");

  // TokenSet STRING_ELEMENTS = TokenSet.create(SINGLE_QUOTE, DOUBLE_QUOTE, SINGLE_QUOTE_CLOSER, DOUBLE_QUOTE_CLOSER);

  TokenSet ASSIGN_OPERATORS = TokenSet.create(CONCAT_EQ, ADD_EQ, MINUS_EQ, MUL_EQ, DEV_EQ, CfmlTokenTypes.ASSIGN);
  TokenSet WORD_BINARY_OPERATIONS =
    TokenSet.create(MOD_L, AND_L, OR_L, NOT_XOR_L, XOR_L, IMP_L, EQ_L, NEQ_L, CONTAINS_L, NOT_CONTAINS_L, GT_L, LT_L, GE_L, LE_L, IN_L);
  TokenSet WORD_PREFIX_OPERATIONS = TokenSet.create(NOT_L);

  TokenSet SYMBOL_BINARY_OPERATIONS = TokenSet
    .create(CfmlTokenTypes.ASSIGN, ADD, ADD_EQ, MINUS, MINUS_EQ, MUL, MUL_EQ, DEV, DEV_EQ, MOD, INT_DEV, POW, AND, OR, CONCAT, CONCAT_EQ,
            EQEQ, NEQ,
            LT, LTE, GT, GTE, ELVIS);
  TokenSet SYMBOL_PREFIX_OPERATIONS = TokenSet.create(MINUS, ADD, INC, DEC, NOT);
  TokenSet SYMBOL_POSTFIX_OPERATIONS = TokenSet.create(INC, DEC);

  // for lexer highlighting
  TokenSet SYMBOL_OPERATIONS =
    TokenSet.orSet(TokenSet.orSet(SYMBOL_BINARY_OPERATIONS, SYMBOL_PREFIX_OPERATIONS), SYMBOL_POSTFIX_OPERATIONS);
  TokenSet WORD_OPERATIONS = TokenSet.orSet(WORD_BINARY_OPERATIONS, WORD_PREFIX_OPERATIONS);
  TokenSet OPERATIONS = TokenSet.orSet(SYMBOL_OPERATIONS, WORD_OPERATIONS);

  // for parsing
  //TokenSet BINARY_OPERATIONS = TokenSet.orSet(SYMBOL_BINARY_OPERATIONS, WORD_BINARY_OPERATIONS);
  TokenSet PREFIX_OPERATIONS = TokenSet.orSet(SYMBOL_PREFIX_OPERATIONS, WORD_PREFIX_OPERATIONS);
  TokenSet POSTFIX_OPERATIONS = SYMBOL_POSTFIX_OPERATIONS;

  TokenSet BRACKETS = TokenSet.create(L_BRACKET, R_BRACKET, L_SQUAREBRACKET, R_SQUAREBRACKET, L_CURLYBRACKET, R_CURLYBRACKET);


  IElementType IDENTIFIER = new CfmlElementType("IDENTIFIER");
  IElementType BAD_CHARACTER = new CfmlElementType("BAD_CHARACTER");

  IElementType BREAK_KEYWORD = new CfmlElementType("BREAK_KEYWORD");
  IElementType ABORT_KEYWORD = new CfmlElementType("ABORT_KEYWORD");
  IElementType INCLUDE_KEYWORD = new CfmlElementType("INCLUDE_KEYWORD");

  IElementType DEFAULT_KEYWORD = new CfmlElementType("DEFAULT_KEYWORD");
  IElementType FUNCTION_KEYWORD = new CfmlElementType("FUNCTION_KEYWORD");
  IElementType SWITCH_KEYWORD = new CfmlElementType("SWITCH_KEYWORD");
  IElementType CASE_KEYWORD = new CfmlElementType("CASE_KEYWORD");
  IElementType DO_KEYWORD = new CfmlElementType("DO_KEYWORD");
  IElementType IF_KEYWORD = new CfmlElementType("IF_KEYWORD");
  IElementType TRY_KEYWORD = new CfmlElementType("TRY_KEYWORD");
  IElementType CATCH_KEYWORD = new CfmlElementType("CATCH_KEYWORD");
  IElementType FINALLY_KEYWORD = new CfmlElementType("FINALLY_KEYWORD");
  IElementType RETHROW_KEYWORD = new CfmlElementType("RETHROW_KEYWORD");
  IElementType ELSE_KEYWORD = new CfmlElementType("ELSE_KEYWORD");
  IElementType VAR_KEYWORD = new CfmlElementType("VAR_KEYWORD");
  IElementType WHILE_KEYWORD = new CfmlElementType("WHILE_KEYWORD");
  IElementType CONTINUE_KEYWORD = new CfmlElementType("CONTINUE_KEYWORD");
  IElementType FOR_KEYWORD = new CfmlElementType("FOR_KEYWORD");
  IElementType RETURN_KEYWORD = new CfmlElementType("RETURN_KEYWORD");
  IElementType SCOPE_KEYWORD = new CfmlElementType("SCOPE_KEYWORD");

  // ColdFusion9
  IElementType COMPONENT_KEYWORD = new CfmlElementType("COMPONENT_KEYWORD");
  IElementType INTERFACE_KEYWORD = new CfmlElementType("INTERFACE_KEYWORD");

  IElementType PAGEENCODING_KEYWORD = new CfmlElementType("PAGEENCODING_KEYWORD");

  IElementType REQUIRED_KEYWORD = new CfmlElementType("REQUIRED_KEYWORD");

  IElementType PUBLIC_KEYWORD = new CfmlElementType("PUBLIC_KEYWORD");
  IElementType PRIVATE_KEYWORD = new CfmlElementType("PRIVATE_KEYWORD");
  IElementType REMOTE_KEYWORD = new CfmlElementType("REMOTE_KEYWORD");
  IElementType PACKAGE_KEYWORD = new CfmlElementType("PACKAGE_KEYWORD");

  IElementType ANY_TYPE = new CfmlElementType("ANY_TYPE");
  IElementType ARRAY_TYPE = new CfmlElementType("ARRAY_TYPE");
  IElementType BINARY_TYPE = new CfmlElementType("BINARY_TYPE");
  IElementType BOOLEAN_TYPE = new CfmlElementType("BOOLEAN_TYPE");
  IElementType DATE_TYPE = new CfmlElementType("DATE_TYPE");
  IElementType GUID_TYPE = new CfmlElementType("GUID_TYPE");
  IElementType NUMERIC_TYPE = new CfmlElementType("NUMERIC_TYPE");
  IElementType QUERY_TYPE = new CfmlElementType("QUERY_TYPE");
  IElementType STRING_TYPE = new CfmlElementType("STRING_TYPE");
  IElementType STRUCT_TYPE = new CfmlElementType("STRUCT_TYPE");
  IElementType UUID_TYPE = new CfmlElementType("UUID_TYPE");
  IElementType VARIABLENAME_TYPE = new CfmlElementType("VARIABLENAME_TYPE");
  IElementType VOID_TYPE = new CfmlElementType("VOID_TYPE");
  IElementType XML_TYPE = new CfmlElementType("XML_TYPE");
  IElementType ACTION_NAME = new CfmlElementType("ACTION_NAME");

  IElementType IMPORT_KEYWORD = new CfmlElementType("IMPORT_KEYWORD");

  TokenSet TYPE_KEYWORDS = TokenSet
    .create(ANY_TYPE, ARRAY_TYPE, BINARY_TYPE, BOOLEAN_TYPE, DATE_TYPE, GUID_TYPE, NUMERIC_TYPE, QUERY_TYPE, STRING_TYPE, STRUCT_TYPE,
            UUID_TYPE, VARIABLENAME_TYPE, VOID_TYPE, XML_TYPE, COMPONENT_KEYWORD);

  TokenSet ACCESS_KEYWORDS = TokenSet.create(PUBLIC_KEYWORD, PRIVATE_KEYWORD, REMOTE_KEYWORD, PACKAGE_KEYWORD);
  TokenSet KEYWORDS = TokenSet
    .create(SCOPE_KEYWORD, DEFAULT_KEYWORD, FUNCTION_KEYWORD, SWITCH_KEYWORD, CASE_KEYWORD, DO_KEYWORD, IF_KEYWORD, TRY_KEYWORD,
            CATCH_KEYWORD, ELSE_KEYWORD, VAR_KEYWORD, WHILE_KEYWORD, CONTINUE_KEYWORD, FOR_KEYWORD, RETHROW_KEYWORD, RETURN_KEYWORD,
            BREAK_KEYWORD, ABORT_KEYWORD, INCLUDE_KEYWORD, BOOLEAN,

            COMPONENT_KEYWORD, INTERFACE_KEYWORD,
            PUBLIC_KEYWORD, PRIVATE_KEYWORD, REMOTE_KEYWORD, PACKAGE_KEYWORD,
            REQUIRED_KEYWORD,
            ANY_TYPE, ARRAY_TYPE, BINARY_TYPE, BOOLEAN_TYPE, DATE_TYPE, GUID_TYPE, NUMERIC_TYPE, QUERY_TYPE, STRING_TYPE, STRUCT_TYPE,
            UUID_TYPE, VARIABLENAME_TYPE, VOID_TYPE, XML_TYPE, FINALLY_KEYWORD,
            IMPORT_KEYWORD, ACTION_NAME);

  TokenSet LOGICAL_OPERATIONS = TokenSet.create(OR, AND, AND_L, OR_L, NOT_XOR_L, XOR_L, IMP_L);
  TokenSet RELATIONAL_OPERATIONS =
    TokenSet.create(EQ_L, NEQ_L, CONTAINS_L, NOT_CONTAINS_L, GT_L, LT_L, GE_L, LE_L, EQEQ, NEQ, LT, LTE, GT, GTE);
  TokenSet ADDITIVE_OPERATIONS = TokenSet.create(ADD, MINUS);
  TokenSet MULTIPLICATIVE_OPERATIONS = TokenSet.create(POW, MUL, DEV, MOD_L, CONCAT);
  TokenSet UNARY_OPERATIONS = TokenSet.create(MINUS, NOT, NOT_L, INC, DEC);
}
