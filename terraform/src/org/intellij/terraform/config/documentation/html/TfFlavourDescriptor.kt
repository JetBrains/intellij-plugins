// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.documentation.html

import com.intellij.openapi.util.text.StringUtilRt
import com.intellij.util.containers.tail
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.LeafASTNode
import org.intellij.markdown.ast.accept
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.ast.impl.ListCompositeNode
import org.intellij.markdown.ast.impl.ListItemCompositeNode
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.GeneratingProvider
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.html.OpenCloseGeneratingProvider
import org.intellij.markdown.html.SimpleTagProvider
import org.intellij.markdown.html.entities.EntityConverter
import org.intellij.markdown.lexer.Compat
import org.intellij.markdown.parser.LinkMap
import org.intellij.terraform.config.documentation.ROOT_DOC_ANCHOR
import java.net.URI

class TfFlavourDescriptor(val root: IElementType) : GFMFlavourDescriptor(true, false) {

  override fun createHtmlGeneratingProviders(linkMap: LinkMap, baseURI: URI?): Map<IElementType, GeneratingProvider> {
    val parentProviders = super.createHtmlGeneratingProviders(linkMap, baseURI).toMutableMap()
    parentProviders[root] = TfDocTagProvider()
    parentProviders[MarkdownElementTypes.LIST_ITEM] = ListItemGeneratingProvider()

    return parentProviders
  }

  open class TfDocTagProvider : OpenCloseGeneratingProvider() {
    override fun openTag(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
      visitor.consumeTagOpen(node, "body")
      visitor.consumeTagOpen(node, "article", """id = "${ROOT_DOC_ANCHOR}"""")
    }

    override fun closeTag(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
      visitor.consumeTagClose("article")
      visitor.consumeTagClose("body")
    }

  }

  internal class ListItemGeneratingProvider : SimpleTagProvider("li") {
    override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
      Compat.assert(node is ListItemCompositeNode)
      openTag(visitor, text, node)
      val listNode = node.parent
      Compat.assert(listNode is ListCompositeNode)
      for (child in node.children) {
        if (child.type == MarkdownElementTypes.PARAGRAPH) {
          ListElementAnchorProvider.processNode(visitor, text, child)
        }
        else {
          child.accept(visitor)
        }
      }
      closeTag(visitor, text, node)
    }

    object ListElementAnchorProvider : SimpleTagProvider("p") {
      override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
        val nodes = node.children
        if (nodes.isEmpty()) return
        val codeSpan = nodes[0].takeIf { it.type == MarkdownElementTypes.CODE_SPAN }
        if (codeSpan != null) {
          val textInNode = EntityConverter.replaceEntities(StringUtilRt.unquoteString(codeSpan.getTextInNode(text).toString(), '`'),
                                                           true,
                                                           true)
          visitor.consumeTagOpen(codeSpan, "span", """id="${textInNode}"""", """name="${textInNode}"""")
          visitor.consumeTagOpen(codeSpan, "code")
          visitor.consumeHtml(textInNode)
          visitor.consumeTagClose("code")
          visitor.consumeTagClose("span")
          nodes.tail().forEach { child ->
            when (child) {
              is LeafASTNode -> {
                visitor.visitLeaf(child)
              }
              else -> {
                child.accept(visitor)
              }
            }
          }
        }
        else {
          node.accept(visitor)
        }
      }
    }
  }
}