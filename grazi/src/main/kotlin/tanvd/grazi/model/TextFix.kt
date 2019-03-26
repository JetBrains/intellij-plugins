package tanvd.grazi.model

/** Representation of fix for text mistake */
data class TextFix(val range: IntRange, val description: String, val category: TyposCategories, val fix: List<String>? = null)
