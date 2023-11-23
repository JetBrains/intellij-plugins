package com.intellij.dts.completion

import com.intellij.dts.lang.DtsFile
import com.intellij.dts.lang.DtsTokenSets
import com.intellij.dts.lang.psi.DtsContainer
import com.intellij.dts.lang.psi.DtsStatement
import com.intellij.dts.util.DtsTreeUtil
import com.intellij.openapi.util.Key
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.util.ProcessingContext

fun dtsBasePattern(): PsiElementPattern.Capture<PsiElement> {
  return psiElement()
    .inFile(psiFile(DtsFile::class.java))
    .withElementType(not(elementType().tokenSet(DtsTokenSets.comments)))
}

fun dtsInsideContainer(): PsiElementPattern.Capture<PsiElement> {
  return psiElement().with(DtsInsideContainer)
}

private object DtsInsideContainer : PatternCondition<PsiElement>("inside container") {
  val key = Key<DtsContainer>("container")

  override fun accepts(element: PsiElement, context: ProcessingContext): Boolean {
    val container = when (val parent = element.parent) {
                      is DtsContainer -> parent
                      is DtsStatement -> DtsTreeUtil.parentNode(parent)?.dtsContent
                      is PsiErrorElement -> parent.parent as? DtsContainer
                      else -> null
                    } ?: return false

    context.put(key, container)
    return true
  }
}

fun ProcessingContext.getDtsContainer(): DtsContainer {
  return get(DtsInsideContainer.key)
}
