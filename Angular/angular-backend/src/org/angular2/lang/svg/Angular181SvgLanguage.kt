// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.svg

import org.angular2.lang.Angular2Bundle
import org.angular2.lang.html.Angular181HtmlLanguage
import org.angular2.lang.html.Angular2TemplateFileElementTypeBase
import org.angular2.lang.html.Angular2TemplateFileTypeBase
import org.angular2.lang.html.Angular2TemplateLanguageBase
import org.angular2.lang.html.Angular2TemplateSyntax
import org.angular2.lang.html.parser.Angular2TemplateParserDefinitionBase
import org.jetbrains.annotations.Nls

object Angular181SvgLanguage : Angular2TemplateLanguageBase(Angular181HtmlLanguage, "Angular181Svg") {

  override fun getDisplayName(): @Nls String =
    Angular2Bundle.message("angular.svg.template.181")

  override val templateSyntax: Angular2TemplateSyntax
    get() = Angular2TemplateSyntax.V_18_1

  override val svgDialect: Boolean
    get() = true
}

object Angular181SvgFileElementType : Angular2TemplateFileElementTypeBase(Angular181SvgLanguage)

object Angular181SvgFileType : Angular2TemplateFileTypeBase(Angular181SvgLanguage) {

  override fun getDescription(): String =
    Angular2Bundle.message("filetype.angular181svg.description")

}

class Angular181SvgParserDefinition : Angular2TemplateParserDefinitionBase(Angular181SvgLanguage)