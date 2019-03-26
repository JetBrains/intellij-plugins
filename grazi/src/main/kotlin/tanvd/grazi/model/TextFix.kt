package tanvd.grazi.model

/** Representation of fix for text mistake */
data class TextFix(val range: IntRange, val description: String, val category: TyposCategories, val fix: List<String>? = null) {
    val fullDescription: String
        get() {
            if (description.isBlank())
                return category.description
            if (description.contains(":"))
                return description
            return "${category.description}: $description"
        }
}
