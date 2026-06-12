package com.intellij.openRewrite.recipe

import com.intellij.openRewrite.YAML_KEY_PRECONDITIONS
import com.intellij.openRewrite.YAML_KEY_RECIPE_LIST
import com.intellij.openRewrite.YAML_KEY_STYLE_CONFIGS

enum class OpenRewriteType(val listKey: String, val additionalListKeys: List<String> = emptyList()) {
  RECIPE(YAML_KEY_RECIPE_LIST, listOf(YAML_KEY_PRECONDITIONS)),
  STYLE(YAML_KEY_STYLE_CONFIGS)
}