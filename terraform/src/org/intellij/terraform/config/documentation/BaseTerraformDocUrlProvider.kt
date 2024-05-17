// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.documentation

import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.service
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.util.childrenOfType
import org.intellij.terraform.config.Constants.HCL_DATASOURCE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_PROVIDER_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_RESOURCE_IDENTIFIER
import org.intellij.terraform.config.Constants.LATEST_VERSION
import org.intellij.terraform.config.model.TypeModelProvider
import org.intellij.terraform.config.model.local.LocalSchemaService
import org.intellij.terraform.config.model.local.LockFileObject
import org.intellij.terraform.config.psi.TerraformDocumentPsi
import org.intellij.terraform.hcl.psi.*


internal abstract class BaseTerraformDocUrlProvider {

  internal companion object {
    @JvmStatic
    val RESOURCES: String = "resources"

    @JvmStatic
    val DATASOURCES: String = "data-sources"

    @JvmStatic
    val PROVIDER: String = "provider"
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
      is HCLProperty -> {
        getBlockInfoForIdentifier(element.childrenOfType<HCLIdentifier>().firstOrNull())
      }
      is HCLIdentifier -> {
        getBlockInfoForIdentifier(element)
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
    val providerData = lockFile?.let { getDataFromLockFile(it, identifier) } ?: getDataFromModel(element, type, identifier)
    BlockData(identifier, type, parameter, providerData)
  }

  private fun getBlockInfoForIdentifier(hclIdentifier: HCLIdentifier?): Pair<HCLBlock, String?>? {
    val parentBlock = hclIdentifier?.let { getBlockForHclIdentifier(it) }
    val paramName = hclIdentifier?.id
    return parentBlock?.let { Pair(it, paramName) }
  }

  private fun getDataFromModel(element: PsiElement,
                               type: String,
                               identifier: String): ProviderData? {
    val provider = if (type in setOf(HCL_RESOURCE_IDENTIFIER, HCL_DATASOURCE_IDENTIFIER)) {
      getProviderName(identifier, type, element)
    } else {
      identifier
    } ?: return null

    val org = getProviderNamespace(provider, element) ?: return null
    return ProviderData(org, provider, LATEST_VERSION)
  }

  private fun getDataFromLockFile(lockFile: PsiFile,
                                  identifier: String): ProviderData? {
    val lockObject = LockFileObject(lockFile)
    val providerId = identifier.substringBefore('_')
    val providerInfo = lockObject.getProviderInfo(providerId) ?: return null
    val provider = providerInfo.name ?: return null
    val org = providerInfo.namespace ?: return null
    val version = providerInfo.version
    return ProviderData(org, provider, version)
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

  private fun getProviderName(identifier: String, resourceType: String, element: PsiElement): String? {
    val model = TypeModelProvider.getModel(element)
    return when (resourceType) {
      HCL_RESOURCE_IDENTIFIER -> model.getResourceType(identifier)?.provider?.type
      HCL_DATASOURCE_IDENTIFIER -> model.getDataSourceType(identifier)?.provider?.type
      else -> null
    }
  }

  private fun findPsiLockFile(element: PsiElement): PsiFile? {
    val project = element.project
    val schemaService = project.service<LocalSchemaService>()
    val lockFile = findContainingVFile(element)?.let { schemaService.findLockFile(it) }
    return lockFile?.let { schemaService.getLockFilePsi(it) }
  }

  private fun findContainingVFile(element: PsiElement): VirtualFile? {
    return element.containingFile.originalFile.virtualFile
           ?: element.containingFile.getUserData(PsiFileFactory.ORIGINAL_FILE)?.virtualFile
  }
}