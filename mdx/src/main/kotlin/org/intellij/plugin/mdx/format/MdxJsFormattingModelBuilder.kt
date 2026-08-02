package org.intellij.plugin.mdx.format

import com.intellij.formatting.FormattingMode
import com.intellij.lang.Language
import com.intellij.lang.javascript.formatter.JSBlockContext
import com.intellij.lang.javascript.formatter.JavascriptFormattingModelBuilder
import com.intellij.psi.codeStyle.CodeStyleSettings

internal class MdxJsFormattingModelBuilder : JavascriptFormattingModelBuilder() {
  override fun createBlockFactory(settings: CodeStyleSettings, dialect: Language, mode: FormattingMode): JSBlockContext {
    return MdxJsBlockContext(settings, dialect, null, mode)
  }
}
