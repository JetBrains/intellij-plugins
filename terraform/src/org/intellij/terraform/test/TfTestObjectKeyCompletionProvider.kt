// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.test

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import org.intellij.terraform.config.codeinsight.TfCompletionUtil
import org.intellij.terraform.config.model.BlockType
import org.intellij.terraform.config.model.TypeModelProvider
import org.intellij.terraform.hcl.codeinsight.HclObjectKeyCompletionProvider
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.hcl.psi.getNameElementUnquoted

internal object TfTestObjectKeyCompletionProvider : HclObjectKeyCompletionProvider() {
  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    val hclObject = getParentHclObject(parameters) ?: return
    val componentProperty = hclObject.parent as? HCLProperty ?: return

    if (TfTestPsiPatterns.TfMockDefaultsProperty.accepts(componentProperty)) {
      val block = componentProperty.parentOfType<HCLBlock>() ?: return
      val resourceOrData = getMockResourceOrData(block) ?: return

      // Here we should suggest only attribute properties
      val filteredCandidates = resourceOrData.properties.values
        .filter { it.computed }
        .filter { hclObject.findProperty(it.name) == null }

      filteredCandidates.forEach { result.addElement(TfCompletionUtil.createPropertyOrBlockType(it)) }
    }
  }

  private fun getMockResourceOrData(block: HCLBlock): BlockType? {
    val identifier = block.getNameElementUnquoted(0)
    val type = block.getNameElementUnquoted(1)

    if (identifier.isNullOrBlank() || type.isNullOrBlank()) return null

    return when (identifier) {
      HCL_MOCK_RESOURCE_IDENTIFIER -> TypeModelProvider.getModel(block).getResourceType(type, block)
      HCL_MOCK_DATA_IDENTIFIER -> TypeModelProvider.getModel(block).getDataSourceType(type, block)
      else -> null
    }
  }
}
