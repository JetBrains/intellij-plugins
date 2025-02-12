// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.documentation

import com.intellij.codeInsight.CodeInsightBundle
import com.intellij.openapi.util.registry.RegistryManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.parentsOfType
import com.intellij.util.concurrency.annotations.RequiresReadLock
import org.intellij.terraform.config.Constants.HCL_DATASOURCE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_LOCALS_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_MODULE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_OUTPUT_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_PROVIDER_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_RESOURCE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_TERRAFORM_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_VARIABLE_IDENTIFIER
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.config.psi.TfDocumentPsi
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLIdentifier
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.hcl.psi.getNameElementUnquoted
import org.jetbrains.annotations.Nls
import java.util.*

internal val FETCH_TIMEOUT: Int = RegistryManager.getInstance().intValue("terraform.registry.connection.timeout", 1000)
internal val NO_DOC: String = CodeInsightBundle.message("no.documentation.found")
internal const val ROOT_DOC_ANCHOR: String = "provider-docs-content"
internal val parentBlocksForDocs = setOf(HCL_RESOURCE_IDENTIFIER, HCL_DATASOURCE_IDENTIFIER, HCL_PROVIDER_IDENTIFIER)

@RequiresReadLock
internal fun getBlockForHclIdentifier(element: HCLIdentifier): HCLBlock? {
  return element.parentsOfType<HCLBlock>(true).firstOrNull {
    block -> block::class.java != element::class.java && parentBlocksForDocs.contains(block.getNameElementUnquoted(0))
  }
}

@RequiresReadLock
internal fun getBlockForDocumentationLink(element: TfDocumentPsi?, blockTypeLiteral: String): HCLBlock? {
  return element?.parentsOfType<HCLBlock>(false)?.firstOrNull { block -> block.getNameElementUnquoted(1) == blockTypeLiteral }
}

@RequiresReadLock
internal fun getHelpWindowHeader(element: PsiElement?): @Nls String {
  return when (element) {
    is HCLProperty -> {
      val block = element.parentOfType<HCLBlock>(true)
      if (TfPsiPatterns.LocalsRootBlock.accepts(block)) {
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
      calculateBlockDescription(element)
    }
    is TfDocumentPsi -> {
      val block = element.parent
      (block as? HCLBlock)?.let { calculateBlockDescription(it) } ?: HCLBundle.message("terraform.doc.block.type.0", HCLBundle.message("terraform.doc.generic.block"), element.name)
    }
    else -> NO_DOC
  }
}

internal fun calculateBlockDescription(element: HCLBlock): @Nls String {
  val type = element.getNameElementUnquoted(0)
  val name = element.name
  return if (TfPsiPatterns.RootBlock.accepts(element)) {
    if (name == element.getNameElementUnquoted(1)) {
      HCLBundle.message("terraform.doc.block.type.0", type?.replaceFirstChar { it.uppercase(Locale.getDefault()) }, element.name)
    }
    else
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
