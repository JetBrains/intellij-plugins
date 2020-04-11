@file:Suppress("UNUSED_PARAMETER")

package name.kropp.intellij.makefile

import com.intellij.lang.*
import com.intellij.lang.parser.*
import com.intellij.psi.*
import com.intellij.psi.tree.*
import name.kropp.intellij.makefile.psi.MakefileTypes.*

object MakefileParserUtil : GeneratedParserUtilBase() {
  private val nonTargetTokens = setOf(EOL, COLON, TAB, SPLIT)
  private val nonPrereqTokens = setOf(EOL, TAB, COLON, OPEN_PAREN, CLOSE_PAREN, OPEN_CURLY, CLOSE_CURLY, ASSIGN, STRING, COMMA, PIPE, SEMICOLON, SPLIT)
  private val nonIdentifierTokens = setOf(EOL, TAB, SPLIT, COLON, OPEN_PAREN, CLOSE_PAREN, OPEN_CURLY, CLOSE_CURLY, ASSIGN, STRING, COMMA)

  // targets
  @JvmStatic
  fun parseNoWhitespaceOrColon(builder: PsiBuilder, level: Int): Boolean = consumeAllNonWsExceptTokens(builder, level, nonTargetTokens)

  @JvmStatic
  fun parseToDollarNoWhitespaceOrColon(builder: PsiBuilder, level: Int): Boolean = consumeAllNonWsExceptTokens(builder, level, nonTargetTokens, errorOnWs = true)

  @JvmStatic
  fun parseNoWhitespaceOrColonBehind(builder: PsiBuilder, level: Int): Boolean {
    if (isWhitespaceBehind(builder)) return true
    return consumeAllNonWsExceptTokens(builder, level, nonTargetTokens, allowEmpty = true)
  }

  @JvmStatic
  fun parseToDollarNoWhitespaceOrColonBehind(builder: PsiBuilder, level: Int): Boolean {
    if (isWhitespaceBehind(builder)) return false
    return consumeAllNonWsExceptTokens(builder, level, nonTargetTokens, allowEmpty = true, errorOnWs = true)
  }

  // prerequisites
  @JvmStatic
  fun parsePrereqNoWhitespace(builder: PsiBuilder, level: Int): Boolean = consumeAllNonWsExceptTokens(builder, level, nonPrereqTokens)

  @JvmStatic
  fun parsePrereqToDollarNoWhitespace(builder: PsiBuilder, level: Int): Boolean = consumeAllNonWsExceptTokens(builder, level, nonPrereqTokens, errorOnWs = true)

  @JvmStatic
  fun parsePrereqNoWhitespaceBehind(builder: PsiBuilder, level: Int): Boolean {
    if (isWhitespaceBehind(builder)) return true
    return consumeAllNonWsExceptTokens(builder, level, nonPrereqTokens, allowEmpty = true)
  }

  @JvmStatic
  fun parsePrereqToDollarNoWhitespaceBehind(builder: PsiBuilder, level: Int): Boolean {
    if (isWhitespaceBehind(builder)) return false
    return consumeAllNonWsExceptTokens(builder, level, nonPrereqTokens, allowEmpty = true, errorOnWs = true)
  }

  // identifiers
  @JvmStatic
  fun parseNoWhitespace(builder: PsiBuilder, level: Int): Boolean = consumeAllNonWsExceptTokens(builder, level, nonIdentifierTokens)

  @JvmStatic
  fun parseToDollarNoWhitespace(builder: PsiBuilder, level: Int): Boolean = consumeAllNonWsExceptTokens(builder, level, nonIdentifierTokens, errorOnWs = true)

  @JvmStatic
  fun parseNoWhitespaceBehind(builder: PsiBuilder, level: Int): Boolean {
    if (isWhitespaceBehind(builder)) return true
    return consumeAllNonWsExceptTokens(builder, level, nonIdentifierTokens, allowEmpty = true)
  }

  @JvmStatic
  fun parseToDollarNoWhitespaceBehind(builder: PsiBuilder, level: Int): Boolean {
    if (isWhitespaceBehind(builder)) return false
    return consumeAllNonWsExceptTokens(builder, level, nonIdentifierTokens, allowEmpty = true, errorOnWs = true)
  }

  private fun isWhitespaceBehind(builder: PsiBuilder): Boolean {
    return builder.rawLookup(0) == TokenType.WHITE_SPACE ||
           builder.rawLookup(-1) == TokenType.WHITE_SPACE
  }

  private fun consumeAllNonWsExceptTokens(builder: PsiBuilder, level: Int, tokens: Set<IElementType>, allowEmpty: Boolean = false, errorOnWs: Boolean = false): Boolean {
    // accept everything till the end of line
    var hasAny = allowEmpty
    do {
      if (builder.tokenType == DOLLAR) {
        val lookAhead = builder.lookAhead(1)
        if (lookAhead == OPEN_CURLY || lookAhead == OPEN_PAREN) {
          return hasAny
        }
      }
      if (builder.tokenType in tokens || builder.tokenType == null) return hasAny
      if (builder.rawLookup(1) == TokenType.WHITE_SPACE) {
        if (errorOnWs) return false
        builder.advanceLexer()
        return true
      }
      builder.advanceLexer()
      hasAny = true
    } while (true)
  }

  @JvmStatic
  fun parseLine(builder: PsiBuilder, level: Int): Boolean = parseLineTokens(builder, setOf(EOL, BACKTICK))

  @JvmStatic
  fun parseLineNotEndef(builder: PsiBuilder, level: Int): Boolean = parseLineTokens(builder, setOf(EOL, KEYWORD_ENDEF))

  private fun parseLineTokens(builder: PsiBuilder, tokens: Set<IElementType>): Boolean {
    // accept everything till the end of line
    var hasAny = false
    do {
      if (builder.tokenType == DOLLAR) {
        val lookAhead = builder.lookAhead(1)
        if (lookAhead == OPEN_CURLY || lookAhead == OPEN_PAREN) {
          return hasAny
        }
      }
      if (builder.tokenType in tokens || builder.tokenType == null) return hasAny
      builder.advanceLexer()
      hasAny = true
    } while (true)
  }

  @JvmStatic
  fun parseVariableUsageCurly(builder: PsiBuilder, level: Int): Boolean = parseVariableUsage(builder, level, true, CLOSE_CURLY)
  @JvmStatic
  fun parseVariableUsageParen(builder: PsiBuilder, level: Int): Boolean = parseVariableUsage(builder, level, false, CLOSE_PAREN)

  @JvmStatic
  fun parseVariableUsage(builder: PsiBuilder, level: Int, acceptFunctionNames: Boolean, end: IElementType): Boolean {
    var curly = 0
    var paren = 0
    if (builder.tokenType == FUNCTION_NAME) {
      if (!acceptFunctionNames) {
        return false
      }
      builder.advanceLexer()
    }
    do {
      when (builder.tokenType) {
        OPEN_PAREN -> paren++
        CLOSE_PAREN -> if (paren > 0) paren-- else return consumeToken(builder, end)
        OPEN_CURLY -> curly++
        CLOSE_CURLY -> if (curly > 0) curly-- else return consumeToken(builder, end)
        EOL -> return false
        null -> return false
      }
      builder.advanceLexer()
    } while (true)
  }
}