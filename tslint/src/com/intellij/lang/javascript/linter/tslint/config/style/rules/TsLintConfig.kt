package com.intellij.lang.javascript.linter.tslint.config.style.rules

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.intellij.application.options.CodeStyle
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.project.Project
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CodeStyleSettingsManager
import com.intellij.webcore.util.JsonUtil

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

  fun getRulesToApply(project: Project): Collection<TsLintSimpleRule<*>> {
    ApplicationManager.getApplication().assertReadAccessAllowed()
    val settings = current(project)
    val languageSettings = language(settings)
    val jsCodeStyleSettings = custom(settings)

    return TslintRulesSet.filter { it.isAvailable(project, languageSettings, jsCodeStyleSettings, this) }
  }

  private fun current(project: Project) = CodeStyle.getSettings(project)
  private fun language(settings: CodeStyleSettings) = settings.getCommonSettings(JavaScriptSupportLoader.TYPESCRIPT)
  private fun custom(settings: CodeStyleSettings) = settings.getCustomSettings(TypeScriptCodeStyleSettings::class.java)

  fun getCurrentSettings(project: Project, rules: Collection<TsLintSimpleRule<*>>): Map<TsLintSimpleRule<*>, Any?> {
    ApplicationManager.getApplication().assertReadAccessAllowed()
    val settings = current(project)
    val languageSettings = language(settings)
    val jsCodeStyleSettings = custom(settings)

    return rules.associate { Pair(it, it.getSettingsValue(languageSettings, jsCodeStyleSettings)) }
  }

  fun applyValues(project: Project, values: Map<TsLintSimpleRule<*>, *>) {
    val settings = current(project)
    val languageSettings = language(settings)
    val jsCodeStyleSettings = custom(settings)
    WriteAction.run<RuntimeException> {
      values.forEach { key, value -> key.setDirectValue(languageSettings, jsCodeStyleSettings, value) }
    }
  }

  fun applyRules(project: Project, rules: Collection<TsLintSimpleRule<*>>) {
    WriteAction.run<RuntimeException> {
      val settingsManager = CodeStyleSettingsManager.getInstance(project)
      if (!settingsManager.USE_PER_PROJECT_SETTINGS) {
        settingsManager.mainProjectCodeStyle = settingsManager.currentSettings.clone()
        settingsManager.USE_PER_PROJECT_SETTINGS = true
      }
      val newSettings = settingsManager.currentSettings
      val newLanguageSettings = language(newSettings)
      val newJsCodeStyleSettings = custom(newSettings)
      rules.forEach { rule -> rule.apply(project, newLanguageSettings, newJsCodeStyleSettings, this) }
    }

  }
}

class TsLintConfigOption(val element: JsonElement) {
  fun isEnabled(): Boolean {
    if (element.isJsonPrimitive) {
      return element.asBoolean
    }
    if (element.isJsonArray) {
      val jsonArray = element.asJsonArray
      return jsonArray.count() > 0 && jsonArray[0].isJsonPrimitive && jsonArray[0].asBoolean
    }
    if (element.isJsonObject) {
      val jsonObject = element.asJsonObject
      val severityValue = jsonObject.get("severity")
      return severityValue != null
             && severityValue.isJsonPrimitive
             && !("none" == severityValue.asString || "off" == severityValue.asString)
    }

    return false
  }

  fun getStringValues(): Collection<String> {
    if (element.isJsonArray) {
      return element.asJsonArray.drop(1).mapNotNull { if (it.isJsonPrimitive) it.asString else null }
    }
    if (element.isJsonObject) {
      return asStringArrayOrSingleString(element.asJsonObject.get("options"))
    }

    return emptyList()
  }

  fun getNumberValue(): Int? {
    if (element.isJsonArray) {
      val jsonArray = element.asJsonArray
      if (jsonArray.count() > 1 && jsonArray[1].isJsonPrimitive)
        return jsonArray[1].asInt
      return null
    }
    if (element.isJsonObject) {
      val optionsElement = element.asJsonObject.get("options")
      if (optionsElement.isJsonPrimitive) {
        return optionsElement.asInt
      }
      if (optionsElement.isJsonArray && optionsElement.asJsonArray.size() > 0) {
        return optionsElement.asJsonArray[0].asInt
      }
    }

    return null
  }

  fun getStringMapValue(): Map<String, String> {
    if (element.isJsonArray) {
      val jsonArray = element.asJsonArray
      return if (jsonArray.count() > 1) asStringMap(jsonArray[1]) else emptyMap()
    }
    if (element.isJsonObject) {
      return asStringMap(element.asJsonObject.get("options"))
    }

    return emptyMap()
  }

  private fun asStringArrayOrSingleString(jsonObject: JsonElement?): List<String> {
    if (jsonObject == null) {
      return emptyList()
    }
    if (jsonObject.isJsonPrimitive) {
      return listOf(jsonObject.asString)
    }
    if (jsonObject.isJsonArray) {
      return JsonUtil.getAsStringList(jsonObject) ?: emptyList()
    }
    return emptyList()
  }

  private fun asStringMap(first: JsonElement?): Map<String, String> {
    if (first == null || !first.isJsonObject) {
      return emptyMap()
    }
    val resultObject = first.asJsonObject
    val result = mutableMapOf<String, String>()
    resultObject.entrySet().forEach {
      if (it.value.isJsonPrimitive) {
        result[it.key] = it.value.asString
      }
    }

    return result
  }
}

