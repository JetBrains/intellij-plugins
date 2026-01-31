// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.svg

import org.angular2.lang.Angular2Bundle
import org.angular2.lang.html.Angular17HtmlLanguage
import org.angular2.lang.html.Angular2TemplateFileElementTypeBase
import org.angular2.lang.html.Angular2TemplateFileTypeBase
import org.angular2.lang.html.Angular2TemplateLanguageBase
import org.angular2.lang.html.Angular2TemplateSyntax
import org.angular2.lang.html.parser.Angular2TemplateParserDefinitionBase
import org.jetbrains.annotations.Nls

object Angular17SvgLanguage : Angular2TemplateLanguageBase(Angular17HtmlLanguage, "Angular17Svg") {

  override fun getDisplayName(): @Nls String =
    Angular2Bundle.message("angular.svg.template.17")

  override val templateSyntax: Angular2TemplateSyntax
    get() = Angular2TemplateSyntax.V_17

  override val svgDialect: Boolean
    get() = true
}

object Angular17SvgFileElementType : Angular2TemplateFileElementTypeBase(Angular17SvgLanguage)

object Angular17SvgFileType : Angular2TemplateFileTypeBase(Angular17SvgLanguage) {

  override fun getDescription(): String =
    Angular2Bundle.message("filetype.angular17svg.description")

}

class Angular17SvgParserDefinition : Angular2TemplateParserDefinitionBase(Angular17SvgLanguage)