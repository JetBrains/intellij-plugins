// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.opentofu.codeinsight

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElementVisitor
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.hcl.psi.getNameElementUnquoted
import org.intellij.terraform.hil.psi.HCLElementLazyReference
import org.intellij.terraform.opentofu.OpenTofuFileType
import org.intellij.terraform.opentofu.patterns.OpenTofuPatterns

internal class OpenTofuReferenceContributor : PsiReferenceContributor() {

  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(OpenTofuPatterns.EncryptionMethodKeysPropertyValue, EncryptionBlockElementNameReferenceProvider)
    registrar.registerReferenceProvider(OpenTofuPatterns.EncryptionMethodPropertyValue, EncryptionBlockElementNameReferenceProvider)
  }
}

internal object EncryptionBlockElementNameReferenceProvider : PsiReferenceProvider() {
  override fun getReferencesByElement(
    element: PsiElement,
    context: ProcessingContext,
  ): Array<PsiReference> {
    if (element.containingFile.fileType != OpenTofuFileType) return PsiReference.EMPTY_ARRAY

    return arrayOf(HCLElementLazyReference(element, false) { incomplete, _ ->
      val property = element.parentOfType<HCLProperty>(false) ?: return@HCLElementLazyReference emptyList()
      CachedValuesManager.getCachedValue(property) {
        val result = findEncryptionElementByFQN(property.value?.text, property)
        CachedValueProvider.Result.create(result, PsiModificationTracker.MODIFICATION_COUNT)
      }
    })
  }
}

internal fun findEncryptionElementByFQN(fqn: String?, element: PsiElement): List<HCLBlock> {
  fqn ?: return emptyList()
  val terraformBlock = element.containingFile.childrenOfType<HCLBlock>().firstOrNull { elem -> TfPsiPatterns.TerraformRootBlock.accepts(elem) }
  val encryptionBlock = terraformBlock?.`object`?.childrenOfType<HCLBlock>()?.firstOrNull { elem -> OpenTofuPatterns.EncryptionBlock.accepts(elem) } ?: return emptyList()
  val (blockType, type, id) = fqn.split('.').takeIf { it.size > 2 } ?: return emptyList()
  val result = mutableListOf<HCLBlock>()
  encryptionBlock.`object`?.acceptChildren(object : HCLElementVisitor() {
    override fun visitBlock(block: HCLBlock) {
      if (block.getNameElementUnquoted(0)?.contentEquals(blockType) == true
          && block.getNameElementUnquoted(1)?.contentEquals(type) == true
          && block.getNameElementUnquoted(2)?.contentEquals(id) == true
      ) result.add(block)
    }
  })
  return result
}
