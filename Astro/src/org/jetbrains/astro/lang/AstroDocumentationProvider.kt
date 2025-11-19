package org.jetbrains.astro.lang

import com.intellij.lang.typescript.documentation.TypeScriptDocumentationProvider
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.xml.util.documentation.HtmlDocumentationProvider

class AstroDocumentationProvider : TypeScriptDocumentationProvider() {
  private val htmlDocProvider = HtmlDocumentationProvider()

  override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
    val tsInfo = super.getQuickNavigateInfo(element, originalElement)
    if (tsInfo != null) return tsInfo

    return htmlDocProvider.getQuickNavigateInfo(element, originalElement)
  }

  override fun getUrlFor(element: PsiElement?, originalElement: PsiElement?): List<String>? {
    val tsUrls = super.getUrlFor(element, originalElement)
    if (tsUrls != null) return tsUrls

    return htmlDocProvider.getUrlFor(element, originalElement)
  }

  override fun getCustomDocumentationElement(editor: Editor, file: PsiFile, contextElement: PsiElement?, targetOffset: Int): PsiElement? {
    val tsElement = super.getCustomDocumentationElement(editor, file, contextElement, targetOffset)
    if (tsElement != null) return tsElement

    return htmlDocProvider.getCustomDocumentationElement(editor, file, contextElement, targetOffset)
  }

  override fun getDocumentationElementForLookupItem(psiManager: PsiManager?, `object`: Any?, element: PsiElement?): PsiElement? {
    val tsElement = super.getDocumentationElementForLookupItem(psiManager, `object`, element)
    if (tsElement != null) return tsElement

    return htmlDocProvider.getDocumentationElementForLookupItem(psiManager, `object`, element)
  }
}
