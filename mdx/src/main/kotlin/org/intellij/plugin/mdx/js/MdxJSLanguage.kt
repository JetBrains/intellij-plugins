package org.intellij.plugin.mdx.js

import com.intellij.lang.PsiBuilder
import com.intellij.lang.javascript.DialectOptionHolder.JS_WITH_JSX
import com.intellij.lang.javascript.JSLanguageDialect
import com.intellij.lang.javascript.JavaScriptSupportLoader.ECMA_SCRIPT_6
import com.intellij.lang.javascript.parsing.JavaScriptParser

class MdxJSLanguage : JSLanguageDialect("MdxJS", JS_WITH_JSX, ECMA_SCRIPT_6) {
  companion object {
    val INSTANCE = MdxJSLanguage()
  }

  override fun getFileExtension(): String = "mdx"
  override fun createParser(builder: PsiBuilder): JavaScriptParser<*, *, *, *> {
    return MdxJSLanguageParser(builder)
  }
}