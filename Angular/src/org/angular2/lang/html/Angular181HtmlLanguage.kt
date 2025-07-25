// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html

import org.angular2.lang.Angular2Bundle
import org.angular2.lang.html.highlighting.Angular2HtmlSyntaxHighlighterFactoryBase
import org.angular2.lang.html.index.Angular2HtmlIdIndexer
import org.angular2.lang.html.index.Angular2HtmlTodoIndexer
import org.angular2.lang.html.parser.Angular2TemplateParserDefinitionBase
import org.jetbrains.annotations.Nls

object Angular181HtmlLanguage : Angular2TemplateLanguageBase(Angular17HtmlLanguage, "Angular181Html") {

  override fun getDisplayName(): @Nls String =
    Angular2Bundle.message("angular.html.template.181")

  override val templateSyntax: Angular2TemplateSyntax
    get() = Angular2TemplateSyntax.V_18_1
}

object Angular181HtmlFileElementType : Angular2TemplateFileElementTypeBase(Angular181HtmlLanguage)

object Angular181HtmlFileType : Angular2TemplateFileTypeBase(Angular181HtmlLanguage) {

  override fun getDescription(): String =
    Angular2Bundle.message("filetype.angular181html.description")

}

open class Angular181HtmlParserDefinition : Angular2TemplateParserDefinitionBase(Angular181HtmlLanguage)

class Angular181HtmlSyntaxHighlighterFactory : Angular2HtmlSyntaxHighlighterFactoryBase(Angular181HtmlLanguage)

internal class Angular181HtmlIdIndexer : Angular2HtmlIdIndexer(Angular181HtmlLanguage)

class Angular181HtmlTodoIndexer : Angular2HtmlTodoIndexer(Angular181HtmlLanguage)