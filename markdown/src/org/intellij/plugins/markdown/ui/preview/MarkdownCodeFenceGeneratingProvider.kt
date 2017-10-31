package org.intellij.plugins.markdown.ui.preview

import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.html.GeneratingProvider
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.plugins.markdown.extensions.MarkdownCodeFencePluginGeneratingProvider
import java.util.*

internal class MarkdownCodeFenceGeneratingProvider(private val pluginCacheProviders: Array<MarkdownCodeFencePluginGeneratingProvider>)
  : GeneratingProvider {

  private fun pluginGeneratedHtml(languageString: String, codeFenceContent: String): String {
    return pluginCacheProviders
      .filter { it.isApplicable(languageString) }.stream()
      .findFirst()
      .map { it.generateHtml(codeFenceContent) }
      .orElse(codeFenceContent)
  }

  override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
    val indentBefore = node.getTextInNode(text).commonPrefixWith(" ".repeat(10)).length

    visitor.consumeHtml("<pre>")

    var state = 0

    var childrenToConsider = node.children
    if (childrenToConsider.last().type == MarkdownTokenTypes.CODE_FENCE_END) {
      childrenToConsider = childrenToConsider.subList(0, childrenToConsider.size - 1)
    }

    var lastChildWasContent = false

    val attributes = ArrayList<String>()
    var languageString: String? = null
    val codeFenceContent = StringBuilder()
    for (child in childrenToConsider) {
      if (state == 1 && child.type in listOf(MarkdownTokenTypes.CODE_FENCE_CONTENT, MarkdownTokenTypes.EOL)) {
        codeFenceContent.append(HtmlGenerator.trimIndents(codeFenceRawText(text, child), indentBefore))
        lastChildWasContent = child.type == MarkdownTokenTypes.CODE_FENCE_CONTENT
      }
      if (state == 0 && child.type == MarkdownTokenTypes.FENCE_LANG) {
        languageString = HtmlGenerator.leafText(text, child).toString().trim().split(' ')[0]
        attributes.add("class=\"languageString-$languageString\"")
      }
      if (state == 0 && child.type == MarkdownTokenTypes.EOL) {
        visitor.consumeTagOpen(node, "code", *attributes.toTypedArray())
        state = 1
      }
    }

    if (state == 1 && languageString != null) {
      val codeFenceGeneratedHtml = pluginGeneratedHtml(languageString, codeFenceContent.toString())
      visitor.consumeHtml(codeFenceGeneratedHtml)
    }

    if (state == 0) {
      visitor.consumeTagOpen(node, "code", *attributes.toTypedArray())
    }
    if (lastChildWasContent) {
      visitor.consumeHtml("\n")
    }
    visitor.consumeHtml("</code></pre>")
  }

  private fun codeFenceRawText(text: String, node: ASTNode): CharSequence {
    return if (node.type != MarkdownTokenTypes.BLOCK_QUOTE) node.getTextInNode(text) else ""
  }
}