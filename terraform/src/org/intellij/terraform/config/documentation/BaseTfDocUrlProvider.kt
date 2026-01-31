// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.documentation

import com.intellij.openapi.application.readAction
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.util.childrenOfType
import org.intellij.terraform.config.Constants.HCL_DATASOURCE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_PROVIDER_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_RESOURCE_IDENTIFIER
import org.intellij.terraform.config.Constants.LATEST_VERSION
import org.intellij.terraform.config.documentation.psi.FAKE_PROVIDER_KEY
import org.intellij.terraform.config.model.ProviderType
import org.intellij.terraform.config.model.TypeModelProvider
import org.intellij.terraform.config.psi.TfDocumentPsi
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLIdentifier
import org.intellij.terraform.hcl.psi.HCLObject
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.hcl.psi.HCLPsiUtil.getRequiredProviderProperty
import org.intellij.terraform.hcl.psi.HCLStringLiteral
import org.intellij.terraform.hcl.psi.common.ProviderDefinedFunction
import org.intellij.terraform.hcl.psi.getNameElementUnquoted
import org.intellij.terraform.hcl.psi.getNameOrText

internal abstract class BaseTfDocUrlProvider {

  internal companion object {
    @JvmStatic
    val RESOURCES: String = "resources"

    @JvmStatic
    val DATASOURCES: String = "data-sources"

    @JvmStatic
    val PROVIDER: String = "provider"

    @JvmStatic
    val FUNCTION: String = "functions"
  }

  internal suspend fun getDocumentationUrl(pointer: SmartPsiElementPointer<PsiElement>): List<String?> {
    val blockData = buildBlockData(pointer)
    return when (blockData.type) {
      HCL_RESOURCE_IDENTIFIER -> listOf(getDocUrl(blockData, RESOURCES))
      HCL_DATASOURCE_IDENTIFIER -> listOf(getDocUrl(blockData, DATASOURCES))
      HCL_PROVIDER_IDENTIFIER -> listOf(getDocUrl(blockData, PROVIDER))
      FUNCTION -> listOf(getDocUrl(blockData, FUNCTION))
      else -> emptyList()
    }
  }

  protected abstract suspend fun getDocUrl(blockData: BlockData, context: String): String?

  protected data class BlockData(val identifier: String?, val type: String?, val parameter: String?, val provider: ProviderData?)

  protected data class ProviderData(val org: String, val provider: String, val version: String)

  private suspend fun buildBlockData(pointer: SmartPsiElementPointer<PsiElement>): BlockData = readAction {
    val element = pointer.element ?: return@readAction BlockData("", "", null, null)
    // These HCL elements are not related with root HCLBlock, that's why we return BlockData directly
    getBlockDataIfProviderFunction(element)?.let { return@readAction it }
    getBlockDataIfRequiredProvider(element)?.let { return@readAction it }

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
      is TfDocumentPsi -> {
        val relevantBlock = getBlockForDocumentationLink(element, element.name)
        relevantBlock?.let { Pair(it, null) }
      }
      else -> Pair(null, null)
    } ?: Pair(null, null)
    val type = block?.getNameElementUnquoted(0) ?: ""
    val identifier = block?.getNameElementUnquoted(1) ?: ""
    val providerData = getProviderData(element, identifier)
    BlockData(identifier, type, parameter, providerData)
  }

  private fun getBlockInfoForIdentifier(hclIdentifier: HCLIdentifier?): Pair<HCLBlock, String?>? {
    val parentBlock = hclIdentifier?.let { getBlockForHclIdentifier(it) }
    val paramName = hclIdentifier?.id
    return parentBlock?.let { Pair(it, paramName) }
  }

  private fun getProviderData(element: PsiElement, identifier: String): ProviderData? {
    val provider = getProvider(identifier, element) ?: return null
    return ProviderData(provider.namespace, provider.type, provider.version.ifEmpty { LATEST_VERSION })
  }

  private fun getProvider(identifier: String, element: PsiElement): ProviderType? {
    element.getUserData(FAKE_PROVIDER_KEY)?.let { return it }

    val model = TypeModelProvider.getModel(element)
    return model.getProviderType(identifier, element)
  }

  private fun getBlockDataIfProviderFunction(element: PsiElement): BlockData? {
    val function = element as? ProviderDefinedFunction<*> ?: element.parent as? ProviderDefinedFunction<*> ?: return null

    val providerName = function.provider.getNameOrText()
    val functionName = function.function.getNameOrText()

    val providerData = getProviderData(function, providerName)
    return BlockData(functionName, FUNCTION, null, providerData)
  }

  private fun getBlockDataIfRequiredProvider(element: PsiElement): BlockData? {
    val providerProperty = element.getRequiredProviderProperty() ?: return null

    val providerName = providerProperty.name
    val providerData = parseToProviderDataFromProperty(providerProperty, providerName)
    return BlockData(providerName, PROVIDER, null, providerData)
  }

  private fun parseToProviderDataFromProperty(providerProperty: HCLProperty, providerName: String): ProviderData? {
    val providerObject = providerProperty.value as? HCLObject ?: return null
    val source = providerObject.findProperty("source")?.value as? HCLStringLiteral ?: return null
    val version = providerObject.findProperty("version")?.value as? HCLStringLiteral ?: return null

    return ProviderData(source.value.takeWhile { it != '/' }, providerName, version.value)
  }
}