// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.editor

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.generation.IndentedCommenter
import com.intellij.lang.Commenter
import com.intellij.lang.Language
import com.intellij.lang.LanguageCommenters
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.xml.XmlCommenter
import com.intellij.openapi.editor.Editor
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.templateLanguages.MultipleLangCommentProvider
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtilCore
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlElement
import com.intellij.psi.xml.XmlTag
import org.intellij.plugins.postcss.PostCssCommentProvider
import org.intellij.plugins.postcss.PostCssLanguage
import org.jetbrains.vuejs.lang.html.VueLanguage

private class VueCommenterProvider : MultipleLangCommentProvider {
  override fun getLineCommenter(file: PsiFile, editor: Editor, lineStartLanguage: Language, lineEndLanguage: Language): Commenter? {
    val minimalElement = editor.caretModel.currentCaret
      .let { findMinimalElementContainingRange(file, it.selectionStart, it.selectionEnd) }
    return minimalElement
             ?.let { PsiUtilCore.findLanguageFromElement(it) }
             ?.takeIf { it != VueLanguage }
             ?.let { elementLanguage ->
               var xmlParent: XmlElement? = null
               // If we have caret within attribute value expression or style attribute and we are commenting line, adjust language
               if (lineStartLanguage == VueLanguage
                   && PsiTreeUtil.getParentOfType(minimalElement, XmlAttributeValue::class.java, XmlTag::class.java)
                     ?.takeIf { it is XmlAttributeValue }
                     ?.also { xmlParent = it } != null
               ) {
                 xmlParent?.language.takeIf { it != VueLanguage }
               }
               else
                 elementLanguage
             }
             ?.let {
               if (it == PostCssLanguage.INSTANCE)
                 PostCssCommentProvider().getLineCommenter(file, editor, lineStartLanguage, lineEndLanguage)
               else
                 LanguageCommenters.INSTANCE.forLanguage(it)
             }
           ?: if (lineStartLanguage == VueLanguage) {
             CodeStyle.getLanguageSettings(file, HTMLLanguage.INSTANCE).let { styleSettings ->
               object : XmlCommenter(), IndentedCommenter {
                 override fun forceIndentedLineComment(): Boolean = !styleSettings.LINE_COMMENT_AT_FIRST_COLUMN
                 override fun forceIndentedBlockComment(): Boolean = !styleSettings.BLOCK_COMMENT_AT_FIRST_COLUMN
               }
             }
           }
           else lineStartLanguage.let { LanguageCommenters.INSTANCE.forLanguage(it) }
  }

  override fun canProcess(file: PsiFile, viewProvider: FileViewProvider): Boolean = file.language == VueLanguage

  private fun findMinimalElementContainingRange(file: PsiFile, startOffset: Int, endOffset: Int): PsiElement? {
    val element1 = file.viewProvider.findElementAt(startOffset, VueLanguage)
    val element2 = file.viewProvider.findElementAt(endOffset - 1, VueLanguage)
    if (element2 == null || element1 == null) return null
    return PsiTreeUtil.findCommonParent(element1, element2)
  }

}