// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html

import org.angular2.lang.Angular2Bundle
import org.angular2.lang.html.highlighting.Angular2HtmlSyntaxHighlighterFactoryBase
import org.angular2.lang.html.index.Angular2HtmlIdIndexer
import org.angular2.lang.html.index.Angular2HtmlTodoIndexer
import org.angular2.lang.html.parser.Angular2TemplateParserDefinitionBase
import org.jetbrains.annotations.Nls

object Angular17HtmlLanguage : Angular2TemplateLanguageBase(Angular2HtmlLanguage, "Angular17Html") {

  override fun getDisplayName(): @Nls String =
    Angular2Bundle.message("angular.html.template.17")

  override val templateSyntax: Angular2TemplateSyntax
    get() = Angular2TemplateSyntax.V_17
}

object Angular17HtmlFileElementType : Angular2TemplateFileElementTypeBase(Angular17HtmlLanguage)

object Angular17HtmlFileType : Angular2TemplateFileTypeBase(Angular17HtmlLanguage) {

  override fun getDescription(): String =
    Angular2Bundle.message("filetype.angular17html.description")

}

open class Angular17HtmlParserDefinition : Angular2TemplateParserDefinitionBase(Angular17HtmlLanguage)

class Angular17HtmlSyntaxHighlighterFactory : Angular2HtmlSyntaxHighlighterFactoryBase(Angular17HtmlLanguage)

class Angular17HtmlTodoIndexer : Angular2HtmlTodoIndexer(Angular17HtmlLanguage)

internal class Angular17HtmlIdIndexer : Angular2HtmlIdIndexer(Angular17HtmlLanguage)