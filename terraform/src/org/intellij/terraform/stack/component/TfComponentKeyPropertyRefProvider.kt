// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.stack.component

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import org.intellij.terraform.config.model.Module
import org.intellij.terraform.hcl.patterns.HCLPatterns
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElement
import org.intellij.terraform.hcl.psi.HCLIdentifier
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.stack.component.TfComponentPsiPatterns.InputsPropertyBlock
import org.intellij.terraform.stack.component.TfComponentPsiPatterns.ProvidersPropertyOfComponent
import org.intellij.terraform.stack.component.TfComponentPsiPatterns.TfComponentFile

internal object TfComponentKeyPropertyRefProvider : PsiReferenceProvider() {
  override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
    if (element !is HCLIdentifier) return PsiReference.EMPTY_ARRAY

    return arrayOf(InputOrProviderReference(element))
  }
}

private class InputOrProviderReference(element: HCLElement) : PsiReferenceBase<HCLElement>(element) {
  override fun resolve(): PsiElement? {
    val property = element.parentOfType<HCLProperty>() ?: return null
    val propertyName = property.name

    val rootPropertyInBlock = property.parentOfType<HCLProperty>() ?: return null
    val rootBlock = rootPropertyInBlock.parentOfType<HCLBlock>() ?: return null
    val module = Module.getAsModuleBlock(rootBlock) ?: return null

    return when {
      InputsPropertyBlock.accepts(rootPropertyInBlock) ->
        module.findVariables(propertyName).firstOrNull()?.declaration
      ProvidersPropertyOfComponent.accepts(rootPropertyInBlock) ->
        module.getDefinedRequiredProviders()?.firstOrNull { it.name == propertyName }
      else -> null
    }
  }
}

internal val KeyPropertyPattern = PlatformPatterns.psiElement(HCLIdentifier::class.java)
  .inFile(TfComponentFile)
  .withParent(HCLPatterns.Property)
  .withSuperParent(2, HCLPatterns.Object)
  .withSuperParent(3, HCLPatterns.Property)
  .withSuperParent(5, HCLPatterns.Block)
