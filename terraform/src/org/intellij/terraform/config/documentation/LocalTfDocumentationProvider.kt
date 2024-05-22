// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.documentation

import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentsOfType
import org.intellij.terraform.config.codeinsight.TfModelHelper
import org.intellij.terraform.config.model.BaseModelType
import org.intellij.terraform.config.model.PropertyType
import org.intellij.terraform.config.model.TypeModel
import org.intellij.terraform.config.model.Variable
import org.intellij.terraform.config.patterns.TerraformPatterns
import org.intellij.terraform.config.psi.TerraformDocumentPsi
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.*
import org.intellij.terraform.hcl.psi.common.LiteralExpression
import org.jetbrains.annotations.Nls

internal object LocalTfDocumentationProvider {

  @Nls
  internal fun fetchLocalDescription(element: PsiElement?): String? {
    return when (element) {
      is HCLProperty -> {
        provideDocForProperty(element)
      }
      is HCLBlock -> {
        provideDocForHclBlock(element)
      } //Block parameters
      is HCLIdentifier -> {
        provideDocForIdentifier(element)
      } //Workaround for documentation - we do not parse type identifier in top-level blocks
      is TerraformDocumentPsi -> {
        getBlockForDocumentationLink(element, element.name)?.let { provideDocForHclBlock(it) }
      }
      else -> null
    }
  }

  private fun provideDocForProperty(element: HCLProperty): @Nls String? {
    val block = element.parentsOfType<HCLBlock>(false).firstOrNull() ?: return null
    val properties = TfModelHelper.getBlockProperties(block)
    val property = properties[element.name] as? PropertyType
    if (property != null) {
      return HCLBundle.message("terraform.doc.property.0.1.br.2", element.name, property.type.presentableText, property.description ?: "")
    }
    else if (TerraformPatterns.LocalsRootBlock.accepts(block)) {
      return HCLBundle.message("terraform.doc.local.value.0", element.name)
    }
    return null
  }

  @Nls
  private fun provideDocForHclBlock(element: HCLBlock): String? {
    return when {
      TerraformPatterns.VariableRootBlock.accepts(element) -> getVariableDocumentation(element)
      TerraformPatterns.ResourceRootBlock.accepts(element) -> getTypedBlockDocumentation(element, "terraform.doc.hcl.resource.0.of.type.1")
      TerraformPatterns.DataSourceRootBlock.accepts(element) -> getTypedBlockDocumentation(element, "terraform.doc.hcl.datasource.0.of.type.1")
      TerraformPatterns.ProviderRootBlock.accepts(element) -> getTypedBlockDocumentation(element, "terraform.doc.hcl.provider.0.of.type.1")
      else -> getGenericBlockDocumentation(element) ?: findParentBlockDocumentation(element)
    }
  }

  @Nls
  private fun getVariableDocumentation(element: HCLBlock): String {
    val variable = Variable(element)
    val typeExpression = (variable.getTypeExpression() as? LiteralExpression)?.unquotedText?.let { " of type $it" } ?: ""
    val description = (variable.getDescription() as? LiteralExpression)?.unquotedText?.let { "<br/> $it" } ?: ""
    val defaultValue = (variable.getDefault() as? HCLValue)?.text?.let { "<br/>Default value: $it" } ?: ""
    return HCLBundle.message("terraform.doc.variable.0.1.2.3", variable.name, typeExpression, description, defaultValue)
  }

  @Nls
  private fun getTypedBlockDocumentation(element: HCLBlock, bundleKey: String): String? {
    val block = TypeModel.RootBlocks.firstOrNull { it.literal == element.getNameElementUnquoted(0) }
    return block?.let {
      HCLBundle.message(bundleKey, element.name, element.getNameElementUnquoted(1), it.description ?: "")
    }
  }

  @Nls
  private fun getGenericBlockDocumentation(element: HCLBlock): String? {
    val block = TypeModel.RootBlocks.firstOrNull { it.literal == element.getNameElementUnquoted(0) }
    return block?.let {
      HCLBundle.message("terraform.doc.block.0.br.1", element.name, it.description ?: "")
    }
  }

  @Nls
  private fun findParentBlockDocumentation(element: HCLBlock): String? {
    val parentBlock = element.parent?.parent as? HCLBlock ?: return null
    return provideDocForHclBlock(parentBlock)
  }

  @Nls
  private fun provideDocForIdentifier(element: HCLIdentifier):  String? {
    val parentBlock = getBlockForHclIdentifier(element) ?: return null
    val parentBlockType = parentBlock.getNameElementUnquoted(1) ?: parentBlock.getNameElementUnquoted(0)
    val property = (TfModelHelper.getBlockProperties(parentBlock)[element.id] as? BaseModelType) ?: return null
    val description = property.description ?: ""
    return HCLBundle.message("terraform.doc.argument.0.1.br.2", parentBlockType, element.id, description)
  }

}