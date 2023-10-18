package com.intellij.dts.zephyr

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
    val type: DtsZephyrPropertyType,
    val required: Boolean,
) {
    class Builder(private val name: String) {
        private var description: String? = null
        private var type: DtsZephyrPropertyType? = null
        private var required: Boolean? = null

        fun setDescription(value: String): Builder {
            if (description == null) description = value
            return this
        }

        fun setType(value: String): Builder {
            if (type == null) type = DtsZephyrPropertyType.fromZephyr(value)
            return this
        }

        fun setRequired(value: Boolean): Builder {
            required = value
            return this
        }

        fun build(): DtsZephyrPropertyBinding = DtsZephyrPropertyBinding(
            name = name,
            description = description,
            type = type ?: DtsZephyrPropertyType.Compound,
            required = required ?: false,
        )
    }
}