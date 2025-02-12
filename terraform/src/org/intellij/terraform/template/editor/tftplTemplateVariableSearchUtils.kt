// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.template.editor

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.util.startOffset
import com.intellij.util.ProcessingContext
import org.intellij.terraform.hil.HILElementTypes
import org.intellij.terraform.hil.psi.ILExpressionHolder
import org.intellij.terraform.hil.psi.ILVariable
import org.intellij.terraform.hil.psi.TerraformTemplateLanguage
import org.intellij.terraform.template.TftplFileViewProvider
import org.intellij.terraform.template.model.TftplVariable
import org.intellij.terraform.template.psi.TftplFile

internal const val HIGH_COMPLETION_PRIORITY: Double = 100.0

internal fun translateToTemplateLanguageElement(position: PsiElement): PsiElement? {
  val viewProvider = getTemplateFileViewProvider(position) ?: return null
  val offsetInFile = computeAbsoluteOffsetInFile(position)
  return viewProvider.findElementAt(offsetInFile, TerraformTemplateLanguage)
}

private fun computeAbsoluteOffsetInFile(position: PsiElement): Int {
  val injectionHostStartOffsetIfAny = InjectedLanguageManager.getInstance(position.project).getInjectionHost(position)?.startOffset ?: 0
  val elementUnderCaretOffset = position.startOffset
  return elementUnderCaretOffset + injectionHostStartOffsetIfAny
}

internal fun getTemplateFileViewProvider(element: PsiElement): TftplFileViewProvider? {
  return InjectedLanguageManager.getInstance(element.project)
    .getTopLevelFile(element)
    ?.viewProvider as? TftplFileViewProvider
}

internal val hilVariablePattern: ElementPattern<PsiElement> =
  PlatformPatterns.or(
    createHilVariablePattern { isTemplateFileElement(it) || isInjectedHil(it) },
    isRightAfterControlKeyword()
  )

internal fun isTemplateFileElement(psiElement: PsiElement): Boolean {
  return psiElement.containingFile is TftplFile
}

internal fun isInjectedHil(psiElement: PsiElement): Boolean {
  return InjectedLanguageManager.getInstance(psiElement.project).isInjectedFragment(psiElement.containingFile)
}

internal fun createHilVariablePattern(test: (PsiElement) -> Boolean = { true }): PsiElementPattern.Capture<PsiElement> {
  return PlatformPatterns.psiElement(HILElementTypes.ID)
    .withParent(ILVariable::class.java)
    .withSuperParent(2, ILExpressionHolder::class.java)
    .with(object : PatternCondition<PsiElement>("isVariableWithoutSiblings") {
      override fun accepts(element: PsiElement, context: ProcessingContext?): Boolean {
        return test(element)
               && PsiTreeUtil.prevCodeLeaf(element)?.elementType == HILElementTypes.INTERPOLATION_START
               && PsiTreeUtil.nextCodeLeaf(element)?.elementType == HILElementTypes.R_CURLY
      }
    })
}

internal fun createPattern(test: (PsiElement) -> Boolean = { true }): PsiElementPattern.Capture<PsiElement> {
  return PlatformPatterns.psiElement()
    .with(object : PatternCondition<PsiElement>("commonHilElementPattern") {
      override fun accepts(element: PsiElement, context: ProcessingContext?): Boolean {
        return test(element)
      }
    })
}

private fun isRightAfterControlKeyword(): PsiElementPattern.Capture<PsiElement> {
  return PlatformPatterns.psiElement(HILElementTypes.ID)
    .with(object : PatternCondition<PsiElement>("isRightAfterControlKeyword") {
      override fun accepts(element: PsiElement, context: ProcessingContext?): Boolean {
        val previousElementType = PsiTreeUtil.prevCodeLeaf(element)?.elementType
        return previousElementType == HILElementTypes.IN_KEYWORD || previousElementType == HILElementTypes.IF_KEYWORD
      }
    })
}

internal fun createVariableLookupSkeleton(variable: TftplVariable): LookupElementBuilder {
  return LookupElementBuilder.create(variable.name)
    .withTypeText(variable.type.presentableName)
    .withIcon(variable.type.icon)
    .withPsiElement(variable.navigationElement)
}