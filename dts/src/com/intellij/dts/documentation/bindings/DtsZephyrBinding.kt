package com.intellij.dts.documentation.bindings

data class DtsZephyrBinding(
    val compatible: String,
    val description: String?,
    val propertyDescriptions: Map<String, String>,
    val child: DtsZephyrBinding?,
    val isChild: Boolean,
) {
    class Builder(
        private val compatible: String,
        private val isChild: Boolean = false,
    ) {
        private var description: String? = null
        private var propertyDescriptions: MutableMap<String, String> = mutableMapOf()
        private var child: Builder? = null

        fun setDescription(value: String): Builder {
            if (description == null) description = value
            return this
        }

        fun setPropertyDescription(name: String, value: String): Builder {
            propertyDescriptions.putIfAbsent(name, value)
            return this
        }

        fun getChildBuilder(): Builder {
            child?.let { return it }
            return Builder(compatible, isChild = true).also { child = it }
        }

        fun build(): DtsZephyrBinding {
            return DtsZephyrBinding(
                compatible,
                description,
                propertyDescriptions,
                child?.build(),
                isChild,
            )
        }
    }
}