package tanvd.grazi.utils

val camelCaseRegex = Regex("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")

fun String.splitCamelCase() = this.split(camelCaseRegex)
