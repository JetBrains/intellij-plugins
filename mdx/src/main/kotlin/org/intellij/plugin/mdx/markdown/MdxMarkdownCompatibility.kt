package org.intellij.plugin.mdx.markdown

import com.intellij.lang.Language
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import org.intellij.plugin.mdx.lang.MdxLanguage
import org.intellij.plugin.mdx.lang.parse.MdxElementTypes
import org.intellij.plugin.mdx.lang.psi.MdxFile
import org.intellij.plugins.markdown.lang.MarkdownCompatibilityChecker
import org.intellij.plugins.markdown.lang.MarkdownElementTypes
import org.intellij.plugins.markdown.ui.actions.MarkdownActionPromoterExtension

class MdxMarkdownCompatibilityChecker : MarkdownCompatibilityChecker {
  override fun isSupportedLanguage(language: Language): Boolean {
    return language == MdxLanguage
  }

  override fun isSupportedContext(language: Language, dataContext: DataContext?): Boolean {
    if (!isSupportedLanguage(language)) return false
    return MdxMarkdown.isMarkdownContext(dataContext)
  }

  override fun isSupportedElement(element: PsiElement): Boolean {
    return MdxMarkdown.isMarkdownElement(element)
  }

  override fun isSupportedRange(file: PsiFile, range: TextRange): Boolean {
    if (file !is MdxFile) return false
    return MdxMarkdown.isMarkdownRange(file, range.startOffset, range.endOffset)
  }
}

class MdxMarkdownActionPromoterExtension : MarkdownActionPromoterExtension {
  override fun shouldPromoteMarkdownActions(context: DataContext): Boolean {
    val editor = context.getData(CommonDataKeys.EDITOR) ?: return false
    val file = context.getData(CommonDataKeys.PSI_FILE) as? MdxFile
               ?: context.getData(CommonDataKeys.PROJECT)
                 ?.let { PsiDocumentManager.getInstance(it).getPsiFile(editor.document) as? MdxFile }
               ?: return false
    return MdxMarkdown.areCaretsInMarkdown(file, editor)
  }
}

object MdxMarkdown {
  private val specialRegionTypes = setOf(
    MdxElementTypes.MDX_BLOCK,
    MdxElementTypes.MDX_ESM_BLOCK,
    MdxElementTypes.MDX_JSX_OPENING_ELEMENT,
    MdxElementTypes.MDX_JSX_CLOSING_ELEMENT,
    MdxElementTypes.MDX_JSX_SELF_CLOSING_ELEMENT,
    MdxElementTypes.MDX_JSX_ATTRIBUTE,
    MdxElementTypes.MDX_EXPRESSION,
    MarkdownElementTypes.CODE_BLOCK,
    MarkdownElementTypes.CODE_FENCE,
    MarkdownElementTypes.CODE_SPAN,
    MarkdownElementTypes.FRONT_MATTER_HEADER,
  )

  fun isMarkdownContext(dataContext: DataContext?): Boolean {
    if (dataContext == null) return true
    val file = dataContext.getData(CommonDataKeys.PSI_FILE) ?: return true
    if (file !is MdxFile) return false
    val editor = dataContext.getData(CommonDataKeys.EDITOR) ?: return true
    return areCaretsInMarkdown(file, editor)
  }

  fun areCaretsInMarkdown(file: MdxFile, editor: Editor): Boolean {
    return ReadAction.computeBlocking<Boolean, Throwable> {
      editor.caretModel.allCarets.all { caret ->
        isMarkdownSelectionRange(file, caret.selectionStart, caret.selectionEnd)
      }
    }
  }

  fun isMarkdownElement(element: PsiElement): Boolean {
    if (element is MdxFile) return true
    val (file, range) = findHostMdxFileAndRange(element) ?: return false
    return hasMarkdownTextInRange(file, range)
  }

  fun isMarkdownRange(file: PsiFile, startOffset: Int, endOffset: Int): Boolean {
    if (file !is MdxFile || file.textLength == 0) return false
    if (startOffset !in 0..file.textLength || endOffset !in 0..file.textLength || startOffset > endOffset) return false
    if (startOffset == endOffset) {
      return isMarkdownOffset(file, startOffset)
    }

    val range = TextRange(startOffset, endOffset)
    if (isInsideSpecialRegion(file, startOffset) || isInsideSpecialRegion(file, endOffset)) return false
    return hasMarkdownTextInRange(file, range)
  }

  private fun isMarkdownSelectionRange(file: MdxFile, startOffset: Int, endOffset: Int): Boolean {
    return isMarkdownRange(file, startOffset, endOffset)
  }

  private fun isMarkdownOffset(file: MdxFile, offset: Int): Boolean {
    if (offset !in 0..file.textLength) return false
    return !isInsideSpecialRegion(file, offset)
  }

  private fun findHostMdxFileAndRange(element: PsiElement): Pair<MdxFile, TextRange>? {
    val range = element.textRange ?: return null
    val containingFile = element.containingFile
    if (containingFile is MdxFile) {
      return containingFile to range
    }
    val mdxFile = containingFile?.viewProvider?.getPsi(MdxLanguage) as? MdxFile
    if (mdxFile != null) {
      return mdxFile to range
    }

    val injectionManager = InjectedLanguageManager.getInstance(element.project)
    val topLevelFile = injectionManager.getTopLevelFile(element) as? MdxFile ?: return null
    return topLevelFile to injectionManager.injectedToHost(element, range)
  }

  private fun isInsideSpecialRegion(file: MdxFile, offset: Int): Boolean {
    return isInsideSpecialRegion(file as PsiElement, offset)
  }

  private fun isInsideSpecialRegion(element: PsiElement, offset: Int): Boolean {
    val range = element.textRange ?: return false
    if (offset <= range.startOffset || offset >= range.endOffset) return false
    if (element.elementType in specialRegionTypes) return true
    return element.childrenSequence().any { isInsideSpecialRegion(it, offset) }
  }

  private fun hasMarkdownTextInRange(element: PsiElement, range: TextRange): Boolean {
    val elementRange = element.textRange ?: return false
    if (!elementRange.intersects(range)) return false
    if (element.elementType in specialRegionTypes) return false
    if (element.firstChild == null) {
      return hasNonBlankTextInRange(element, range)
    }
    return element.childrenSequence().any { hasMarkdownTextInRange(it, range) }
  }

  private fun PsiElement.childrenSequence(): Sequence<PsiElement> {
    return generateSequence(firstChild) { it.nextSibling }
  }

  private fun hasNonBlankTextInRange(element: PsiElement, range: TextRange): Boolean {
    val elementRange = element.textRange ?: return false
    val startOffset = maxOf(range.startOffset, elementRange.startOffset)
    val endOffset = minOf(range.endOffset, elementRange.endOffset)
    if (startOffset >= endOffset) return false
    return element.text.substring(startOffset - elementRange.startOffset, endOffset - elementRange.startOffset).isNotBlank()
  }
}
