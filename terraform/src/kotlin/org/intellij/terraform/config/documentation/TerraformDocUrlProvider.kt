// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.documentation

import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.service
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.SyntaxTraverser
import org.intellij.terraform.config.codeinsight.ModelHelper
import org.intellij.terraform.config.model.TypeModelProvider
import org.intellij.terraform.config.model.local.LocalSchemaService
import org.intellij.terraform.config.psi.TerraformDocumentPsi
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLIdentifier
import org.intellij.terraform.hcl.psi.HCLObject
import org.intellij.terraform.hcl.psi.getNameElementUnquoted


internal abstract class TerraformDocUrlProvider {

  companion object {
    const val VERSION: String = "version"
    const val LATEST_VERSION: String = "latest"
    const val TERRAFORM_DOMAIN: String = "terraform.io"
    const val REGISTRY_DOMAIN: String = "registry.terraform.io"

    const val RESOURCES: String = "resources"
    const val DATASOURCES: String = "data-sources"
    const val PROVIDER: String = "provider"
  }

  suspend fun getDocumentationUrl(element: PsiElement): List<String?> {
    val (block, parameter) = getBlockAndParam(element) ?: return emptyList()
    val identifier = readAction { block.getNameElementUnquoted(1) } ?: return emptyList()
    return when (readAction { block.getNameElementUnquoted(0) }) {
      "resource" -> listOf(getResourceOrDataSourceDocUrl(identifier, RESOURCES, block, parameter))
      "data" -> listOf(getResourceOrDataSourceDocUrl(identifier, DATASOURCES, block, parameter))
      "provider" -> listOf(getProviderDocUrl(identifier, block, parameter))
      else -> emptyList()
    }
  }

  protected abstract suspend fun getProviderDocUrl(provider: String, element: PsiElement, property: String? = null): String?

  protected abstract suspend fun getResourceOrDataSourceDocUrl(identifier: String, type: String, element: PsiElement, property: String? = null): String?

  protected data class ProviderData(val org: String, val provider: String, val version: String)

  protected suspend fun buildProviderInfo(element: PsiElement, resourceName: String, resourceType: String): ProviderData? {
    val providerDescription = findPsiLockFile(element)?.let { findProviderDescription(it) }
    val provider = if (resourceType in setOf(RESOURCES, DATASOURCES)) {
      if (providerDescription != null) {
        getProvider(providerDescription)
      } else {
        getProviderNameFromModel(resourceName, resourceType, element)
      }
    } else {
      resourceName
    } ?: return null

    val org = if (providerDescription != null) {
      getProviderNamespace(providerDescription)
    } else {
      getProviderNamespaceFromModel(provider, element)
    } ?: return null
    val version = getProviderVersion(providerDescription)

    return ProviderData(org, provider, version)
  }

  private suspend fun getProvider(lockBlock: HCLBlock?): String? = readAction {
    val providerUrl =
      lockBlock?.name?.split("/").takeIf { it?.size == 3 && it[0] == REGISTRY_DOMAIN || it?.get(0) == TERRAFORM_DOMAIN }?.let { it[2] }
    providerUrl
  }

  private suspend fun getProviderNamespace(lockBlock: HCLBlock?): String? = readAction {
    val providerUrl =
      lockBlock?.name?.split("/").takeIf { it?.size == 3 && it[0] == REGISTRY_DOMAIN || it?.get(0) == TERRAFORM_DOMAIN }?.let { it[1] }
    providerUrl
  }

  private suspend fun getProviderVersion(lockBlock: HCLBlock?): String = readAction {
    if (lockBlock?.`object` != null && lockBlock.`object` is HCLObject) {
      val providerVersion = (lockBlock.`object` as HCLObject).propertyList.firstOrNull { it.name == VERSION }?.value?.text
      StringUtil.unquoteString(providerVersion ?: LATEST_VERSION)
    }
    else {
      LATEST_VERSION
    }
  }

  protected fun getResourceId(identifier: String): String {
    val stringList = identifier.split("_", limit = 2)
    val id = if (stringList.size < 2) identifier else stringList[1]
    return id
  }

  private suspend fun getProviderNamespaceFromModel(identifier: String, element: PsiElement): String? = readAction {
    val model = TypeModelProvider.getModel(element)
    model.getProviderType(identifier)?.namespace
  }


  private suspend fun getProviderNameFromModel(identifier: String, resourceType: String, element: PsiElement): String? = readAction {
    val model = TypeModelProvider.getModel(element)
    when (resourceType) {
      RESOURCES -> model.getResourceType(identifier)?.provider?.type
      DATASOURCES -> model.getDataSourceType(identifier)?.provider?.type
      else -> null
    }
  }

  private suspend fun findProviderDescription(it: PsiFile): HCLBlock? = readAction {
    SyntaxTraverser.psiTraverser(it)
      .filter(HCLBlock::class.java).firstOrNull { it.nameElements[0].text == PROVIDER }
  }

  private suspend fun findPsiLockFile(element: PsiElement): PsiFile? = readAction {
    val project = element.project
    val schemaService = project.service<LocalSchemaService>()
    val vFile = element.containingFile.originalFile.virtualFile
    val lockFile = schemaService.findLockFile(vFile)
    val psiLockFile = lockFile?.let { PsiManager.getInstance(project).findFile(it) }
    psiLockFile
  }

  private suspend fun getBlockAndParam(element: PsiElement?): Pair<HCLBlock, String?>? = readAction {
    when (element) {
      is HCLBlock -> {
        Pair(element, null)
      }
      is HCLIdentifier -> {
        val parentBlock = getBlockForHclIdentifier(element)
        val paramName = parentBlock?.let { ModelHelper.getBlockProperties(it)[element.id]?.name }
        parentBlock?.let { Pair(it, paramName) }
      }
      is TerraformDocumentPsi -> {
        val relevantBlock = getBlockForDocumentationLink(element, element.name)
        relevantBlock?.let { Pair(it, null) }
      }
      else -> null
    }
  }

}