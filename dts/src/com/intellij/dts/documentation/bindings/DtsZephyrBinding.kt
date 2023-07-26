package com.intellij.dts.documentation.bindings

data class DtsZephyrBinding(
    val compatible: String,
    val description: String?,
    val propertyDescriptions: Map<String, String>,
    val child: DtsZephyrBinding?
) {
    class Builder(private val compatible: String) {
        private var description: String? = null
        private var propertyDescriptions: MutableMap<String, String> = mutableMapOf()
        private var child: Builder? = null

        fun setDescription(value: String) {
            if (description == null) description = value
        }

        fun setPropertyDescription(name: String, value: String) {
            propertyDescriptions.putIfAbsent(name, value)
        }

        fun getChild(): Builder {
            child?.let { return it }
            return Builder(compatible).also { child = it }
        }

        fun build(): DtsZephyrBinding {
            return DtsZephyrBinding(
                compatible,
                description,
                propertyDescriptions,
                child?.build(),
            )
        }
    }
}