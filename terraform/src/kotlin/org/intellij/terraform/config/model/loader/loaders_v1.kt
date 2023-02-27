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

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import org.intellij.terraform.config.Constants
import org.intellij.terraform.config.model.*
import org.intellij.terraform.config.model.Function
import org.intellij.terraform.config.model.loader.TerraformMetadataLoader.Companion.LOG

object BaseLoaderV1 : BaseLoader {
  override val version: String
    get() = "1"

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

    var isBlock = false

    val rawType = value.string("Type")

    // 'type' for PropertyType, 'innerTypeProperties' for BlockType
    var innerTypeProperties: List<PropertyOrBlockType>? = null
    var type = parseType(context, rawType)

    val elem = value.obj("Elem")
    if (elem != null && elem.isNotEmpty()) {
      // Valid only for TypeSet and TypeList, should parse internal structure
      // TODO: ensure set only for TypeSet and TypeList
      val et = elem.string("Type") ?: elem.string("type")
      if (et == "SchemaElements") {
        val elementsType = elem.string("ElementsType") ?: elem.string("elements-type")
        if (elementsType != null) {
          val parsedElementsType = parseType(context, elementsType)
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
      } else if (et == "SchemaInfo") {
        val o = elem.obj("Info") ?: elem.obj("info")
        if (o != null) {
          innerTypeProperties = o.fields().asSequence().map { parseSchemaElement(context, it, fqn) }.toList()
          if (type == Types.Array) {
            isBlock = true
          }
        }
      } else if (et == null) {
        /*
          Something like with 'Value' == 'String':
          dimensions = {
            instanceId = "i-bp1247jeep0y53nu3bnk,i-bp11gdcik8z6dl5jm84p"
            device = "/dev/vda1,/dev/vdb1"
          }
         */
        val t = elem.string("Type") ?: elem.string("type")
        if (t != null) {
          val parsedElementsType = parseType(context, t)
          when (rawType) {
            "List" -> type = ListType(parsedElementsType)
            "Set" -> type = SetType(parsedElementsType)
            "Map" -> type = MapType(parsedElementsType)
            t -> {
              // a bit weird but occurs, sometimes useful for ValidateFunc
            }
            else -> warnOrFailInInternalMode("$fqn: Type ($rawType) unexpected when present Elem.Type ($t)")
          }
        }
      } else {
        warnOrFailInInternalMode("$fqn: unexpected Elem.Type: $et")
      }
    }

    val conflicts: List<String>? = value.array("ConflictsWith")?.mapNotNull { it.textValue() }?.map { it.pool(context) }

    val deprecated = value.string("Deprecated")
    val has_default: Boolean = value.obj("Default")?.isNotEmpty() ?: false
    val has_default_function: Boolean = value.string("DefaultFunc")?.isNotEmpty() ?: false
    // || m["InputDefault"]?.string("value") != null // Not sure about this property TODO: Investigate how it works in terraform

    val additional = context.model.external[fqn] ?: TypeModelProvider.Additional(name)
    // TODO: Consider move 'has_default' to Additional

    val required = additional.required ?: value.boolean("Required") ?: false
    val optional = additional.optional ?: value.boolean("Optional") ?: false
    val computed = value.boolean("Computed") ?: false

    if (type == Types.Object) {
      isBlock = true
    }

    val description = additional.description ?: value.string("Description")

    // External description and hint overrides one from model
    if (isBlock) {
      // TODO: Do something with a additional.hint
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
    return when (string?.removePrefix("Type")) {
      "Bool" -> Types.Boolean
      "Int" -> Types.Number
      "Float" -> Types.Number
      "String" -> Types.String
      "List" -> Types.Array
      "Set" -> Types.Array
      "Map" -> Types.Object
      "Any" -> Types.Any
      else -> Types.Invalid
    }
  }
}

abstract class ProviderLoader(protected val base: BaseLoader) : VersionedMetadataLoader {
  override fun isSupportedType(type: String) = type == "provider"

  override fun load(context: LoadContext, json: ObjectNode, file: String) {
    val model = context.model
    val name = json.string("name")!!.pool(context)
    val provider = json.obj("provider")
    if (provider == null) {
      LOG.warn("No provider schema in file '$file'")
      return
    }
    if (model.loaded.containsKey("provider.$name")) {
      LOG.warn("Provider '$name' is already loaded from '${model.loaded["provider.$name"]}'")
      return
    }
    model.loaded["provider.$name"] = file
    val info = parseProviderInfo(context, name, provider)
    model.providers.add(info)
    val resources = json.obj("resources")
    val dataSources = json.obj("data-sources")
    if (resources == null && dataSources == null) {
      LOG.warn("No resources nor data-sources defined for provider '$name' in file '$file'")
    }
    resources?.let { it.fields().asSequence().mapTo(model.resources) { parseResourceInfo(context, it, info) } }
    dataSources?.let { it.fields().asSequence().mapTo(model.dataSources) { parseDataSourceInfo(context, it, info) } }
  }

  private fun parseProviderInfo(context: LoadContext, name: String, obj: ObjectNode): ProviderType {
    return ProviderType(name, obj.fields().asSequence().map { base.parseSchemaElement(context, it, name) }.toList())
  }

  private fun parseResourceInfo(context: LoadContext, entry: Map.Entry<String, Any?>, info: ProviderType): ResourceType {
    val name = entry.key.pool(context)
    assert(entry.value is ObjectNode) { "Right part of resource should be object" }
    val obj = entry.value as ObjectNode
    val timeouts = getTimeoutsBlock(context, obj)
    return ResourceType(name, info, obj.fields().asSequence().map { base.parseSchemaElement(context, it, name) }.plus(timeouts).filterNotNull().toList())
  }

  private fun parseDataSourceInfo(context: LoadContext, entry: Map.Entry<String, Any?>, info: ProviderType): DataSourceType {
    val name = entry.key.pool(context)
    assert(entry.value is ObjectNode) { "Right part of data-source should be object" }
    val obj = entry.value as ObjectNode
    val timeouts = getTimeoutsBlock(context, obj)
    return DataSourceType(name, info, obj.fields().asSequence().map { base.parseSchemaElement(context, it, name) }.plus(timeouts).filterNotNull().toList())
  }

  private fun getTimeoutsBlock(context: LoadContext, obj: ObjectNode): PropertyOrBlockType? {
    val value = obj.remove(Constants.TIMEOUTS) ?: return null
    assert(value is ArrayNode) { "${Constants.TIMEOUTS} should be an array" }
    val array = value as? ArrayNode ?: return null
    for (element in array) {
      assert(element is TextNode) { "${Constants.TIMEOUTS} array elements should be string, got ${element?.javaClass?.name}" }
    }
    val timeouts = array.mapNotNull { it.textValue() }.map { it.pool(context) }
    if (timeouts.isEmpty()) return null
    return BlockType("timeouts", 0,
        description = "Amount of time a specific operation is allowed to take before being considered an error", // TODO: Improve description
        properties = timeouts.map { PropertyType(it, Types.String, optional = true).pool(context) }.toMap()
        // TODO: ^ Check type, should be Time? (allowed suffixes are s, m, h)
    ).pool(context)
  }
}

abstract class ProvisionerLoader(protected val base: BaseLoader) : VersionedMetadataLoader {
  override fun isSupportedType(type: String) = type == "provisioner"
  override fun load(context: LoadContext, json: ObjectNode, file: String) {
    val model = context.model
    val name = json.string("name")!!.pool(context)
    val provisioner = json.obj("schema")
    if (provisioner == null) {
      LOG.warn("No provisioner schema in file '$file'")
      return
    }
    if (model.loaded.containsKey("provisioner.$name")) {
      LOG.warn("Provisioner '$name' is already loaded from '${model.loaded["provisioner.$name"]}'")
      return
    }
    model.loaded["provisioner.$name"] = file
    val info = ProvisionerType(name, provisioner.fields().asSequence().map { base.parseSchemaElement(context, it, name) }.toList())
    model.provisioners.add(info)
  }
}

abstract class BackendLoader(protected val base: BaseLoader) : VersionedMetadataLoader {
  override fun isSupportedType(type: String) = type == "backend"
  override fun load(context: LoadContext, json: ObjectNode, file: String) {
    val model = context.model
    val name = json.string("name")!!.pool(context)
    val backend = json.obj("schema")
    if (backend == null) {
      LOG.warn("No backend schema in file '$file'")
      return
    }
    if (model.loaded.containsKey("backend.$name")) {
      LOG.warn("Backend '$name' is already loaded from '${model.loaded["backend.$name"]}'")
      return
    }
    model.loaded["backend.$name"] = file
    val info = BackendType(name, backend.fields().asSequence().map { base.parseSchemaElement(context, it, name) }.toList())
    model.backends.add(info)
  }
}

class ProviderLoaderV1 : ProviderLoader(BaseLoaderV1) {
  override fun isSupportedVersion(version: String) = version == base.version
}

class ProvisionerLoaderV1 : ProvisionerLoader(BaseLoaderV1) {
  override fun isSupportedVersion(version: String) = version == base.version
}

class BackendLoaderV1 : BackendLoader(BaseLoaderV1) {
  override fun isSupportedVersion(version: String) = version == base.version
}

class FunctionsLoaderV1 : VersionedMetadataLoader {
  override fun isSupportedType(type: String) = type == "functions"
  override fun isSupportedVersion(version: String) = version == "1"

  override fun load(context: LoadContext, json: ObjectNode, file: String) {
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
    for ((k, v) in functions.fields()) {
      if (v !is ObjectNode) continue
      assert(v.string("Name").equals(k)) { "Name mismatch: $k != ${v.string("Name")}" }
      val returnType = BaseLoaderV1.parseType(context, v.string("ReturnType")!!)
      val args = v.array("ArgTypes")!!.mapNotNull { it.textValue() }.map { BaseLoaderV1.parseType(context, it) }.map { Argument(it) }.toMutableList()
      val variadic = v.boolean("Variadic") ?: false
      var va: VariadicArgument? = null
      if (variadic) {
        va = VariadicArgument(BaseLoaderV1.parseType(context, v.string("VariadicType")))
      }
      model.functions.add(Function(k.pool(context), returnType, *args.toTypedArray(), variadic = va))
    }
  }

}
