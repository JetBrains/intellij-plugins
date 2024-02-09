// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config

import com.intellij.openapi.components.service
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.SyntaxTraverser
import org.intellij.terraform.config.model.TypeModelProvider
import org.intellij.terraform.config.model.local.LocalSchemaService
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLObject


object TerraformDocumentationUrlProvider {

  const val RESOURCES: String = "resources"
  const val DATASOURCES: String = "data-sources"
  private const val PROVIDER = "provider"
  private const val VERSION = "version"
  private const val LATEST_VERSION = "latest"

  @JvmStatic
  fun getProviderUrl(provider: String, element: PsiElement, property: String? = null): String {
    val lockBlock = findPsiLockFile(element)?.let { findProviderDescription(it) }
    val org = if (lockBlock != null) getProviderNamespace(lockBlock) else getProviderNamespaceFromModel(provider, element) ?: return ""
    val version = getProviderVersion(lockBlock)
    return "https://registry.terraform.io/providers/${org}/${provider}/${version}/docs" + (property?.let { "#$it" } ?: "")
  }

  @JvmStatic
  fun getResourceOrDataSourceUrl(identifier: String, type: String, element: PsiElement, property: String? = null): String {
    val lockBlock = findPsiLockFile(element)?.let { findProviderDescription(it) }
    val provider = if (lockBlock != null) getProvider(lockBlock) else getProviderNameFromModel(identifier, type, element) ?: return ""
    val org = if (lockBlock != null) getProviderNamespace(lockBlock) else getProviderNamespaceFromModel(provider ?: "", element)
    val version = getProviderVersion(lockBlock)
    val id = getResourceId(identifier)
    return "https://registry.terraform.io/providers/${org}/${provider}/${version}/docs/${type}/${id}" + (property?.let { "#$it" } ?: "")
  }

  private fun getProvider(lockBlock: HCLBlock?): String? {
    val providerUrl =
      lockBlock?.name?.split("/").takeIf { it?.size == 3 && it[0] == "registry.terraform.io" || it?.get(0) == "terraform.io" }?.let { it[2] }
    return providerUrl
  }

  private fun getProviderNamespace(lockBlock: HCLBlock?): String? {
    val providerUrl =
      lockBlock?.name?.split("/").takeIf { it?.size == 3 && it[0] == "registry.terraform.io" || it?.get(0) == "terraform.io" }?.let { it[1] }
    return providerUrl
  }

  private fun getProviderVersion(lockBlock: HCLBlock?): String {
    if (lockBlock?.`object` == null || lockBlock.`object` !is HCLObject) return LATEST_VERSION
    val providerVersion = (lockBlock.`object` as HCLObject).propertyList
                            .firstOrNull { it.name == VERSION }?.value?.text
    return StringUtil.unquoteString(providerVersion ?: LATEST_VERSION)
  }

  private fun getResourceId(identifier: String): String {
    val stringList = identifier.split("_", limit = 2)
    val id = if (stringList.size < 2) identifier else stringList[1]
    return id
  }

  private fun getProviderNamespaceFromModel(identifier: String, element: PsiElement): String? {
    val model = TypeModelProvider.getModel(element)
    return model.getProviderType(identifier)?.namespace
  }


  private fun getProviderNameFromModel(identifier: String, resourceType: String, element: PsiElement): String? {
    val model = TypeModelProvider.getModel(element)
    return when (resourceType) {
      RESOURCES -> model.getResourceType(identifier)?.provider?.type
      DATASOURCES -> model.getDataSourceType(identifier)?.provider?.type
      else -> null
    }
  }

  private fun findProviderDescription(it: PsiFile) = SyntaxTraverser.psiTraverser(it)
    .filter(HCLBlock::class.java).firstOrNull { it.nameElements[0].text == PROVIDER }

  private fun findPsiLockFile(element: PsiElement): PsiFile? {
    val project = element.project
    val schemaService = project.service<LocalSchemaService>()
    val vFile = element.containingFile.originalFile.virtualFile
    val lockFile = schemaService.findLockFile(vFile)
    val psiLockFile = lockFile?.let { PsiManager.getInstance(project).findFile(it) }
    return psiLockFile
  }

}