package org.angular2.lang.html

import com.intellij.javascript.web.html.WebFrameworkHtmlDialect
import com.intellij.lang.Language

abstract class Angular2TemplateLanguageBase(baseLanguage: Language, id: String) : WebFrameworkHtmlDialect(baseLanguage, id), Angular2HtmlDialect {
  override val svgDialect: Boolean
    get() = false
}