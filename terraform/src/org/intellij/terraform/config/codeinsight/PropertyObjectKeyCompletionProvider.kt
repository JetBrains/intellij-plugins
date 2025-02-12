// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.codeinsight

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder.create
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.intellij.util.containers.tail
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.getIncomplete
import org.intellij.terraform.config.model.*
import org.intellij.terraform.hcl.psi.*

object PropertyObjectKeyCompletionProvider : TfConfigCompletionContributor.TfCompletionProvider() {
  private val LOG = Logger.getInstance(PropertyObjectKeyCompletionProvider::class.java)

  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    val position = parameters.position // DQS, SQS or ID
    val parent = position.parent // Literal or Identifier or Object
    val obj = PsiTreeUtil.getParentOfType(parent, HCLObject::class.java, false) ?: return
    if (parent is HCLStringLiteral || parent is HCLIdentifier) {
      // Do not complete values
      if (HCLPsiUtil.isPartOfPropertyValue(parent)) {
        return addPropertyValueCompletions(result, obj)
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

  private fun addPropertyValueCompletions(result: CompletionResultSet, obj: HCLObject) {
    val inner = obj.parent ?: return
    val block = inner.getParent(HCLBlock::class.java, true) ?: return
    LOG.debug { "TF.PropertyObjectKeyCompletionProvider.InSomethingValue{block=$block, inner=$inner}" }

    val name: String = getLeftmostName(inner) ?: return
    val type = block.getNameElementUnquoted(0)

    if (name == "providers" && type == "module") {
      val module = block.getTerraformModule()
      val providers = module.getDefinedProviders().map { it.second }
      result.addAllElements(providers.map { create(it).withInsertHandler(QuoteInsertHandler) })
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
    else if (type == "module") {
      val pathToRoot = pathToRootObject(property)
      val objTypeProperty = findObjectTypeInModule(block, pathToRoot.reversed())
      val properties = objTypeProperty?.elements?.map { PropertyType(it.key, it.value ?: Types.Any) } ?: return
      result.addAllElements(properties.map { TfCompletionUtil.createPropertyOrBlockType(it) })
    }
  }

  private fun pathToRootObject(element: HCLProperty): List<String> {
    return generateSequence(element) { current ->
      ProgressManager.checkCanceled()
      if (current.parent is HCLObject && current.parent?.parent is HCLProperty) {
        current.parent.parent as? HCLProperty
      }
      else null
    }.map { it.name }.toList()
  }

  private fun findObjectTypeInModule(block: HCLBlock, pathFromRoot: List<String>): ObjectType? {
    val rootObject: ObjectType? = block.getTerraformModule().findVariables(pathFromRoot.first()).firstOrNull()?.getType() as? ObjectType
    return pathFromRoot.tail().fold(rootObject) { current, pathElement ->
      current?.elements?.get(pathElement) as? ObjectType ?: return current
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
    val defined = TfCompletionUtil.getOriginalObject(parameters, obj).propertyList.map { it.name }
    val providers = module.getDefinedProviders()
      .map { it.second }
      .filter { !defined.contains(it) || (incomplete != null && it.contains(incomplete)) }
    result.addAllElements(
      providers.map { create(it).withInsertHandler(ResourcePropertyInsertHandler) })
    return
  }
}
