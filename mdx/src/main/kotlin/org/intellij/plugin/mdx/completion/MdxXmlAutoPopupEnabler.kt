package org.intellij.plugin.mdx.completion

import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parents
import com.intellij.xml.psi.codeInsight.XmlAutoPopupEnabler
import org.intellij.plugin.mdx.lang.parse.MdxJsxScanner
import org.intellij.plugin.mdx.lang.psi.MdxFile
import org.intellij.plugins.markdown.lang.MarkdownElementTypes
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes

/**
 * Surfaces JSX tag-name auto-completion when the user types `<` (or extends a `<My` prefix) in an MDX
 * file. [com.intellij.codeInsight.editorActions.XmlAutoPopupHandler] otherwise vetoes the popup because
 * a freshly-typed, still-unbalanced `<`/`<My` isn't yet projected into the MdxJS layer; this enabler
 * bypasses that veto for a genuine JSX-tag-start context outside fenced/inline/indented code and front
 * matter (where `<` is plain text).
 */
internal class MdxXmlAutoPopupEnabler : XmlAutoPopupEnabler {
  override fun shouldShowPopup(file: PsiFile, offset: Int): Boolean {
    if (file !is MdxFile) return false
    if (!MdxJsxScanner.isJsxTagStartContext(file.text, offset)) return false
    return !isInNonJsxContext(file, offset)
  }

  /**
   * `<` is plain text (not a JSX tag opener) inside fenced code, inline code spans, indented code and
   * front matter. The base Markdown PSI already classifies these, so consult it rather than re-deriving
   * the block structure here.
   */
  private fun isInNonJsxContext(file: PsiFile, offset: Int): Boolean {
    val element = file.findElementAt((offset - 1).coerceAtLeast(0)) ?: return false
    return element.parents(withSelf = true).any { it.elementType in NON_JSX_ELEMENT_TYPES }
  }
}

private val NON_JSX_ELEMENT_TYPES = setOf(
  MarkdownElementTypes.CODE_FENCE,
  MarkdownTokenTypes.CODE_FENCE_CONTENT,
  MarkdownTokenTypes.CODE_FENCE_START,
  MarkdownTokenTypes.CODE_FENCE_END,
  MarkdownElementTypes.CODE_SPAN,
  MarkdownElementTypes.CODE_BLOCK,
  MarkdownTokenTypes.CODE_LINE,
  MarkdownElementTypes.FRONT_MATTER_HEADER,
)
