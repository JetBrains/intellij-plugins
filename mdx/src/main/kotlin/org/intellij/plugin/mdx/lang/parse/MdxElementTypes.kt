package org.intellij.plugin.mdx.lang.parse

import com.intellij.psi.PsiFile
import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.tree.IStubFileElementType
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementType
import org.intellij.plugin.mdx.lang.MdxLanguage

object MdxElementTypes {
  @JvmField
  val JSX_BLOCK: IElementType = MarkdownElementType("JSX_BLOCK")
  @JvmField
  val MDX_ESM_BLOCK: IElementType = MarkdownElementType("MDX_ESM_BLOCK")
  @JvmField
  val MDX_JSX_FLOW_ELEMENT: IElementType = MarkdownElementType("MDX_JSX_FLOW_ELEMENT")
  @JvmField
  val MDX_JSX_TEXT_ELEMENT: IElementType = MarkdownElementType("MDX_JSX_TEXT_ELEMENT")
  @JvmField
  val MDX_JSX_OPENING_ELEMENT: IElementType = MarkdownElementType("MDX_JSX_OPENING_ELEMENT")
  @JvmField
  val MDX_JSX_CLOSING_ELEMENT: IElementType = MarkdownElementType("MDX_JSX_CLOSING_ELEMENT")
  @JvmField
  val MDX_JSX_SELF_CLOSING_ELEMENT: IElementType = MarkdownElementType("MDX_JSX_SELF_CLOSING_ELEMENT")
  @JvmField
  val MDX_JSX_ATTRIBUTE: IElementType = MarkdownElementType("MDX_JSX_ATTRIBUTE")
  @JvmField
  val MDX_JSX_EXPRESSION: IElementType = MarkdownElementType("MDX_JSX_EXPRESSION")
  val MDX_FILE_NODE_TYPE = IStubFileElementType<PsiFileStub<PsiFile>>("MDX", MdxLanguage)
}
