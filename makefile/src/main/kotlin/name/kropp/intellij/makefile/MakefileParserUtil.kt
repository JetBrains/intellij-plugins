package name.kropp.intellij.makefile

import com.intellij.lang.*
import com.intellij.lang.parser.*
import com.intellij.psi.*
import com.intellij.psi.tree.*
import name.kropp.intellij.makefile.psi.MakefileTypes.*

object MakefileParserUtil : GeneratedParserUtilBase() {
  @JvmStatic
  fun parseNoWhitespaceOrColon(builder: PsiBuilder, level: Int): Boolean {
    return consumeAllNonWsExceptTokens(builder, level, setOf(EOL, COLON, DOUBLECOLON))
  }

  @JvmStatic
  fun parseNoWhitespace(builder: PsiBuilder, level: Int): Boolean {
    return consumeAllNonWsExceptTokens(builder, level, setOf(EOL, TAB, COLON, DOLLAR, OPEN_PAREN, CLOSE_PAREN, OPEN_CURLY, CLOSE_CURLY, ASSIGN, STRING, COMMA))
  }

  private fun consumeAllNonWsExceptTokens(builder: PsiBuilder, level: Int, tokens: Set<IElementType>): Boolean {
    // accept everything till the end of line
    var hasAny = false
    do {
/*
      if (builder.tokenType == DOLLAR) {
        if (builder.lookAhead(1) == OPEN_CURLY) {
          builder.advanceLexer()
          builder.advanceLexer()
          if (parseVariableUsage(builder, level + 1, true, CLOSE_CURLY)) {
            hasAny = true
          }
        } else if (builder.lookAhead(1) == OPEN_PAREN) {
          builder.advanceLexer()
          builder.advanceLexer()
          if (parseVariableUsage(builder, level + 1, true, CLOSE_PAREN)) {
            hasAny = true
          }
        }
      }
*/
      if (builder.tokenType in tokens || builder.tokenType == null) return hasAny
      if (builder.rawLookup(1) == TokenType.WHITE_SPACE) {
        builder.advanceLexer()
        return true
      }
      builder.advanceLexer()
      hasAny = true
    } while (true)
  }

  @JvmStatic
  fun parseLine(builder: PsiBuilder, level: Int): Boolean {
    // accept everything till the end of line
    do {
      if (builder.tokenType == EOL || builder.tokenType == null) return true
      builder.advanceLexer()
    } while (true)
  }

  @JvmStatic
  fun parseLineNotEndef(builder: PsiBuilder, level: Int): Boolean {
    // accept everything till the end of line
    do {
      if (builder.tokenType == EOL || builder.tokenType == KEYWORD_ENDEF || builder.tokenType == null) return true
      builder.advanceLexer()
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
      val current = builder.tokenType
      when (current) {
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

  private fun IElementType?.isComment() = this == COMMENT || this == DOC_COMMENT
}