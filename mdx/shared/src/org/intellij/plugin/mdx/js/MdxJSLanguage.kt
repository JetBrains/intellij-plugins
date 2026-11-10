package org.intellij.plugin.mdx.js

import com.intellij.lang.javascript.DialectOptionHolder.Companion.JS_WITH_JSX
import com.intellij.lang.javascript.JSLanguageDialect
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.openapi.fileTypes.LanguageFileType
import org.intellij.plugin.mdx.lang.MdxFileType

class MdxJSLanguage private constructor() : JSLanguageDialect("MdxJS", JS_WITH_JSX, JavascriptLanguage) {
  companion object {
    val INSTANCE = MdxJSLanguage()
  }

  override fun getAssociatedFileType(): LanguageFileType =
    MdxFileType
}
