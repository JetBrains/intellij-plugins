// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.plugin.mdx.lang

import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.xml.XmlTag
import org.intellij.plugin.mdx.js.MdxJSLanguage
import org.intellij.plugin.mdx.lang.parse.MdxElementTypes

internal enum class MdxCommentContext(val linePrefix: String?, val blockPrefix: String, val blockSuffix: String) {
  MDX(null, "{/*", "*/}"),
  JAVASCRIPT("//", "/*", "*/"),
}

/** Selects one comment context for a range in the committed MDX document. */
internal fun mdxCommentContext(file: PsiFile, range: TextRange, tagContext: MdxCommentContext): MdxCommentContext {
  val mdx = file.viewProvider.getPsi(MdxLanguage) ?: return MdxCommentContext.MDX
  val element = commonElement(mdx, range) ?: return MdxCommentContext.MDX
  val construct = generateSequence(element) { it.parent }.firstOrNull {
    it.elementType == MdxElementTypes.MDX_ESM_BLOCK || it.elementType == MdxElementTypes.MDX_EXPRESSION
  } ?: return if (generateSequence(element) { it.parent }.any { it.elementType in TAG_ELEMENTS }) tagContext else MdxCommentContext.MDX

  val constructRange = construct.textRange
  if (range.startOffset <= constructRange.startOffset) return MdxCommentContext.MDX
  if (construct.elementType == MdxElementTypes.MDX_EXPRESSION &&
      mdx.viewProvider.contents[constructRange.endOffset - 1] == '}' && range.endOffset >= constructRange.endOffset) {
    return MdxCommentContext.MDX
  }

  val javascript = file.viewProvider.getPsi(MdxJSLanguage.INSTANCE) ?: return MdxCommentContext.MDX
  val jsElement = commonElement(javascript, range) ?: return MdxCommentContext.MDX
  for (parent in generateSequence(jsElement) { it.parent }) {
    if (parent is JSEmbeddedContent && insideBraces(parent, range)) return MdxCommentContext.JAVASCRIPT
    if (parent is XmlTag) {
      val body = jsxBodyRange(parent)
      if (body.contains(range)) return MdxCommentContext.MDX
      if (range.endOffset <= body.startOffset || range.startOffset >= body.endOffset) return tagContext
    }
  }
  return MdxCommentContext.JAVASCRIPT
}

private fun jsxBodyRange(tag: XmlTag): TextRange {
  val first = tag.firstChild
  if (first?.elementType != JSTokenTypes.XML_START_TAG_LIST) return tag.value.textRange
  val last = tag.lastChild
  val end = if (last.elementType == JSTokenTypes.XML_END_TAG_LIST) last.textRange.startOffset else tag.textRange.endOffset
  return TextRange(first.textRange.endOffset, end)
}

private val TAG_ELEMENTS = setOf(
  MdxElementTypes.MDX_JSX_OPENING_ELEMENT,
  MdxElementTypes.MDX_JSX_CLOSING_ELEMENT,
  MdxElementTypes.MDX_JSX_SELF_CLOSING_ELEMENT,
  MdxElementTypes.MDX_JSX_ATTRIBUTE,
)

private fun insideBraces(element: PsiElement, range: TextRange): Boolean {
  val first = element.firstChild ?: return false
  val last = element.lastChild ?: return false
  return first.text == "{" && range.startOffset >= first.textRange.endOffset &&
         (last.text != "}" || range.endOffset <= last.textRange.startOffset)
}

/** Finds the common element in the specified PSI root. */
internal fun commonElement(file: PsiFile, range: TextRange): PsiElement? {
  if (file.textLength == 0) return null
  val start = file.node.findLeafElementAt(range.startOffset.coerceAtMost(file.textLength - 1))?.psi ?: return null
  if (range.isEmpty) return start
  val end = file.node.findLeafElementAt(range.endOffset - 1)?.psi ?: return null
  return PsiTreeUtil.findCommonParent(start, end)
}
