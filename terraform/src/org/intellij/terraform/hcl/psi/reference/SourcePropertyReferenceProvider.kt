// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.psi.reference

import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet
import com.intellij.util.ProcessingContext
import org.intellij.terraform.config.Constants.HCL_SOURCE_IDENTIFIER
import org.intellij.terraform.config.patterns.TfPsiPatterns.propertyWithName
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLObject
import org.intellij.terraform.hcl.psi.HCLPsiUtil
import org.intellij.terraform.hcl.psi.HCLStringLiteral

internal object SourcePropertyReferenceProvider : PsiReferenceProvider() {

  override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference?> {
    if (element !is HCLStringLiteral || !HCLPsiUtil.isPropertyValue(element)) {
      return PsiReference.EMPTY_ARRAY
    }
    return FileReferenceSet.createSet(element, true, false, false).allReferences
  }

  fun register(registrar: PsiReferenceRegistrar, sourceBlockPattern: PsiElementPattern.Capture<HCLBlock>) {
    registrar.registerReferenceProvider(getSourceLiteralPattern(sourceBlockPattern), this)
  }

  private fun getSourceLiteralPattern(sourceBlockPattern: PsiElementPattern.Capture<HCLBlock>) =
    PlatformPatterns.psiElement(HCLStringLiteral::class.java)
      .withParent(propertyWithName(HCL_SOURCE_IDENTIFIER))
      .withSuperParent(2, HCLObject::class.java)
      .withSuperParent(3, sourceBlockPattern)
}
