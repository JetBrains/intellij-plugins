// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html

import org.angular2.lang.Angular2Bundle
import org.angular2.lang.html.highlighting.Angular2HtmlSyntaxHighlighterFactoryBase
import org.angular2.lang.html.index.Angular2HtmlIdIndexer
import org.angular2.lang.html.index.Angular2HtmlTodoIndexer
import org.angular2.lang.html.parser.Angular2TemplateParserDefinitionBase
import org.jetbrains.annotations.Nls

internal object Angular20HtmlLanguage : Angular2TemplateLanguageBase(Angular181HtmlLanguage, "Angular20Html") {
  override fun getDisplayName(): @Nls String =
    Angular2Bundle.message("angular.html.template.20")

  override val templateSyntax: Angular2TemplateSyntax
    get() = Angular2TemplateSyntax.V_20
}

object Angular20HtmlFileElementType : Angular2TemplateFileElementTypeBase(Angular20HtmlLanguage)

object Angular20HtmlFileType : Angular2TemplateFileTypeBase(Angular20HtmlLanguage) {

  override fun getDescription(): String =
    Angular2Bundle.message("filetype.angular20html.description")

}

open class Angular20HtmlParserDefinition : Angular2TemplateParserDefinitionBase(Angular20HtmlLanguage)

private class Angular20HtmlSyntaxHighlighterFactory : Angular2HtmlSyntaxHighlighterFactoryBase(Angular20HtmlLanguage)

private class Angular20HtmlIdIndexer : Angular2HtmlIdIndexer(Angular20HtmlLanguage)

private class Angular20HtmlTodoIndexer : Angular2HtmlTodoIndexer(Angular20HtmlLanguage)