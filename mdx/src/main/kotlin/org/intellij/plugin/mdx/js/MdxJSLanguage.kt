package org.intellij.plugin.mdx.js

import com.intellij.lang.javascript.DialectOptionHolder.Companion.JS_WITH_JSX
import com.intellij.lang.javascript.JSLanguageDialect
import com.intellij.lang.javascript.JavaScriptSupportLoader.ECMA_SCRIPT_6
import com.intellij.openapi.fileTypes.LanguageFileType
import org.intellij.plugin.mdx.lang.MdxFileType

internal class MdxJSLanguage private constructor() : JSLanguageDialect("MdxJS", JS_WITH_JSX, ECMA_SCRIPT_6) {
  companion object {
    val INSTANCE = MdxJSLanguage()
  }

  override fun getAssociatedFileType(): LanguageFileType =
    MdxFileType.INSTANCE
}