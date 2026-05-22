// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.test.codeinsight

import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import org.intellij.terraform.hcl.patterns.HCLPatterns
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElement
import org.intellij.terraform.hcl.psi.HCLIdentifier
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.test.patterns.TfTestPsiPatterns

internal class TfTestReferenceContributor : PsiReferenceContributor() {
  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(TfTestVariableIdentifier, TfTestVariableReferenceProvider)
  }
}

internal object TfTestVariableReferenceProvider : PsiReferenceProvider() {
  override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> {
    if (element !is HCLIdentifier) return PsiReference.EMPTY_ARRAY
    return arrayOf(TfVariableReferenceInTfTest(element))
  }

  class TfVariableReferenceInTfTest(element: HCLElement) : PsiReferenceBase<HCLElement>(element) {
    override fun resolve(): PsiElement? {
      val property = element.parentOfType<HCLProperty>() ?: return null

      val variablesBlock = property.parentOfType<HCLBlock>() ?: return null
      if (!TfTestPsiPatterns.TfVariablesBlock.accepts(variablesBlock))
        return null

      val module = TfTestHelper.getVariablesModule(variablesBlock)
      return module?.findVariables(property.name)?.firstOrNull()?.declaration
    }
  }
}

internal val TfTestVariableIdentifier: PsiElementPattern.Capture<HCLIdentifier> = psiElement(HCLIdentifier::class.java)
  .inFile(TfTestPsiPatterns.TfTestFile)
  .withParent(HCLPatterns.Property)
  .withSuperParent(2, HCLPatterns.Object)
  .withSuperParent(3, TfTestPsiPatterns.TfVariablesBlock)
