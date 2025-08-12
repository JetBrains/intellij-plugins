// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html

import org.angular2.lang.Angular2Bundle
import org.angular2.lang.html.highlighting.Angular2HtmlSyntaxHighlighterFactoryBase
import org.angular2.lang.html.parser.Angular2TemplateParserDefinitionBase
import org.jetbrains.annotations.Nls

object Angular2HtmlLanguage : Angular2TemplateLanguageBase(INSTANCE, "Angular2Html"), Angular2HtmlDialect {

  override fun getDisplayName(): @Nls String =
    Angular2Bundle.message("angular.html.template")

  override val templateSyntax: Angular2TemplateSyntax
    get() = Angular2TemplateSyntax.V_2
}

object Angular2HtmlFileElementType : Angular2TemplateFileElementTypeBase(Angular2HtmlLanguage)

object Angular2HtmlFileType : Angular2TemplateFileTypeBase(Angular2HtmlLanguage) {

  override fun getDescription(): String =
    Angular2Bundle.message("filetype.angular2html.description")

}

open class Angular2HtmlParserDefinition : Angular2TemplateParserDefinitionBase(Angular2HtmlLanguage)

class Angular2HtmlSyntaxHighlighterFactory : Angular2HtmlSyntaxHighlighterFactoryBase(Angular2HtmlLanguage)