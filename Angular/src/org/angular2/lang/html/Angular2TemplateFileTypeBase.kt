package org.angular2.lang.html

import com.intellij.javascript.web.html.WebFrameworkHtmlFileType
import org.angular2.lang.expr.Angular2Language

abstract class Angular2TemplateFileTypeBase(language: Angular2TemplateLanguageBase)
  : WebFrameworkHtmlFileType(language, "Angular${language.templateSyntax.version}${if (language.svgDialect) "Svg" else "Html"}",
                             if (language.svgDialect) "svg" else "html") {
  init {
    // Initialize Angular 2 language as well
    @Suppress("UnusedExpression")
    Angular2Language
  }
}