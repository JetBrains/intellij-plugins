// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.svg

import org.angular2.lang.Angular2Bundle
import org.angular2.lang.html.Angular2HtmlDialect
import org.angular2.lang.html.Angular2HtmlLanguage
import org.angular2.lang.html.Angular2TemplateFileElementTypeBase
import org.angular2.lang.html.Angular2TemplateFileTypeBase
import org.angular2.lang.html.Angular2TemplateLanguageBase
import org.angular2.lang.html.Angular2TemplateSyntax
import org.angular2.lang.html.parser.Angular2TemplateParserDefinitionBase
import org.jetbrains.annotations.Nls

object Angular2SvgLanguage : Angular2TemplateLanguageBase(Angular2HtmlLanguage, "Angular2Svg"), Angular2HtmlDialect {

  override fun getDisplayName(): @Nls String =
    Angular2Bundle.message("angular.svg.template")

  override val templateSyntax: Angular2TemplateSyntax
    get() = Angular2TemplateSyntax.V_2

  override val svgDialect: Boolean
    get() = true
}

object Angular2SvgFileElementType : Angular2TemplateFileElementTypeBase(Angular2SvgLanguage)

object Angular2SvgFileType : Angular2TemplateFileTypeBase(Angular2SvgLanguage) {

  override fun getDescription(): String =
    Angular2Bundle.message("filetype.angular2svg.description")

}

class Angular2SvgParserDefinition : Angular2TemplateParserDefinitionBase(Angular2SvgLanguage)