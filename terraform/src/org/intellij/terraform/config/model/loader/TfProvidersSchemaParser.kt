// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model.loader

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.intellij.util.asSafely
import org.intellij.terraform.config.Constants
import org.intellij.terraform.config.model.BlockType
import org.intellij.terraform.config.model.ContainerType
import org.intellij.terraform.config.model.HclType
import org.intellij.terraform.config.model.ListType
import org.intellij.terraform.config.model.MapType
import org.intellij.terraform.config.model.NestingInfo
import org.intellij.terraform.config.model.NestingType
import org.intellij.terraform.config.model.ObjectType
import org.intellij.terraform.config.model.PropertyOrBlockType
import org.intellij.terraform.config.model.PropertyType
import org.intellij.terraform.config.model.ProviderTier
import org.intellij.terraform.config.model.SetType
import org.intellij.terraform.config.model.TupleType
import org.intellij.terraform.config.model.Types
import org.intellij.terraform.config.model.boolean
import org.intellij.terraform.config.model.number
import org.intellij.terraform.config.model.obj
import org.intellij.terraform.config.model.string
import java.util.Locale

internal object TfProvidersSchemaParser {
  /*
    type schema struct {
      Version uint64 `json:"version"`
      Block   *block `json:"block,omitempty"`
    }
   */
  fun parseSchema(context: LoadContext, obj: ObjectNode, name: String): BlockType? {
    val block = obj.obj("block") ?: return null
    return parseBlock(context, block, name, null)
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

    val type: HclType
    val rawType = value.get("type")
    if (rawType != null) {
      type = parseType(context, rawType)
    }
    else {
      val nested_type = value.obj("nested_type")
      type = parseAttrNestedType(context, nested_type!!, fqn)
    }

    val description = value.string("description")
    val descriptionKind = value.string("description_kind") ?: "plain"

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
        descriptionKind = descriptionKind.pool(context),
        optional = optional,
        required = required,
        computed = computed,
        deprecated = if (deprecated) "DEPRECATED" else null,
        properties = type.asSafely<ContainerType<*>>()?.elements
          ?.asSafely<ObjectType>()?.elements?.mapValues { (k, v) -> PropertyType(k, type = v ?: Types.Any) }.orEmpty()
      )
    }

    // External description and hint overrides one from model
    return PropertyType(
      name.pool(context), type, hint = additional?.hint,
      description = description?.pool(context),
      descriptionKind = descriptionKind.pool(context),
      optional = optional,
      required = required,
      computed = computed,
      sensitive = sensitive,
      deprecated = if (deprecated) "DEPRECATED" else null,
    ).pool(context)
  }

  // https://developer.hashicorp.com/terraform/language/attr-as-blocks
  private fun isAttributeAsBlock(type: HclType): Boolean {
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
    val nestingMode = value.string("nesting_mode")
    val minItems = value.number("min_items")
    val maxItems = value.number("max_items")

    return parseBlock(
      context,
      block,
      name,
      NestingInfo(NestingType.fromString(nestingMode!!)!!, minItems?.toInt(), maxItems?.toInt())
    )
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
    val blockTypes = block.obj("block_types")

    val description = block.string("description")
    val descriptionKind = block.string("description_kind") ?: "plain"

    val deprecated = block.boolean("deprecated") ?: false

    val attrs = attributes?.properties()?.asSequence()?.map {
      parseAttribute(context, it.key, it.value, name)
    } ?: emptySequence()

    val blocks = blockTypes?.properties()?.asSequence()?.map {
      parseBlockType(context, it.key, it.value)
    } ?: emptySequence()

    return BlockType(
      name.pool(context),
      description = description?.pool(context),
      descriptionKind = descriptionKind.pool(context),
      deprecated = if (deprecated) "DEPRECATED" else null,
      nesting = nesting,
      properties = (attrs + blocks).associateBy { it.name }
    ).pool(context)
  }

  private fun parseType(context: LoadContext, node: JsonNode): HclType {
    if (node.isTextual) {
      val string = node.asText()
      return when (string?.lowercase(Locale.getDefault())) {
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
          val attributes = obj.properties().associate { it.key to parseType(context, it.value) }

          // optional is a list of names (strings)
          val optional: Set<String>? =
            if (node.size() == 3)
              (node.get(2) as ArrayNode).elements().asSequence().map { it.textValue() }.toSet()
            else
              null

          return ObjectType(attributes, optional)
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
  private fun parseAttrNestedType(context: LoadContext, node: ObjectNode, fqnPrefix: String): HclType {
    val attributes = node.obj("attributes")

    val nestingMode = node.string("nesting_mode")
    val minItems = node.number("min_items")
    val maxItems = node.number("max_items")

    NestingInfo(NestingType.fromString(nestingMode!!)!!, minItems?.toInt(), maxItems?.toInt())

    val attrs = attributes?.properties()?.asSequence()?.map { parseAttribute(context, it.key, it.value, fqnPrefix) }?.toList()
                ?: emptyList()

    val nested = ObjectType(attrs.associate { it.name to it.asType() }).pool(context)

    when (nestingMode) {
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

private fun PropertyOrBlockType.asType(): HclType? {
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
  val tier: ProviderTier = ProviderTier.TIER_NONE,
)
