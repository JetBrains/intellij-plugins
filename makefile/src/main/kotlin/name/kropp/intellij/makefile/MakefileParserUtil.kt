package name.kropp.intellij.makefile

import com.intellij.lang.*
import com.intellij.lang.parser.*
import com.intellij.psi.tree.*
import name.kropp.intellij.makefile.psi.MakefileTypes.*

object MakefileParserUtil : GeneratedParserUtilBase() {
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