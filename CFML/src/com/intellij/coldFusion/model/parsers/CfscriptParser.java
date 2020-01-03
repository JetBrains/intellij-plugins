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
import com.intellij.coldFusion.model.CfmlUtil;
import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.coldFusion.model.psi.stubs.CfmlStubElementTypes;
import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;

import java.util.Objects;

import static com.intellij.coldFusion.model.lexer.CfscriptTokenTypes.*;
import static com.intellij.coldFusion.model.parsers.CfmlKeywordsKt.isKeyword;
import static com.intellij.coldFusion.model.parsers.CfmlKeywordsKt.parseKeyword;

/**
 * Created by Lera Nikolaenko
 */
public class CfscriptParser {
  // parse statement or statements block in curly brackets
  // if no curly brackets than parse only one statement

  private static void parseExpression(PsiBuilder myBuilder) {
    (new CfmlExpressionParser(myBuilder)).parseExpression();
  }

  private static void parseRValue(PsiBuilder myBuilder) {
    (new CfmlExpressionParser(myBuilder)).parseRValue();
  }

  private static boolean parseOptionalIDAndTags_PropertyUtil(PsiBuilder myBuilder) {
    PsiBuilder.Marker propertyBodyMarker = myBuilder.mark();

    // parse Type ID attributes
    // parse ID attributes
    // parse attributes

    if (myBuilder.getTokenType() == IDENTIFIER) {
      myBuilder.advanceLexer();
      if (myBuilder.getTokenType() == CfmlTokenTypes.ASSIGN) {
        propertyBodyMarker.rollbackTo();
        CfmlParser.parseAttributes(myBuilder, "cfproperty", IDENTIFIER, true);
        return true;
      }
      else if (myBuilder.getTokenType() == IDENTIFIER) {
        PsiBuilder.Marker attributesMarker = myBuilder.mark();
        myBuilder.advanceLexer();
        if (myBuilder.getTokenType() == CfmlTokenTypes.ASSIGN) {
          attributesMarker.rollbackTo();
          propertyBodyMarker.drop();
          CfmlParser.parseAttributes(myBuilder, "cfproperty", IDENTIFIER, true);
          return true;
        }
        else {
          attributesMarker.drop();
          propertyBodyMarker.rollbackTo();
          return false;
        }
      }
      //try to parse type in "property type name;" expression//
      else if (myBuilder.getTokenType() == POINT) {
        if (!parseType(myBuilder) || myBuilder.getTokenType() != IDENTIFIER) {
          propertyBodyMarker.rollbackTo();
          return false;
        }
      }
      else if (myBuilder.getTokenType() == SEMICOLON) {
        propertyBodyMarker.drop();
        return true;
      }
    }
    propertyBodyMarker.drop();
    return false;
  }

  private static boolean parseProperty(PsiBuilder myBuilder) {
    assert myBuilder.getTokenType() == IDENTIFIER;
    PsiBuilder.Marker propertyMarker = myBuilder.mark();
    myBuilder.advanceLexer();

    // parse Type ID attributes
    // parse ID attributes
    // parse attributes
    if (!parseOptionalIDAndTags_PropertyUtil(myBuilder)) {
      if (!parseType(myBuilder)) {
        propertyMarker.drop();
        return false;
      }
      if (!parseOptionalIDAndTags_PropertyUtil(myBuilder)) {
        propertyMarker.drop();
        return false;
      }
      else {
        propertyMarker.done(CfmlElementTypes.PROPERTY);
        return true;
      }
    }
    else {
      propertyMarker.done(CfmlElementTypes.PROPERTY);
      return true;
    }
  }

  private void parseAction(PsiBuilder myBuilder) {
    // http://help.adobe.com/en_US/ColdFusion/9.0/Developing/WSc3ff6d0ea77859461172e0811cbec0999c-7ffa.html
    assert myBuilder.getTokenType() == IDENTIFIER;
    final String actionName = myBuilder.getTokenText();
    PsiBuilder.Marker actionMarker = myBuilder.mark();
    myBuilder.remapCurrentToken(ACTION_NAME);
    myBuilder.advanceLexer();
    if (myBuilder.getTokenType() == CfmlTokenTypes.ASSIGN) {
      actionMarker.rollbackTo();
      myBuilder.remapCurrentToken(IDENTIFIER);
      (new CfmlExpressionParser(myBuilder)).parseStatement();
      eatSemicolon(myBuilder);
    }
    else {
      CfmlParser.parseAttributes(myBuilder, "cfproperty", IDENTIFIER, true);//it's not really property, but have same syntax rules
      if ("param".equalsIgnoreCase(actionName)) {
        // See link above.
        eatSemicolon(myBuilder);
      }
      else {
        if (actionName != null && isKeyword(actionName) && Objects.requireNonNull(parseKeyword(actionName)).getOmitCodeBlock() && myBuilder.getTokenType() == SEMICOLON) {
          //do nothing
        } else{
          parseFunctionBody(myBuilder);
        }
      }
      actionMarker.done(CfmlElementTypes.ACTION);
    }
  }

  private void parseStatement(PsiBuilder myBuilder) {
    // PsiBuilder.Marker statement = myBuilder.mark();

    if (myBuilder.getTokenType() == IDENTIFIER &&
        "property".equalsIgnoreCase(myBuilder.getTokenText()) &&
        parseProperty(myBuilder)) {
    }
    else {
      (new CfmlExpressionParser(myBuilder)).parseStatement();
    }

    if(myBuilder.getTokenType() == L_CURLYBRACKET) {
      parseScript(myBuilder, false);
    } else {
      eatSemicolon(myBuilder);
    }
    // statement.done(CfscriptElementTypes.STATEMENT);
  }

  private static void parseCondition(PsiBuilder myBuilder) {
    (new CfmlExpressionParser(myBuilder)).parseExpression();
  }

  private static void parseInclude(PsiBuilder myBuilder) {
    if (myBuilder.getTokenType() != INCLUDE_KEYWORD) {
      return;
    }

    PsiBuilder.Marker marker = myBuilder.mark();
    myBuilder.advanceLexer();
    IElementType tokenType = myBuilder.getTokenType();
    if (tokenType != CfmlTokenTypes.SINGLE_QUOTE && tokenType != CfmlTokenTypes.DOUBLE_QUOTE) {
      marker.drop();
      return;
    }
    (new CfmlExpressionParser(myBuilder)).parseString();
    marker.done(CfmlElementTypes.INCLUDEEXPRESSION);
  }

  private static void parseImport(PsiBuilder myBuilder) {
    if (myBuilder.getTokenType() != IMPORT_KEYWORD) {
      return;
    }
    PsiBuilder.Marker marker = myBuilder.mark();
    myBuilder.advanceLexer();
    IElementType tokenType = myBuilder.getTokenType();
    if (tokenType == CfmlTokenTypes.SINGLE_QUOTE || tokenType == CfmlTokenTypes.DOUBLE_QUOTE) {
      (new CfmlExpressionParser(myBuilder)).parseString();
    }
    else if (tokenType == IDENTIFIER) {
      (new CfmlExpressionParser(myBuilder)).parseComponentReference();
    }
    marker.done(CfmlElementTypes.IMPORTEXPRESSION);
  }

  // eating numeric or string
  private static boolean parseConstant(PsiBuilder myBuilder) {
    IElementType tokenType = myBuilder.getTokenType();
    if (tokenType == INTEGER ||
        tokenType == DOUBLE || tokenType == BOOLEAN) {
      myBuilder.advanceLexer();
      return true;
    }
    else if (tokenType == CfmlTokenTypes.SINGLE_QUOTE || tokenType == CfmlTokenTypes.DOUBLE_QUOTE) {
      myBuilder.advanceLexer();
      tokenType = myBuilder.getTokenType();
      if (tokenType != CfmlTokenTypes.SINGLE_QUOTE_CLOSER && tokenType != CfmlTokenTypes.DOUBLE_QUOTE_CLOSER && !myBuilder.eof()) {
        myBuilder.advanceLexer();
      }
      tokenType = myBuilder.getTokenType();
      if (tokenType != CfmlTokenTypes.SINGLE_QUOTE_CLOSER && tokenType != CfmlTokenTypes.DOUBLE_QUOTE_CLOSER) {
        myBuilder.error(CfmlBundle.message("cfml.parsing.quote.expected"));
        return true;
      }
      myBuilder.advanceLexer();
      return true;
    }
    return false;
  }

  private boolean parseConditionInBrackets(PsiBuilder myBuilder) {
    if (myBuilder.getTokenType() != L_BRACKET) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.close.bracket.expected"));
      return false;
    }
    myBuilder.advanceLexer();
    parseCondition(myBuilder);
    if (myBuilder.getTokenType() != R_BRACKET) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.close.bracket.expected"));
      return true;
    }
    myBuilder.advanceLexer();
    return true;
  }

  private void parseComponentOrInterface(PsiBuilder myBuilder) {
    if (myBuilder.getTokenType() != COMPONENT_KEYWORD &&
        myBuilder.getTokenType() != INTERFACE_KEYWORD) {
      return;
    }
    PsiBuilder.Marker componentMarker = myBuilder.mark();
    boolean isComponent = myBuilder.getTokenType() == COMPONENT_KEYWORD;
    myBuilder.advanceLexer();
    /*
    if (myBuilder.getTokenType() != IDENTIFIER) {
      // myBuilder.error("cfml.parsing.identifier.expected");
    } else {
      final String nameOrAttribute = myBuilder.getTokenText();
      final CfmlTagDescription componentTag = CfmlLangInfo.myTagAttributes.get("cfcomponent");
      if (nameOrAttribute != null && !componentTag.hasAttribute(nameOrAttribute.toLowerCase())) {
        myBuilder.advanceLexer();
      }
    }
    */
    if (myBuilder.getTokenType() == FUNCTION_KEYWORD) {
      componentMarker.rollbackTo();
      parseFunctionExpression(myBuilder, false);
      return;
    }
    CfmlParser.parseAttributes(myBuilder, isComponent ? "cfcomponent" : "cfinterface", IDENTIFIER, true);
    if (myBuilder.getTokenType() != L_CURLYBRACKET) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.open.curly.bracket.expected"));
      componentMarker.drop();
      return;
    }
    myBuilder.advanceLexer();
    if (myBuilder.getTokenType() == PAGEENCODING_KEYWORD) {
      myBuilder.advanceLexer();
      IElementType tokenType = myBuilder.getTokenType();
      if (tokenType == CfmlTokenTypes.SINGLE_QUOTE || tokenType == CfmlTokenTypes.DOUBLE_QUOTE) {
        (new CfmlExpressionParser(myBuilder)).parseString();
        eatSemicolon(myBuilder);
      }
      else {
        myBuilder.error(CfmlBundle.message("cfml.parsing.string.expected"));
      }
    }
    parseScript(myBuilder, false, false, true);
    componentMarker.done(CfmlStubElementTypes.COMPONENT_DEFINITION);
  }

  private void parseIfExpression(PsiBuilder myBuilder) {
    if (myBuilder.getTokenType() != IF_KEYWORD) {
      return;
    }
    PsiBuilder.Marker ifExprMarker = myBuilder.mark();
    myBuilder.advanceLexer();
    if (!parseConditionInBrackets(myBuilder)) {
      ifExprMarker.drop();
      return;
    }

    parseScript(myBuilder, false);
    if (myBuilder.getTokenType() == ELSE_KEYWORD) {
      myBuilder.advanceLexer();
      if (myBuilder.getTokenType() == IF_KEYWORD) {
        parseIfExpression(myBuilder);
      }
      else {
        parseScript(myBuilder, false);
      }
    }
    ifExprMarker.done(CfmlElementTypes.IFEXPRESSION);
  }

  private void parseWhileExpression(PsiBuilder myBuilder) {
    if (myBuilder.getTokenType() != WHILE_KEYWORD) {
      return;
    }
    PsiBuilder.Marker whileMarker = myBuilder.mark();
    myBuilder.advanceLexer();
    parseConditionInBrackets(myBuilder);
    parseScript(myBuilder, false);
    whileMarker.done(CfmlElementTypes.WHILEEXPRESSION);
  }

  private void parseDoWhileExpression(PsiBuilder myBuilder) {
    if (myBuilder.getTokenType() != DO_KEYWORD) {
      return;
    }
    PsiBuilder.Marker doWhileMarker = myBuilder.mark();
    myBuilder.advanceLexer();
    if (myBuilder.getTokenType() != L_CURLYBRACKET) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.open.bracket.expected"));
      doWhileMarker.drop();
      return;
    }
    parseScript(myBuilder, false);
    if (myBuilder.getTokenType() != WHILE_KEYWORD) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.keyword.expected", "while"));
      doWhileMarker.done(CfmlElementTypes.DOWHILEEXPRESSION);
      return;
    }
    myBuilder.advanceLexer();
    parseConditionInBrackets(myBuilder);
    if (myBuilder.getTokenType() != SEMICOLON) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.semicolon.expected"));
    }
    else {
      myBuilder.advanceLexer();
    }
    doWhileMarker.done(CfmlElementTypes.DOWHILEEXPRESSION);
  }

  private static void eatSemicolon(PsiBuilder myBuilder) {
    if (myBuilder.getTokenType() != SEMICOLON) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.semicolon.expected"));
    }
    else {
      myBuilder.advanceLexer();
    }
  }

  private void parseForExpression(PsiBuilder myBuilder) {
    if (myBuilder.getTokenType() != FOR_KEYWORD) {
      return;
    }
    PsiBuilder.Marker forExpressionMarker = myBuilder.mark();
    myBuilder.advanceLexer();

    // eat opening bracket
    if (myBuilder.getTokenType() != L_BRACKET) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.close.bracket.expected"));
      forExpressionMarker.drop();
      return;
    }
    myBuilder.advanceLexer();

    if (!tryParseForIn(myBuilder)) {
      parseStatement(myBuilder);

      parseCondition(myBuilder);
      eatSemicolon(myBuilder);

      (new CfmlExpressionParser(myBuilder)).parseStatement();
    }
    if (myBuilder.getTokenType() != R_BRACKET) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.close.bracket.expected"));
      forExpressionMarker.done(CfmlElementTypes.FOREXPRESSION);
      return;
    }
    myBuilder.advanceLexer();

    parseScript(myBuilder, false);

    forExpressionMarker.done(CfmlElementTypes.FOREXPRESSION);
  }

  private static boolean tryParseForIn(PsiBuilder myBuilder) {
    PsiBuilder.Marker startMarker = myBuilder.mark();

    if (myBuilder.getTokenType() == VAR_KEYWORD) {
      myBuilder.advanceLexer();
    }
    PsiBuilder.Marker definitionMarker = myBuilder.mark();
    if (!(new CfmlExpressionParser(myBuilder)).parseReference(false)) {
      startMarker.rollbackTo();
      return false;
    }
    else {
      definitionMarker.done(CfmlElementTypes.FORVARIABLE);
    }
    if (myBuilder.getTokenType() != IN_L) {
      startMarker.rollbackTo();
      return false;
    }
    myBuilder.advanceLexer();

    if (!(new CfmlExpressionParser(myBuilder)).parseReference(false)) {
      (new CfmlExpressionParser(myBuilder)).parseArrayDefinition();
    }
    startMarker.drop();
    return true;
  }

  private void parseFunctionBody(PsiBuilder myBuilder) {
    PsiBuilder.Marker functionBodyMarker = myBuilder.mark();
    if (myBuilder.getTokenType() != L_CURLYBRACKET) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.open.curly.bracket.or.semicolon.expected"));
      functionBodyMarker.drop();
      return;
    }
    parseScript(myBuilder, false);
    // TODO eat return keyword
    functionBodyMarker.done(CfmlElementTypes.FUNCTIONBODY);
  }

  /**
   * ColdFusion9: [required] [type] name [= default value (constant)], ...
   */
  private void parseParametersList(PsiBuilder myBuilder) {
    PsiBuilder.Marker argumentMarker = myBuilder.mark();
    // parsing required keyword is present
    if (myBuilder.getTokenType() == REQUIRED_KEYWORD) {
      myBuilder.advanceLexer();
    }
    // try to parse type
    final PsiBuilder.Marker marker = myBuilder.mark();
    if (!parseType(myBuilder) || myBuilder.getTokenType() != IDENTIFIER) {
      marker.rollbackTo();
    }
    else {
      marker.drop();
    }
    // parse parameter name
    if (myBuilder.getTokenType() == IDENTIFIER) {
      myBuilder.advanceLexer();

      if (myBuilder.getTokenType() == CfmlTokenTypes.ASSIGN) { // parse default value
        // TODO: to find out whether it can be something different from a constant
        myBuilder.advanceLexer();
        final PsiBuilder.Marker defaultValueMarker = myBuilder.mark();
        if (!parseConstant(myBuilder)) {
          parseRValue(myBuilder);
        }
        defaultValueMarker.done(CfmlElementTypes.VALUE);
      }

      if (myBuilder.getTokenType() == IDENTIFIER) {
        CfmlParser.parseAttributes(myBuilder, "cfparameter", IDENTIFIER, true);
      }

      argumentMarker.done(CfmlElementTypes.FUNCTION_ARGUMENT);
      if (myBuilder.getTokenType() == COMMA) {
        myBuilder.advanceLexer();
        parseParametersList(myBuilder);
      }
    }
    else {
      argumentMarker.drop();
    }
  }

  private void parseParametersListInBrackets(PsiBuilder myBuilder) {
    PsiBuilder.Marker argumentsList = myBuilder.mark();
    eatLeftBracket(myBuilder);
    parseParametersList(myBuilder);
    eatRightBracket(myBuilder);
    argumentsList.done(CfmlElementTypes.PARAMETERS_LIST);
  }

  void parseFunctionExpression(PsiBuilder myBuilder, boolean anonymous) {
    PsiBuilder.Marker functionMarker = myBuilder.mark();
    if (!anonymous) {
      if (ACCESS_KEYWORDS.contains(myBuilder.getTokenType())) {
        myBuilder.advanceLexer();
      }
      if (myBuilder.getTokenType() != FUNCTION_KEYWORD) {
        parseType(myBuilder);
      }
    }
    if (myBuilder.getTokenType() != FUNCTION_KEYWORD) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.function.expected"));
      functionMarker.drop();
      return;
    }
    myBuilder.advanceLexer();
    if (myBuilder.getTokenType() != IDENTIFIER &&
        !(myBuilder.getTokenType() == DEFAULT_KEYWORD) && !anonymous) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.identifier.expected"));
    }
    else {
      if (!anonymous) myBuilder.advanceLexer();
    }
    parseParametersListInBrackets(myBuilder);
    CfmlParser.parseAttributes(myBuilder, "cffunction", IDENTIFIER, true);
    parseFunctionBody(myBuilder);
    functionMarker.done(CfmlElementTypes.FUNCTION_DEFINITION);
  }

  private void parseDOTDOTAndScript(PsiBuilder myBuilder) {
    if (myBuilder.getTokenType() != DOTDOT) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.dot.dot.expected"));
    }
    else {
      myBuilder.advanceLexer();
    }
    if (myBuilder.getTokenType() != L_CURLYBRACKET) {
      if (myBuilder.getTokenType() == CASE_KEYWORD) {
        return;
      }
      int offset = myBuilder.getCurrentOffset();
      while (!myBuilder.eof() &&
             myBuilder.getTokenType() != BREAK_KEYWORD &&
             myBuilder.getTokenType() != DEFAULT_KEYWORD &&
             myBuilder.getTokenType() != CASE_KEYWORD &&
             myBuilder.getTokenType() != R_CURLYBRACKET && !isEndOfScript(myBuilder)) {
        if (!parseScript(myBuilder, false)) {
          break;
        }
        if (offset == myBuilder.getCurrentOffset()) {
          break;
        }
        offset = myBuilder.getCurrentOffset();
      }

      if (myBuilder.getTokenType() == BREAK_KEYWORD) {
        myBuilder.advanceLexer();
        eatSemicolon(myBuilder);
      }
    }
    else {
      parseScript(myBuilder, false);
    }
  }

  private void parseCaseExpression(PsiBuilder myBuilder) {
    if (myBuilder.getTokenType() != CASE_KEYWORD) {
      return;
    }
    PsiBuilder.Marker caseExpressionMarker = myBuilder.mark();
    myBuilder.advanceLexer();
    if (!parseConstant(myBuilder)) {
      if (myBuilder.getTokenType() == IDENTIFIER) {
        myBuilder.error(CfmlBundle.message("cfml.parsing.constant.expected"));
        myBuilder.advanceLexer();
      }
      else {
        myBuilder.error(CfmlBundle.message("cfml.parsing.constant.expected"));
      }
    }
    parseDOTDOTAndScript(myBuilder);
    caseExpressionMarker.done(CfmlElementTypes.CASEEXPRESSION);
  }


  private void parseSwitchExpression(PsiBuilder myBuilder) {
    PsiBuilder.Marker switchMarker = myBuilder.mark();
    if (myBuilder.getTokenType() != SWITCH_KEYWORD) {
      return;
    }
    myBuilder.advanceLexer();
    if (!eatLeftBracket(myBuilder)) {
      switchMarker.drop();
      return;
    }
    parseExpression(myBuilder);
    eatRightBracket(myBuilder);
    if (myBuilder.getTokenType() != L_CURLYBRACKET) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.open.curly.bracket.expected"));
      switchMarker.drop();
      return;
    }
    myBuilder.advanceLexer();
    while (myBuilder.getTokenType() == CASE_KEYWORD) {
      parseCaseExpression(myBuilder);
    }
    if (myBuilder.getTokenType() == DEFAULT_KEYWORD) {
      PsiBuilder.Marker caseExpressionMarker = myBuilder.mark();
      myBuilder.advanceLexer();
      parseDOTDOTAndScript(myBuilder);
      caseExpressionMarker.done(CfmlElementTypes.CASEEXPRESSION);
    }
    while (myBuilder.getTokenType() == CASE_KEYWORD) {
      parseCaseExpression(myBuilder);
    }
    if (myBuilder.getTokenType() != R_CURLYBRACKET) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.close.curly.bracket.expected"));
    }
    else {
      myBuilder.advanceLexer();
    }
    switchMarker.done(CfmlElementTypes.SWITCHEXPRESSION);
  }

  private static boolean eatLeftBracket(PsiBuilder myBuilder) {
    if (myBuilder.getTokenType() != L_BRACKET) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.open.bracket.expected"));
      return false;
    }
    myBuilder.advanceLexer();
    return true;
  }

  private static void eatRightBracket(PsiBuilder myBuilder) {
    if (myBuilder.getTokenType() != R_BRACKET) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.close.bracket.expected"));
      return;
    }
    myBuilder.advanceLexer();
  }

  private static boolean parseType(PsiBuilder myBuilder) {
    if (TYPE_KEYWORDS.contains(myBuilder.getTokenType())) {
      final PsiBuilder.Marker typeMarker = myBuilder.mark();
      myBuilder.advanceLexer();
      typeMarker.done(CfmlElementTypes.TYPE);
    }
    else {
      if (STRING_ELEMENTS.contains(myBuilder.getTokenType())) {
        final PsiBuilder.Marker stringMarker = myBuilder.mark();
        myBuilder.advanceLexer();
        if (myBuilder.getTokenType() != STRING_TEXT) {
          stringMarker.rollbackTo();
          myBuilder.error(CfmlBundle.message("cfml.parsing.string.expected"));
          return false;
        }
        final PsiBuilder.Marker typeMarker = myBuilder.mark();
        myBuilder.advanceLexer();
        typeMarker.done(CfmlElementTypes.TYPE);
        if (!STRING_ELEMENTS.contains(myBuilder.getTokenType())) {
          stringMarker.rollbackTo();
          myBuilder.error(CfmlBundle.message("cfml.parsing.string.expected"));
          return false;
        }
        myBuilder.advanceLexer();
        stringMarker.done(CfmlElementTypes.STRING_LITERAL);
        return true;
      }
      if (myBuilder.getTokenType() != IDENTIFIER) {
        myBuilder.error(CfmlBundle.message("cfml.parsing.type.expected"));
        return false;
      }
      final PsiBuilder.Marker typeMarker = myBuilder.mark();
      myBuilder.advanceLexer();
      while (myBuilder.getTokenType() == POINT) {
        myBuilder.advanceLexer();
        if (myBuilder.getTokenType() != IDENTIFIER) {
          myBuilder.error(CfmlBundle.message("cfml.parsing.type.expected"));
          typeMarker.done(CfmlElementTypes.TYPE);
          return true;
        }
        myBuilder.advanceLexer();
      }
      typeMarker.done(CfmlElementTypes.TYPE);
    }
    return true;
  }

  private void parseCatchExpression(PsiBuilder myBuilder) {
    if (myBuilder.getTokenType() != CATCH_KEYWORD) {
      return;
    }
    PsiBuilder.Marker catchExpressionMarker = myBuilder.mark();
    myBuilder.advanceLexer();
    if (!eatLeftBracket(myBuilder)) {
      catchExpressionMarker.drop();
      return;
    }
    parseType(myBuilder);
    if (myBuilder.getTokenType() != IDENTIFIER) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.identifier.expected"));
      catchExpressionMarker.drop();
      return;
    }
    myBuilder.advanceLexer();
    eatRightBracket(myBuilder);
    catchExpressionMarker.done(CfmlElementTypes.CATCHEXPRESSION);
    parseScript(myBuilder, false);
  }

  private void parseTryCatchExpression(PsiBuilder myBuilder) {
    if (myBuilder.getTokenType() != TRY_KEYWORD) {
      return;
    }
    PsiBuilder.Marker tryCatchMarker = myBuilder.mark();
    myBuilder.advanceLexer();
    parseScript(myBuilder, false);
    while (myBuilder.getTokenType() == CATCH_KEYWORD) {
      parseCatchExpression(myBuilder);
    }
    if (myBuilder.getTokenType() == FINALLY_KEYWORD) {
      myBuilder.advanceLexer();
      parseScript(myBuilder, false);
    }

    tryCatchMarker.done(CfmlElementTypes.TRYCATCHEXPRESSION);
  }

  public boolean parseScript(PsiBuilder myBuilder, boolean betweenScriptTags) {
    return parseScript(myBuilder, betweenScriptTags, true, false);
  }

  public boolean parseScript(PsiBuilder myBuilder, boolean betweenScriptTags, boolean createBlockOfStatments, boolean waitForRightBracket) {
    PsiBuilder.Marker blockOfStatements = null;
    if (myBuilder.getTokenType() == L_CURLYBRACKET) {
      waitForRightBracket = true;
      blockOfStatements = myBuilder.mark();
      myBuilder.advanceLexer();
    }
    while (!isEndOfScript(myBuilder)) {
      int lexerPosition = myBuilder.getCurrentOffset();
      if (myBuilder.getTokenType() == INCLUDE_KEYWORD) {
        parseInclude(myBuilder);
      }
      else if (myBuilder.getTokenType() == IMPORT_KEYWORD) {
        parseImport(myBuilder);
      }
      else if (myBuilder.getTokenType() == COMPONENT_KEYWORD ||
               myBuilder.getTokenType() == INTERFACE_KEYWORD) {
        parseComponentOrInterface(myBuilder);
      }
      else if (ACCESS_KEYWORDS.contains(myBuilder.getTokenType()) ||
               myBuilder.getTokenType() == FUNCTION_KEYWORD
        ) {
        parseFunctionExpression(myBuilder, false);
      }
      else if (TYPE_KEYWORDS.contains(myBuilder.getTokenType())) {
        if (!tryParseStatement(myBuilder)) {
          parseFunctionExpression(myBuilder, false);
        }
      }
      else if (myBuilder.getTokenType() == VAR_KEYWORD) {
        parseStatement(myBuilder);
      }
      else if (myBuilder.getTokenType() == IF_KEYWORD) {
        parseIfExpression(myBuilder);
      }
      else if (myBuilder.getTokenType() == WHILE_KEYWORD) {
        parseWhileExpression(myBuilder);
      }
      else if (myBuilder.getTokenType() == DO_KEYWORD) {
        parseDoWhileExpression(myBuilder);
      }
      else if (myBuilder.getTokenType() == FOR_KEYWORD) {
        parseForExpression(myBuilder);
      }
      else if (myBuilder.getTokenType() == SWITCH_KEYWORD) {
        parseSwitchExpression(myBuilder);
      } else if (myBuilder.getTokenType() == RETHROW_KEYWORD) {
        myBuilder.advanceLexer();
        eatSemicolon(myBuilder);
      }
      else if (myBuilder.getTokenType() == RETURN_KEYWORD) {
        parseReturnStatement(myBuilder);
      }
      else if (myBuilder.getTokenType() == BREAK_KEYWORD ||
               myBuilder.getTokenType() == ABORT_KEYWORD) {
        myBuilder.advanceLexer();
        eatSemicolon(myBuilder);
      }
      else if (CfmlUtil.isActionName(myBuilder)) {
        parseAction(myBuilder);
      }
      else if (myBuilder.getTokenType() == L_CURLYBRACKET) {
        parseScript(myBuilder, false);
      }
      else if (myBuilder.getTokenType() == R_CURLYBRACKET) {
        if (waitForRightBracket) {
          myBuilder.advanceLexer();
          if (blockOfStatements != null) {
            if (createBlockOfStatments) {
              blockOfStatements.done(CfmlElementTypes.BLOCK_OF_STATEMENTS);
            }
            else {
              blockOfStatements.drop();
            }
          }
          return true;
        }
        else {
          myBuilder.error(CfmlBundle.message("cfml.parsing.unexpected.token"));
          myBuilder.advanceLexer();
          return false;
        }
      }
      else if (myBuilder.getTokenType() == TRY_KEYWORD) {
        parseTryCatchExpression(myBuilder);
      }
      else if (KEYWORDS.contains(myBuilder.getTokenType())) {
        if (myBuilder.getTokenType() == VAR_KEYWORD || myBuilder.getTokenType() == SCOPE_KEYWORD) {
          parseStatement(myBuilder);
        }
        else if (myBuilder.getTokenType() != CONTINUE_KEYWORD &&
                 myBuilder.getTokenType() != RETURN_KEYWORD &&
                 myBuilder.getTokenType() != BREAK_KEYWORD) {
          PsiBuilder.Marker errorMarker = myBuilder.mark();
          myBuilder.advanceLexer();
          errorMarker.error(CfmlBundle.message("cfml.parsing.unexpected.token"));
        }
        else {
          myBuilder.advanceLexer();
          eatSemicolon(myBuilder);
        }
      }
      else {
        PsiBuilder.Marker marker = myBuilder.mark();
        if (parseType(myBuilder) && myBuilder.getTokenType() == FUNCTION_KEYWORD) {
          marker.rollbackTo();
          parseFunctionExpression(myBuilder, false);
        }
        else {
          marker.rollbackTo();
          if (myBuilder.getTokenType() == IDENTIFIER) {
            PsiBuilder.Marker assignMarker = myBuilder.mark();
            myBuilder.advanceLexer();
            if (myBuilder.getTokenType() == CfmlTokenTypes.ASSIGN) {
              assignMarker.rollbackTo();
              new CfmlExpressionParser(myBuilder).parseExpression();
              eatSemicolon(myBuilder);
            }
            else {
              assignMarker.rollbackTo();
              parseStatement(myBuilder);
            }
          }
          else {
            parseStatement(myBuilder);
          }
        }
      }

      if (!betweenScriptTags && !waitForRightBracket) {
        break;
      }
      if (lexerPosition == myBuilder.getCurrentOffset()) {
        myBuilder.error(CfmlBundle.message("cfml.parsing.unexpected.token"));
        myBuilder.advanceLexer();
        if (blockOfStatements != null) {
          blockOfStatements.drop();
        }
        return false;
      }

      /*
      IN_KEYWORD,
      VAR_KEYWORD,
      */
    }
    if (blockOfStatements != null) {
      blockOfStatements.drop();
    }
    return true;
  }

  private boolean tryParseStatement(PsiBuilder myBuilder) {
    PsiBuilder.Marker referenceMarker = myBuilder.mark();
    myBuilder.advanceLexer();
    if (myBuilder.getTokenType() == CfmlTokenTypes.ASSIGN) {
      referenceMarker.rollbackTo();
      parseStatement(myBuilder);
      return true;
    }
    referenceMarker.rollbackTo();
    return false;
  }

  private static void parseReturnStatement(PsiBuilder myBuilder) {
    myBuilder.advanceLexer();

    if (!(new CfmlExpressionParser(myBuilder)).parseStructureDefinition()) {
      if (!(new CfmlExpressionParser(myBuilder)).parseArrayDefinition()) {
        (new CfmlExpressionParser(myBuilder)).parseExpression();
      }
    }
    eatSemicolon(myBuilder);
  }

  private static boolean isEndOfScript(PsiBuilder myBuilder) {
    return myBuilder.getTokenType() == null ||
           myBuilder.getTokenType() == CfmlTokenTypes.OPENER ||
           myBuilder.getTokenType() == CfmlTokenTypes.LSLASH_ANGLEBRACKET;
  }
}
