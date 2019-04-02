package tanvd.grazi.utils

val blankRegex = Regex("[\\n\\s]*")
/** Considers whitespaces, tabs and newlines */
fun String.isBlankWithNewLines() = blankRegex.matches(this)

val camelCaseRegex = Regex("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")
fun String.splitCamelCase() = this.split(camelCaseRegex)

fun String.splitSnakeCase() = this.split("_")


val urlRegex = Regex("(http(s)?://.)?(www\\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_+.~#?&//=]*)")
fun String.isUrl() = urlRegex.matches(this)
