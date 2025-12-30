// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html.psi.formatter

import com.intellij.lang.html.HTMLLanguage
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings.IndentOptions
import com.intellij.psi.codeStyle.PsiBasedFileIndentOptionsProvider
import com.intellij.psi.formatter.xml.HtmlCodeStyleSettings
import org.angular2.lang.html.Angular2HtmlLanguage

class Angular2HtmlFileIndentOptionsProvider : PsiBasedFileIndentOptionsProvider() {

  override fun getIndentOptionsByPsiFile(settings: CodeStyleSettings, file: PsiFile): IndentOptions? {
    if (file.language.isKindOf(Angular2HtmlLanguage)) {
      // Treat file as an HTML file and apply HTML indentation settings
      var htmlIndentOptions = settings.getLanguageIndentOptions(HTMLLanguage.INSTANCE)
      if (settings.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_UNIFORM_INDENT) {
        htmlIndentOptions = htmlIndentOptions.clone() as IndentOptions
        htmlIndentOptions.isOverrideLanguageOptions = true
      }
      return htmlIndentOptions
    }
    return null
  }
}