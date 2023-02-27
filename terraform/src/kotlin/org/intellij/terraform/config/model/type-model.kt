// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model

// Model for element types

interface Type {
  val presentableText: String
}

open class TypeImpl(protected val baseName: String) : Type {
  override val presentableText: String
    get() = baseName

  override fun toString(): String {
    return presentableText
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is TypeImpl) return false

    return presentableText == other.presentableText
  }

  override fun hashCode(): Int {
    return presentableText.hashCode()
  }
}

open class PrimitiveType(name: String) : TypeImpl(name) {

}

// HCL2 expression types, from github.com/hashicorp/hcl2/typeexpr and github.com/zclconf/go-cty/cty
// null as inner type means error in type definition
abstract class ContainerType<T>(name: String, val elements: T) : TypeImpl(name) {
  override val presentableText: String
    get() {
      if (elements == null) {
        return baseName
      }
      if (elements is Type) {
        return "${baseName}(${elements.presentableText})"
      }
      return "${baseName}(${elements})"
    }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ContainerType<*>) return false
    if (!super.equals(other)) return false

    return elements == other.elements
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + (elements?.hashCode() ?: 0)
    return result
  }
}

// List is a sequence of values identified by consecutive whole numbers starting with zero.
// E.g. `list(number)` will match `[10, 20, 42]`
open class ListType(elements: Type?) : ContainerType<Type?>("list", elements)
// Set is a collection of unique values that do not have any secondary identifiers or ordering.
open class SetType(elements: Type?) : ContainerType<Type?>("set", elements)
// Map is a collection of values where each is identified by a string label.
// E.g. `map(number)` will match `{a=10, b=20}`
open class MapType(elements: Type?) : ContainerType<Type?>("map", elements)
// Tuple is a sequence of elements identified by consecutive whole numbers starting with zero,
// where each element has its own type.
// E.g. `tuple([string, number, bool])` would match a value like `["a", 15, true]`
open class TupleType(elements: List<Type?>) : ContainerType<List<Type?>>("tuple", elements) {
  override val presentableText: String
    get() = "${baseName}([${elements.joinToString(", ")}])"
}

open class OptionalType(nested: Type?) : ContainerType<Type?>("optional", nested)

// Object is a collection of named attributes that each have their own type.
// Extra fields allowed.
// E.g. `object({ name=string, age=number })` will match `{ name = "John", age = 52 }` and `{ name = "John", age = 52, extra = true }`
interface ObjectType : Type {
  val elements: Map<String, Type?>?
}

fun ObjectType(elements: Map<String, Type?>?, optional: Set<String>? = null): ObjectType {
  return ObjectTypeImpl(elements, optional)
}

private open class ObjectTypeImpl(elements: Map<String, Type?>?, val optional: Set<String>? = null) : ContainerType<Map<String, Type?>?>(
  "object", elements), ObjectType {
  override val presentableText: String
    get() {
      if (elements.isNullOrEmpty()) {
        return baseName
      }
      return "${baseName}({${elements.entries.joinToString(", ") { it.key + "=" + it.value }}})"
    }
}

fun isListType(type: Type?) :Boolean {
  return when (type) {
    is ListType, is SetType, is TupleType -> true
    else -> false
  }
}
fun isObjectType(type: Type?) :Boolean {
  return when (type) {
    is MapType, is ObjectType -> true
    else -> false
  }
}

/**
 * Returns a type that any of inputs could be casted to
 *
 * Note, it returns null if cannot find common type, not Any
 */
fun getCommonSupertype(input: Collection<Type?>): Type? {
  if (input.isEmpty()) return null
  for (type in input) {
    when (type) {
      null -> return null
      Types.Any -> return Types.Any
    }
  }
  val set = input.filterNotNull().toSet()
  if (set.size == 1) {
    return set.first()
  }
  // two or more types

  if (Types.String in set) {
    if (set.size == 2) {
      val another = set.first { it != Types.String }
      if (Types.String.isConvertibleTo(another)) return another
    }
    if (set.all { it.isConvertibleTo(Types.String) }) return Types.String
  }

  if (set.all { isListType(it) }) {
    val innerTypes = HashSet<Type>(set.size)
    for (t in set) {
      when (t) {
        is ListType -> innerTypes.add(t.elements ?: Types.Any)
        is SetType -> innerTypes.add(t.elements ?: Types.Any)
        is TupleType -> innerTypes.add(getCommonSupertype(t.elements) ?: Types.Any)
      }
    }
    val commonInnerType = getCommonSupertype(innerTypes) ?: Types.Any
    return ListType(commonInnerType)
  }

  if (set.all { it is ObjectType }) {
    val common = HashMap<String, MutableList<Type?>>()
    val maps = set.filterIsInstance(ObjectType::class.java).mapNotNull { it.elements }
    for (map in maps) {
      for ((k, v) in map) {
        common.getOrPut(k, { ArrayList(1) }).add(v)
      }
    }
    return ObjectType(common.mapValues { getCommonSupertype(it.value) })
  }

  if (set.all { isObjectType(it) }) {
    val innerTypes = HashSet<Type>(set.size)
    for (t in set) {
      when (t) {
        is MapType -> innerTypes.add(t.elements ?: Types.Any)
        is ObjectType -> t.elements?.values?.filterNotNullTo(innerTypes)
      }
    }
    val commonInnerType = getCommonSupertype(innerTypes) ?: Types.Any
    return MapType(commonInnerType)
  }

  if (set.any { it is OptionalType }) {
    return getCommonSupertype(set.map { if (it is OptionalType) it.elements else it })
  }

  // TODO: Consider using isConvertibleTo
  return null
}


// Based on getConversionKnown from
// github.com/zclconf/go-cty/cty/convert/conversion.go
fun Type.isConvertibleTo(other: Type): Boolean {
  if (this == other) return true

  if (other == Types.Any) return true
  if (other is OptionalType) {
    if (this is OptionalType) {
      return this.elements == null || other.elements == null || this.elements.isConvertibleTo(other.elements)
    }
    return other.elements == null || this.isConvertibleTo(other.elements)
  }

  when (this) {
    Types.String -> return other in setOf(Types.Number, Types.Boolean)
    Types.Number -> return other == Types.String
    Types.Boolean -> return other == Types.String
    Types.Null -> return true
    Types.Any -> return true

    is PrimitiveType -> return false

    is ObjectType -> {
      if (other is MapType) {
        if (this.elements == null || other.elements == null) return true
        if (this.elements!!.isEmpty()) return true
        val uniq = this.elements!!.values.filterNotNull().toSet()
        return uniq.size == 1 && uniq.first().isConvertibleTo(other.elements)
      }
      if (other is ObjectType) {
        // other may have same or less elements
        if (this.elements == null || other.elements == null) return true
        if (!this.elements!!.keys.containsAll(other.elements!!.filterValues { it !is OptionalType }.keys)) return false
        for ((name, type) in this.elements!!) {
          if (type is OptionalType) continue
          if (!other.elements!!.containsKey(name)) continue
          if (type == null) continue // TODO: check this
          val oT = other.elements!![name] ?: continue
          if (!type.isConvertibleTo(oT)) return false
        }
        return true
      }
    }

    is MapType -> {
      if (other is MapType) {
        if (this.elements == null || other.elements == null) return true
        return this.elements.isConvertibleTo(other.elements)
      }
      if (other is ObjectType) {
        // generally it's thin ice

        if (this.elements == null || other.elements == null) return true
        return other.elements!!.values.filterNotNull().toSet().all { this.elements.isConvertibleTo(it) }
      }
      return false
    }

    is TupleType -> {
      if (other is TupleType) {
        if (this.elements.size != other.elements.size) return false
        for (i in this.elements.indices) {
          val tT = this.elements[i] ?: continue
          val oT = other.elements[i] ?: continue
          if (!tT.isConvertibleTo(oT)) return false
        }
        return true
      }
      if (other is ListType) {
        // tuple(number, any) -> list(any) - OK
        if (other.elements == null) return true
        return this.elements.filterNotNull().all { it.isConvertibleTo(other.elements) }
      }
      if (other is SetType) {
        // tuple(number, any) -> set(any) - OK
        if (other.elements == null) return true
        return this.elements.filterNotNull().all { it.isConvertibleTo(other.elements) }
      }
      return false
    }

    is ListType -> return (other is SetType && (this.elements == null || other.elements == null || this.elements.isConvertibleTo(other.elements))) ||
        (other is ListType && (this.elements == null || other.elements == null || this.elements.isConvertibleTo(other.elements)))
    is SetType -> return (other is SetType && (this.elements == null || other.elements == null || this.elements.isConvertibleTo(other.elements))) ||
        (other is ListType && (this.elements == null || other.elements == null || this.elements.isConvertibleTo(other.elements)))

  }


  return false
}

open class BaseModelType(val description: String? = null,
                         val description_kind: String? = null,
                         val optional: Boolean = false,
                         val required: Boolean = false,
                         val computed: Boolean = false,
                         val deprecated: String? = null,
                         val conflictsWith: List<String>? = null
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is BaseModelType) return false

    if (description != other.description) return false
    if (optional != other.optional) return false
    if (required != other.required) return false
    if (computed != other.computed) return false
    if (deprecated != other.deprecated) return false
    if (conflictsWith != other.conflictsWith) return false

    return true
  }

  override fun hashCode(): Int {
    var result = description?.hashCode() ?: 0
    result = 31 * result + required.hashCode()
    result = 31 * result + (deprecated?.hashCode() ?: 0)
    result = 31 * result + computed.hashCode()
    result = 31 * result + (conflictsWith?.hashCode() ?: 0)
    return result
  }
}

//region hints
interface Hint
open class SimpleHint(vararg val hint: String) : Hint

// TODO: Use some 'Reference' class
open class ReferenceHint(vararg val hint: String) : Hint

open class SimpleValueHint(vararg hint: String) : SimpleHint(*hint)
//endregion hints

// TODO: Support 'default' values for certain types
open class PropertyType(override val name: String, val type: Type,
                        val hint: Hint? = null,
                        val injectionAllowed: Boolean = true,
                        description: String? = null,
                        description_kind: String? = null,
                        optional: Boolean = true, required: Boolean = false, computed: Boolean = false,
                        val sensitive: Boolean = false,
                        deprecated: String? = null,
                        conflictsWith: List<String>? = null,
                        val has_default: Boolean = false
) : BaseModelType(description = description, description_kind = description_kind,
                  optional = optional && !required, required = required, computed = computed,
                  deprecated = deprecated, conflictsWith = conflictsWith), PropertyOrBlockType {

  override fun toString(): String {
    return "PropertyType(name='$name', type='${type.presentableText}')"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    if (!super.equals(other)) return false

    other as PropertyType

    if (name != other.name) return false
    if (type != other.type) return false
    if (hint != other.hint) return false
    if (injectionAllowed != other.injectionAllowed) return false
    if (has_default != other.has_default) return false

    return true
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + name.hashCode()
    result = 31 * result + type.hashCode()
    result = 31 * result + (hint?.hashCode() ?: 0)
    result = 31 * result + injectionAllowed.hashCode()
    result = 31 * result + has_default.hashCode()
    return result
  }

}

enum class NestingType {
  NestingSingle,
  NestingGroup,
  NestingList,
  NestingSet,
  NestingMap,
  ;

  companion object {
    fun fromString(s: String): NestingType? {
      return when (s) {
        "single" -> NestingSingle
        "group" -> NestingGroup
        "list" -> NestingList
        "set" -> NestingSet
        "map" -> NestingMap
        else -> null
      }
    }
  }
}

data class NestingInfo(val type: NestingType, val mix: Int?, val max: Int?)

open class BlockType(val literal: String, val args: Int = 0,
                     description: String? = null,
                     description_kind: String? = null,
                     optional: Boolean = true, required: Boolean = false, computed: Boolean = false,
                     deprecated: String? = null,
                     conflictsWith: List<String>? = null,
                     val nesting: NestingInfo? = null,
                     val properties: Map<String, PropertyOrBlockType> = emptyMap()
) : BaseModelType(description = description, description_kind = description_kind,
                  optional = optional && !required, required = required, computed = computed,
                  deprecated = deprecated, conflictsWith = conflictsWith), PropertyOrBlockType, ObjectType {
  override val name: String
    get() = literal

  override val elements: Map<String, Type?>
    get() = properties.mapValues {
      when (val value = it.value) {
        is PropertyType -> value.type
        is BlockType -> value
        else -> throw IllegalStateException("Unexpected value type: ${value.javaClass.canonicalName}")
      }
    }

  override val presentableText: String
    get() {
      if (elements.isEmpty()) {
        return literal
      }
      return "${literal}({${elements.entries.joinToString(", ") { it.key + "=" + it.value }}})"
    }


  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    if (!super.equals(other)) return false

    other as BlockType

    if (literal != other.literal) return false
    if (args != other.args) return false
    if (properties != other.properties) return false

    return true
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + literal.hashCode()
    result = 31 * result + args
    result = 31 * result + properties.hashCode()
    return result
  }

  override fun toString(): String {
    return "BlockType(${this.presentableText})"
  }
}

interface PropertyOrBlockType {
  companion object {
    val EMPTY_ARRAY: Array<out PropertyOrBlockType> = emptyArray()
  }

  val name: String

  // If one of optional or required is set, then this item can come from the configuration.
  // Both cannot be set.
  // If Optional is set, the value is optional.
  // If Required is set, the value is required.
  //
  // One of these must be set if the value is not computed.
  // That is: value either comes from the config, is computed, or is both.
  val optional: Boolean
  val required: Boolean
  val computed: Boolean

  val deprecated: String?
  val conflictsWith: List<String>?

  val configurable: Boolean
    get() = optional || required
}

object Types {
  val Identifier = TypeImpl("identifier")

  val String = PrimitiveType("string")
  val Number = PrimitiveType("number")
  val Boolean = PrimitiveType("bool")
  val Null = PrimitiveType("null") // TODO: Unify Null and Any
  val Any = PrimitiveType("any") // supertype, like java.lang.Object

  val Array = ListType(null)
  val Object = ObjectType(null)
  val Invalid = TypeImpl("invalid") // special value for parsing errors, unsupported types, etc


  // Separate, as could be used as String, Number, Boolean, etc
  val StringWithInjection = PrimitiveType("string")

  val SimpleValueTypes = setOf(String, Number, Boolean)

  // cty types
  val Expression = TypeImpl("expression")
}

class ResourceType(val type: String, val provider: ProviderType, properties: List<PropertyOrBlockType>) : BlockType("resource", 2, properties = withDefaults(properties, TypeModel.AbstractResource)) {
  override fun toString(): String {
    return "ResourceType(type='$type', provider=${provider.type})"
  }

  override val presentableText: String
    get() = "resource($type)"
}

class DataSourceType(val type: String, val provider: ProviderType, properties: List<PropertyOrBlockType>) : BlockType("data", 2, properties = withDefaults(properties, TypeModel.AbstractDataSource)) {
  override fun toString(): String {
    return "DataSourceType(type='$type', provider=${provider.type})"
  }

  override val presentableText: String
    get() = "data-source($type)"
}

class ProviderType(val type: String, properties: List<PropertyOrBlockType>) : BlockType("provider", 1, properties = withDefaults(properties, TypeModel.AbstractProvider)) {
  override fun toString(): String {
    return "ProviderType(type='$type')"
  }

  override val presentableText: String
    get() = "provider($type)"
}

class ProvisionerType(val type: String, properties: List<PropertyOrBlockType>) : BlockType("provisioner", 1, properties = withDefaults(properties, TypeModel.AbstractResourceProvisioner)) {
  override fun toString(): String {
    return "ProvisionerType(type='$type')"
  }

  override val presentableText: String
    get() = "provisioner($type)"
}

class BackendType(val type: String, properties: List<PropertyOrBlockType>) : BlockType("backend", 1, properties = withDefaults(properties, TypeModel.AbstractBackend)) {
  override fun toString(): String {
    return "BackendType(type='$type')"
  }

  override val presentableText: String
    get() = "backend($type)"
}

class ModuleType(override val name: String, properties: List<PropertyOrBlockType>) : BlockType("module", 1, properties = withDefaults(properties, TypeModel.Module)) {
  override fun toString(): String {
    return "ModuleType(name='$name')"
  }

  override val presentableText: String
    get() = "module($name)"
}

private fun withDefaults(properties: List<PropertyOrBlockType>, default: BlockType): Map<String, PropertyOrBlockType> {
  if (properties.isEmpty()) return default.properties
  val result = HashMap<String, PropertyOrBlockType>(default.properties.size + properties.size)
  result.putAll(default.properties)
  result.putAll(properties.toMap())
  return result
}