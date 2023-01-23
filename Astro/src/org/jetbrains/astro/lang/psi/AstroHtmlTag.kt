// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSLiteralExpressionKind
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.impl.source.html.HtmlTagDelegate
import com.intellij.psi.impl.source.tree.TreeElement
import com.intellij.psi.impl.source.xml.XmlTagDelegate
import com.intellij.psi.impl.source.xml.XmlTagImpl
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import org.jetbrains.astro.lang.AstroLanguage

class AstroHtmlTag(type: IElementType) : XmlTagImpl(type), HtmlTag, JSLiteralExpression {

  override fun getLanguage(): Language {
    return AstroLanguage.INSTANCE
  }

  override fun isCaseSensitive(): Boolean {
    return true
  }

  override fun toString(): String {
    return "AstroTag:$name"
  }

  override fun createDelegate(): XmlTagDelegate {
    return AstroHtmlLiteralExpressionImplDelegate()
  }

  override fun replace(other: JSExpression): JSExpression =
    JSChangeUtil.replaceExpression(this, other)

  override fun getIndexingData(): JSElementIndexingData? = null

  override fun getValueAsPropertyName(): String? = null

  override fun getSignificantValue(): String? = null

  override fun getExpressionKind(computeExactNumericKind: Boolean): JSLiteralExpressionKind {
    return JSLiteralExpressionKind.XML
  }

  private inner class AstroHtmlLiteralExpressionImplDelegate : HtmlTagDelegate(this@AstroHtmlTag) {
    override fun createTagFromText(text: String): XmlTag {
      val file = PsiFileFactory.getInstance(project)
        .createFileFromText("foo.astro", language, text, false, true)
      val root = file.firstChild
      assert(root is AstroRootContent) { "Failed to parse as tag $text" }
      val tag = (root as AstroRootContent).childrenOfType<AstroHtmlTag>().firstOrNull()
      assert(tag != null) { "Failed to parse as tag $text" }
      return tag!!
    }

    override fun deleteChildInternalSuper(child: ASTNode) {
      this@AstroHtmlTag.deleteChildInternalSuper(child)
    }

    override fun addInternalSuper(first: TreeElement?, last: ASTNode?, anchor: ASTNode?, before: Boolean?): TreeElement? {
      return this@AstroHtmlTag.addInternalSuper(first, last, anchor, before)
    }

    override fun createAttribute(qname: String, value: String): XmlAttribute {
      return AstroHtmlAttributeImpl.createAttribute(this@AstroHtmlTag, qname, value, null)
    }
  }
}