// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.codeinsight

import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.util.parentOfType
import com.intellij.util.concurrency.annotations.RequiresReadLock
import org.intellij.terraform.config.Constants.HCL_DATASOURCE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_EPHEMERAL_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_MODULE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_OUTPUT_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_PROVIDER_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_RESOURCE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_TERRAFORM_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_VARIABLE_IDENTIFIER
import org.intellij.terraform.config.model.BlockType
import org.intellij.terraform.config.model.HclType
import org.intellij.terraform.config.model.ListType
import org.intellij.terraform.config.model.MapType
import org.intellij.terraform.config.model.Module
import org.intellij.terraform.config.model.ModuleType
import org.intellij.terraform.config.model.PropertyOrBlockType
import org.intellij.terraform.config.model.PropertyType
import org.intellij.terraform.config.model.TfTypeModel
import org.intellij.terraform.config.model.TypeModelProvider
import org.intellij.terraform.config.model.Types
import org.intellij.terraform.config.model.Variable
import org.intellij.terraform.config.model.getType
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLObject
import org.intellij.terraform.hcl.psi.HCLStringLiteral
import org.intellij.terraform.hcl.psi.getNameElementUnquoted
import org.intellij.terraform.opentofu.model.getEncryptionKeyProviderProperties
import org.intellij.terraform.opentofu.model.getEncryptionMethodProperties
import org.intellij.terraform.opentofu.patterns.OpenTofuPatterns
import org.intellij.terraform.stack.component.TfComponentFileType
import org.intellij.terraform.stack.component.TfComponentHelper
import org.intellij.terraform.terragrunt.TerragruntFileType
import org.intellij.terraform.terragrunt.codeinsight.TerragruntUnitHelper
import java.util.Locale

internal object TfModelHelper {
  private val LOG = Logger.getInstance(TfModelHelper::class.java)

  fun getBlockProperties(block: HCLBlock): Map<String, PropertyOrBlockType> {
    val fileType = block.containingFile.originalFile.fileType
    val blockProperties = when (fileType) {
      TerragruntFileType -> TerragruntUnitHelper.getBlockProperties(block)
      TfComponentFileType -> TfComponentHelper.getBlockProperties(block)
      else -> getBlockPropertiesInternal(block)
    }
    return blockProperties.filter { it.value.canBeUsedIn(fileType) }
  }

  private fun getBlockPropertiesInternal(block: HCLBlock): Map<String, PropertyOrBlockType> {
    val type = block.getNameElementUnquoted(0) ?: return emptyMap()
    // Special case for 'backend' blocks, since it's located not in root

    when {
      TfPsiPatterns.Backend.accepts(block) -> return getBackendProperties(block)
      TfPsiPatterns.DynamicBlockContent.accepts(block) -> {
        val dynamic = block.parentOfType<HCLBlock>(withSelf = false) ?: return emptyMap()

        val origin = dynamic.parentOfType<HCLBlock>(withSelf = false) ?: return emptyMap()
        // origin is either ResourceRootBlock, DataSourceRootBlock, ProviderRootBlock or ProvisionerBlock
        val blockType = getBlockProperties(origin)[dynamic.name] as? BlockType ?: return emptyMap()
        return blockType.properties
      }
      TfPsiPatterns.DynamicBlock.accepts(block) -> return TfTypeModel.ResourceDynamic.properties
      TfPsiPatterns.ProvisionerBlock.accepts(block) -> return getProvisionerProperties(block)
      TfPsiPatterns.ResourceLifecycleBlock.accepts(block) -> return TfTypeModel.ResourceLifecycle.properties
      TfPsiPatterns.ResourceConnectionBlock.accepts(block) -> return getConnectionProperties(block)
      OpenTofuPatterns.KeyProviderBlock.accepts(block) -> return getEncryptionKeyProviderProperties(block)
      OpenTofuPatterns.EncryptionMethodBlock.accepts(block) -> return getEncryptionMethodProperties(block)
      block.parent !is PsiFile -> return traverseParentBlockProperties(block, type)
    }

    val props: Map<String, PropertyOrBlockType> = when (type) {
      HCL_PROVIDER_IDENTIFIER -> getProviderProperties(block)
      HCL_RESOURCE_IDENTIFIER -> getResourceProperties(block)
      HCL_DATASOURCE_IDENTIFIER -> getDataSourceProperties(block)
      HCL_EPHEMERAL_IDENTIFIER -> getEphemeralProperties(block)
      HCL_MODULE_IDENTIFIER -> getModuleProperties(block)
      HCL_TERRAFORM_IDENTIFIER -> getTerraformProperties(block)
      else -> TfTypeModel.RootBlocksMap[type]?.properties ?: emptyMap()
    }
    return props
  }

  fun getAbstractBlockType(block: HCLBlock): BlockType? {
    val type = block.getNameElementUnquoted(0) ?: return null
    if (block.parent is PsiFile) {
      return TfTypeModel.RootBlocksMap[type]
    }

    // non-root blocks, match using patterns
    return when {
      TfPsiPatterns.Backend.accepts(block) -> TfTypeModel.AbstractBackend
      TfPsiPatterns.DynamicBlock.accepts(block) -> TfTypeModel.ResourceDynamic
      TfPsiPatterns.DynamicBlockContent.accepts(block) -> TfTypeModel.AbstractResourceDynamicContent
      TfPsiPatterns.ProvisionerBlock.accepts(block) -> TfTypeModel.AbstractResourceProvisioner
      TfPsiPatterns.ResourceLifecycleBlock.accepts(block) -> TfTypeModel.ResourceLifecycle
      TfPsiPatterns.ResourceConnectionBlock.accepts(block) -> TfTypeModel.Connection
      else -> null
    }
  }

  fun getBlockType(block: HCLBlock): HclType? {
    val type = block.getNameElementUnquoted(0) ?: return null

    // non-root blocks, match using patterns
    if (TfPsiPatterns.Backend.accepts(block)) {
      val fallback = TfTypeModel.AbstractBackend
      val name = block.getNameElementUnquoted(1) ?: return fallback
      return TypeModelProvider.getModel(block).getBackendType(name) ?: return fallback
    }
    if (TfPsiPatterns.DynamicBlockContent.accepts(block)) {
      val fallback = TfTypeModel.AbstractResourceDynamicContent
      val dynamic = block.parentOfType<HCLBlock>(withSelf = false) ?: return fallback

      val origin = dynamic.parentOfType<HCLBlock>(withSelf = false) ?: return fallback
      // origin is either ResourceRootBlock, DataSourceRootBlock, ProviderRootBlock or ProvisionerBlock
      return getBlockProperties(origin)[dynamic.name] as? BlockType ?: return fallback
    }
    if (TfPsiPatterns.DynamicBlock.accepts(block)) {
      // TODO: consider more specific content instead of AbstractResourceDynamicContent
      return TfTypeModel.ResourceDynamic
    }
    if (TfPsiPatterns.ProvisionerBlock.accepts(block)) {
      val fallback = TfTypeModel.AbstractResourceProvisioner
      val name = block.getNameElementUnquoted(1) ?: return fallback
      return TypeModelProvider.getModel(block).getProvisionerType(name)
    }
    if (TfPsiPatterns.ResourceLifecycleBlock.accepts(block)) {
      return TfTypeModel.ResourceLifecycle
    }
    if (TfPsiPatterns.ResourceConnectionBlock.accepts(block)) {
      return TfTypeModel.Connection
    }

    if (type !in TfTypeModel.RootBlocksMap.keys || block.parent !is PsiFile) {
      return null
    }

    if (type == HCL_PROVIDER_IDENTIFIER) {
      val fallback = TfTypeModel.AbstractProvider
      val name = block.getNameElementUnquoted(1) ?: return fallback
      return TypeModelProvider.getModel(block).getProviderType(name, block) ?: fallback
    }
    if (type == HCL_RESOURCE_IDENTIFIER) {
      val fallback = TfTypeModel.AbstractResource
      val name = block.getNameElementUnquoted(1) ?: return wrapIfCountForEach(fallback, block)
      return wrapIfCountForEach(TypeModelProvider.getModel(block).getResourceType(name, block) ?: fallback, block)
    }
    if (type == HCL_DATASOURCE_IDENTIFIER) {
      val fallback = TfTypeModel.AbstractDataSource
      val name = block.getNameElementUnquoted(1) ?: return wrapIfCountForEach(fallback, block)
      return wrapIfCountForEach(TypeModelProvider.getModel(block).getDataSourceType(name, block) ?: fallback, block)
    }
    if (type == HCL_MODULE_IDENTIFIER) {
      val fallback = TfTypeModel.Module
      val name = block.getNameElementUnquoted(1) ?: return fallback
      val module = Module.getAsModuleBlock(block) ?: return fallback
      val result = HashMap<String, HclType?>()

      val outputs = module.getDefinedOutputs()
      for (output in outputs) {
        val value = output.`object`?.findProperty("value")?.value
        result[output.name] = value.getType() ?: Types.Any
      }

      // TODO: Should variables be in type?
      val variables = module.getAllVariables()
      for (variable in variables) {
        result[variable.name] = variable.getCombinedType()
      }

      return ModuleType(name, result.map { PropertyType(it.key, type = it.value ?: Types.Any) })
    }
    if (type == HCL_TERRAFORM_IDENTIFIER) {
      return TfTypeModel.Terraform
    }
    if (type == HCL_VARIABLE_IDENTIFIER) {
      val variable = Variable(block)
      return variable.getCombinedType()
    }
    if (type == HCL_OUTPUT_IDENTIFIER) {
      val value = block.`object`?.findProperty("value")?.value ?: return Types.Any
      return value.getType()
    }

    return TfTypeModel.RootBlocksMap[type]
  }

  private fun wrapIfCountForEach(type: BlockType, block: HCLBlock): HclType {
    val obj = block.`object` ?: return type
    if (obj.findProperty("count") != null) {
      return ListType(type)
    }
    else if (obj.findProperty("for_each") != null) {
      return MapType(type)
    }
    return type
  }

  fun traverseParentBlockProperties(block: HCLBlock, type: String): Map<String, PropertyOrBlockType> {
    // TODO: Speedup, remove recursive up-traverse
    val bp = block.parent as? HCLObject ?: return emptyMap()
    val bpp = bp.parent as? HCLBlock ?: return emptyMap()
    val properties = getBlockProperties(bpp)
    val candidate: BlockType? = properties[type] as? BlockType
    return candidate?.properties ?: emptyMap()
  }

  private fun getProviderProperties(block: HCLBlock): Map<String, PropertyOrBlockType> {
    val type = block.getNameElementUnquoted(1)
    val providerType = if (!type.isNullOrBlank()) TypeModelProvider.getModel(block).getProviderType(type, block) else null
    return getPropertiesWithDefaults(TfTypeModel.AbstractProvider, providerType)
  }

  private fun getProvisionerProperties(block: HCLBlock): Map<String, PropertyOrBlockType> {
    val type = block.getNameElementUnquoted(1)
    val provisionerType = if (!type.isNullOrBlank()) TypeModelProvider.getModel(block).getProvisionerType(type) else null
    return getPropertiesWithDefaults(TfTypeModel.AbstractResourceProvisioner, provisionerType)
  }

  private fun getBackendProperties(block: HCLBlock): Map<String, PropertyOrBlockType> {
    val type = block.getNameElementUnquoted(1)
    val backendType = if (!type.isNullOrBlank()) TypeModelProvider.getModel(block).getBackendType(type) else null
    return getPropertiesWithDefaults(TfTypeModel.AbstractBackend, backendType)
  }

  @Suppress("UNUSED_PARAMETER")
  private fun getTerraformProperties(block: HCLBlock): Map<String, PropertyOrBlockType> {
    return TfTypeModel.Terraform.properties
  }

  private fun getConnectionProperties(block: HCLBlock): Map<String, PropertyOrBlockType> {
    val type = block.`object`?.findProperty("type")?.value
    val properties = HashMap<String, PropertyOrBlockType>()
    properties.putAll(TfTypeModel.Connection.properties)
    if (type is HCLStringLiteral) {
      when (type.value.lowercase(Locale.getDefault()).trim()) {
        "ssh" -> properties.putAll(TfTypeModel.ConnectionPropertiesSSH)
        "winrm" -> properties.putAll(TfTypeModel.ConnectionPropertiesWinRM)
        // TODO: Support interpolation resolving
        else -> LOG.warn("Unsupported 'connection' block type '${type.value}'")
      }
    }
    if (type == null) {
      // ssh by default
      properties.putAll(TfTypeModel.ConnectionPropertiesSSH)
    }
    return properties
  }

  fun getResourceProperties(block: HCLBlock): Map<String, PropertyOrBlockType> {
    val type = block.getNameElementUnquoted(1)
    val resourceType = if (!type.isNullOrBlank()) TypeModelProvider.getModel(block).getResourceType(type, block) else null
    return getPropertiesWithDefaults(TfTypeModel.AbstractResource, resourceType)
  }

  private fun getDataSourceProperties(block: HCLBlock): Map<String, PropertyOrBlockType> {
    val type = block.getNameElementUnquoted(1)
    val dataSourceType = if (!type.isNullOrBlank()) TypeModelProvider.getModel(block).getDataSourceType(type, block) else null
    return getPropertiesWithDefaults(TfTypeModel.AbstractDataSource, dataSourceType)
  }

  private fun getEphemeralProperties(block: HCLBlock): Map<String, PropertyOrBlockType> {
    val type = block.getNameElementUnquoted(1)
    val ephemeralType = if (!type.isNullOrBlank()) TypeModelProvider.getModel(block).getEphemeralType(type) else null
    return getPropertiesWithDefaults(TfTypeModel.AbstractEphemeralResource, ephemeralType)
  }

  @RequiresReadLock
  fun getAllTypesForBlockByIdentifier(blockPointer: SmartPsiElementPointer<HCLBlock>): List<BlockType> {
    val block = blockPointer.element ?: return emptyList()
    val typeString = block.getNameElementUnquoted(0) ?: return emptyList()
    val identifier = block.getNameElementUnquoted(1) ?: return emptyList()
    val model = TypeModelProvider.getModel(block)
    val types = when (typeString) {
      HCL_RESOURCE_IDENTIFIER -> model.allResources().filter { it.type == identifier }.toList()
      HCL_DATASOURCE_IDENTIFIER -> model.allDataSources().filter { it.type == identifier }.toList()
      HCL_PROVIDER_IDENTIFIER -> model.allProviders().filter { it.type == identifier }.toList()
      HCL_EPHEMERAL_IDENTIFIER -> model.allEphemeralResources().filter { it.type == identifier }.toList()
      else -> emptyList()
    }
    return types
  }

  private fun getModuleProperties(block: HCLBlock): Map<String, PropertyOrBlockType> {
    val defaults = TfTypeModel.Module.properties
    val module = Module.getAsModuleBlock(block) ?: return defaults
    val variables = module.getAllVariables()
    if (variables.isEmpty()) {
      return defaults
    }

    val properties = HashMap<String, PropertyOrBlockType>()
    properties.putAll(defaults)
    for (v in variables) {
      val hasDefault = v.getDefault() != null
      properties[v.name] = PropertyType(v.name, v.getType() ?: Types.Any, required = !hasDefault)
    }
    return properties
  }

  private fun getPropertiesWithDefaults(defaults: BlockType, origin: BlockType?): Map<String, PropertyOrBlockType> {
    if (origin == null) return defaults.properties
    val result = HashMap<String, PropertyOrBlockType>(defaults.properties.size + origin.properties.size)
    result.putAll(defaults.properties)
    result.putAll(origin.properties)
    return result
  }
}
