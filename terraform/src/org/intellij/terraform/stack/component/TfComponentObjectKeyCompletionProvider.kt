// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.stack.component

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import org.intellij.terraform.config.Constants
import org.intellij.terraform.config.codeinsight.TfCompletionUtil
import org.intellij.terraform.config.codeinsight.TfModelHelper
import org.intellij.terraform.config.model.HclTypeImpl
import org.intellij.terraform.config.model.Module
import org.intellij.terraform.config.model.PropertyType
import org.intellij.terraform.hcl.codeinsight.HclObjectKeyCompletionProvider
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLObject
import org.intellij.terraform.hcl.psi.HCLProperty

internal object TfComponentObjectKeyCompletionProvider : HclObjectKeyCompletionProvider() {
  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    val position = parameters.position
    val parent = position.parent

    val hclObject = parent as? HCLObject ?: parent.parent as? HCLObject ?: return
    val componentProperty = hclObject.parent as? HCLProperty ?: return
    val componentBlock = componentProperty.parentOfType<HCLBlock>() ?: return

    val properties = if (TfComponentPsiPatterns.InputsPropertyBlock.accepts(componentProperty)) {
      TfModelHelper.getModuleRequiredVariables(componentBlock).values
    }
    else if (TfComponentPsiPatterns.ProvidersPropertyOfComponent.accepts(componentProperty)) {
      val module = Module.Companion.getAsModuleBlock(componentBlock)
      val providers = module?.getDefinedRequiredProviders() ?: return
      mapProvidersToProperties(providers)
    }
    else null

    val filteredCandidates = properties?.filter { hclObject.findProperty(it.name) == null }
    filteredCandidates?.forEach { result.addElement(TfCompletionUtil.createPropertyOrBlockType(it)) }
  }

  private fun mapProvidersToProperties(providers: List<HCLProperty>): List<PropertyType> = providers.map { provider ->
    val versionProperty = (provider.value as? HCLObject)?.findProperty(Constants.HCL_VERSION_IDENTIFIER)
    val version = versionProperty?.value?.text?.let { StringUtil.unquoteString(it) } ?: ""

    PropertyType(provider.name, HclTypeImpl(version))
  }
}