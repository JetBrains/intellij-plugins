// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.psi

import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.parentsOfType
import com.intellij.util.ProcessingContext
import org.intellij.terraform.config.Constants.HCL_MODULE_IDENTIFIER
import org.intellij.terraform.config.model.Module
import org.intellij.terraform.config.model.getTerraformModule
import org.intellij.terraform.config.patterns.TfPsiPatterns.TerraformConfigFile
import org.intellij.terraform.hcl.patterns.HCLPatterns
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElement
import org.intellij.terraform.hcl.psi.HCLIdentifier
import org.intellij.terraform.hcl.psi.HCLIndexSelectExpression
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.hcl.psi.HCLPsiUtil
import org.intellij.terraform.hcl.psi.HCLSelectExpression
import org.intellij.terraform.hcl.psi.HCLStringLiteral
import org.intellij.terraform.hcl.psi.common.Identifier
import org.intellij.terraform.hcl.psi.common.LiteralExpression
import org.intellij.terraform.hcl.psi.getNameElementUnquoted
import org.intellij.terraform.hil.psi.HCLElementLazyReference

internal abstract class TfProviderReferenceBase : PsiReferenceProvider() {
  override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> {
    if (element !is HCLIdentifier && element !is HCLStringLiteral)
      return PsiReference.EMPTY_ARRAY

    val parent = element.parent
    // aliased form `type.alias`: keep the reference on the alias part only (ignore `type.alias[0]` index access)
    if (parent is HCLSelectExpression && (parent is HCLIndexSelectExpression || parent.field !== element))
      return PsiReference.EMPTY_ARRAY

    if (!isApplicable(element)) return PsiReference.EMPTY_ARRAY

    return arrayOf(HCLElementLazyReference(element, soft = false) { incomplete, _ ->
      val module = targetModule(element) ?: return@HCLElementLazyReference emptyList()
      if (incomplete)
        module.getDefinedProviders().map { it.first }
      else
        getElementText(element)?.let { module.findProviders(it) } ?: emptyList()
    })
  }

  abstract fun isApplicable(element: PsiElement): Boolean

  abstract fun targetModule(element: PsiElement): Module?

  /** The full `type.alias` text for the aliased form, otherwise the identifier/literal text. */
  private fun getElementText(element: PsiElement): String? {
    val parent = element.parent
    if (parent is HCLSelectExpression && parent !is HCLIndexSelectExpression) return parent.text
    return when (element) {
      is LiteralExpression -> element.unquotedText
      is Identifier -> element.name
      else -> null
    }
  }

  fun registerTo(registrar: PsiReferenceRegistrar, providerPropertyPattern: PsiElementPattern.Capture<HCLProperty>) {
    registrar.registerReferenceProvider(
      psiElement()
        .and(PlatformPatterns.or(HCLPatterns.Identifier, HCLPatterns.Literal))
        .inFile(TerraformConfigFile)
        .withParent(providerPropertyPattern), this)

    registrar.registerReferenceProvider(
      psiElement()
        .and(PlatformPatterns.or(HCLPatterns.Identifier, HCLPatterns.Literal))
        .inFile(TerraformConfigFile)
        .withParent(HCLPatterns.SelectExpression)
        .withSuperParent(2, providerPropertyPattern), this)
  }
}

internal object ResourceProviderReference : TfProviderReferenceBase() {

  override fun isApplicable(element: PsiElement): Boolean =
    element.parent is HCLSelectExpression || HCLPsiUtil.isPropertyValue(element)

  override fun targetModule(element: PsiElement): Module? = (element as? HCLElement)?.getTerraformModule()
}

internal object ModuleProvidersReference : TfProviderReferenceBase() {

  override fun isApplicable(element: PsiElement): Boolean = moduleBlock(element) != null

  override fun targetModule(element: PsiElement): Module? {
    val block = moduleBlock(element) ?: return null
    return if (HCLPsiUtil.isPartOfPropertyKey(element)) Module.getAsModuleBlock(block)
    else (element as? HCLElement)?.getTerraformModule()
  }

  private fun moduleBlock(element: PsiElement): HCLBlock? {
    val property = element.parentOfType<HCLProperty>() ?: return null
    return property.parentsOfType<HCLBlock>().firstOrNull {
      it.getNameElementUnquoted(0) == HCL_MODULE_IDENTIFIER
    }
  }
}
