// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.svg

import org.angular2.lang.Angular2Bundle
import org.angular2.lang.html.Angular22HtmlLanguage
import org.angular2.lang.html.Angular2TemplateFileElementTypeBase
import org.angular2.lang.html.Angular2TemplateFileTypeBase
import org.angular2.lang.html.Angular2TemplateLanguageBase
import org.angular2.lang.html.Angular2TemplateSyntax
import org.angular2.lang.html.parser.Angular2TemplateParserDefinitionBase
import org.jetbrains.annotations.Nls

object Angular22SvgLanguage : Angular2TemplateLanguageBase(Angular22HtmlLanguage, "Angular22Svg") {

  override fun getDisplayName(): @Nls String =
    Angular2Bundle.message("angular.svg.template.22")

  override val templateSyntax: Angular2TemplateSyntax
    get() = Angular2TemplateSyntax.V_22

  override val svgDialect: Boolean
    get() = true
}

object Angular22SvgFileElementType : Angular2TemplateFileElementTypeBase(Angular22SvgLanguage)

object Angular22SvgFileType : Angular2TemplateFileTypeBase(Angular22SvgLanguage) {

  override fun getDescription(): String =
    Angular2Bundle.message("filetype.angular22svg.description")

}

class Angular22SvgParserDefinition : Angular2TemplateParserDefinitionBase(Angular22SvgLanguage)