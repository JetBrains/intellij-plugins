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
package com.intellij.coldFusion.model.parsers;

import com.intellij.coldFusion.CfmlBundle;
import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.Nullable;

import static com.intellij.coldFusion.model.lexer.CfscriptTokenTypes.*;

/**
 * Created by Lera Nikolaenko
 */
public class CfmlExpressionParser {
  private PsiBuilder myBuilder = null;

  public CfmlExpressionParser(final PsiBuilder builder) {
    myBuilder = builder;
  }

  /*
     EXPRESSION := VALUE | VALUE BOP VALUE
  */
  /*
  public boolean parseExpression() {
      boolean ifComplex = false;

      if (closeExpressionToken()) return false;
      PsiBuilder.Marker expressionMarker = myBuilder.mark();
      parseOperand();
      while (!closeExpressionToken()) {
          int offset = myBuilder.getCurrentOffset();
          ifComplex = true;
          if (BINARY_OPERATIONS.contains(getTokenType())) {
              advance();
              if (closeExpressionToken()) {
                  myBuilder.error(CfmlBundle.message("cfml.parsing.right.operand.missed"));
                  break;
              }
          } else {
              myBuilder.error(CfmlBundle.message("cfml.parsing.binary.op.expected"));
          }
          parseOperand();
          if (myBuilder.getCurrentOffset() == offset) {
              myBuilder.error(CfmlBundle.message("cfml.parsing.unexpected.token"));
              advance();
          }
      }
      if (ifComplex) {
          expressionMarker.done(CfmlCompositeElementTypes.NONE);
      } else {
          expressionMarker.drop();
      }
      return true;
  }
  */
  public boolean parseExpression() {
    if (myBuilder.getTokenType() == FUNCTION_KEYWORD) {
      new CfscriptParser().parseFunctionExpression(myBuilder, true);
    }
    PsiBuilder.Marker expr = myBuilder.mark();
    if (!parseBinaryExpression()) {
      expr.drop();
      return false;
    }
    if (getTokenType() == QUESTION) {
      advance();
      if (!parseExpression()) {
        myBuilder.error(CfmlBundle.message("cfml.parsing.expression.expected"));
        expr.drop();
        return true;
      }
      if (getTokenType() == DOTDOT) {
        advance();
        if (!parseExpression()) {
          myBuilder.error(CfmlBundle.message("cfml.parsing.expression.expected"));
          expr.drop();
          return true;
        }
      }
      else {
        myBuilder.error(CfmlBundle.message("cfml.parsing.dot.dot.expected"));
        expr.drop();
        return true;
      }
      expr.done(CfmlElementTypes.TERNARY_EXPRESSION);
    } else if (myBuilder.getTokenType() == ELVIS) {
      advance();
      if (!parseExpression()) {
        myBuilder.error(CfmlBundle.message("cfml.parsing.expression.expected"));
        expr.drop();
        return true;
      }
      expr.done(CfmlElementTypes.BINARY_EXPRESSION);
    }
    else {
      expr.drop();
    }
    return true;
  }

  private boolean parseBinaryExpression() {
    PsiBuilder.Marker expr = myBuilder.mark();
    if (!parseRelationalExpression()) {
      expr.drop();
      return false;
    }
    while (LOGICAL_OPERATIONS.contains(myBuilder.getTokenType())) {
      myBuilder.advanceLexer();
      if (!parseRelationalExpression()) {
        myBuilder.error(CfmlBundle.message("cfml.parsing.expression.expected"));
      }
      expr.done(CfmlElementTypes.BINARY_EXPRESSION);
      expr = expr.precede();
    }
    expr.drop();
    return true;
  }

  private boolean parseRelationalExpression() {

    PsiBuilder.Marker expr = myBuilder.mark();
    if (!parseAdditiveExpression()) {
      expr.drop();
      return false;
    }
    while (RELATIONAL_OPERATIONS.contains(myBuilder.getTokenType())) {
      myBuilder.advanceLexer();
      if (!parseAdditiveExpression()) {
        myBuilder.error(CfmlBundle.message("cfml.parsing.expression.expected"));
      }
      expr.done(CfmlElementTypes.BINARY_EXPRESSION);
      expr = expr.precede();
    }
    expr.drop();
    return true;
  }

  private boolean parseAdditiveExpression() {
    PsiBuilder.Marker expr = myBuilder.mark();
    if (!parseMultiplicativeExpression()) {
      expr.drop();
      return false;
    }
    while (ADDITIVE_OPERATIONS.contains(myBuilder.getTokenType())) {
      myBuilder.advanceLexer();
      if (!parseMultiplicativeExpression()) {
        myBuilder.error(CfmlBundle.message("cfml.parsing.expression.expected"));
      }
      expr.done(CfmlElementTypes.BINARY_EXPRESSION);
      expr = expr.precede();
    }
    expr.drop();
    return true;
  }

  private boolean parseMultiplicativeExpression() {
    PsiBuilder.Marker expr = myBuilder.mark();
    if (parseAssignmentIfValid()) {
      expr.drop();
      return true;
    }
    if (!parseUnaryExpression()) {
      expr.drop();
      return false;
    }
    while (MULTIPLICATIVE_OPERATIONS.contains(myBuilder.getTokenType())) {
      myBuilder.advanceLexer();
      if (!parseUnaryExpression()) {
        myBuilder.error(CfmlBundle.message("cfml.parsing.expression.expected"));
      }
      expr.done(CfmlElementTypes.BINARY_EXPRESSION);
      expr = expr.precede();
    }
    expr.drop();
    return true;
  }

  private boolean parseAssignmentIfValid() {
    PsiBuilder.Marker marker = myBuilder.mark();
    if (!parseAssignmentExpression(false)) {
      marker.rollbackTo();
      return false;
    }
    marker.drop();
    return true;
  }

  private boolean parseUnaryExpression() {
    final IElementType tokenType = myBuilder.getTokenType();
    if (UNARY_OPERATIONS.contains(tokenType)) {
      final PsiBuilder.Marker expr = myBuilder.mark();
      myBuilder.advanceLexer();
      if (!parseUnaryExpression()) {
        myBuilder.error(CfmlBundle.message("cfml.parsing.expression.expected"));
      }
      expr.done(CfmlElementTypes.UNARY_EXPRESSION);
      return true;
    }
    else {
      parseOperand();
      return true;
    }
  }

  private boolean parseAssignmentExpression(boolean allowDotDot) {
    PsiBuilder.Marker statementMarker = myBuilder.mark();
    int statementMarkerPosition = myBuilder.getCurrentOffset();
    if (getTokenType() == VAR_KEYWORD) {
      advance();
    }
    if (!parseLValue()) {
      statementMarker.done(CfmlElementTypes.SCRIPT_EXPRESSION);
      myBuilder.error(CfmlBundle.message("cfml.parsing.l.value.expected"));
      return false;
    }
    IElementType tokenType = getTokenType();
    if (tokenType != CfmlTokenTypes.ASSIGN && (!allowDotDot || tokenType != DOTDOT)) {
      statementMarker.done(CfmlElementTypes.SCRIPT_EXPRESSION);
      myBuilder.error(CfmlBundle.message("cfml.parsing.assignment.expected"));
      return false;
    }
    else {
      advance();
    }
    if (!parseRValue()) {
      statementMarker.done(CfmlElementTypes.SCRIPT_EXPRESSION);
      myBuilder.error(CfmlBundle.message("cfml.parsing.right.operand.missed"));
      return false;
    }
    if (statementMarkerPosition != myBuilder.getCurrentOffset()) {
      statementMarker.done(CfmlElementTypes.ASSIGNMENT);
    }
    else {
      statementMarker.drop();
    }
    return true;
  }

  public boolean parseRValue() {
    if (!parseStructureDefinition()) {
      PsiBuilder.Marker referenceExpression = myBuilder.mark();
      if (!parseNewExpression()) {
        referenceExpression.rollbackTo();
        if (!parseArrayDefinition()) {
          if (!parseExpression()) {
            return false;
          }
        }
      }
      else if (myBuilder.getTokenType() == POINT) {
        referenceExpression.rollbackTo();
        parseReference(false);
      }
      else {
        referenceExpression.drop();
      }
    }
    return true;
  }

  public void parsePrefixOperationExpression() {
    if (!PREFIX_OPERATIONS.contains(getTokenType())) {
      return;
    }
    PsiBuilder.Marker prefixExpressionMarker = myBuilder.mark();
    advance();
    if (!parseLValue()) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.l.value.expected"));
    }
    prefixExpressionMarker.done(CfmlElementTypes.NONE);
  }

  /**
   * STATEMENT := ASSIGN | FUNCTION_CALL_NAME | MODIFICATION_EXPRESSION
   * ASSIGN := LVALUE ASSIGN_OP (STRUCT_DEF | ARRAY_DEF | EXPRESSION)
   * MODIFICATION_EXPRESSION := PREFIX_OP LVALUE
   * ASSIGN_OP := = | += | *= ...
   */
  public void parseStatement() {
    if (myBuilder.eof() || closeExpressionToken()) {
      return;
    }
    int offset = myBuilder.getCurrentOffset();
    // parse prefix operation with value
    if (PREFIX_OPERATIONS.contains(getTokenType())) {
      parsePrefixOperationExpression();
      return;
    }
    PsiBuilder.Marker statementMarker = myBuilder.mark();
    if (getTokenType() == VAR_KEYWORD) {
      // advance();
      parseAssignmentExpression(false);
      statementMarker.drop();
      return;
    }
    // parse function call
    if (!parseLValue()) {
      statementMarker.drop();
      return;
    }

    boolean isClearAssign = false;
    boolean isProbableVariableDef = false;
    if (POSTFIX_OPERATIONS.contains(getTokenType())) {
      advance();
      statementMarker.done(CfmlElementTypes.NONE);
      return;
    }
    else if (!ASSIGN_OPERATORS.contains(getTokenType())) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.statement.expected"));
      if (OPERATIONS.contains(getTokenType())) {
        advance();
        myBuilder.error(CfmlBundle.message("cfml.parsing.assignment.expected"));
        // TODO: test code
        isClearAssign = true;
      }
    }
    else {
      isProbableVariableDef = isClearAssign = (getTokenType() == CfmlTokenTypes.ASSIGN);
      if (myBuilder.eof()) {
        statementMarker.drop();
        return;
      }
      advance();
    }
    if (!isClearAssign || !parseStructureDefinition()) {
      if (!isClearAssign || !parseNewExpression()) {
        if (!isClearAssign || !parseArrayDefinition()) {
          if (!parseExpression()) {
            if (offset == myBuilder.getCurrentOffset()) {
              statementMarker.drop();
              myBuilder.error(CfmlBundle.message("cfml.parsing.unexpected.token"));
              advance();
              return;
            }
            myBuilder.error(CfmlBundle.message("cfml.parsing.right.operand.missed"));
            statementMarker.drop();
            return;
          }
        }
      }
    }
    if (offset == myBuilder.getCurrentOffset()) {
      statementMarker.drop();
      myBuilder.error(CfmlBundle.message("cfml.parsing.unexpected.token"));
      advance();
      return;
    }
    if (isProbableVariableDef) {
      statementMarker.done(CfmlElementTypes.ASSIGNMENT);
    }
    else {
      statementMarker.drop();
    }
  }

  // TODO: think about whether I can make this code more cfml independent (using CLOSER or ANGLEBRACKET keywords)

  private boolean closeExpressionToken() {
    return getTokenType() == CfmlTokenTypes.END_EXPRESSION ||
           getTokenType() == CLOSESHARP ||
           getTokenType() == CfmlTokenTypes.CLOSER ||
           getTokenType() == CfmlTokenTypes.R_ANGLEBRACKET ||
           myBuilder.eof() ||
           getTokenType() == R_BRACKET ||
           getTokenType() == R_SQUAREBRACKET ||
           getTokenType() == COMMA ||
           getTokenType() == CfmlTokenTypes.ASSIGN ||
           getTokenType() == CfmlTokenTypes.OPENER ||
           getTokenType() == CfmlTokenTypes.LSLASH_ANGLEBRACKET ||
           getTokenType() == SEMICOLON ||
           getTokenType() == R_CURLYBRACKET ||
           getTokenType() == L_CURLYBRACKET/* ||
                getTokenTYpe() == CfmlTokenTypes.R*/;
  }

  // parse ([EXPRESSION])*

  private void parseArrayAccess() {
    if (getTokenType() != L_SQUAREBRACKET) {
      return;
    }
    advance();
    parseExpression();
    if (getTokenType() != R_SQUAREBRACKET) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.square.bracket.expected"));
    }
    else {
      advance();
    }
  }

  private boolean parseID(boolean ifSharpsInIDs) {
    if (!ifSharpsInIDs) {
      if (getTokenType() == IDENTIFIER || CfscriptTokenTypes.KEYWORDS.contains(getTokenType())) {
        advance();
        return true;
      }
    }
    else {
      // SHARPED_ID := (#EXPRESSION#)*IDENTIFIER(#EXPRESSION#)*
      if (getTokenType() != OPENSHARP && getTokenType() != IDENTIFIER) {
        return false;
      }
      while (getTokenType() == OPENSHARP || getTokenType() == IDENTIFIER) {
        while (getTokenType() == OPENSHARP) {
          parseSharpExpr();
        }
        if (getTokenType() == IDENTIFIER) {
          advance();
        }
        while (getTokenType() == OPENSHARP) {
          parseSharpExpr();
        }
      }
      return true;
    }
    return false;
  }

  public void parseComponentReference() {
    assert myBuilder.getTokenType() == IDENTIFIER;
    PsiBuilder.Marker componentReferenceMarker = myBuilder.mark();
    while (myBuilder.getTokenType() == IDENTIFIER || KEYWORDS.contains(myBuilder.getTokenType())) {
      myBuilder.advanceLexer();
      if (myBuilder.getTokenType() != POINT) {
        break;
      }
      myBuilder.advanceLexer();
    }
    componentReferenceMarker.done(CfmlElementTypes.COMPONENT_REFERENCE);
  }

  // TODO: add flag "if component reference"
  public boolean parseReference(boolean ifSharpsInIDs) {
    boolean isReference = false;
    PsiBuilder.Marker referenceExpression = myBuilder.mark();
    boolean start = true;

    while (getTokenType() == POINT || start) {
      if (start) {
        if (getTokenType() == SCOPE_KEYWORD) {
          advance();
          if (getTokenType() == POINT) {
            advance();
          }
          else {
            myBuilder.error(CfmlBundle.message("cfml.parsing.dot.expected"));
          }
        }
        else if (parseNewExpression()) {
          if (getTokenType() == POINT) {
            advance();
          }
        }
      }

      if (!start && getTokenType() == POINT) {
        advance();
      }
      start = false;
      if (!parseID(ifSharpsInIDs)) {
        break;
      }
      isReference = true;
      referenceExpression.done(CfmlElementTypes.REFERENCE_EXPRESSION);

      do {
        if (getTokenType() == L_BRACKET) {
          isReference = false;
          referenceExpression = referenceExpression.precede();
          parseArgumentsList();
          referenceExpression.done(CfmlElementTypes.FUNCTION_CALL_EXPRESSION);
        }
        // parse ([])*
        int arrayAccessNumber = 0;
        while (getTokenType() == L_SQUAREBRACKET) {
          isReference = true;
          referenceExpression = referenceExpression.precede();
          parseArrayAccess();
          // referenceExpression.done(CfmlElementTypes.ARRAY_ACCESS);
          referenceExpression.done(CfmlElementTypes.NONE);
          arrayAccessNumber++;
        }
        if (arrayAccessNumber != 1) {
          break;
        }
      }
      while (getTokenType() == L_BRACKET);
      referenceExpression = referenceExpression.precede();
    }
    referenceExpression.drop();

    return isReference;
  }

  // Parsing up to the first error the left value of assignment in cfset tag
  // it differs from parseReferenceOrMethodCall just in:
  //   1) it must check if it is not a method call
  //   2) if value is inside quotes than dynamic variable naming available

  private boolean parseLValue() {
    boolean isReference;

    if (getTokenType() == CfmlTokenTypes.DOUBLE_QUOTE || getTokenType() == CfmlTokenTypes.SINGLE_QUOTE) {
      advance();
      /*if (myBuilder.getTokenType() == STRING_TEXT) {
        advance();
      }*/
      PsiBuilder.Marker lValueMarker = myBuilder.mark();
      isReference = (parseReference(true) || parseStringReference(true));
      if (!isReference) {
        lValueMarker.error(CfmlBundle.message("cfml.parsing.l.value.expected"));
      }
      else {
        lValueMarker.drop();
      }

      if (getTokenType() == OPENSHARP) {
        parseSharpExpr();
      }
      if (getTokenType() != CfmlTokenTypes.DOUBLE_QUOTE_CLOSER && getTokenType() != CfmlTokenTypes.SINGLE_QUOTE_CLOSER) {
        myBuilder.error(CfmlBundle.message("cfml.parsing.quote.expected"));
        // doneBefore(lValueMarker, "Quote expected");
      }
      else {
        // lValueMarker.drop();
        advance();
      }
      return true;
    }
    else {
      boolean result = parseReference(false);
      if (POSTFIX_OPERATIONS.contains(getTokenType())) {
        advance();
        return false;
      }
      return result;
    }
  }

  public boolean parseStringReference(boolean isLValueInQuotes) {
    boolean isReference = false;
    if (isLValueInQuotes) {
      PsiBuilder.Marker stringReferenceExpression = myBuilder.mark();
      advance();
      stringReferenceExpression.drop();
      isReference = true; //it is not exactly a reference, it is a String_Text type as new Field name in create structure expression;
    }
    return isReference;
  }

  private boolean parseArgumentsList() {
    if (getTokenType() == L_BRACKET) {
      PsiBuilder.Marker argumentList = myBuilder.mark();
      advance();
      if (getTokenType() != R_BRACKET) {
        boolean first = true;
        while (first || getTokenType() == COMMA) {
          if (!first) {
            advance();
          }
          first = false;
          PsiBuilder.Marker markArgumentName = myBuilder.mark();
          if (getTokenType() == IDENTIFIER) {
            advance();
            if (getTokenType() == CfmlTokenTypes.ASSIGN) {
              PsiBuilder.Marker assignment = markArgumentName.precede();
              markArgumentName.done(CfmlElementTypes.ARGUMENT_NAME);
              advance();
              parseExpression();
              assignment.done(CfmlElementTypes.ASSIGNMENT);
              continue;
            }
            else if (getTokenType() == COMMA || getTokenType() == R_BRACKET) {
              markArgumentName.done(CfmlElementTypes.ARGUMENT_NAME);
              continue;
            }
          }
          else if (parseRValue()) {
            markArgumentName.drop();
            continue;
          }
          markArgumentName.rollbackTo();
          parseExpression();
        }
      }
      if (getTokenType() != R_BRACKET) {
        argumentList.done(CfmlElementTypes.SCRIPT_EXPRESSION);
        myBuilder.error(CfmlBundle.message("cfml.parsing.close.bracket.expected"));
        return true;
      }
      advance();
      argumentList.done(CfmlElementTypes.ARGUMENT_LIST);
      return true;
    }
    return false;
  }

  /*
     ASSIGNLIST := LVALUE (=|:) EXPRESSION, ASSIGNLIST | LVALUE (=|:) EXPRESSION
  */

  private void parseAssignsList() {
    while(true) {
      parseAssignmentExpression(true);
      if (getTokenType() != COMMA) break;
      advance();
    }
  }

  public boolean parseNewExpression() {
    if (getTokenType() != IDENTIFIER) {
      return false;
    }
    if (!"new".equalsIgnoreCase(getTokenText())) {
      return false;
    }
    PsiBuilder.Marker newExpression = mark();
    advance();
    PsiBuilder.Marker constructorCall = mark();
    if (getTokenType() == CfmlTokenTypes.DOUBLE_QUOTE) {
      parseString();
    }
    else if (getTokenType() == CfscriptTokenTypes.IDENTIFIER) {
      parseComponentReference();
    }
    else {
      myBuilder.error(CfmlBundle.message("cfml.parsing.identifier.expected"));
    }
    parseArgumentsList();
    constructorCall.done(CfmlElementTypes.COMPONENT_CONSTRUCTOR_CALL);
    newExpression.done(CfmlElementTypes.NEW_EXPRESSION);
    return true;
  }

  /*
     STRUCTURE_DEFINITION := {ASSIGNLIST}
  */

  public boolean parseStructureDefinition() {
    PsiBuilder.Marker structDefMarker = mark();
    IElementType tokenType = getTokenType();
    if (tokenType == L_CURLYBRACKET) {
      advance();
      if (getTokenType() != R_CURLYBRACKET) {
        parseAssignsList();
      }
    }
    else {
      structDefMarker.drop();
      return false;
    }

    if (getTokenType() != R_CURLYBRACKET) {
      structDefMarker.done(CfmlElementTypes.SCRIPT_EXPRESSION);
      myBuilder.error(CfmlBundle.message("cfml.parsing.close.bracket.expected"));
      return true;
    }
    advance();
    structDefMarker.drop();
    return true;
  }

  /*
     ARRAY_DEFINITION := [EXPRESSION_LIST]
  */

  public boolean parseArrayDefinition() {
    IElementType tokenType = getTokenType();
    if (tokenType != L_SQUAREBRACKET) {
      return false;
    }
    PsiBuilder.Marker arrayDefMarker = mark();
    getTokenType();
    advance();
    if (getTokenType() != R_SQUAREBRACKET) {
      parseRValuesList();
    }

    if (getTokenType() != R_SQUAREBRACKET) {
      arrayDefMarker.done(CfmlElementTypes.SCRIPT_EXPRESSION);
      myBuilder.error(CfmlBundle.message("cfml.parsing.square.bracket.expected"));
      return true;
    }
    advance();
    arrayDefMarker.drop();
    return true;
  }

  public void parseString() {
    IElementType tokenType = getTokenType();
    assert tokenType == CfmlTokenTypes.SINGLE_QUOTE || tokenType == CfmlTokenTypes.DOUBLE_QUOTE;
    PsiBuilder.Marker stringLiteral = myBuilder.mark();
    advance();
    if (getTokenType() != CfmlTokenTypes.SINGLE_QUOTE_CLOSER && getTokenType() != CfmlTokenTypes.DOUBLE_QUOTE_CLOSER && !myBuilder.eof()) {
      parseStringText();
    }
    if (getTokenType() != CfmlTokenTypes.SINGLE_QUOTE_CLOSER && getTokenType() != CfmlTokenTypes.DOUBLE_QUOTE_CLOSER) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.quote.expected"));
      stringLiteral.done(CfmlElementTypes.STRING_LITERAL);
      return;
    }
    advance();
    stringLiteral.done(CfmlElementTypes.STRING_LITERAL);
  }

  /*
     VALUE := (EXPRESSION) | SMART_IDENTIFIER | "STRING" | 'STRING' |
         FUNCTION_DEFINITION(EXPR_LIST) | INTEGER | DOUBLE | SHARP_EXPRESSION |
         PREFIX_OP IDENTIFIER | IDENTIFIER POSTFIX_OP | ARRAY_DEFINITION | STRUCTURE_DEFINITION
  */

  private void parseOperand() {
    // PsiBuilder.Marker valueMarker = mark();
    IElementType tokenType = getTokenType();
    if (tokenType == L_BRACKET) {
      advance();
      parseExpression();
      if (getTokenType() != R_BRACKET) {
        myBuilder.error(CfmlBundle.message("cfml.parsing.close.bracket.expected"));
        return;
      }
      advance();
    }
    if (parseArrayDefinition() || parseStructureDefinition()) {
    }
    else if (tokenType == CfmlTokenTypes.SINGLE_QUOTE || tokenType == CfmlTokenTypes.DOUBLE_QUOTE) {
      parseString();
    }
    else if (tokenType == INTEGER) {
      PsiBuilder.Marker integerLiteral = myBuilder.mark();
      advance();
      integerLiteral.done(CfmlElementTypes.INTEGER_LITERAL);
    }
    else if (tokenType == BOOLEAN) {
      PsiBuilder.Marker integerLiteral = myBuilder.mark();
      advance();
      integerLiteral.done(CfmlElementTypes.BOOLEAN_LITERAL);
    }
    else if (tokenType == DOUBLE) {
      PsiBuilder.Marker integerLiteral = myBuilder.mark();
      advance();
      integerLiteral.done(CfmlElementTypes.DOUBLE_LITERAL);
    }
    else if (tokenType == OPENSHARP) {
      parseSharpExpr();
    }
    else if (PREFIX_OPERATIONS.contains(tokenType)) {
      advance();
      parseOperand();
      // parseReference(false, false);
    }
    else if (tokenType == BAD_CHARACTER) {
      PsiBuilder.Marker badCharMark = myBuilder.mark();
      while (getTokenType() == BAD_CHARACTER) {
        advance();
      }
      badCharMark.error(CfmlBundle.message("cfml.parsing.unexpected.token"));
    }
    else {
      parseReference(false);
      if (POSTFIX_OPERATIONS.contains(getTokenType())) {
        advance();
      }
    }
  }

  /*
     SHARP_EXPRESSION := #EXPRESSION#
  */

  private void parseSharpExpr() {
    // PsiBuilder.Marker sharpExprMarker = mark();

    if (getTokenType() != OPENSHARP) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.sharp.expected"));
      return;
    }
    advance();
    parseExpression();
    if (getTokenType() != CLOSESHARP) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.sharp.expected"));
    }
    else {
      advance();
    }
    // sharpExprMarker.done(CfmlElementTypes.SHARPS_EXPRESSION);
  }

  /*
     STRING_TEXT := Eps | CfscriptTokenTypes.STRING_TEXT | STRING_TEXT SHARP_EXPRESSION STRING_TEXT;
  */

  private void parseStringText() {
    IElementType tokenType = getTokenType();
    if (tokenType != CfmlTokenTypes.STRING_TEXT && tokenType != OPENSHARP) {
      return;
    }

    if (tokenType == CfmlTokenTypes.STRING_TEXT) {
      advance();
    }
    else {/*if (tokenType == OPENSHARP) {*/
      parseSharpExpr();
    }
    parseStringText();
  }

  /*
     EXPRESSION_LIST := EXPRESSION | EXPRESSION, EXPRESSION_LIST
  */

  private void parseRValuesList() {
    if (!parseStructureDefinition()) {
      if (!parseArrayDefinition()) {
        parseExpression();
      }
    }
    if (getTokenType() == COMMA) {
      advance();
      parseRValuesList();
    }
  }

  // util methods

  @Nullable
  private String getTokenText() {
    return myBuilder.getTokenText();
  }

  @Nullable
  private IElementType getTokenType() {
    return myBuilder.getTokenType();
  }

  private void advance() {
    myBuilder.advanceLexer();
  }

  private PsiBuilder.Marker mark() {
    return myBuilder.mark();
  }
}
