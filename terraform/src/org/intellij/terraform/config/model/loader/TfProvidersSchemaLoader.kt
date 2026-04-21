// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model.loader

import com.fasterxml.jackson.databind.node.ObjectNode
import org.intellij.terraform.config.model.ActionType
import org.intellij.terraform.config.model.Argument
import org.intellij.terraform.config.model.BlockType
import org.intellij.terraform.config.model.DataSourceType
import org.intellij.terraform.config.model.EphemeralType
import org.intellij.terraform.config.model.HclTypeImpl
import org.intellij.terraform.config.model.ProviderType
import org.intellij.terraform.config.model.ResourceType
import org.intellij.terraform.config.model.TfFunction
import org.intellij.terraform.config.model.array
import org.intellij.terraform.config.model.obj
import org.intellij.terraform.config.model.string

internal class TfProvidersSchemaLoader : VersionedMetadataLoader {
  private val supportedVersions: Set<String> = setOf("0.1", "0.2", "1.0")

  override fun isSupportedVersion(version: String): Boolean = version in supportedVersions

  override fun isSupportedType(type: String): Boolean = type == "terraform-providers-schema-json"

  override fun load(context: LoadContext, json: ObjectNode, fileName: String) {
    val model = context.model
    val providerSchemas = (json.obj("schemas") ?: json).obj("provider_schemas") ?: return
    for ((n, provider) in providerSchemas.properties().asSequence()) {
      val coordinates = ProviderType.parseCoordinates(n)
      val providerFullName = "${coordinates.namespace}/${coordinates.name}"
      val providerKey = "provider.$providerFullName"
      if (model.loaded.containsKey(providerKey)) {
        TfMetadataLoader.LOG.warn("Provider '$providerFullName' is already loaded from '${model.loaded[providerKey]}'")
        continue
      }
      provider as ObjectNode
      model.loaded[providerKey] = fileName
      val providerInfo = provider.obj("provider")?.let { parseProviderInfo(context, coordinates.name, coordinates.namespace, it, json) }
                         ?: ProviderType(coordinates.name, emptyList(), coordinates.namespace)
      model.providers.add(providerInfo)

      val resources = provider.obj("resource_schemas")
      val dataSources = provider.obj("data_source_schemas")
      if (resources == null && dataSources == null) {
        TfMetadataLoader.LOG.warn("No resources nor data-sources defined for provider '$providerFullName' in file '$fileName'")
      }
      resources?.let { resource -> resource.properties().mapTo(model.resources) { parseResourceInfo(context, it, providerInfo) } }
      dataSources?.let { dataSource -> dataSource.properties().mapTo(model.dataSources) { parseDataSourceInfo(context, it, providerInfo) } }

      provider.obj("ephemeral_resource_schemas")?.let { ephemeralResource ->
        ephemeralResource.properties().mapNotNullTo(model.ephemeralResources) {
          parseEphemeralResourceInfo(context, it, providerInfo)
        }
      }

      provider.obj("action_schemas")?.let { action ->
        action.properties().mapNotNullTo(model.actions) {
          parseActionInfo(context, it, providerInfo)
        }
      }

      provider.obj("functions")?.let { function ->
        function.properties()?.mapNotNullTo(model.providerDefinedFunctions) { parseProviderFunctionInfo(context, it, providerInfo) }
      }
    }
  }

  private fun parseProviderInfo(context: LoadContext, name: String, namespace: String, obj: ObjectNode, file: ObjectNode): ProviderType? {
    val parsed = TfProvidersSchemaParser.parseSchema(context, obj, name) ?: return null
    val providerMetadata = TfProvidersSchemaParser.parseMetadata(file.obj("metadata"), name, namespace)
    return ProviderType(providerMetadata.name,
                        parsed.properties.values.toList(),
                        providerMetadata.namespace,
                        providerMetadata.tier,
                        providerMetadata.version,
                        parsed)
  }

  private fun <T> parseBlockInfo(
    context: LoadContext,
    entry: Map.Entry<String, Any?>,
    factory: (String, BlockType) -> T,
  ): T? {
    val name = entry.key.pool(context)
    val objectNode = entry.value as? ObjectNode ?: return null
    val block = TfProvidersSchemaParser.parseSchema(context, objectNode, name) ?: return null
    return factory(name, block)
  }

  private fun parseResourceInfo(context: LoadContext, entry: Map.Entry<String, Any?>, info: ProviderType): ResourceType =
    parseBlockInfo(context, entry) { name, block ->
      ResourceType(name, info, block.properties.values.toList(), block)
    } ?: throw IllegalArgumentException("can't parse schema parseResourceInfo ${entry.key}, entry = $entry")

  private fun parseDataSourceInfo(context: LoadContext, entry: Map.Entry<String, Any?>, info: ProviderType): DataSourceType =
    parseBlockInfo(context, entry) { name, block ->
      DataSourceType(name, info, block.properties.values.toList(), block)
    } ?: throw IllegalArgumentException("can't parse schema parseDataSourceInfo ${entry.key}, entry = $entry")

  private fun parseEphemeralResourceInfo(context: LoadContext, entry: Map.Entry<String, Any?>, info: ProviderType): EphemeralType? =
    parseBlockInfo(context, entry) { name, block ->
      EphemeralType(name, info, block)
    }

  private fun parseActionInfo(context: LoadContext, entry: Map.Entry<String, Any?>, info: ProviderType): ActionType? =
    parseBlockInfo(context, entry) { name, block ->
      ActionType(name, info, block)
    }

  private fun parseProviderFunctionInfo(context: LoadContext, entry: Map.Entry<String, Any?>, info: ProviderType): TfFunction? {
    val name = entry.key.pool(context)
    val objectNode = entry.value as? ObjectNode ?: return null

    val description = objectNode.string("description")
    val returnType = objectNode.string("return_type").orEmpty()
    val parameters = objectNode.array("parameters")
      ?.filterIsInstance<ObjectNode>()
      ?.map { Argument(HclTypeImpl(it.string("type").orEmpty()), it.string("name")) }
      ?.toTypedArray().orEmpty()

    return TfFunction(
      name,
      HclTypeImpl(returnType),
      arguments = parameters,
      description = description,
      providerType = info.type
    )
  }

}
