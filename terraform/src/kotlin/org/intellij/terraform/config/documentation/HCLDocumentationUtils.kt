// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.documentation

import com.intellij.codeInsight.CodeInsightBundle
import com.intellij.openapi.util.registry.RegistryManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.parentsOfType
import com.intellij.util.concurrency.annotations.RequiresReadLock
import org.intellij.terraform.config.patterns.TerraformPatterns
import org.intellij.terraform.config.psi.TerraformDocumentPsi
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLIdentifier
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.hcl.psi.getNameElementUnquoted
import org.jetbrains.annotations.Nls

internal val FETCH_TIMEOUT: Int = RegistryManager.getInstance().intValue("terraform.registry.connection.timeout", 1000)
internal val NO_DOC: String = CodeInsightBundle.message("no.documentation.found")

internal const val HCL_RESOURCE_IDENTIFIER: String = "resource"
internal const val HCL_DATASOURCE_IDENTIFIER: String = "data"
internal const val HCL_PROVIDER_IDENTIFIER: String = "provider"
internal const val HCL_MODULE_IDENTIFIER: String = "module"
internal const val HCL_VARIABLE_IDENTIFIER: String = "variable"
internal const val HCL_OUTPUT_IDENTIFIER: String = "output"
internal const val HCL_TERRAFORM_IDENTIFIER: String = "terraform"
internal const val HCL_LOCALS_IDENTIFIER: String = "locals"


@RequiresReadLock
internal fun getBlockForHclIdentifier(element: HCLIdentifier): HCLBlock? {
  return element.parentsOfType<HCLBlock>(true).firstOrNull { block -> block.name != element.id }
}

@RequiresReadLock
internal fun getBlockForDocumentationLink(element: TerraformDocumentPsi?, blockTypeLiteral: String): HCLBlock? {
  return element?.parentsOfType<HCLBlock>(false)?.firstOrNull { block -> block.getNameElementUnquoted(1) == blockTypeLiteral }
}

@RequiresReadLock
internal fun getHelpWindowHeader(element: PsiElement?): @Nls String {
  return when (element) {
    is HCLProperty -> {
      val block = element.parentOfType<HCLBlock>(true)
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
          HCL_MODULE_IDENTIFIER -> HCLBundle.message("terraform.doc.module.0", name) // todo: add short source
          HCL_VARIABLE_IDENTIFIER -> HCLBundle.message("terraform.doc.input.variable.0", name) // todo: add short type
          HCL_OUTPUT_IDENTIFIER -> HCLBundle.message("terraform.doc.output.value.0", name) // todo: add short type
          HCL_PROVIDER_IDENTIFIER -> HCLBundle.message("terraform.doc.provider.0", name)
          HCL_RESOURCE_IDENTIFIER -> HCLBundle.message("terraform.doc.resource.0.of.type.1", name, element.getNameElementUnquoted(1))
          HCL_DATASOURCE_IDENTIFIER -> HCLBundle.message("terraform.doc.data.source.0.of.type.1", name, element.getNameElementUnquoted(1))
          HCL_TERRAFORM_IDENTIFIER -> HCLBundle.message("terraform.doc.terraform.configuration")
          HCL_LOCALS_IDENTIFIER -> HCLBundle.message("terraform.doc.local.values")
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
