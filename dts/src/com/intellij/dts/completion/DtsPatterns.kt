package com.intellij.dts.completion

import com.intellij.dts.lang.DtsFile
import com.intellij.dts.lang.DtsTokenSets
import com.intellij.dts.lang.psi.DtsCellArray
import com.intellij.dts.lang.psi.DtsContainer
import com.intellij.dts.lang.psi.DtsProperty
import com.intellij.dts.lang.psi.DtsPropertyContent
import com.intellij.dts.lang.psi.DtsStatement
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.dts.lang.psi.DtsValue
import com.intellij.dts.util.DtsTreeUtil
import com.intellij.openapi.util.Key
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns.elementType
import com.intellij.patterns.PlatformPatterns.not
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.PlatformPatterns.psiFile
import com.intellij.patterns.PlatformPatterns.string
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.TokenType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import com.intellij.util.ProcessingContext
import com.intellij.util.asSafely

private val errorOrProperty = TokenSet.create(TokenType.ERROR_ELEMENT, DtsTypes.PROPERTY)
private val errorOrSubNode = TokenSet.create(TokenType.ERROR_ELEMENT, DtsTypes.SUB_NODE)
private val badCharacter = TokenSet.create(TokenType.BAD_CHARACTER)

fun dtsBasePattern(): PsiElementPattern.Capture<PsiElement> {
  return psiElement()
    .inFile(psiFile(DtsFile::class.java))
    .withElementType(not(elementType().tokenSet(DtsTokenSets.comments)))
    .withElementType(not(elementType().tokenSet(badCharacter)))
}

fun dtsProperty(): PsiElementPattern.Capture<PsiElement> {
  return psiElement().withElementType(errorOrProperty)
}

fun dtsSubNode(): PsiElementPattern.Capture<PsiElement> {
  return psiElement().andOr(
    psiElement().withElementType(errorOrSubNode),
    psiElement().withElementType(DtsTypes.PROPERTY).withText(not(string().contains("="))),
  )
}

fun dtsInsideContainer(): PsiElementPattern.Capture<PsiElement> {
  return psiElement().with(InsideContainer)
}

private object InsideContainer : PatternCondition<PsiElement>("inside container") {
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
  return get(InsideContainer.key)
}

fun dtsFirstValue(): PsiElementPattern.Capture<out PsiElement> {
  return psiElement(DtsValue::class.java).with(PropertyFirstValue)
}

private object PropertyFirstValue : PatternCondition<PsiElement>("first value") {
  val key = Key<DtsProperty>("property")

  override fun accepts(element: PsiElement, context: ProcessingContext): Boolean {
    if (element !is DtsValue) return false

    // PSI hierarchy: value -> property content -> property
    val content = element.parent.asSafely<DtsPropertyContent>() ?: return false
    if (content.firstChild != element) return false

    val property = content.parent.asSafely<DtsProperty>() ?: return false

    context.put(key, property)
    return true
  }
}

fun ProcessingContext.getDtsProperty(): DtsProperty {
  return get(PropertyFirstValue.key)
}

fun dtsFirstCell(): PsiElementPattern.Capture<PsiElement> {
  return psiElement().with(ArrayFirstCell)
}

private object ArrayFirstCell : PatternCondition<PsiElement>("first cell") {
  override fun accepts(element: PsiElement, context: ProcessingContext?): Boolean {
    val array = element.parent.asSafely<DtsCellArray>() ?: return false
    val cell = array.dtsValues.firstOrNull() ?: return false

    return cell == element || cell.endOffset == element.startOffset
  }
}