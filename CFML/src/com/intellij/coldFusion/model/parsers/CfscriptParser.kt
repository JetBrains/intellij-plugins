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
package com.intellij.coldFusion.model.parsers

import com.intellij.coldFusion.CfmlBundle
import com.intellij.coldFusion.model.CfmlUtil
import com.intellij.coldFusion.model.lexer.CfmlTokenTypes
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes.*
import com.intellij.coldFusion.model.psi.stubs.CfmlStubElementTypes
import com.intellij.lang.PsiBuilder

/**
 * Created by Lera Nikolaenko
 */
class CfscriptParser {
  // parse statement or statements block in curly brackets
  // if no curly brackets than parse only one statement

  private fun parseExpression(myBuilder: PsiBuilder) {
    CfmlExpressionParser(myBuilder).parseExpression()
  }

  private fun parseRValue(myBuilder: PsiBuilder) {
    CfmlExpressionParser(myBuilder).parseRValue()
  }

  private fun parseOptionalIDAndTags_PropertyUtil(myBuilder: PsiBuilder): Boolean {
    val propertyBodyMarker = myBuilder.mark()

    // parse Type ID attributes
    // parse ID attributes
    // parse attributes

    if (myBuilder.tokenType === IDENTIFIER) {
      myBuilder.advanceLexer()
      if (myBuilder.tokenType === CfmlTokenTypes.ASSIGN) {
        propertyBodyMarker.rollbackTo()
        CfmlParser.parseAttributes(myBuilder, "cfproperty", IDENTIFIER, true)
        return true
      }
      else if (myBuilder.tokenType === IDENTIFIER) {
        val attributesMarker = myBuilder.mark()
        myBuilder.advanceLexer()
        if (myBuilder.tokenType === CfmlTokenTypes.ASSIGN) {
          attributesMarker.rollbackTo()
          propertyBodyMarker.drop()
          CfmlParser.parseAttributes(myBuilder, "cfproperty", IDENTIFIER, true)
          return true
        }
        else {
          attributesMarker.drop()
          propertyBodyMarker.rollbackTo()
          return false
        }
      }
      else if (myBuilder.tokenType === POINT) {
        if (!parseType(myBuilder) || myBuilder.tokenType !== IDENTIFIER) {
          propertyBodyMarker.rollbackTo()
          return false
        }
      }
      else if (myBuilder.tokenType === SEMICOLON) {
        propertyBodyMarker.drop()
        return true
      } //try to parse type in "property type name;" expression//
    }
    propertyBodyMarker.drop()
    return false
  }

  private fun parseProperty(myBuilder: PsiBuilder): Boolean {
    assert(myBuilder.tokenType === IDENTIFIER)
    val propertyMarker = myBuilder.mark()
    myBuilder.advanceLexer()

    // parse Type ID attributes
    // parse ID attributes
    // parse attributes
    if (!parseOptionalIDAndTags_PropertyUtil(myBuilder)) {
      if (!parseType(myBuilder)) {
        propertyMarker.drop()
        return false
      }
      if (!parseOptionalIDAndTags_PropertyUtil(myBuilder)) {
        propertyMarker.drop()
        return false
      }
      else {
        propertyMarker.done(CfmlElementTypes.PROPERTY)
        return true
      }
    }
    else {
      propertyMarker.done(CfmlElementTypes.PROPERTY)
      return true
    }
  }

  private fun parseAction(myBuilder: PsiBuilder) {
    // http://help.adobe.com/en_US/ColdFusion/9.0/Developing/WSc3ff6d0ea77859461172e0811cbec0999c-7ffa.html
    assert(myBuilder.tokenType === IDENTIFIER)
    val actionName = myBuilder.tokenText
    val actionMarker = myBuilder.mark()
    myBuilder.remapCurrentToken(ACTION_NAME)
    myBuilder.advanceLexer()
    if (myBuilder.tokenType === CfmlTokenTypes.ASSIGN) {
      actionMarker.rollbackTo()
      myBuilder.remapCurrentToken(IDENTIFIER)
      CfmlExpressionParser(myBuilder).parseStatement()
      eatSemicolon(myBuilder)
    }
    else {
      CfmlParser.parseAttributes(myBuilder, "cfproperty", IDENTIFIER, true) //it's not really property, but have same syntax rules
      if ("param".equals(actionName!!, ignoreCase = true)) {
        // See link above.
        eatSemicolon(myBuilder)
      }
      else {
        parseFunctionBody(myBuilder)
      }
      actionMarker.done(CfmlElementTypes.ACTION)
    }
  }

  private fun parseStatement(myBuilder: PsiBuilder) {
    // PsiBuilder.Marker statement = myBuilder.mark();

    if (myBuilder.tokenType === IDENTIFIER &&
        "property".equals(myBuilder.tokenText!!, ignoreCase = true) &&
        parseProperty(myBuilder)) {
    }
    else {
      CfmlExpressionParser(myBuilder).parseStatement()
    }

    if (myBuilder.tokenType === L_CURLYBRACKET) {
      parseScript(myBuilder, false)
    }
    else {
      eatSemicolon(myBuilder)
    }
    // statement.done(CfscriptElementTypes.STATEMENT);
  }

  private fun parseCondition(myBuilder: PsiBuilder) {
    CfmlExpressionParser(myBuilder).parseExpression()
  }

  private fun parseInclude(myBuilder: PsiBuilder) {
    if (myBuilder.tokenType !== INCLUDE_KEYWORD) {
      return
    }

    val marker = myBuilder.mark()
    myBuilder.advanceLexer()
    val tokenType = myBuilder.tokenType
    if (tokenType !== CfmlTokenTypes.SINGLE_QUOTE && tokenType !== CfmlTokenTypes.DOUBLE_QUOTE) {
      marker.drop()
      return
    }
    CfmlExpressionParser(myBuilder).parseString()
    marker.done(CfmlElementTypes.INCLUDEEXPRESSION)
  }

  private fun parseImport(myBuilder: PsiBuilder) {
    if (myBuilder.tokenType !== IMPORT_KEYWORD) {
      return
    }
    val marker = myBuilder.mark()
    myBuilder.advanceLexer()
    val tokenType = myBuilder.tokenType
    if (tokenType === CfmlTokenTypes.SINGLE_QUOTE || tokenType === CfmlTokenTypes.DOUBLE_QUOTE) {
      CfmlExpressionParser(myBuilder).parseString()
    }
    else if (tokenType === IDENTIFIER) {
      CfmlExpressionParser(myBuilder).parseComponentReference()
    }
    marker.done(CfmlElementTypes.IMPORTEXPRESSION)
  }

  // eating numeric or string
  private fun parseConstant(myBuilder: PsiBuilder): Boolean {
    var tokenType = myBuilder.tokenType
    if (tokenType === INTEGER ||
        tokenType === DOUBLE || tokenType === BOOLEAN) {
      myBuilder.advanceLexer()
      return true
    }
    else if (tokenType === CfmlTokenTypes.SINGLE_QUOTE || tokenType === CfmlTokenTypes.DOUBLE_QUOTE) {
      myBuilder.advanceLexer()
      tokenType = myBuilder.tokenType
      if (tokenType !== CfmlTokenTypes.SINGLE_QUOTE_CLOSER && tokenType !== CfmlTokenTypes.DOUBLE_QUOTE_CLOSER && !myBuilder.eof()) {
        myBuilder.advanceLexer()
      }
      tokenType = myBuilder.tokenType
      if (tokenType !== CfmlTokenTypes.SINGLE_QUOTE_CLOSER && tokenType !== CfmlTokenTypes.DOUBLE_QUOTE_CLOSER) {
        myBuilder.error(CfmlBundle.message("cfml.parsing.quote.expected"))
        return true
      }
      myBuilder.advanceLexer()
      return true
    }
    return false
  }

  private fun parseConditionInBrackets(myBuilder: PsiBuilder): Boolean {
    if (myBuilder.tokenType !== L_BRACKET) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.close.bracket.expected"))
      return false
    }
    myBuilder.advanceLexer()
    parseCondition(myBuilder)
    if (myBuilder.tokenType !== R_BRACKET) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.close.bracket.expected"))
      return true
    }
    myBuilder.advanceLexer()
    return true
  }

  private fun parseComponentOrInterface(myBuilder: PsiBuilder) {
    if (myBuilder.tokenType !== COMPONENT_KEYWORD && myBuilder.tokenType !== INTERFACE_KEYWORD) {
      return
    }
    val componentMarker = myBuilder.mark()
    val isComponent = myBuilder.tokenType === COMPONENT_KEYWORD
    myBuilder.advanceLexer()
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
    if (myBuilder.tokenType === FUNCTION_KEYWORD) {
      componentMarker.rollbackTo()
      parseFunctionExpression(myBuilder, false)
      return
    }
    CfmlParser.parseAttributes(myBuilder, if (isComponent) "cfcomponent" else "cfinterface", IDENTIFIER, true)
    if (myBuilder.tokenType !== L_CURLYBRACKET) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.open.curly.bracket.expected"))
      componentMarker.drop()
      return
    }
    myBuilder.advanceLexer()
    if (myBuilder.tokenType === PAGEENCODING_KEYWORD) {
      myBuilder.advanceLexer()
      val tokenType = myBuilder.tokenType
      if (tokenType === CfmlTokenTypes.SINGLE_QUOTE || tokenType === CfmlTokenTypes.DOUBLE_QUOTE) {
        CfmlExpressionParser(myBuilder).parseString()
        eatSemicolon(myBuilder)
      }
      else {
        myBuilder.error(CfmlBundle.message("cfml.parsing.string.expected"))
      }
    }
    parseScript(myBuilder, false, false, true)
    componentMarker.done(CfmlStubElementTypes.COMPONENT_DEFINITION)
  }

  private fun parseIfExpression(myBuilder: PsiBuilder) {
    if (myBuilder.tokenType !== IF_KEYWORD) {
      return
    }
    val ifExprMarker = myBuilder.mark()
    myBuilder.advanceLexer()
    if (!parseConditionInBrackets(myBuilder)) {
      ifExprMarker.drop()
      return
    }

    parseScript(myBuilder, false)
    if (myBuilder.tokenType === ELSE_KEYWORD) {
      myBuilder.advanceLexer()
      if (myBuilder.tokenType === IF_KEYWORD) {
        parseIfExpression(myBuilder)
      }
      else {
        parseScript(myBuilder, false)
      }
    }
    ifExprMarker.done(CfmlElementTypes.IFEXPRESSION)
  }

  private fun parseWhileExpression(myBuilder: PsiBuilder) {
    if (myBuilder.tokenType !== WHILE_KEYWORD) {
      return
    }
    val whileMarker = myBuilder.mark()
    myBuilder.advanceLexer()
    parseConditionInBrackets(myBuilder)
    parseScript(myBuilder, false)
    whileMarker.done(CfmlElementTypes.WHILEEXPRESSION)
  }

  private fun parseDoWhileExpression(myBuilder: PsiBuilder) {
    if (myBuilder.tokenType !== DO_KEYWORD) {
      return
    }
    val doWhileMarker = myBuilder.mark()
    myBuilder.advanceLexer()
    if (myBuilder.tokenType !== L_CURLYBRACKET) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.open.bracket.expected"))
      doWhileMarker.drop()
      return
    }
    parseScript(myBuilder, false)
    if (myBuilder.tokenType !== WHILE_KEYWORD) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.keyword.expected", "while"))
      doWhileMarker.done(CfmlElementTypes.DOWHILEEXPRESSION)
      return
    }
    myBuilder.advanceLexer()
    parseConditionInBrackets(myBuilder)
    if (myBuilder.tokenType !== SEMICOLON) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.semicolon.expected"))
    }
    else {
      myBuilder.advanceLexer()
    }
    doWhileMarker.done(CfmlElementTypes.DOWHILEEXPRESSION)
  }

  private fun eatSemicolon(myBuilder: PsiBuilder) {
    if (myBuilder.tokenType !== SEMICOLON) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.semicolon.expected"))
    }
    else {
      myBuilder.advanceLexer()
    }
  }

  private fun parseForExpression(myBuilder: PsiBuilder) {
    if (myBuilder.tokenType !== FOR_KEYWORD) {
      return
    }
    val forExpressionMarker = myBuilder.mark()
    myBuilder.advanceLexer()

    // eat opening bracket
    if (myBuilder.tokenType !== L_BRACKET) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.close.bracket.expected"))
      forExpressionMarker.drop()
      return
    }
    myBuilder.advanceLexer()

    if (!tryParseForIn(myBuilder)) {
      parseStatement(myBuilder)

      parseCondition(myBuilder)
      eatSemicolon(myBuilder)

      CfmlExpressionParser(myBuilder).parseStatement()
    }
    if (myBuilder.tokenType !== R_BRACKET) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.close.bracket.expected"))
      forExpressionMarker.done(CfmlElementTypes.FOREXPRESSION)
      return
    }
    myBuilder.advanceLexer()

    parseScript(myBuilder, false)

    forExpressionMarker.done(CfmlElementTypes.FOREXPRESSION)
  }

  private fun tryParseForIn(myBuilder: PsiBuilder): Boolean {
    val startMarker = myBuilder.mark()

    if (myBuilder.tokenType === VAR_KEYWORD) {
      myBuilder.advanceLexer()
    }
    val definitionMarker = myBuilder.mark()
    if (!CfmlExpressionParser(myBuilder).parseReference(false)) {
      startMarker.rollbackTo()
      return false
    }
    else {
      definitionMarker.done(CfmlElementTypes.FORVARIABLE)
    }
    if (myBuilder.tokenType !== IN_L) {
      startMarker.rollbackTo()
      return false
    }
    myBuilder.advanceLexer()

    if (!CfmlExpressionParser(myBuilder).parseReference(false)) {
      CfmlExpressionParser(myBuilder).parseArrayDefinition()
    }
    startMarker.drop()
    return true
  }

  private fun parseFunctionBody(myBuilder: PsiBuilder) {
    val functionBodyMarker = myBuilder.mark()
    if (myBuilder.tokenType !== L_CURLYBRACKET) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.open.curly.bracket.expected"))
      functionBodyMarker.drop()
      return
    }
    parseScript(myBuilder, false)
    // TODO eat return keyword
    functionBodyMarker.done(CfmlElementTypes.FUNCTIONBODY)
  }

  /**
   * ColdFusion9: [required] [type] name [= default value (constant)], ...
   */
  private fun parseParametersList(myBuilder: PsiBuilder) {
    val argumentMarker = myBuilder.mark()
    // parsing required keyword is present
    if (myBuilder.tokenType === REQUIRED_KEYWORD) {
      myBuilder.advanceLexer()
    }
    // try to parse type
    val marker = myBuilder.mark()
    if (!parseType(myBuilder) || myBuilder.tokenType !== IDENTIFIER) {
      marker.rollbackTo()
    }
    else {
      marker.drop()
    }
    // parse parameter name
    if (myBuilder.tokenType === IDENTIFIER) {
      myBuilder.advanceLexer()

      if (myBuilder.tokenType === CfmlTokenTypes.ASSIGN) { // parse default value
        // TODO: to find out whether it can be something different from a constant
        myBuilder.advanceLexer()
        val defaultValueMarker = myBuilder.mark()
        if (!parseConstant(myBuilder)) {
          parseRValue(myBuilder)
        }
        defaultValueMarker.done(CfmlElementTypes.VALUE)
      }

      if (myBuilder.tokenType === IDENTIFIER) {
        CfmlParser.parseAttributes(myBuilder, "cfparameter", IDENTIFIER, true)
      }

      argumentMarker.done(CfmlElementTypes.FUNCTION_ARGUMENT)
      if (myBuilder.tokenType === COMMA) {
        myBuilder.advanceLexer()
        parseParametersList(myBuilder)
      }
    }
    else {
      argumentMarker.drop()
    }
  }

  private fun parseParametersListInBrackets(myBuilder: PsiBuilder) {
    val argumentsList = myBuilder.mark()
    eatLeftBracket(myBuilder)
    parseParametersList(myBuilder)
    eatRightBracket(myBuilder)
    argumentsList.done(CfmlElementTypes.PARAMETERS_LIST)
  }

  internal fun parseFunctionExpression(myBuilder: PsiBuilder, anonymous: Boolean) {
    val functionMarker = myBuilder.mark()
    if (!anonymous) {
      if (ACCESS_KEYWORDS.contains(myBuilder.tokenType)) {
        myBuilder.advanceLexer()
      }
      if (myBuilder.tokenType !== FUNCTION_KEYWORD) {
        parseType(myBuilder)
      }
    }
    if (myBuilder.tokenType !== FUNCTION_KEYWORD) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.function.expected"))
      functionMarker.drop()
      return
    }
    myBuilder.advanceLexer()
    if (myBuilder.tokenType !== IDENTIFIER &&
        !(myBuilder.tokenType === DEFAULT_KEYWORD) && !anonymous) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.identifier.expected"))
    }
    else {
      if (!anonymous) myBuilder.advanceLexer()
    }
    parseParametersListInBrackets(myBuilder)
    CfmlParser.parseAttributes(myBuilder, "cffunction", IDENTIFIER, true)
    parseFunctionBody(myBuilder)
    functionMarker.done(CfmlElementTypes.FUNCTION_DEFINITION)
  }

  private fun parseDOTDOTAndScript(myBuilder: PsiBuilder) {
    if (myBuilder.tokenType !== DOTDOT) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.dot.dot.expected"))
    }
    else {
      myBuilder.advanceLexer()
    }
    if (myBuilder.tokenType !== L_CURLYBRACKET) {
      if (myBuilder.tokenType === CASE_KEYWORD) {
        return
      }
      var offset = myBuilder.currentOffset
      while (!myBuilder.eof() &&
             myBuilder.tokenType !== BREAK_KEYWORD &&
             myBuilder.tokenType !== DEFAULT_KEYWORD &&
             myBuilder.tokenType !== CASE_KEYWORD &&
             myBuilder.tokenType !== R_CURLYBRACKET && !isEndOfScript(myBuilder)) {
        if (!parseScript(myBuilder, false)) {
          break
        }
        if (offset == myBuilder.currentOffset) {
          break
        }
        offset = myBuilder.currentOffset
      }

      if (myBuilder.tokenType === BREAK_KEYWORD) {
        myBuilder.advanceLexer()
        eatSemicolon(myBuilder)
      }
    }
    else {
      parseScript(myBuilder, false)
    }
  }

  private fun parseCaseExpression(myBuilder: PsiBuilder) {
    if (myBuilder.tokenType !== CASE_KEYWORD) {
      return
    }
    val caseExpressionMarker = myBuilder.mark()
    myBuilder.advanceLexer()
    if (!parseConstant(myBuilder)) {
      if (myBuilder.tokenType === IDENTIFIER) {
        myBuilder.error(CfmlBundle.message("cfml.parsing.constant.expected"))
        myBuilder.advanceLexer()
      }
      else {
        myBuilder.error(CfmlBundle.message("cfml.parsing.constant.expected"))
      }
    }
    parseDOTDOTAndScript(myBuilder)
    caseExpressionMarker.done(CfmlElementTypes.CASEEXPRESSION)
  }


  private fun parseSwitchExpression(myBuilder: PsiBuilder) {
    val switchMarker = myBuilder.mark()
    if (myBuilder.tokenType !== SWITCH_KEYWORD) {
      return
    }
    myBuilder.advanceLexer()
    if (!this.eatLeftBracket(myBuilder)) {
      switchMarker.drop()
      return
    }
    this.parseExpression(myBuilder)
    this.eatRightBracket(myBuilder)
    if (myBuilder.tokenType !== L_CURLYBRACKET) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.open.curly.bracket.expected"))
      switchMarker.drop()
      return
    }
    myBuilder.advanceLexer()
    while (myBuilder.tokenType === CASE_KEYWORD) {
      parseCaseExpression(myBuilder)
    }
    if (myBuilder.tokenType === DEFAULT_KEYWORD) {
      val caseExpressionMarker = myBuilder.mark()
      myBuilder.advanceLexer()
      parseDOTDOTAndScript(myBuilder)
      caseExpressionMarker.done(CfmlElementTypes.CASEEXPRESSION)
    }
    while (myBuilder.tokenType === CASE_KEYWORD) {
      parseCaseExpression(myBuilder)
    }
    if (myBuilder.tokenType !== R_CURLYBRACKET) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.close.curly.bracket.expected"))
    }
    else {
      myBuilder.advanceLexer()
    }
    switchMarker.done(CfmlElementTypes.SWITCHEXPRESSION)
  }

  private fun eatLeftBracket(myBuilder: PsiBuilder): Boolean {
    if (myBuilder.tokenType !== L_BRACKET) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.open.bracket.expected"))
      return false
    }
    myBuilder.advanceLexer()
    return true
  }

  private fun eatRightBracket(myBuilder: PsiBuilder) {
    if (myBuilder.tokenType !== R_BRACKET) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.close.bracket.expected"))
      return
    }
    myBuilder.advanceLexer()
  }

  private fun parseType(myBuilder: PsiBuilder): Boolean {
    if (TYPE_KEYWORDS.contains(myBuilder.tokenType)) {
      val typeMarker = myBuilder.mark()
      myBuilder.advanceLexer()
      typeMarker.done(CfmlElementTypes.TYPE)
    }
    else {
      if (myBuilder.tokenType !== IDENTIFIER) {
        myBuilder.error(CfmlBundle.message("cfml.parsing.type.expected"))
        return false
      }
      val typeMarker = myBuilder.mark()
      myBuilder.advanceLexer()
      while (myBuilder.tokenType === POINT) {
        myBuilder.advanceLexer()
        if (myBuilder.tokenType !== IDENTIFIER) {
          myBuilder.error(CfmlBundle.message("cfml.parsing.type.expected"))
          typeMarker.done(CfmlElementTypes.TYPE)
          return true
        }
        myBuilder.advanceLexer()
      }
      typeMarker.done(CfmlElementTypes.TYPE)
    }
    return true
  }

  private fun parseCatchExpression(myBuilder: PsiBuilder) {
    if (myBuilder.tokenType !== CATCH_KEYWORD) {
      return
    }
    val catchExpressionMarker = myBuilder.mark()
    myBuilder.advanceLexer()
    if (!eatLeftBracket(myBuilder)) {
      catchExpressionMarker.drop()
      return
    }
    parseType(myBuilder)
    if (myBuilder.tokenType !== IDENTIFIER) {
      myBuilder.error(CfmlBundle.message("cfml.parsing.identifier.expected"))
      catchExpressionMarker.drop()
      return
    }
    myBuilder.advanceLexer()
    eatRightBracket(myBuilder)
    catchExpressionMarker.done(CfmlElementTypes.CATCHEXPRESSION)
    parseScript(myBuilder, false)
  }

  private fun parseTryCatchExpression(myBuilder: PsiBuilder) {
    if (myBuilder.tokenType !== TRY_KEYWORD) {
      return
    }
    val tryCatchMarker = myBuilder.mark()
    myBuilder.advanceLexer()
    parseScript(myBuilder, false)
    while (myBuilder.tokenType === CATCH_KEYWORD) {
      parseCatchExpression(myBuilder)
    }
    if (myBuilder.tokenType === FINALLY_KEYWORD) {
      myBuilder.advanceLexer()
      parseScript(myBuilder, false)
    }

    tryCatchMarker.done(CfmlElementTypes.TRYCATCHEXPRESSION)
  }

  @JvmOverloads
  fun parseScript(myBuilder: PsiBuilder,
                  betweenScriptTags: Boolean,
                  createBlockOfStatments: Boolean = true,
                  waitForRightBracket: Boolean = false): Boolean {
    var waitForRightBracket = waitForRightBracket
    var blockOfStatements: PsiBuilder.Marker? = null
    if (myBuilder.tokenType === L_CURLYBRACKET) {
      waitForRightBracket = true
      blockOfStatements = myBuilder.mark()
      myBuilder.advanceLexer()
    }
    while (!isEndOfScript(myBuilder)) {
      val lexerPosition = myBuilder.currentOffset
      if (myBuilder.tokenType === INCLUDE_KEYWORD) {
        parseInclude(myBuilder)
      }
      else if (myBuilder.tokenType === IMPORT_KEYWORD) {
        parseImport(myBuilder)
      }
      else if (myBuilder.tokenType === COMPONENT_KEYWORD || myBuilder.tokenType === INTERFACE_KEYWORD) {
        parseComponentOrInterface(myBuilder)
      }
      else if (ACCESS_KEYWORDS.contains(myBuilder.tokenType) || myBuilder.tokenType === FUNCTION_KEYWORD) {
        parseFunctionExpression(myBuilder, false)
      }
      else if (TYPE_KEYWORDS.contains(myBuilder.tokenType)) {
        if (!tryParseStatement(myBuilder)) {
          parseFunctionExpression(myBuilder, false)
        }
      }
      else if (myBuilder.tokenType === VAR_KEYWORD) {
        parseStatement(myBuilder)
      }
      else if (myBuilder.tokenType === IF_KEYWORD) {
        parseIfExpression(myBuilder)
      }
      else if (myBuilder.tokenType === WHILE_KEYWORD) {
        parseWhileExpression(myBuilder)
      }
      else if (myBuilder.tokenType === DO_KEYWORD) {
        parseDoWhileExpression(myBuilder)
      }
      else if (myBuilder.tokenType === FOR_KEYWORD) {
        parseForExpression(myBuilder)
      }
      else if (myBuilder.tokenType === SWITCH_KEYWORD) {
        parseSwitchExpression(myBuilder)
      }
      else if (myBuilder.tokenType === RETHROW_KEYWORD) {
        myBuilder.advanceLexer()
        eatSemicolon(myBuilder)
      }
      else if (myBuilder.tokenType === RETURN_KEYWORD) {
        parseReturnStatement(myBuilder)
      }
      else if (myBuilder.tokenType === BREAK_KEYWORD || myBuilder.tokenType === ABORT_KEYWORD) {
        myBuilder.advanceLexer()
        eatSemicolon(myBuilder)
      }
      else if (CfmlUtil.isActionName(myBuilder)) {
        parseAction(myBuilder)
      }
      else if (myBuilder.tokenType === L_CURLYBRACKET) {
        parseScript(myBuilder, false)
      }
      else if (myBuilder.tokenType === R_CURLYBRACKET) {
        if (waitForRightBracket) {
          myBuilder.advanceLexer()
          if (blockOfStatements != null) {
            if (createBlockOfStatments) {
              blockOfStatements.done(CfmlElementTypes.BLOCK_OF_STATEMENTS)
            }
            else {
              blockOfStatements.drop()
            }
          }
          return true
        }
        else {
          myBuilder.error(CfmlBundle.message("cfml.parsing.unexpected.token"))
          myBuilder.advanceLexer()
          return false
        }
      }
      else if (myBuilder.tokenType === TRY_KEYWORD) {
        parseTryCatchExpression(myBuilder)
      }
      else if (KEYWORDS.contains(myBuilder.tokenType)) {
        if (myBuilder.tokenType === VAR_KEYWORD || myBuilder.tokenType === SCOPE_KEYWORD) {
          parseStatement(myBuilder)
        }
        else if (myBuilder.tokenType !== CONTINUE_KEYWORD &&
                 myBuilder.tokenType !== RETURN_KEYWORD &&
                 myBuilder.tokenType !== BREAK_KEYWORD) {
          val errorMarker = myBuilder.mark()
          myBuilder.advanceLexer()
          errorMarker.error(CfmlBundle.message("cfml.parsing.unexpected.token"))
        }
        else {
          myBuilder.advanceLexer()
          eatSemicolon(myBuilder)
        }
      }
      else {
        val marker = myBuilder.mark()
        if (parseType(myBuilder) && myBuilder.tokenType === FUNCTION_KEYWORD) {
          marker.rollbackTo()
          parseFunctionExpression(myBuilder, false)
        }
        else {
          marker.rollbackTo()
          if (myBuilder.tokenType === IDENTIFIER) {
            val assignMarker = myBuilder.mark()
            myBuilder.advanceLexer()
            if (myBuilder.tokenType === CfmlTokenTypes.ASSIGN) {
              assignMarker.rollbackTo()
              CfmlExpressionParser(myBuilder).parseExpression()
              eatSemicolon(myBuilder)
            }
            else {
              assignMarker.rollbackTo()
              parseStatement(myBuilder)
            }
          }
          else {
            parseStatement(myBuilder)
          }
        }
      }

      if (!betweenScriptTags && !waitForRightBracket) {
        break
      }
      if (lexerPosition == myBuilder.currentOffset) {
        myBuilder.error(CfmlBundle.message("cfml.parsing.unexpected.token"))
        myBuilder.advanceLexer()
        blockOfStatements?.drop()
        return false
      }

      /*
      IN_KEYWORD,
      VAR_KEYWORD,
      */
    }
    blockOfStatements?.drop()
    return true
  }

  private fun tryParseStatement(myBuilder: PsiBuilder): Boolean {
    val referenceMarker = myBuilder.mark()
    myBuilder.advanceLexer()
    if (myBuilder.tokenType === CfmlTokenTypes.ASSIGN) {
      referenceMarker.rollbackTo()
      parseStatement(myBuilder)
      return true
    }
    referenceMarker.rollbackTo()
    return false
  }

  private fun parseReturnStatement(myBuilder: PsiBuilder) {
    myBuilder.advanceLexer()

    if (!CfmlExpressionParser(myBuilder).parseStructureDefinition()) {
      if (!CfmlExpressionParser(myBuilder).parseArrayDefinition()) {
        CfmlExpressionParser(myBuilder).parseExpression()
      }
    }
    eatSemicolon(myBuilder)
  }

  private fun isEndOfScript(myBuilder: PsiBuilder): Boolean {
    return myBuilder.tokenType == null ||
           myBuilder.tokenType === CfmlTokenTypes.OPENER ||
           myBuilder.tokenType === CfmlTokenTypes.LSLASH_ANGLEBRACKET
  }
}
