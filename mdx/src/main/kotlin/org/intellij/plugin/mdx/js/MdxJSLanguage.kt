package org.intellij.plugin.mdx.js

import com.intellij.lang.PsiBuilder
import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.JSLanguageDialect
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lang.javascript.parsing.JavaScriptParser

class MdxJSLanguage : JSLanguageDialect("MdxJS", DialectOptionHolder.JSX, JavaScriptSupportLoader.JSX_HARMONY) {
    companion object {
        val INSTANCE = MdxJSLanguage()
    }

    override fun getFileExtension(): String = "mdx"
    override fun createParser(builder: PsiBuilder): JavaScriptParser<*, *, *, *> {
        return MdxJSLanguageParser(builder)
    }
}