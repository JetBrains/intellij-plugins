// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.svg

import org.angular2.lang.Angular2Bundle
import org.angular2.lang.html.Angular20HtmlLanguage
import org.angular2.lang.html.Angular2TemplateFileElementTypeBase
import org.angular2.lang.html.Angular2TemplateFileTypeBase
import org.angular2.lang.html.Angular2TemplateLanguageBase
import org.angular2.lang.html.Angular2TemplateSyntax
import org.angular2.lang.html.parser.Angular2TemplateParserDefinitionBase
import org.jetbrains.annotations.Nls

object Angular20SvgLanguage : Angular2TemplateLanguageBase(Angular20HtmlLanguage, "Angular20Svg") {

  override fun getDisplayName(): @Nls String =
    Angular2Bundle.message("angular.svg.template.20")

  override val templateSyntax: Angular2TemplateSyntax
    get() = Angular2TemplateSyntax.V_20

  override val svgDialect: Boolean
    get() = true
}

object Angular20SvgFileElementType : Angular2TemplateFileElementTypeBase(Angular20SvgLanguage)

object Angular20SvgFileType : Angular2TemplateFileTypeBase(Angular20SvgLanguage) {

  override fun getDescription(): String =
    Angular2Bundle.message("filetype.angular20svg.description")

}

class Angular20SvgParserDefinition : Angular2TemplateParserDefinitionBase(Angular20SvgLanguage)