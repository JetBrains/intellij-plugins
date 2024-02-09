// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentsOfType
import org.intellij.terraform.config.TerraformDocumentationUrlProvider.DATASOURCES
import org.intellij.terraform.config.TerraformDocumentationUrlProvider.RESOURCES
import org.intellij.terraform.config.TerraformDocumentationUrlProvider.getProviderUrl
import org.intellij.terraform.config.TerraformDocumentationUrlProvider.getResourceOrDataSourceUrl
import org.intellij.terraform.config.codeinsight.ModelHelper
import org.intellij.terraform.config.model.*
import org.intellij.terraform.config.patterns.TerraformPatterns
import org.intellij.terraform.config.psi.TerraformDocumentPsi
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.*
import org.intellij.terraform.hcl.psi.common.LiteralExpression
import org.jetbrains.annotations.Nls

//TODO Reimplement with a modern API
internal class TerraformDocumentationProvider : AbstractDocumentationProvider() {
  override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): @Nls String? {
    if (element is HCLProperty) {
      val block = element.parent?.parent as? HCLBlock
      if (TerraformPatterns.LocalsRootBlock.accepts(block)) {
        return HCLBundle.message("terraform.doc.label.local.value.0", element.name)
      }
      return HCLBundle.message("terraform.doc.property.0", element.name)
    }

    if (element is HCLBlock) {
      val type = element.getNameElementUnquoted(0)
      val name = element.name

      if (TerraformPatterns.RootBlock.accepts(element)) {
        when (type) {
          "module" -> return HCLBundle.message("terraform.doc.module.0", name) // todo: add short source
          "variable" -> return HCLBundle.message("terraform.doc.input.variable.0", name) // todo: add short type
          "output" -> return HCLBundle.message("terraform.doc.output.value.0", name) // todo: add short type
          "provider" -> return HCLBundle.message("terraform.doc.provider.0", name)
          "resource" -> return HCLBundle.message("terraform.doc.resource.0.of.type.1", name, element.getNameElementUnquoted(1))
          "data" -> return HCLBundle.message("terraform.doc.data.source.0.of.type.1", name, element.getNameElementUnquoted(1))

          "terraform" -> return HCLBundle.message("terraform.doc.terraform.configuration")
          "locals" -> return HCLBundle.message("terraform.doc.local.values")
        }
      }
      return null
    }
    //Workaround for documentation - we do not parse type identifier in top-level blocks
    if (element is TerraformDocumentPsi) {
      return HCLBundle.message("terraform.doc.block.type.0", element.name)
    }

    return null
  }

  override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): @Nls String? {
    if (element == null) return null
    if (!TerraformPatterns.TerraformFile.accepts(element.containingFile)) return null

    if (element is HCLProperty) {
      val block = element.parentsOfType<HCLBlock>(false).firstOrNull() ?: return null
      val properties = ModelHelper.getBlockProperties(block)
      val property = properties[element.name] as? PropertyType
      if (property != null) {
        return HCLBundle.message("terraform.doc.property.0.1.br.2", element.name, property.type.presentableText, property.description ?: "")
      }
      if (TerraformPatterns.LocalsRootBlock.accepts(block)) {
        return HCLBundle.message("terraform.doc.local.value.0", element.name)
      }
    }
    else if (element is HCLBlock) {
      if (TerraformPatterns.RootBlock.accepts(element)) {
        if (TerraformPatterns.VariableRootBlock.accepts(element)) {
          val variable = Variable(element)
          val typeExpression = (variable.getTypeExpression() as? LiteralExpression)?.unquotedText?.let { " of type ${it}" } ?: ""
          val description = (variable.getDescription() as? LiteralExpression)?.unquotedText?.let { "<br/> ${it}" } ?: ""
          val defaultValue = (variable.getDefault() as? HCLValue)?.text?.let { "<br/>Default value: ${it}" } ?: ""

          return HCLBundle.message("terraform.doc.variable.0.1.2.3", variable.name, typeExpression, description, defaultValue)
        }
        if (TerraformPatterns.ResourceRootBlock.accepts(element)) {
          return HCLBundle.message("terraform.doc.hcl.resource.0.of.type.1", element.name, element.getNameElementUnquoted(1))
        }
        val block = TypeModel.RootBlocks.firstOrNull { it.literal == element.getNameElementUnquoted(0) } ?: return null
        return HCLBundle.message("terraform.doc.block.0.br.1", element.name, block.description ?: "")
      }
      val pp = element.parent?.parent
      if (pp is HCLBlock) {
        val properties = ModelHelper.getBlockProperties(pp)
        val block = properties[element.getNameElementUnquoted(0)!!] as? BlockType ?: return HCLBundle.message("unknown.block.0", element.name)
        return HCLBundle.message("terraform.doc.block.0.br.1", element.name, block.description ?: "")
      }
    }
    //Block parameters
    else if (element is HCLIdentifier) {
      val parentBlock = getBlockForHclIdentifier(element) ?: return null
      val parentBlockType = parentBlock.getNameElementUnquoted(1) ?: parentBlock.getNameElementUnquoted(0)
      val property = (ModelHelper.getBlockProperties(parentBlock)[element.id] as? BaseModelType) ?: return null
      val description = property.description ?: ""
      return HCLBundle.message("terraform.doc.argument.0.1.br.2", parentBlockType, element.id, description)
    }
    //Workaround for documentation - we do not parse type identifier in top-level blocks
    else if (element is TerraformDocumentPsi) {
      val relevantBlock = getBlockForDocumentationLink(element, element.name) ?: return null
      val description = when (val typeClass = ModelHelper.getBlockType(relevantBlock)) {
                          is BaseModelType -> typeClass.description
                          else -> ""
                        } ?: ""
      return HCLBundle.message("terraform.doc.block.type.0.br.1", element.name, description)
    }
    return null
  }

  override fun getUrlFor(element: PsiElement?, originalElement: PsiElement?): List<String>? {
    when (element) {
      is HCLBlock -> {
        return getDocumentationUrl(element)?.ifEmpty { null }
      }
      is HCLIdentifier -> {
        val parentBlock = getBlockForHclIdentifier(element) ?: return null
        val paramName = ModelHelper.getBlockProperties(parentBlock)[element.id]?.name ?: return null
        return getDocumentationUrl(parentBlock, paramName)?.ifEmpty { null }
      }
      is TerraformDocumentPsi -> {
        val relevantBlock = getBlockForDocumentationLink(element, element.name) ?: return null
        return getDocumentationUrl(relevantBlock)?.ifEmpty { null }
      }
      else -> return null
    }
  }

  private fun getBlockForHclIdentifier(element: HCLIdentifier) = element.parentsOfType<HCLBlock>(true)
    .firstOrNull { block -> block.name != element.id }

  private fun getBlockForDocumentationLink(element: TerraformDocumentPsi?, blockTypeLiteral: String): HCLBlock? =
    element?.parentsOfType<HCLBlock>(false)
      ?.firstOrNull { block -> block.getNameElementUnquoted(1) == blockTypeLiteral }

  private fun getDocumentationUrl(element: HCLBlock,
                                  paramName: String? = null): List<String>? {
    val identifier = element.getNameElementUnquoted(1) ?: return null
    return when (element.getNameElementUnquoted(0)) {
      "resource" -> listOf(getResourceOrDataSourceUrl(identifier, RESOURCES, element, paramName))
      "data" -> listOf(getResourceOrDataSourceUrl(identifier, DATASOURCES, element, paramName))
      "provider" -> listOf(getProviderUrl(identifier, element, paramName))
      else -> null
    }
  }
}
