package com.intellij.mdx.backend.format

import com.intellij.formatting.FormattingDocumentModel
import com.intellij.formatting.WrapType
import com.intellij.lang.javascript.psi.JSFunctionExitPoint
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.xml.HtmlPolicy
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlText

/**
 * The JSX wrap/indent policy, shared by the foreign JS/JSX formatter ([MdxJsFormattingModelBuilder]) and the
 * host block ([MdxFormattingModelBuilder]'s `MdxBlock`, which needs the same "would this tag force its own
 * line" answer to place the host-side `MDX_JSX_CLOSING_ELEMENT` block correctly): a single source of truth
 * instead of two independently maintained copies of the same rules.
 */
internal class MdxJsxHtmlPolicy(settings: CodeStyleSettings, model: FormattingDocumentModel) : HtmlPolicy(settings, model) {
  override fun getWrappingTypeForTagBegin(tag: XmlTag): WrapType {
    val parent = tag.parent
    if (isRestrictedProduction(parent)) return WrapType.NONE
    if (parent !is XmlTag) return WrapType.NORMAL
    return if (newlineProhibitedBefore(tag)) WrapType.NONE else super.getWrappingTypeForTagBegin(tag)
  }

  override fun indentChildrenOf(parentTag: XmlTag): Boolean {
    return parentTag.name.isEmpty() || super.indentChildrenOf(parentTag)
  }

  override fun allowWrapBeforeText(): Boolean {
    return false
  }

  override fun insertLineBreakBeforeTag(xmlTag: XmlTag): Boolean {
    return false
  }

  override fun checkName(tag: XmlTag, option: String): Boolean {
    return checkName(tag, option, false)
  }

  override fun isInlineTag(tag: XmlTag): Boolean {
    return StringUtil.isCapitalized(tag.name) || super.isInlineTag(tag)
  }

  private fun newlineProhibitedBefore(tag: PsiElement): Boolean {
    val prevSibling = tag.prevSibling
    return (prevSibling is PsiWhiteSpace
            && prevSibling.getPrevSibling() is XmlText)
  }

  private fun isRestrictedProduction(parent: PsiElement?): Boolean {
    return parent is JSFunctionExitPoint
  }
}
