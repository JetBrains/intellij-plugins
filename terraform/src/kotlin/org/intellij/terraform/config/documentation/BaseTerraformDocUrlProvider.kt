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


internal abstract class BaseTerraformDocUrlProvider {

  protected companion object {
    protected const val VERSION: String = "version"
    protected const val TERRAFORM_DOMAIN: String = "terraform.io"
    protected const val REGISTRY_DOMAIN: String = "registry.terraform.io"

    @JvmStatic
    protected val LATEST_VERSION: String = "latest"
    @JvmStatic
    protected val RESOURCES: String = "resources"
    @JvmStatic
    protected val DATASOURCES: String = "data-sources"
    @JvmStatic
    protected val PROVIDER: String = "provider"
  }

  internal suspend fun getDocumentationUrl(element: PsiElement?): List<String?> {
    element ?: return emptyList()

    val blockData = buildBlockData(element)
    return when (blockData.type) {
      HCL_RESOURCE_IDENTIFIER -> listOf(getDocUrl(blockData, RESOURCES))
      HCL_DATASOURCE_IDENTIFIER -> listOf(getDocUrl(blockData, DATASOURCES))
      HCL_PROVIDER_IDENTIFIER -> listOf(getDocUrl(blockData, PROVIDER))
      else -> emptyList()
    }
  }

  protected abstract suspend fun getDocUrl(blockData: BlockData, context: String): String?

  protected data class BlockData(val identifier: String?, val type: String?, val parameter: String?, val provider: ProviderData?)

  protected data class ProviderData(val org: String, val provider: String, val version: String)

  private suspend fun buildBlockData(element: PsiElement): BlockData = readAction {
    val (block, parameter) = when (element) {
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
      else -> Pair(null, null)
    } ?: Pair(null, null)
    val type = block?.getNameElementUnquoted(0) ?: ""
    val identifier = block?.getNameElementUnquoted(1) ?: ""

    val lockFile = findPsiLockFile(element)
    val providerData = lockFile?.let { getDataFromLockFile(it, type, identifier) } ?: getDataFromModel(element, type, identifier)

    BlockData(identifier, type, parameter, providerData)
  }

  private fun getDataFromModel(element: PsiElement,
                               type: String,
                               identifier: String): ProviderData? {
    val provider = if (type in setOf(HCL_RESOURCE_IDENTIFIER, HCL_DATASOURCE_IDENTIFIER)) {
      getProvider(identifier, type, element)
    }
    else {
      identifier
    } ?: return null

    val org = getProviderNamespace(provider, element) ?: return null
    val version = getProviderVersion(null)
    return ProviderData(org, provider, version)
  }

  private fun getDataFromLockFile(lockFile: PsiFile,
                                  type: String,
                                  identifier: String): ProviderData? {
    val providerDescription = findProviderDescription(lockFile, identifier)
    val provider = if (type in setOf(HCL_RESOURCE_IDENTIFIER, HCL_DATASOURCE_IDENTIFIER)) {
      getProvider(providerDescription)
    }
    else {
      identifier
    } ?: return null
    val org = getProviderNamespace(providerDescription) ?: return null
    val version = getProviderVersion(providerDescription)
    return ProviderData(org, provider, version)
  }

  private fun getProvider(lockBlock: HCLBlock?): String? {
    val providerUrl =
      lockBlock?.name?.split("/").takeIf { it?.size == 3 && it[0] == REGISTRY_DOMAIN || it?.get(0) == TERRAFORM_DOMAIN }?.let { it[2] }
    return providerUrl
  }

  private fun getProviderNamespace(lockBlock: HCLBlock?): String? {
    val providerUrl =
      lockBlock?.name?.split("/").takeIf { it?.size == 3 && it[0] == REGISTRY_DOMAIN || it?.get(0) == TERRAFORM_DOMAIN }?.let { it[1] }
    return providerUrl
  }

  private fun getProviderVersion(lockBlock: HCLBlock?): String  {
    return if (lockBlock?.`object` != null && lockBlock.`object` is HCLObject) {
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

  private fun getProviderNamespace(identifier: String, element: PsiElement): String? {
    val model = TypeModelProvider.getModel(element)
    return model.getProviderType(identifier)?.namespace
  }


  private fun getProvider(identifier: String, resourceType: String, element: PsiElement): String? {
    val model = TypeModelProvider.getModel(element)
    return when (resourceType) {
      HCL_RESOURCE_IDENTIFIER -> model.getResourceType(identifier)?.provider?.type
      HCL_DATASOURCE_IDENTIFIER -> model.getDataSourceType(identifier)?.provider?.type
      else -> null
    }
  }

  private fun findProviderDescription(it: PsiFile, identifier: String): HCLBlock? {
    return SyntaxTraverser.psiTraverser(it)
      .filter(HCLBlock::class.java).filter{
        it.nameElements[0].text == PROVIDER && getProvider(it) == identifier
      }.firstOrNull()
  }

  private fun findPsiLockFile(element: PsiElement): PsiFile?  {
    val project = element.project
    val schemaService = project.service<LocalSchemaService>()
    val vFile = element.containingFile.originalFile.virtualFile ?: return null
    val lockFile = schemaService.findLockFile(vFile)
    val psiLockFile = lockFile?.let { PsiManager.getInstance(project).findFile(it) }
    return psiLockFile
  }

}