package com.intellij.dts.documentation

import com.intellij.lang.documentation.DocumentationMarkup.CONTENT_ELEMENT
import com.intellij.lang.documentation.DocumentationMarkup.DEFINITION_ELEMENT
import com.intellij.lang.documentation.DocumentationMarkup.PRE_ELEMENT
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.text.HtmlBuilder
import com.intellij.openapi.util.text.HtmlChunk

class DtsDocumentationHtmlBuilder {
  private val definitionBuilder = HtmlBuilder()
  private val contentBuilder = HtmlBuilder()

  fun definition(vararg chunks: HtmlChunk) {
    if (!definitionBuilder.isEmpty) definitionBuilder.append(HtmlChunk.br())
    appendToDefinition(*chunks)
  }

  fun appendToDefinition(vararg chunks: HtmlChunk) {
    chunks.forEach(definitionBuilder::append)
  }

  fun content(vararg chunks: HtmlChunk) {
    if (!contentBuilder.isEmpty) contentBuilder.append(HtmlChunk.br())
    chunks.forEach(contentBuilder::append)
  }

  fun build(): @NlsSafe String {
    val builder = HtmlBuilder()

    builder.append(definitionBuilder.wrapWith(PRE_ELEMENT).wrapWith(DEFINITION_ELEMENT))
    builder.append(contentBuilder.wrapWith(CONTENT_ELEMENT))

    return builder.wrapWithHtmlBody().toString()
  }
}