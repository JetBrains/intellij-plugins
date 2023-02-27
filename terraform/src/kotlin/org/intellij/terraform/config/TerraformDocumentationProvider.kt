// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.hcl.psi.HCLValue
import org.intellij.terraform.hcl.psi.common.LiteralExpression
import org.intellij.terraform.hcl.psi.getNameElementUnquoted
import org.intellij.terraform.config.codeinsight.ModelHelper
import org.intellij.terraform.config.model.BlockType
import org.intellij.terraform.config.model.PropertyType
import org.intellij.terraform.config.model.TypeModel
import org.intellij.terraform.config.model.Variable
import org.intellij.terraform.config.patterns.TerraformPatterns
import org.jetbrains.annotations.Nls

internal class TerraformDocumentationProvider : AbstractDocumentationProvider() {
  override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): @Nls String? {
    if (element is HCLProperty) {
      val block = element.parent?.parent as? HCLBlock
      if (TerraformPatterns.LocalsRootBlock.accepts(block)) {
        return "local value ${element.name}"
      }
      return "property ${element.name}"
    }

    if (element is HCLBlock) {
      val type = element.getNameElementUnquoted(0)
      val name = element.name

      if (TerraformPatterns.RootBlock.accepts(element)) {
        when (type) {
          "module" -> return "module \"$name\"" // todo: add short source
          "variable" -> return "input variable \"$name\"" // todo: add short type
          "output" -> return "output value \"$name\"" // todo: add short type
          "provider" -> return "provider \"$name\""
          "resource" -> return "resource \"$name\" of type ${element.getNameElementUnquoted(1)}"
          "data" -> return "data source \"$name\" of type ${element.getNameElementUnquoted(1)}"

          "terraform" -> return "terraform configuration"
          "locals" -> return "local values"
        }
      }
      return null
    }
    return null
  }

  override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): @Nls String? {
    if (element == null) return null
    if (!TerraformPatterns.TerraformFile.accepts(element.containingFile)) return null

    if (element is HCLProperty) {
      val block = element.parent?.parent as? HCLBlock ?: return null
      val properties = ModelHelper.getBlockProperties(block)
      val property = properties[element.name] as? PropertyType
      if (property != null) {
        return buildString {
          append("Property ")
          append(element.name)
          append(" (")
          append(property.type.presentableText)
          append(")")
          if (property.description != null) {
            append("<br/>")
            append(property.description)
          }
        }
      }
      if (TerraformPatterns.LocalsRootBlock.accepts(block)) {
        return "Local value ${element.name}"
      }
    } else if (element is HCLBlock) {
      if (TerraformPatterns.RootBlock.accepts(element)) {
        if (TerraformPatterns.VariableRootBlock.accepts(element)) {
          val variable = Variable(element)
          variable.getTypeExpression()
          variable.getDescription()
          return buildString {
            append("Variable ")
            append(variable.name)
            (variable.getTypeExpression() as? LiteralExpression)?.let {
              append(" of type ")
              append(it.unquotedText)
            }
            (variable.getDescription() as? LiteralExpression)?.let {
              append("<br/><br/>")
              append(it.unquotedText)
            }
            (variable.getDefault() as? HCLValue)?.let {
              append("<br/>Default value: ")
              append(it.text)
            }
          }
        }
        if (TerraformPatterns.ResourceRootBlock.accepts(element)) {
          return "Resource ${element.name} of type ${element.getNameElementUnquoted(1)}"
        }
        val block = TypeModel.RootBlocks.firstOrNull { it.literal == element.getNameElementUnquoted(0) } ?: return null
        return buildString {
          append("Block ")
          append(element.name)
          if (block.description != null) {
            append("<br/>")
            append(block.description)
          }
        }
      }
      val pp = element.parent?.parent
      if (pp is HCLBlock) {
        val properties = ModelHelper.getBlockProperties(pp)
        val block = properties[element.getNameElementUnquoted(0)!!] as? BlockType ?: return "Unknown block ${element.name}"
        return buildString {
          append("Block ")
          append(element.name)
          if (block.description != null) {
            append("<br/>")
            append(block.description)
          }
        }
      }
    }
    return null
  }

  override fun getUrlFor(element: PsiElement?, originalElement: PsiElement?): List<String>? {
    if (element is HCLBlock) {
      if (TerraformPatterns.RootBlock.accepts(element)) {
        val identifier = element.getNameElementUnquoted(1) ?: return null
        return when (element.getNameElementUnquoted(0)) {
          "resource" -> listOf(getResourceOrDataSourceUrl(identifier, 'r'))
          "data" -> listOf(getResourceOrDataSourceUrl(identifier, 'd'))
          "provider" -> listOf(getProviderUrl(identifier))
          else -> null
        }
      }
    }

    return null
  }

  // https://www.terraform.io/docs/providers/$PROVIDER/index.html
  private fun getProviderUrl(provider: String): String {
    return "https://www.terraform.io/docs/providers/$provider/index.html"
  }

  //https://www.terraform.io/docs/providers/$PROVIDER/$TYPE/$NAME.html
  private fun getResourceOrDataSourceUrl(identifier: String, type: Char): String {
    val (provider, id) = identifier.split("_", limit = 2)
    return "https://www.terraform.io/docs/providers/$provider/$type/$id"
  }
}
