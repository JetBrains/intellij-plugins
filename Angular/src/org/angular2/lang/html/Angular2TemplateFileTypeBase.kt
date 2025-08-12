package org.angular2.lang.html

import com.intellij.javascript.web.html.WebFrameworkHtmlFileType

abstract class Angular2TemplateFileTypeBase(language: Angular2TemplateLanguageBase)
  : WebFrameworkHtmlFileType(language, "Angular${language.templateSyntax.version}${if (language.svgDialect) "Svg" else "Html"}",
                             if (language.svgDialect) "svg" else "html")