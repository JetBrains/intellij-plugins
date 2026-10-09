// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html

import org.angular2.lang.Angular2Bundle
import org.angular2.lang.html.highlighting.Angular2HtmlSyntaxHighlighterFactoryBase
import org.angular2.lang.html.index.Angular2HtmlIdIndexer
import org.angular2.lang.html.index.Angular2HtmlTodoIndexer
import org.angular2.lang.html.parser.Angular2TemplateParserDefinitionBase
import org.jetbrains.annotations.Nls

internal object Angular22HtmlLanguage : Angular2TemplateLanguageBase(Angular20HtmlLanguage, "Angular22Html") {
  override fun getDisplayName(): @Nls String =
    Angular2Bundle.message("angular.html.template.22")

  override val templateSyntax: Angular2TemplateSyntax
    get() = Angular2TemplateSyntax.V_22
}

object Angular22HtmlFileElementType : Angular2TemplateFileElementTypeBase(Angular22HtmlLanguage)

object Angular22HtmlFileType : Angular2TemplateFileTypeBase(Angular22HtmlLanguage) {

  override fun getDescription(): String =
    Angular2Bundle.message("filetype.angular22html.description")

}

open class Angular22HtmlParserDefinition : Angular2TemplateParserDefinitionBase(Angular22HtmlLanguage)

internal class Angular22HtmlSyntaxHighlighterFactory : Angular2HtmlSyntaxHighlighterFactoryBase(Angular22HtmlLanguage)

internal class Angular22HtmlIdIndexer : Angular2HtmlIdIndexer(Angular22HtmlLanguage)

internal class Angular22HtmlTodoIndexer : Angular2HtmlTodoIndexer(Angular22HtmlLanguage)