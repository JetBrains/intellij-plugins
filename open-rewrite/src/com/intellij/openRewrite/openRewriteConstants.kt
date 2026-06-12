package com.intellij.openRewrite

internal const val RECIPE_FILE_NAME = "rewrite.yml"

internal const val YAML_KEY_NAME = "name"
internal const val YAML_KEY_TYPE = "type"
internal const val YAML_KEY_RECIPE_LIST = "recipeList"
internal const val YAML_KEY_STYLE_CONFIGS = "styleConfigs"
internal const val YAML_KEY_PRECONDITIONS = "preconditions"

internal const val RECIPE_CLASS_NAME = "org.openrewrite.Recipe"
internal const val OPTION_CLASS_NAME = "org.openrewrite.Option"
internal const val STYLE_CLASS_NAME = "org.openrewrite.style.Style"
internal const val OPEN_REWRITE_PACKAGE_PREFIX = "org.openrewrite."

internal val REWRITE_TYPE_REGEX: Regex = Regex("specs\\.openrewrite\\.org/\\w+/(recipe|style)")
internal val RECIPE_TYPE_REGEX: Regex = Regex("specs\\.openrewrite\\.org/\\w+/recipe")
internal const val RECIPE_TYPE_SUFFIX: String = "recipe"
internal const val RECIPE_STYLE_SUFFIX: String = "style"

internal const val OPEN_REWRITE_NOTIFICATION_GROUP_ID = "OpenRewrite"