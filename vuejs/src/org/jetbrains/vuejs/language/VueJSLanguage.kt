package org.jetbrains.vuejs.language

import com.intellij.lang.PsiBuilder
import com.intellij.lang.ecmascript6.parsing.ES6ExpressionParser
import com.intellij.lang.ecmascript6.parsing.ES6FunctionParser
import com.intellij.lang.ecmascript6.parsing.ES6Parser
import com.intellij.lang.ecmascript6.parsing.ES6StatementParser
import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.JSLanguageDialect
import com.intellij.lang.javascript.parsing.JSPsiTypeParser
import com.intellij.lang.javascript.parsing.JavaScriptParser

class VueJSLanguage : JSLanguageDialect("VueJS", DialectOptionHolder.ECMA_6) {
  override fun getFileExtension(): String {
    return "js"
  }

  override fun createParser(builder: PsiBuilder): JavaScriptParser<*, *, *, *> {
    return ES6Parser<ES6ExpressionParser<*>, ES6StatementParser<*>, ES6FunctionParser<*>, JSPsiTypeParser<*>>(builder)
  }


  companion object {
    val INSTANCE = VueJSLanguage()
  }
}
