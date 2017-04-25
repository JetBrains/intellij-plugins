package org.jetbrains.vuejs.language

import com.intellij.lang.PsiBuilder
import com.intellij.lang.ecmascript6.parsing.ES6ExpressionParser
import com.intellij.lang.ecmascript6.parsing.ES6FunctionParser
import com.intellij.lang.ecmascript6.parsing.ES6Parser
import com.intellij.lang.ecmascript6.parsing.ES6StatementParser
import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.JSLanguageDialect
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.parsing.JSPsiTypeParser
import com.intellij.lang.javascript.parsing.JavaScriptParser

class VueJSLanguage : JSLanguageDialect("VueJS", DialectOptionHolder.OTHER) {
  override fun getFileExtension(): String {
    return "js"
  }

  override fun createParser(builder: PsiBuilder): JavaScriptParser<*, *, *, *> {
    return VueJSParser(builder)
  }

  class VueJSParser(builder: PsiBuilder) : ES6Parser<ES6ExpressionParser<*>, ES6StatementParser<*>, ES6FunctionParser<*>, JSPsiTypeParser<JavaScriptParser<*, *, *, *>>>(builder) {
    init {
      myStatementParser = object: ES6StatementParser<VueJSParser>(this) {
        override fun parseSourceElement() {
          if (builder.tokenType === JSTokenTypes.LBRACE) {
            parseExpressionStatement()
            return
          }
          super.parseSourceElement()
        }
      }
    }
  }

  companion object {
    val INSTANCE = VueJSLanguage()
  }
}
