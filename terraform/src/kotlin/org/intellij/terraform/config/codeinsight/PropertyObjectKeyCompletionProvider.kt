// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.codeinsight

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.intellij.openapi.diagnostic.debug
import org.intellij.terraform.hcl.psi.*
import org.intellij.terraform.config.codeinsight.TerraformConfigCompletionContributor.Companion.getIncomplete
import org.intellij.terraform.config.model.Module
import org.intellij.terraform.config.model.getTerraformModule

object PropertyObjectKeyCompletionProvider : TerraformConfigCompletionContributor.OurCompletionProvider() {
  private val LOG = Logger.getInstance(PropertyObjectKeyCompletionProvider::class.java)

  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    val position = parameters.position // DQS, SQS or ID
    val parent = position.parent // Literal or Identifier or Object
    val obj = PsiTreeUtil.getParentOfType(parent, HCLObject::class.java, false) ?: return
    if (parent is HCLStringLiteral || parent is HCLIdentifier) {
      // Do not complete values
      if (HCLPsiUtil.isPartOfPropertyValue(parent)) {
        return addPropertyValueCompletions(parameters, result, obj)
      }
    }
    LOG.debug { "TF.PropertyObjectKeyCompletionProvider{position=$position, parent=$parent, obj=$obj}" }
    when (obj.parent) {
      is HCLProperty -> {
        return addPropertyCompletions(parameters, result, obj)
      }
      is HCLBlock -> {
        return addInnerBlockCompletions(parameters, result, obj)
      }
      else -> return
    }
  }

  private fun getLeftmostName(element: PsiElement): String? {
    if (element is HCLProperty) return element.name
    if (element is HCLBlock) return element.getNameElementUnquoted(0)
    return null
  }

  private fun addPropertyValueCompletions(parameters: CompletionParameters, result: CompletionResultSet, obj: HCLObject) {
    val inner = obj.parent ?: return
    val block = inner.getParent(HCLBlock::class.java, true) ?: return
    LOG.debug { "TF.PropertyObjectKeyCompletionProvider.InSomethingValue{block=$block, inner=$inner}" }

    val name: String = getLeftmostName(inner) ?: return
    val type = block.getNameElementUnquoted(0)

    if (name == "providers" && type == "module") {
      val module = block.getTerraformModule()
      val providers = module.getDefinedProviders().map { it.second }
      result.addAllElements(providers.map { TerraformConfigCompletionContributor.create(it) })
      return
    }
  }

  private fun addPropertyCompletions(parameters: CompletionParameters, result: CompletionResultSet, obj: HCLObject) {
    val property = obj.parent as? HCLProperty ?: return
    val block = PsiTreeUtil.getParentOfType(property, HCLBlock::class.java) ?: return
    LOG.debug { "TF.PropertyObjectKeyCompletionProvider.Property{block=$block, inner-property=$property}" }

    val type = block.getNameElementUnquoted(0)
    // TODO: Replace with 'ReferenceHint'
    if (property.name == "providers" && type == "module") {
      handleModuleProvidersMapping(block, parameters, obj, result)
      return
    }
  }

  private fun addInnerBlockCompletions(parameters: CompletionParameters, result: CompletionResultSet, obj: HCLObject) {
    val innerBlock = PsiTreeUtil.getParentOfType(obj, HCLBlock::class.java) ?: return
    val block = PsiTreeUtil.getParentOfType(innerBlock, HCLBlock::class.java, true) ?: return
    LOG.debug { "TF.PropertyObjectKeyCompletionProvider.Block{block=$block, inner-block=$block}" }

    val type = block.getNameElementUnquoted(0)
    // TODO: Replace with 'ReferenceHint'
    if (innerBlock.name == "providers" && type == "module") {
      handleModuleProvidersMapping(block, parameters, obj, result)
      return
    }
  }

  private fun handleModuleProvidersMapping(block: HCLBlock, parameters: CompletionParameters, obj: HCLObject, result: CompletionResultSet) {
    val module = Module.getAsModuleBlock(block) ?: return
    val incomplete: String? = getIncomplete(parameters)
    val defined = TerraformConfigCompletionContributor.getOriginalObject(parameters, obj).propertyList.map { it.name }
    val providers = module.getDefinedProviders()
        .map { it.second }
        .filter { !defined.contains(it) || (incomplete != null && it.contains(incomplete)) }
    result.addAllElements(providers.map { TerraformConfigCompletionContributor.create(it).withInsertHandler(ResourcePropertyInsertHandler) })
    return
  }
}
