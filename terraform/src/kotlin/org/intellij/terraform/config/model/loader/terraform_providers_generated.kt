/*
 * Copyright 2000-2021 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.terraform.config.model.loader

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.intellij.terraform.config.Constants
import org.intellij.terraform.config.model.*

object TFBaseLoader {
  /*
    type schema struct {
      Version uint64 `json:"version"`
      Block   *block `json:"block,omitempty"`
    }
   */
  fun parseSchema(context: LoadContext, obj: ObjectNode, name: String): Pair<BlockType, Int> {
    val version = obj.number("version")!!.toInt()
    val block = obj.obj("block")!!
    return parseBlock(context, block, name, null) to version
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

    if (type is SetType) {
      return BlockType(
        name.pool(context),
        description = description?.pool(context),
        description_kind = description_kind.pool(context),
        optional = optional,
        required = required,
        computed = computed,
        deprecated = if (deprecated) "DEPRECATED" else null,
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


  /*
    type blockType struct {
      NestingMode string `json:"nesting_mode,omitempty"`
      Block       *block `json:"block,omitempty"`
      MinItems    uint64 `json:"min_items,omitempty"`
      MaxItems    uint64 `json:"max_items,omitempty"`
    }
 */
  private fun parseBlockType(context: LoadContext, name: String, value: Any?, fqnPrefix: String): PropertyOrBlockType {
    if (value !is ObjectNode) {
      error("Right part of schema element (field parameters) should be object")
    }

    if (name == Constants.TIMEOUTS) {
      throw IllegalStateException(Constants.TIMEOUTS + " not expected here")
    }

    val fqn = "$fqnPrefix.$name"

    val block = value.obj("block")!!
    val nesting_mode = value.string("nesting_mode")
    val min_items = value.number("min_items")
    val max_items = value.number("max_items")

    val additional = context.model.external[fqn]

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
      parseBlockType(context, it.key, it.value, name)
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
      return when (string?.toLowerCase()) {
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

class TerraformProvidersSchema : VersionedMetadataLoader {
  override fun isSupportedVersion(version: String): Boolean = version in listOf("0.1", "0.2", "1.0")
  override fun isSupportedType(type: String): Boolean = type == "terraform-providers-schema-json"

  override fun load(context: LoadContext, json: ObjectNode, file: String) {
    val model = context.model

    val providers = json.obj("provider_schemas")
    for ((n, provider) in providers!!.fields().asSequence()) {
      val name = n.split("/").takeIf { it.size == 3 && it[0] == "registry.terraform.io" || it[0] == "terraform.io" }?.let { it[2] } ?: n
      provider as ObjectNode
      if (model.loaded.containsKey("provider.$name")) {
        TerraformMetadataLoader.LOG.warn("Provider '$name' is already loaded from '${model.loaded["provider.$name"]}'")
        continue
      }
      model.loaded["provider.$name"] = file
      val info = provider.obj("provider")?.let { parseProviderInfo(context, name, it) } ?: ProviderType(name, emptyList())
      model.providers.add(info)
      val resources = provider.obj("resource_schemas")
      val dataSources = provider.obj("data_source_schemas")
      if (resources == null && dataSources == null) {
        TerraformMetadataLoader.LOG.warn("No resources nor data-sources defined for provider '$name' in file '$file'")
      }
      resources?.let { it.fields().asSequence().mapTo(model.resources) { parseResourceInfo(context, it, info) } }
      dataSources?.let { it.fields().asSequence().mapTo(model.dataSources) { parseDataSourceInfo(context, it, info) } }
    }

  }

  private fun parseProviderInfo(context: LoadContext, name: String, obj: ObjectNode): ProviderType {
    val (parsed, version) = TFBaseLoader.parseSchema(context, obj, name)
    // TODO: Support description and version
    return ProviderType(name, parsed.properties.values.toList())
  }

  private fun parseResourceInfo(context: LoadContext, entry: Map.Entry<String, Any?>, info: ProviderType): ResourceType {
    val name = entry.key.pool(context)
    assert(entry.value is ObjectNode) { "Right part of resource should be object" }
    val obj = entry.value as ObjectNode
    val (parsed, version) = TFBaseLoader.parseSchema(context, obj, name)
    return ResourceType(name, info, parsed.properties.values.toList())    // TODO: Support description and version
  }

  private fun parseDataSourceInfo(context: LoadContext, entry: Map.Entry<String, Any?>, info: ProviderType): DataSourceType {
    val name = entry.key.pool(context)
    assert(entry.value is ObjectNode) { "Right part of data-source should be object" }
    val obj = entry.value as ObjectNode
    val (parsed, version) = TFBaseLoader.parseSchema(context, obj, name)
    // TODO: Support description
    return DataSourceType(name, info, parsed.properties.values.toList())
  }
}

