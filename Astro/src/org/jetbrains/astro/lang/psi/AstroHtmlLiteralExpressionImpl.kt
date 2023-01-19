// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.frameworks.jsx.references.JSXNamespaceReferenceSet
import com.intellij.lang.javascript.psi.ecma6.impl.JSXXmlLiteralExpressionImpl
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiReference
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.impl.source.xml.TagNameReference
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.SmartList

class AstroHtmlLiteralExpressionImpl(type: IElementType) : JSXXmlLiteralExpressionImpl(type), HtmlTag {
  override fun isCaseSensitive(): Boolean {
    return true
  }

  override fun toString(): String {
    return "AstroTag:$name"
  }

  override fun createDelegate(): JSXmlLiteralExpressionImplDelegate {
    return AstroHtmlLiteralExpressionImplDelegate()
  }

  private inner class AstroHtmlLiteralExpressionImplDelegate : JSXmlLiteralExpressionImplDelegate() {
    override fun createTagFromText(text: String): XmlTag {
      val file = PsiFileFactory.getInstance(project)
        .createFileFromText("foo.astro", language, text, false, true)
      val root = file.firstChild
      assert(root is AstroRootContent) { "Failed to parse as tag $text" }
      val tag = (root as AstroRootContent).firstChild
      assert(tag is AstroHtmlLiteralExpressionImpl) { "Failed to parse as tag $text" }
      return tag as AstroHtmlLiteralExpressionImpl
    }

    override fun createAttribute(qname: String, value: String): XmlAttribute {
      return AstroHtmlAttributeImpl.createAttribute(this@AstroHtmlLiteralExpressionImpl, qname, value, null)
    }

    override fun getNamespacePrefix(name: String): String {
      val endOfPrefix = name.lastIndexOf(".")
      return if (endOfPrefix > 0) name.substring(0, endOfPrefix) else super.getNamespacePrefix(name)
    }

    override fun createPrefixReferences(startTagName: ASTNode,
                                        prefix: String,
                                        tagRef: TagNameReference?): Collection<PsiReference> {
      val offset: Int = startTagName.startOffset - startOffset
      return SmartList(*JSXNamespaceReferenceSet(this@AstroHtmlLiteralExpressionImpl, prefix, offset).references)
    }
  }
}