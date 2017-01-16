package com.intellij.lang.javascript.linter.tslint.config.style.rules

import com.google.gson.JsonElement
import com.google.gson.JsonObject

private val TO_PROCESS = arrayOf("rules", "jsRules")

class TsLintConfigWrapper(config: JsonObject) {

  private val options: Map<String, TsLintConfigOption>

  init {
    val result = mutableMapOf<String, TsLintConfigOption>()

    TO_PROCESS.forEach { name ->
      if (config.has(name)) {
        val jsRules = config[name]
        if (jsRules.isJsonObject) {
          jsRules.asJsonObject.entrySet().forEach { result[it.key] = TsLintConfigOption(it.value) }
        }
      }
    }

    options = result
  }

  fun getOption(name: String): TsLintConfigOption? = options[name]
}

class TsLintConfigOption(val element: JsonElement) {
  fun isTrue(): Boolean = element.isJsonPrimitive && element.asJsonPrimitive.isBoolean && element.asBoolean
}