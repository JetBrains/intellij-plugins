/*
 * Copyright 2000-2020 JetBrains s.r.o.
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

import com.fasterxml.jackson.databind.node.ObjectNode
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.lang.impl.PsiBuilderImpl
import org.intellij.terraform.hcl.HCLParser
import org.intellij.terraform.hcl.psi.HCLExpression
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.config.Constants
import org.intellij.terraform.config.TerraformLanguage
import org.intellij.terraform.config.TerraformParserDefinition
import org.intellij.terraform.config.inspection.TypeSpecificationValidator
import org.intellij.terraform.config.model.*
import org.intellij.terraform.config.model.Function
import org.intellij.terraform.config.model.loader.TerraformMetadataLoader.Companion.LOG

object BaseLoaderV2 : BaseLoader {
  override val version: String
    get() = "2"

  override fun parseSchemaElement(context: LoadContext, entry: Map.Entry<String, Any?>, fqnPrefix: String): PropertyOrBlockType {
    return parseSchemaElement(context, entry.key, entry.value, fqnPrefix)
  }

  override fun parseSchemaElement(context: LoadContext, name: String, value: Any?, fqnPrefix: String): PropertyOrBlockType {
    assert(value is ObjectNode) { "Right part of schema element (field parameters) should be object" }
    if (value !is ObjectNode) {
      throw IllegalStateException()
    }

    if (name == Constants.TIMEOUTS) {
      throw IllegalStateException(Constants.TIMEOUTS + " not expected here")
    }

    val fqn = "$fqnPrefix.$name"

    val configExplicitMode = value.string("ConfigExplicitMode")
    val configImplicitMode = value.string("ConfigImplicitMode")
    var isBlock = when {
      configExplicitMode == "Attr" -> false
      configExplicitMode == "Block" -> true
      configImplicitMode == "Attr" -> false
      configImplicitMode == "Block" -> true

      /*
        From terraform code:
        > Computed-only schemas are always handled as attributes,
        > because they never appear in configuration.
        Yet for us it's ok to use them as blocks, see terraform-metadata for code
       */
      configImplicitMode == "ComputedBlock" -> true

      else -> value.boolean("IsBlock") ?: false
    }

    val rawType = value.string("Type")

    // 'type' for PropertyType, 'innerTypeProperties' for BlockType
    var innerTypeProperties: List<PropertyOrBlockType>? = null
    var type = parseType(context, rawType)

    val elem = value.obj("Elem")
    if (elem != null) {
      when (val et = elem.string("Type")) {
        "SchemaElements" -> {
          if (isBlock) {
            warnOrFailInInternalMode("$fqn: is block but Elem.Type is SchemaElements")
          }
          val elementsType = elem.string("ElementsType")
          if (elementsType == null) {
            warnOrFailInInternalMode("$fqn: Elem.ElementsType is null")
          } else {
            val parsedElementsType = parseSimpleType(elementsType)
            when (rawType) {
              "List" -> type = ListType(parsedElementsType)
              "Set" -> type = SetType(parsedElementsType)
              "Map" -> type = MapType(parsedElementsType)
              elementsType -> {
                // a bit weird but occurs, sometimes useful for ValidateFunc
              }
              else -> warnOrFailInInternalMode("$fqn: Type ($rawType) unexpected for SchemaElements ($elementsType)")
            }
          }
        }
        "SchemaInfo" -> {
          val o = elem.obj("Info")
          if (!isBlock) {
            if (configExplicitMode == "Attr") {
              // Special case for blocks which could be also set as attr with empty list/set
              // TODO: Reconsider keeping it as PropertyType
              isBlock = true
            } else {
              warnOrFailInInternalMode("$fqn: is not block yet have Elem.Type == SchemaInfo")
            }
          }

          // null due to omitempty, means no arguments in block
          innerTypeProperties = o?.fields()?.asSequence()?.map { parseSchemaElement(context, it, fqn) }?.toList() ?: emptyList()

          // TODO: Set type as List/Set/Map type with innerTypeProperties
          //  Convert innerTypeProperties to parts of ObjectType
        }
        null -> {
          // Subclass of schema.ValueType originally used, e.g. TypeString
          /*
            "kms_encryption_context": {
              Type:     schema.TypeMap,
              Optional: true,
              Elem: schema.TypeString,
            },
          */
          val t = elem.string("Value")
          if (isBlock) {
            warnOrFailInInternalMode("$fqn: is block but Elem.Value is '$t'")
          }
          if (t == rawType) {
            // just ignore, looks like bug in terraform plugin source code
          } else if (t != null) {
            val parsedInnerType = parseSimpleType(t)
            when (rawType) {
              "List" -> type = ListType(parsedInnerType)
              "Set" -> type = SetType(parsedInnerType)
              "Map" -> type = MapType(parsedInnerType)
              else -> warnOrFailInInternalMode("$fqn: unexpected $t, $rawType")
            }
          } else {
            warnOrFailInInternalMode("$fqn: Elem.Value is null")
          }
        }
        else -> {
          warnOrFailInInternalMode("$fqn: unexpected Elem.Type: $et")
        }
      }
    }

    val conflicts: List<String>? = value.array("ConflictsWith")?.mapNotNull { it.textValue() }?.map { it.pool(context) }

    val deprecated = value.string("Deprecated")
    val has_default: Boolean = value.obj("Default")?.isNotEmpty() ?: false
    val has_default_function: Boolean = value.string("DefaultFunc")?.isNotEmpty() ?: false


    val additional = context.model.external[fqn] ?: TypeModelProvider.Additional(name)

    val required = additional.required ?: value.boolean("Required") ?: false
    val optional = additional.optional ?: value.boolean("Optional") ?: false
    val computed = value.boolean("Computed") ?: false

    val description = additional.description ?: value.string("Description")

    // External description and hint overrides one from model
    if (isBlock) {
      val properties: Map<String, PropertyOrBlockType> = innerTypeProperties?.toMap() ?: emptyMap()
      return BlockType(name.pool(context),
          description = description?.pool(context),
          optional = optional,
          required = required,
          computed = computed,
          deprecated = deprecated?.pool(context),
          conflictsWith = conflicts,
          properties = properties).pool(context)
    }
    return PropertyType(name.pool(context), type, hint = additional.hint,
        description = description?.pool(context),
        optional = optional,
        required = required,
        computed = computed,
        deprecated = deprecated?.pool(context),
        conflictsWith = conflicts,
        has_default = has_default || has_default_function).pool(context)
  }

  override fun parseType(context: LoadContext, string: String?): Type {
    if (string == null) return Types.Invalid
    val paren = string.indexOf('(')
    if (paren == -1) {
      return parseSimpleType(string)
    }
    assert(string.last() == ')')
    val input = string.toLowerCase()
    val expression = generateValuePsi(input)
    assert(expression != null) { "Failed to parse type (generate psi): $input" }
    val type = expression?.let { TypeSpecificationValidator(null, true).getType(it) }
    assert(type != null) { "Failed to parse type: $input" }
    return type ?: Types.Object
  }

  private fun parseSimpleType(string: String?): Type {
    /*
    From  terraform/helper/schema/valuetype.go
    const (
            TypeInvalid ValueType = iota
            TypeBool
            TypeInt
            TypeFloat
            TypeString
            TypeList
            TypeMap
            TypeSet
            typeObject
    )
    */
    return when (string?.removePrefix("cty.")) {
      "Bool" -> Types.Boolean
      "Int" -> Types.Number
      "Float" -> Types.Number
      "String" -> Types.String
      "List" -> Types.Array
      "Set" -> Types.Array
      "Map" -> Types.Object
      "Any" -> Types.Any

      // HCL2
      // schema.NestingMode for Blocks:
      // * NestingSingle
      // * NestingGroup
      // * NestingList
      // * NestingSet
      // * NestingMap
      "NestingSingle" -> Types.Any
      "NestingGroup" -> Types.Any
      "NestingList" -> Types.Array
      "NestingSet" -> Types.Array
      "NestingMap" -> Types.Object

      // HCL2
      // cty types
      "Number" -> Types.Number
      "Expression" -> Types.Expression

      null -> Types.Invalid
      else -> {
        val message = "Unsupported type '$string'"
        warnOrFailInInternalMode(message)
        Types.Invalid
      }
    }
  }

  private fun generateValuePsi(input: String): HCLExpression? {
    val text = "foo = $input"

    val parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(TerraformLanguage)
    val lexer = TerraformParserDefinition.createLexer()
    val builder = PsiBuilderImpl(null, null, parserDefinition, lexer, null, text, null, null)
    val parser = HCLParser()

    val root = parser.parse(parserDefinition.fileNodeType, builder)
    val property = root.psi.firstChild as HCLProperty
    return property.value
  }
}

class ProviderLoaderV2 : ProviderLoader(BaseLoaderV2) {
  override fun isSupportedVersion(version: String) = version == base.version
}

class ProvisionerLoaderV2 : ProvisionerLoader(BaseLoaderV2) {
  override fun isSupportedVersion(version: String) = version == base.version
}

class BackendLoaderV2 : BackendLoader(BaseLoaderV2) {
  override fun isSupportedVersion(version: String) = version == base.version
}

class FunctionsLoaderV2 : VersionedMetadataLoader {
  override fun isSupportedType(type: String) = type == "functions"
  override fun isSupportedVersion(version: String) = version == "2"

  override fun load(context: LoadContext, json: ObjectNode, file: String) {
    val base = BaseLoaderV2
    val model = context.model
    val functions = json.obj("schema")
    if (functions == null) {
      LOG.warn("No functions schema in file '$file'")
      return
    }
    if (model.loaded.containsKey("functions")) {
      LOG.warn("Functions definitions already loaded from '${model.loaded["functions"]}'")
      return
    }
    model.loaded["functions"] = file
    for ((name, v) in functions.fields()) {
      if (v !is ObjectNode) continue
      val returnType = base.parseType(context, v.string("ReturnType")!!)
      val parameters: List<ObjectNode> = v.array("Parameters")?.mapNotNull { it as? ObjectNode } ?: emptyList()
      val variadic = v.obj("VariadicParameter")

      val args = parameters.map { Argument(name = it.string("Name"), type = base.parseType(context, it.string("Type"))) }
      val va: VariadicArgument? = variadic?.let {
        VariadicArgument(name = it.string("Name"), type = base.parseType(context, it.string("Type")))
      }

      model.functions.add(Function(
        name = name.pool(context),
        ret = returnType,
        arguments = *args.toTypedArray(),
        variadic = va
      ))
    }
  }

}
