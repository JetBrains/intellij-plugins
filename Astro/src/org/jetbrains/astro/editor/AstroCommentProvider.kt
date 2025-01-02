// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.editor

import com.intellij.lang.Commenter
import com.intellij.lang.Language
import com.intellij.lang.LanguageCommenters
import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.editing.JavascriptCommenter
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.xml.XmlCommenter
import com.intellij.openapi.editor.Editor
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.css.impl.util.editor.CssCommenter
import com.intellij.psi.templateLanguages.MultipleLangCommentProvider
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtilCore
import org.intellij.plugins.postcss.PostCssCommentProvider
import org.intellij.plugins.postcss.PostCssLanguage
import org.jetbrains.astro.lang.AstroLanguage
import org.jetbrains.astro.lang.frontmatter.AstroFrontmatterLanguage
import org.jetbrains.astro.lang.psi.AstroContentRoot

private class AstroCommentProvider : MultipleLangCommentProvider {
  override fun getLineCommenter(file: PsiFile, editor: Editor, lineStartLanguage: Language, lineEndLanguage: Language): Commenter {
    val minimalElement = editor.caretModel.currentCaret
      .let { findMinimalElementContainingRange(file, it.selectionStart, it.selectionEnd) }
    return minimalElement
      ?.let { PsiUtilCore.findLanguageFromElement(it) }
      ?.let {
        if (it == PostCssLanguage.INSTANCE)
          return PostCssCommentProvider().getLineCommenter(file, editor, lineStartLanguage, lineEndLanguage)!!

        val element = file.findElementAt(editor.caretModel.offset)
        when {
          element?.language?.isKindOf(CSSLanguage.INSTANCE) == true ->
            CssCommenter()
          lineStartLanguage == AstroFrontmatterLanguage.INSTANCE ||
          lineStartLanguage.baseLanguage == JavascriptLanguage ||
          element?.language == JavascriptLanguage ||
          element?.parent is JSEmbeddedContent &&
          element.parent !is AstroContentRoot ||
          // Used for block comments because their language suddenly changes to Astro
          element?.parent?.language == AstroFrontmatterLanguage.INSTANCE ->
            JavascriptCommenter()
          else ->
            XmlCommenter()
        }
      } ?: lineStartLanguage.let { LanguageCommenters.INSTANCE.forLanguage(it) }
  }

  override fun canProcess(file: PsiFile, viewProvider: FileViewProvider): Boolean {
    return file.language === AstroLanguage.INSTANCE
  }

  private fun findMinimalElementContainingRange(file: PsiFile, startOffset: Int, endOffset: Int): PsiElement? {
    val element1 = file.viewProvider.findElementAt(startOffset, AstroLanguage.INSTANCE)
    val element2 = file.viewProvider.findElementAt(endOffset - 1, AstroLanguage.INSTANCE)
    if (element2 == null || element1 == null) return null
    return PsiTreeUtil.findCommonParent(element1, element2)
  }
}