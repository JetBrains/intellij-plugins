// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model.loader

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.intellij.util.asSafely
import org.intellij.terraform.config.Constants
import org.intellij.terraform.config.model.*
import java.util.*

object TfBaseLoader {
  /*
    type schema struct {
      Version uint64 `json:"version"`
      Block   *block `json:"block,omitempty"`
    }
   */
  fun parseSchema(context: LoadContext, obj: ObjectNode, name: String): Pair<BlockType, Int>? {
    val version = obj.number("version")!!.toInt()
    val block = obj.obj("block") ?: return null
    return parseBlock(context, block, name, null) to version
  }

  internal fun parseMetadata(obj: ObjectNode?, name: String, namespace: String): ProviderMetadata {
    val attrs = obj?.obj("${namespace}/${name}".lowercase())?.obj("attributes") ?: return ProviderMetadata()
    return ProviderMetadata(
      attrs.string("name") ?: "",
      attrs.string("namespace") ?: "",
      attrs.string("full-name") ?: "",
      attrs.string("source") ?: "",
      attrs.string("version") ?: "",
      attrs.string("tier")?.let { ProviderTier.findByLabel(it) } ?: ProviderTier.TIER_NONE
    )
  }

  /*
type attribute struct {
AttributeType       json.RawMessage `json:"type,omitempty"`
AttributeNestedType *nestedType     `json:"nested_type,omitempty"`
Description         string          `json:"description,omitempty"`
DescriptionKind     string          `json:"description_kind,omitempty"`
Deprecated          bool            `json:"deprecated,omitempty"`
Required            bool            `json:"required,omitempty"`
Optional            bool            `json:"optional,omitempty"`
Computed            bool            `json:"computed,omitempty"`
Sensitive           bool            `json:"sensitive,omitempty"`
}
 */
  private fun parseAttribute(context: LoadContext, name: String, value: Any?, fqnPrefix: String): PropertyOrBlockType {
    if (value !is ObjectNode) {
      error("Right part of schema element (field parameters) should be object")
    }
    if (name == Constants.TIMEOUTS) {
      throw IllegalStateException(Constants.TIMEOUTS + " not expected here")
    }

    val fqn = "$fqnPrefix.$name"

    val type: Type
    val rawType = value.get("type")
    if (rawType != null) {
      type = parseType(context, rawType)
    }
    else {
      val nested_type = value.obj("nested_type")
      type = parseAttrNestedType(context, nested_type!!, fqn)
    }


    val description = value.string("description")
    val description_kind = value.string("description_kind") ?: "plain"

    val deprecated = value.boolean("deprecated") ?: false
    val required = value.boolean("required") ?: false
    val optional = value.boolean("optional") ?: false
    val computed = value.boolean("computed") ?: false
    val sensitive = value.boolean("sensitive") ?: false

    val additional = context.model.external[fqn]

    if (isAttributeAsBlock(type)) {
      return BlockType(
        name.pool(context),
        description = description?.pool(context),
        description_kind = description_kind.pool(context),
        optional = optional,
        required = required,
        computed = computed,
        deprecated = if (deprecated) "DEPRECATED" else null,
        properties = type.asSafely<ContainerType<*>>()?.elements
          ?.asSafely<ObjectType>()?.elements?.mapValues { (k, v) -> PropertyType(k, type = v ?: Types.Any) }.orEmpty()
      ).pool(context)
    }

    // External description and hint overrides one from model
    return PropertyType(
      name.pool(context), type, hint = additional?.hint,
      description = description?.pool(context),
      description_kind = description_kind.pool(context),
      optional = optional,
      required = required,
      computed = computed,
      sensitive = sensitive,
      deprecated = if (deprecated) "DEPRECATED" else null,
    ).pool(context)
  }

  // https://developer.hashicorp.com/terraform/language/attr-as-blocks
  private fun isAttributeAsBlock(type: Type): Boolean {
    if (type !is ContainerType<*>) return false
    if (type !is SetType && type !is ListType) return false
    return type.elements is ObjectType || type.elements == null
  }

  /*
    type blockType struct {
      NestingMode string `json:"nesting_mode,omitempty"`
      Block       *block `json:"block,omitempty"`
      MinItems    uint64 `json:"min_items,omitempty"`
      MaxItems    uint64 `json:"max_items,omitempty"`
    }
 */
  private fun parseBlockType(context: LoadContext, name: String, value: Any?): PropertyOrBlockType {
    if (value !is ObjectNode) {
      error("Right part of schema element (field parameters) should be object")
    }

    if (name == Constants.TIMEOUTS) {
      throw IllegalStateException(Constants.TIMEOUTS + " not expected here")
    }

    val block = value.obj("block")!!
    val nesting_mode = value.string("nesting_mode")
    val min_items = value.number("min_items")
    val max_items = value.number("max_items")

    val parsed = parseBlock(context, block, name,
                            NestingInfo(NestingType.fromString(nesting_mode!!)!!, min_items?.toInt(), max_items?.toInt()))

    return parsed
  }

  /*
   type block struct {
     Attributes      map[string]*attribute `json:"attributes,omitempty"`
     BlockTypes      map[string]*blockType `json:"block_types,omitempty"`
     Description     string                `json:"description,omitempty"`
     DescriptionKind string                `json:"description_kind,omitempty"`
     Deprecated      bool                  `json:"deprecated,omitempty"`
   }
  */
  private fun parseBlock(context: LoadContext, block: ObjectNode, name: String, nesting: NestingInfo?): BlockType {
    val attributes = block.obj("attributes")
    val block_types = block.obj("block_types")

    val description = block.string("description")
    val description_kind = block.string("description_kind") ?: "plain"

    val deprecated = block.boolean("deprecated") ?: false

    val attrs: List<PropertyOrBlockType> = attributes?.fields()?.asSequence()?.map {
      parseAttribute(context, it.key, it.value, name)
    }?.toList() ?: emptyList()

    val blocks: List<PropertyOrBlockType> = block_types?.fields()?.asSequence()?.map {
      parseBlockType(context, it.key, it.value)
    }?.toList() ?: emptyList()

    return BlockType(name.pool(context),
                     description = description?.pool(context),
                     description_kind = description_kind.pool(context),
                     deprecated = if (deprecated) "DEPRECATED" else null,
                     nesting = nesting,
                     properties = (attrs + blocks).associateBy { it.name }).pool(context)
  }

  private fun parseType(context: LoadContext, node: JsonNode): Type {
    if (node.isTextual) {
      val string = node.asText()
      return when ( string?.lowercase(Locale.getDefault())) {
        "bool" -> Types.Boolean
        "number" -> Types.Number
        "string" -> Types.String
        "dynamic" -> Types.Any
        null -> Types.Invalid
        else -> {
          warnOrFailInInternalMode("Unsupported type '$node'")
          Types.Invalid
        }
      }.pool(context)
    }

    // Based on cty.Type#MarshalJSON
    if (node.isArray) {
      node as ArrayNode
      assert(node.get(0).isTextual)
      when (node.get(0).textValue()) {
        "list" -> {
          assert(node.size() == 2)
          return ListType(parseType(context, node.get(1))).pool(context)
        }
        "set" -> {
          assert(node.size() == 2)
          return SetType(parseType(context, node.get(1))).pool(context)
        }
        "map" -> {
          assert(node.size() == 2)
          return MapType(parseType(context, node.get(1))).pool(context)
        }
        "object" -> {
          assert(node.get(1).isObject)
          assert(node.size() == 2 || (node.size() == 3 && node.get(2).isArray))

          val obj = node.get(1) as ObjectNode
          val attributes = obj.fields().asSequence().associate { it.key to parseType(context, it.value) }

          // optional is a list of names (strings)
          val optional: Set<String>? =
            if (node.size() == 3)
              (node.get(2) as ArrayNode).elements().asSequence().map { it.textValue() }.toSet()
            else
              null

          return ObjectType(attributes, optional).pool(context)
        }
        "tuple" -> {
          assert(node.get(1).isArray)
          assert(node.size() == 2)
          val elements = (node.get(1) as ArrayNode).elements().asSequence().map { parseType(context, it) }.toList()
          return TupleType(elements).pool(context)
        }
      }
    }

    warnOrFailInInternalMode("Unsupported type '$node'")
    return Types.Invalid
  }


  /*
type nestedType struct {
Attributes  map[string]*attribute `json:"attributes,omitempty"`
NestingMode string                `json:"nesting_mode,omitempty"`
MinItems    uint64                `json:"min_items,omitempty"`
MaxItems    uint64                `json:"max_items,omitempty"`
}
   */
  private fun parseAttrNestedType(context: LoadContext, node: ObjectNode, fqnPrefix: String): Type {
    val attributes = node.obj("attributes")

    val nesting_mode = node.string("nesting_mode")
    val min_items = node.number("min_items")
    val max_items = node.number("max_items")

    NestingInfo(NestingType.fromString(nesting_mode!!)!!, min_items?.toInt(), max_items?.toInt())

    val attrs = attributes?.fields()?.asSequence()?.map { parseAttribute(context, it.key, it.value, fqnPrefix) }?.toList()
                ?: emptyList()

    val nested = ObjectType(attrs.associate { it.name to it.asType() }).pool(context)

    when (nesting_mode) {
      "single" -> return Types.Any // TODO: check
      //"group" -> return Types.Any // TODO: check
      "list" -> return ListType(nested).pool(context)
      "set" -> return SetType(nested).pool(context)
      "map" -> return MapType(nested).pool(context)
    }

    warnOrFailInInternalMode("Unsupported nested type: $node")
    return Types.Invalid
  }

}

private fun PropertyOrBlockType.asType(): Type? {
  return when (this) {
    is PropertyType -> this.type
    is BlockType -> this
    else -> null
  }
}

internal data class ProviderMetadata(
  val name: String = "",
  val namespace: String = "",
  val fullName: String = "",
  val source: String = "",
  val version: String = "",
  val tier: ProviderTier = ProviderTier.TIER_NONE
)

internal class TfProvidersSchema : VersionedMetadataLoader {
  override fun isSupportedVersion(version: String): Boolean = version in listOf("0.1", "0.2", "1.0")
  override fun isSupportedType(type: String): Boolean = type == "terraform-providers-schema-json"

  override fun load(context: LoadContext, json: ObjectNode, fileName: String) {
    val model = context.model
    val providerSchemas = (json.obj("schemas") ?: json).obj("provider_schemas") ?: return
    for ((n, provider) in providerSchemas.fields().asSequence()) {
      val coordinates = ProviderType.parseCoordinates(n)
      val providerFullName = "${coordinates.namespace}/${coordinates.name}"
      val providerKey = "provider.$providerFullName"
      if (model.loaded.containsKey(providerKey)) {
        TfMetadataLoader.LOG.warn("Provider '$providerFullName' is already loaded from '${model.loaded[providerKey]}'")
        continue
      }
      provider as ObjectNode
      model.loaded[providerKey] = fileName
      val providerInfo = provider.obj("provider")?.let { parseProviderInfo(context, coordinates.name, coordinates.namespace, it, json) } ?: ProviderType(coordinates.name, emptyList(), coordinates.namespace)
      model.providers.add(providerInfo)

      val resources = provider.obj("resource_schemas")
      val dataSources = provider.obj("data_source_schemas")
      if (resources == null && dataSources == null) {
        TfMetadataLoader.LOG.warn("No resources nor data-sources defined for provider '$providerFullName' in file '$fileName'")
      }
      resources?.let { it.fields().asSequence().mapTo(model.resources) { parseResourceInfo(context, it, providerInfo) } }
      dataSources?.let { it.fields().asSequence().mapTo(model.dataSources) { parseDataSourceInfo(context, it, providerInfo) } }

      val providerDefinedFunctions = provider.obj("functions")
      providerDefinedFunctions?.let {
        it.fields()?.asSequence()?.mapNotNullTo(model.providerDefinedFunctions) { parseProviderFunctionInfo(context, it, providerInfo) }
      }
    }
  }

  private fun parseProviderInfo(context: LoadContext, name: String, namespace: String, obj: ObjectNode, file: ObjectNode): ProviderType? {
    val (parsed, version) = TfBaseLoader.parseSchema(context, obj, name) ?: return null
    val providerMetadata = TfBaseLoader.parseMetadata(file.obj("metadata"), name, namespace)
    return ProviderType(providerMetadata.name, parsed.properties.values.toList(), providerMetadata.namespace, providerMetadata.tier, providerMetadata.version, parsed)
  }

  private fun parseResourceInfo(context: LoadContext, entry: Map.Entry<String, Any?>, info: ProviderType): ResourceType {
    val name = entry.key.pool(context)
    assert(entry.value is ObjectNode) { "Right part of resource should be object" }
    val obj = entry.value as ObjectNode
    val (parsed, version) = TfBaseLoader.parseSchema(context, obj, name)
                            ?: throw IllegalArgumentException("can't parse schema parseResourceInfo $name, entry = $entry")
    return ResourceType(name, info, parsed.properties.values.toList(), parsed)
  }

  private fun parseDataSourceInfo(context: LoadContext, entry: Map.Entry<String, Any?>, info: ProviderType): DataSourceType {
    val name = entry.key.pool(context)
    assert(entry.value is ObjectNode) { "Right part of data-source should be object" }
    val obj = entry.value as ObjectNode
    val (parsed, version) = TfBaseLoader.parseSchema(context, obj, name)
                            ?: throw IllegalArgumentException("can't parse schema parseDataSourceInfo $name, entry = $entry")
    return DataSourceType(name, info, parsed.properties.values.toList(), parsed)
  }

  private fun parseProviderFunctionInfo(context: LoadContext, entry: Map.Entry<String, Any?>, info: ProviderType): TfFunction? {
    val name = entry.key.pool(context)
    val objectNode = entry.value as? ObjectNode ?: return null

    val description = objectNode.string("description")
    val returnType = objectNode.string("return_type").orEmpty()
    val parameters = objectNode.array("parameters")
      ?.mapNotNull { it as? ObjectNode }
      ?.map { Argument(TypeImpl(it.string("type").orEmpty()), it.string("name")) }
      ?.toTypedArray().orEmpty()

    return TfFunction(
      name,
      TypeImpl(returnType),
      arguments = parameters,
      description = description,
      providerType = info.type
    )
  }
}

