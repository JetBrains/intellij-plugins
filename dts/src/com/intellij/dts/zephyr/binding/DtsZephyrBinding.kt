package com.intellij.dts.zephyr.binding

import com.intellij.dts.lang.DtsPropertyType
import com.intellij.dts.lang.DtsPropertyValue
import com.intellij.openapi.util.NlsSafe

data class DtsZephyrBinding(
  val compatible: @NlsSafe String?,
  val path: @NlsSafe String?,
  val description: @NlsSafe String?,
  val properties: Map<String, DtsZephyrPropertyBinding>,
  val child: DtsZephyrBinding?,
  val isChild: Boolean,
  val allowUndeclaredProperties: Boolean,
) {
  class Builder(
    private val isChild: Boolean = false,
  ) {
    private var compatible: String? = null
    private var path: String? = null
    private var description: String? = null
    private var properties: MutableMap<String, DtsZephyrPropertyBinding.Builder> = mutableMapOf()
    private var child: Builder? = null
    private var allowUndeclaredProperties: Boolean? = null

    fun setCompatible(value: String): Builder {
      if (compatible == null) compatible = value
      return this
    }

    fun setPath(value: String): Builder {
      if (path == null) path = value
      return this
    }

    fun setDescription(value: String): Builder {
      if (description == null) description = value
      return this
    }

    fun setAllowUndeclaredProperties(value: Boolean): Builder {
      allowUndeclaredProperties = value
      return this
    }

    fun getPropertyBuilder(name: String): DtsZephyrPropertyBinding.Builder {
      return properties.getOrPut(name) { DtsZephyrPropertyBinding.Builder(name) }
    }

    fun getChildBuilder(): Builder {
      child?.let { return it }
      return Builder(isChild = true).also { child = it }
    }

    fun build(): DtsZephyrBinding {
      compatible?.let { child?.setCompatible(it) }

      return DtsZephyrBinding(
        compatible = compatible,
        path = path,
        description = description,
        properties = properties.mapValues { (_, builder) -> builder.build() },
        child = child?.build(),
        isChild = isChild,
        allowUndeclaredProperties = allowUndeclaredProperties ?: false,
      )
    }
  }
}

data class DtsZephyrPropertyBinding(
  val name: @NlsSafe String,
  val description: @NlsSafe String?,
  val type: DtsPropertyType,
  val default: DtsPropertyValue?,
  val const: DtsPropertyValue?,
  val enum: List<DtsPropertyValue>?,
  val required: Boolean,
) {
  class Builder(private val name: String) {
    private var description: String? = null
    private var type: DtsPropertyType? = null
    private var required: Boolean? = null
    private var default: DtsPropertyValue? = null
    private var const: DtsPropertyValue? = null
    private var enum: List<DtsPropertyValue>? = null

    fun setDescription(value: String): Builder {
      if (description == null) description = value
      return this
    }

    fun setType(value: String): Builder {
      if (type == null) type = DtsPropertyType.fromZephyr(value)
      return this
    }

    fun setDefault(value: Any): Builder {
      if (default == null) default = DtsPropertyValue.fromZephyr(value)
      return this
    }

    fun setConst(value: Any): Builder {
      if (const == null) const = DtsPropertyValue.fromZephyr(value)
      return this
    }

    fun setEnum(value: List<Any>) {
      if (enum != null) return

      val mapped = value
        .mapNotNull(DtsPropertyValue.Companion::fromZephyr)
        .filter { it is DtsPropertyValue.String || it is DtsPropertyValue.Int }

      if (mapped.size != value.size) return

      enum = mapped
    }

    fun setRequired(value: Boolean): Builder {
      required = value
      return this
    }

    fun build(): DtsZephyrPropertyBinding = DtsZephyrPropertyBinding(
      name = name,
      description = description,
      type = type ?: DtsPropertyType.Compound,
      required = required ?: false,
      default = default,
      const = const,
      enum = enum,
    )
  }
}