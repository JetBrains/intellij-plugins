package org.jetbrains.vuejs.language

import com.intellij.lang.PsiBuilder
import com.intellij.lang.ecmascript6.parsing.ES6ExpressionParser
import com.intellij.lang.ecmascript6.parsing.ES6FunctionParser
import com.intellij.lang.ecmascript6.parsing.ES6Parser
import com.intellij.lang.ecmascript6.parsing.ES6StatementParser
import com.intellij.lang.javascript.*
import com.intellij.lang.javascript.parsing.JSPsiTypeParser
import com.intellij.lang.javascript.parsing.JavaScriptParser
import com.intellij.psi.tree.IElementType

class VueJSLanguage : JSLanguageDialect("VueJS", DialectOptionHolder.ECMA_6, JavaScriptSupportLoader.ECMA_SCRIPT_6) {
  override fun getFileExtension(): String {
    return "js"
  }

  override fun createParser(builder: PsiBuilder): JavaScriptParser<*, *, *, *> {
    return VueJSParser(builder)
  }

  class VueJSParser(builder: PsiBuilder) : ES6Parser<ES6ExpressionParser<*>, ES6StatementParser<*>, ES6FunctionParser<*>, JSPsiTypeParser<JavaScriptParser<*, *, *, *>>>(builder) {
    init {
      myStatementParser = object : ES6StatementParser<VueJSParser>(this) {
        override fun parseSourceElement() {
          if (builder.tokenType === JSTokenTypes.LBRACE) {
            parseExpectedExpression(builder)
            return
          }
          super.parseSourceElement()
        }
      }
    }

    fun parseVue(root: IElementType) {
      val rootMarker = builder.mark()
      while (!builder.eof()) {
        parseExpectedExpression(builder)
      }
      rootMarker.done(root)
    }

    private fun parseExpectedExpression(builder: PsiBuilder) {
      if (!myExpressionParser.parseExpressionOptional()) {
        builder.error(JSBundle.message("javascript.parser.message.expected.expression"))
        builder.advanceLexer()
      }
    }
  }

  companion object {
    val INSTANCE = VueJSLanguage()
  }
}
