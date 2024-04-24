// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.codeinsight

import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiFile
import org.intellij.terraform.config.Constants.HCL_DATASOURCE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_MODULE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_OUTPUT_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_PROVIDER_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_RESOURCE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_TERRAFORM_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_VARIABLE_IDENTIFIER
import org.intellij.terraform.config.model.*
import org.intellij.terraform.config.patterns.TerraformPatterns
import org.intellij.terraform.hcl.psi.*
import java.util.*

object ModelHelper {
  private val LOG = Logger.getInstance(ModelHelper::class.java)

  fun getBlockProperties(block: HCLBlock): Map<String, PropertyOrBlockType> {
    val type = block.getNameElementUnquoted(0) ?: return emptyMap()
    // Special case for 'backend' blocks, since it's located not in root
    if (TerraformPatterns.Backend.accepts(block)) {
      return getBackendProperties(block)
    }
    if (TerraformPatterns.DynamicBlockContent.accepts(block)) {
      val dynamic = block.getParent(HCLBlock::class.java, true) ?: return emptyMap()
      assert(TerraformPatterns.DynamicBlock.accepts(dynamic))
      val origin = dynamic.getParent(HCLBlock::class.java, true) ?: return emptyMap()
      // origin is either ResourceRootBlock, DataSourceRootBlock, ProviderRootBlock or ProvisionerBlock
      val blockType = getBlockProperties(origin)[dynamic.name] as? BlockType ?: return emptyMap()
      return blockType.properties
    }
    if (TerraformPatterns.DynamicBlock.accepts(block)) {
      return TypeModel.ResourceDynamic.properties
    }
    if (TerraformPatterns.ProvisionerBlock.accepts(block)) {
      return getProvisionerProperties(block)
    }
    if (TerraformPatterns.ResourceLifecycleBlock.accepts(block)) {
      return TypeModel.ResourceLifecycle.properties
    }
    if (TerraformPatterns.ResourceConnectionBlock.accepts(block)) {
      return getConnectionProperties(block)
    }

    if (block.parent !is PsiFile) {
      return getModelBlockProperties(block, type)
    }
    val props: Map<String, PropertyOrBlockType> = when (type) {
      HCL_PROVIDER_IDENTIFIER -> getProviderProperties(block)
      HCL_RESOURCE_IDENTIFIER -> getResourceProperties(block)
      HCL_DATASOURCE_IDENTIFIER -> getDataSourceProperties(block)
      HCL_MODULE_IDENTIFIER -> getModuleProperties(block)
      HCL_TERRAFORM_IDENTIFIER -> getTerraformProperties(block)
      else -> TypeModel.RootBlocksMap[type]?.properties ?: emptyMap()
    }
    return props
  }

  fun getAbstractBlockType(block: HCLBlock): BlockType? {
    val type = block.getNameElementUnquoted(0) ?: return null
    if (block.parent is PsiFile) {
      return TypeModel.RootBlocksMap[type]
    }

    // non-root blocks, match using patterns
    if (TerraformPatterns.Backend.accepts(block)) {
      return TypeModel.AbstractBackend
    }
    if (TerraformPatterns.DynamicBlock.accepts(block)) {
      return TypeModel.ResourceDynamic
    }
    if (TerraformPatterns.DynamicBlockContent.accepts(block)) {
      return TypeModel.AbstractResourceDynamicContent
    }
    if (TerraformPatterns.ProvisionerBlock.accepts(block)) {
      return TypeModel.AbstractResourceProvisioner
    }
    if (TerraformPatterns.ResourceLifecycleBlock.accepts(block)) {
      return TypeModel.ResourceLifecycle
    }
    if (TerraformPatterns.ResourceConnectionBlock.accepts(block)) {
      return TypeModel.Connection
    }
    return null
  }

  fun getBlockType(block: HCLBlock): Type? {
    val type = block.getNameElementUnquoted(0) ?: return null

    // non-root blocks, match using patterns
    if (TerraformPatterns.Backend.accepts(block)) {
      val fallback = TypeModel.AbstractBackend
      val name = block.getNameElementUnquoted(1) ?: return fallback
      return TypeModelProvider.getModel(block).getBackendType(name) ?: return fallback
    }
    if (TerraformPatterns.DynamicBlockContent.accepts(block)) {
      val fallback = TypeModel.AbstractResourceDynamicContent
      val dynamic = block.getParent(HCLBlock::class.java, true) ?: return fallback
      assert(TerraformPatterns.DynamicBlock.accepts(dynamic))
      val origin = dynamic.getParent(HCLBlock::class.java, true) ?: return fallback
      // origin is either ResourceRootBlock, DataSourceRootBlock, ProviderRootBlock or ProvisionerBlock
      return getBlockProperties(origin)[dynamic.name] as? BlockType ?: return fallback
    }
    if (TerraformPatterns.DynamicBlock.accepts(block)) {
      // TODO: consider more specific content instead of AbstractResourceDynamicContent
      return TypeModel.ResourceDynamic
    }
    if (TerraformPatterns.ProvisionerBlock.accepts(block)) {
      val fallback = TypeModel.AbstractResourceProvisioner
      val name = block.getNameElementUnquoted(1) ?: return fallback
      return TypeModelProvider.getModel(block).getProvisionerType(name)
    }
    if (TerraformPatterns.ResourceLifecycleBlock.accepts(block)) {
      return TypeModel.ResourceLifecycle
    }
    if (TerraformPatterns.ResourceConnectionBlock.accepts(block)) {
      return TypeModel.Connection
    }

    if (type !in TypeModel.RootBlocksMap.keys || block.parent !is PsiFile) {
      return null
    }

    if (type == HCL_PROVIDER_IDENTIFIER) {
      val fallback = TypeModel.AbstractProvider
      val name = block.getNameElementUnquoted(1) ?: return fallback
      return TypeModelProvider.getModel(block).getProviderType(name)
    }
    if (type == HCL_RESOURCE_IDENTIFIER) {
      val fallback = TypeModel.AbstractResource
      val name = block.getNameElementUnquoted(1) ?: return wrapIfCountForEach(fallback, block)
      return wrapIfCountForEach(TypeModelProvider.getModel(block).getResourceType(name) ?: fallback, block)
    }
    if (type == HCL_DATASOURCE_IDENTIFIER) {
      val fallback = TypeModel.AbstractDataSource
      val name = block.getNameElementUnquoted(1) ?: return wrapIfCountForEach(fallback, block)
      return wrapIfCountForEach(TypeModelProvider.getModel(block).getDataSourceType(name) ?: fallback, block)
    }
    if (type == HCL_MODULE_IDENTIFIER) {
      val fallback = TypeModel.Module
      val name = block.getNameElementUnquoted(1) ?: return fallback
      val module = Module.getAsModuleBlock(block) ?: return fallback
      val result = HashMap<String, Type?>()

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
      return TypeModel.Terraform
    }
    if (type == HCL_VARIABLE_IDENTIFIER) {
      val variable = Variable(block)
      return variable.getCombinedType()
    }
    if (type == HCL_OUTPUT_IDENTIFIER) {
      val value = block.`object`?.findProperty("value")?.value ?: return Types.Any
      return value.getType()
    }

    return TypeModel.RootBlocksMap[type]
  }

  private fun wrapIfCountForEach(type: BlockType, block: HCLBlock): Type {
    val obj = block.`object` ?: return type
    if (obj.findProperty("count") != null) {
      return ListType(type)
    }
    else if (obj.findProperty("for_each") != null) {
      return MapType(type)
    }
    return type
  }

  private fun getModelBlockProperties(block: HCLBlock, type: String): Map<String, PropertyOrBlockType> {
    // TODO: Speedup, remove recursive up-traverse
    val bp = block.parent as? HCLObject ?: return emptyMap()
    val bpp = bp.parent as? HCLBlock ?: return emptyMap()
    val properties = getBlockProperties(bpp)
    val candidate: BlockType? = properties[type] as? BlockType
    return candidate?.properties ?: emptyMap()
  }

  private fun getProviderProperties(block: HCLBlock): Map<String, PropertyOrBlockType> {
    val type = block.getNameElementUnquoted(1)
    val providerType = if (type != null) TypeModelProvider.getModel(block).getProviderType(type) else null
    return getPropertiesWithDefaults(TypeModel.AbstractProvider, providerType)
  }

  private fun getProvisionerProperties(block: HCLBlock): Map<String, PropertyOrBlockType> {
    val type = block.getNameElementUnquoted(1)
    val provisionerType = if (type != null) TypeModelProvider.getModel(block).getProvisionerType(type) else null
    return getPropertiesWithDefaults(TypeModel.AbstractResourceProvisioner, provisionerType)
  }

  private fun getBackendProperties(block: HCLBlock): Map<String, PropertyOrBlockType> {
    val type = block.getNameElementUnquoted(1)
    val backendType = if (type != null) TypeModelProvider.getModel(block).getBackendType(type) else null
    return getPropertiesWithDefaults(TypeModel.AbstractBackend, backendType)
  }

  @Suppress("UNUSED_PARAMETER")
  private fun getTerraformProperties(block: HCLBlock): Map<String, PropertyOrBlockType> {
    return TypeModel.Terraform.properties
  }

  private fun getConnectionProperties(block: HCLBlock): Map<String, PropertyOrBlockType> {
    val type = block.`object`?.findProperty("type")?.value
    val properties = HashMap<String, PropertyOrBlockType>()
    properties.putAll(TypeModel.Connection.properties)
    if (type is HCLStringLiteral) {
      when (type.value.lowercase(Locale.getDefault()).trim()) {
        "ssh" -> properties.putAll(TypeModel.ConnectionPropertiesSSH)
        "winrm" -> properties.putAll(TypeModel.ConnectionPropertiesWinRM)
        // TODO: Support interpolation resolving
        else -> LOG.warn("Unsupported 'connection' block type '${type.value}'")
      }
    }
    if (type == null) {
      // ssh by default
      properties.putAll(TypeModel.ConnectionPropertiesSSH)
    }
    return properties
  }

  fun getResourceProperties(block: HCLBlock): Map<String, PropertyOrBlockType> {
    val type = block.getNameElementUnquoted(1)
    val resourceType = if (type != null) TypeModelProvider.getModel(block).getResourceType(type) else null
    return getPropertiesWithDefaults(TypeModel.AbstractResource, resourceType)
  }

  private fun getDataSourceProperties(block: HCLBlock): Map<String, PropertyOrBlockType> {
    val type = block.getNameElementUnquoted(1)
    val dataSourceType = if (type != null) TypeModelProvider.getModel(block).getDataSourceType(type) else null
    return getPropertiesWithDefaults(TypeModel.AbstractDataSource, dataSourceType)
  }

  private fun getModuleProperties(block: HCLBlock): Map<String, PropertyOrBlockType> {
    val defaults = TypeModel.Module.properties
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
