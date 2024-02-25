// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.documentation

import com.intellij.codeInsight.CodeInsightBundle
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentsOfType
import org.intellij.terraform.config.codeinsight.ModelHelper
import org.intellij.terraform.config.model.*
import org.intellij.terraform.config.patterns.TerraformPatterns
import org.intellij.terraform.config.psi.TerraformDocumentPsi
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.*
import org.intellij.terraform.hcl.psi.common.LiteralExpression
import org.jetbrains.annotations.Nls

internal const val READ_TIMEOUT: Int = 1000
internal val NO_DOC: String = CodeInsightBundle.message("no.documentation.found")

internal fun provideDocForProperty(element: HCLProperty): @Nls String? {
  val block = element.parentsOfType<HCLBlock>(false).firstOrNull()
  if (block == null) {
    return null
  }
  else {
    val properties = ModelHelper.getBlockProperties(block)
    val property = properties[element.name] as? PropertyType
    if (property != null) {
      return HCLBundle.message("terraform.doc.property.0.1.br.2", element.name, property.type.presentableText, property.description
                                                                                                               ?: "")
    }
    else if (TerraformPatterns.LocalsRootBlock.accepts(block)) {
      return HCLBundle.message("terraform.doc.local.value.0", element.name)
    }
    else {
      return null
    }
  }
}

internal fun provideDocForHclBlock(element: HCLBlock): @Nls String? {
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
    else {
      val block = TypeModel.RootBlocks.firstOrNull { it.literal == element.getNameElementUnquoted(0) }
      if (block == null) {
        return null
      }
      else {
        return HCLBundle.message("terraform.doc.block.0.br.1", element.name, block.description ?: "")
      }
    }
  }
  else {
    val pp = element.parent?.parent
    if (pp is HCLBlock) {
      val properties = ModelHelper.getBlockProperties(pp)
      val block = properties[element.getNameElementUnquoted(0)!!] as? BlockType
      if (block == null) {
        return HCLBundle.message("unknown.block.0", element.name)
      }
      else {
        return HCLBundle.message("terraform.doc.block.0.br.1", element.name, block.description ?: "")
      }
    }
    else {
      return null
    }
  }
}

internal fun provideDocForIdentifier(element: HCLIdentifier): @Nls String? {
  val parentBlock = getBlockForHclIdentifier(element) ?: return null
  val parentBlockType = parentBlock.getNameElementUnquoted(1) ?: parentBlock.getNameElementUnquoted(0)
  val property = (ModelHelper.getBlockProperties(parentBlock)[element.id] as? BaseModelType) ?: return null
  val description = property.description ?: ""
  return HCLBundle.message("terraform.doc.argument.0.1.br.2", parentBlockType, element.id, description)
}

fun getBlockForHclIdentifier(element: HCLIdentifier): HCLBlock? {
  return element.parentsOfType<HCLBlock>(true).firstOrNull { block -> block.name != element.id }
}

internal fun getBlockForDocumentationLink(element: TerraformDocumentPsi?, blockTypeLiteral: String): HCLBlock? {
  return element?.parentsOfType<HCLBlock>(false)?.firstOrNull { block -> block.getNameElementUnquoted(1) == blockTypeLiteral }
}

internal fun getHelpWindowHeader(element: PsiElement?): @Nls String  {
  return when (element) {
    is HCLProperty -> {
      val block = element.parent?.parent as? HCLBlock
      if (TerraformPatterns.LocalsRootBlock.accepts(block)) {
        HCLBundle.message("terraform.doc.label.local.value.0", element.name)
      }
      else {
        HCLBundle.message("terraform.doc.property.0", element.name)
      }
    }
    is HCLIdentifier -> {
      val parentBlock = getBlockForHclIdentifier(element)
      val parentBlockType = parentBlock?.getNameElementUnquoted(1) ?: parentBlock?.getNameElementUnquoted(0)
      if (parentBlockType != null) {
        HCLBundle.message("terraform.doc.argument.0.1", parentBlockType, element.id)
      }
      else {
        NO_DOC
      }
    }
    is HCLBlock -> {
      val type = element.getNameElementUnquoted(0)
      val name = element.name
      if (TerraformPatterns.RootBlock.accepts(element)) {
        when (type) {
          "module" -> HCLBundle.message("terraform.doc.module.0", name) // todo: add short source
          "variable" -> HCLBundle.message("terraform.doc.input.variable.0", name) // todo: add short type
          "output" -> HCLBundle.message("terraform.doc.output.value.0", name) // todo: add short type
          "provider" -> HCLBundle.message("terraform.doc.provider.0", name)
          "resource" -> HCLBundle.message("terraform.doc.resource.0.of.type.1", name, element.getNameElementUnquoted(1))
          "data" -> HCLBundle.message("terraform.doc.data.source.0.of.type.1", name, element.getNameElementUnquoted(1))

          "terraform" -> HCLBundle.message("terraform.doc.terraform.configuration")
          "locals" -> HCLBundle.message("terraform.doc.local.values")
          else -> NO_DOC
        }
      }
      else {
        NO_DOC
      }
    }
    is TerraformDocumentPsi -> {
      HCLBundle.message("terraform.doc.block.type.0", element.name)
    }
    else -> NO_DOC
  }
}
