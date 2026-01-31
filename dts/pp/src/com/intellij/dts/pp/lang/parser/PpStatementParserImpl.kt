package com.intellij.dts.pp.lang.parser

import com.intellij.dts.pp.lang.PpTokenTypes
import com.intellij.lang.PsiBuilder
import com.intellij.lang.parser.GeneratedParserUtilBase._NONE_
import com.intellij.lang.parser.GeneratedParserUtilBase._NOT_
import com.intellij.lang.parser.GeneratedParserUtilBase.consumeToken
import com.intellij.lang.parser.GeneratedParserUtilBase.consumeTokenFast
import com.intellij.lang.parser.GeneratedParserUtilBase.current_position_
import com.intellij.lang.parser.GeneratedParserUtilBase.empty_element_parsed_guard_
import com.intellij.lang.parser.GeneratedParserUtilBase.enter_section_
import com.intellij.lang.parser.GeneratedParserUtilBase.exit_section_
import com.intellij.lang.parser.GeneratedParserUtilBase.recursion_guard_
import com.intellij.lang.parser.GeneratedParserUtilBase.report_error_
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

internal class PpStatementParserImpl(private val tokenTypes: PpTokenTypes) : PpStatementParser {
  private val statementTokenSet = TokenSet.create(tokenTypes.statement)

  override fun getStatementTokens(): TokenSet = statementTokenSet

  override fun parseStatement(tokenType: IElementType, builderFactory: () -> PsiBuilder): Boolean {
    return when (tokenType) {
      tokenTypes.directive -> statement(builderFactory(), 1)
      else -> false
    }
  }

  /* ********************************************************** */
  // statement STATEMENT_END
  fun statement(builder_: PsiBuilder, level_: Int): Boolean {
    if (!recursion_guard_(builder_, level_, "statement")) return false

    val marker_ = enter_section_(builder_, level_, _NONE_, tokenTypes.statement, "<statement>")
    var result_ = statementContent(builder_, level_ + 1)
    result_ = result_ && consumeToken(builder_, tokenTypes.statementEnd)
    PpParserUtilBase.exit_section_(builder_, level_, marker_, result_, false, null)
    return result_
  }

  /* ********************************************************** */
  // DIRECTIVE token* { pin = 0 }
  fun statementContent(builder_: PsiBuilder, level_: Int): Boolean {
    if (!recursion_guard_(builder_, level_, "statementContent")) return false

    val marker_ = enter_section_(builder_, level_, _NONE_)
    var result_ = report_error_(builder_, consumeToken(builder_, tokenTypes.directive))
    result_ = statementContent2(builder_, level_ + 1) && result_
    PpParserUtilBase.exit_section_(builder_, level_, marker_, result_, true, ::statementRecover)

    return true
  }

  // token*
  private fun statementContent2(builder_: PsiBuilder, level_: Int): Boolean {
    if (!recursion_guard_(builder_, level_, "statementContent2")) return false

    while (true) {
      val pos_ = current_position_(builder_)
      if (!token(builder_, level_ + 1)) break
      if (!empty_element_parsed_guard_(builder_, "statementContent2", pos_)) break
    }
    return true
  }

  /* ********************************************************** */
  // !(STATEMENT_END)
  private fun statementRecover(builder_: PsiBuilder, level_: Int): Boolean {
    if (!recursion_guard_(builder_, level_, "statementRecover")) return false

    val marker_ = enter_section_(builder_, level_, _NOT_)
    val result_ = !statementRecover0(builder_, level_ + 1)
    PpParserUtilBase.exit_section_(builder_, level_, marker_, result_, false, null)

    return result_
  }

  // (STATEMENT_END)
  private fun statementRecover0(builder_: PsiBuilder, level_: Int): Boolean {
    if (!recursion_guard_(builder_, level_, "statementRecover0")) return false

    val marker_ = enter_section_(builder_)
    val result_ = consumeTokenFast(builder_, tokenTypes.statementEnd)
    exit_section_(builder_, marker_, null, result_)

    return result_
  }

  /* ********************************************************** */
  // IDENTIFIER
  //   | OPERATOR_OR_PUNCTUATOR
  //   | BINARY_LITERAL
  //   | OCTAL_LITERAL
  //   | DECIMAL_LITERAL
  fun token(builder: PsiBuilder, level: Int): Boolean {
    if (!recursion_guard_(builder, level, "token")) return false

    var result = consumeToken(builder, tokenTypes.identifier)
    if (!result) result = consumeToken(builder, tokenTypes.operatorOrPunctuator)
    if (!result) result = consumeToken(builder, tokenTypes.lineBreak)
    if (!result) result = consumeToken(builder, tokenTypes.headerName)
    if (!result) result = consumeToken(builder, tokenTypes.integerLiteral)
    if (!result) result = consumeToken(builder, tokenTypes.charLiteral)
    if (!result) result = consumeToken(builder, tokenTypes.floatLiteral)
    if (!result) result = consumeToken(builder, tokenTypes.stringLiteral)
    return result
  }
}