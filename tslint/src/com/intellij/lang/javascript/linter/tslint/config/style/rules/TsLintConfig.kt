package com.intellij.lang.javascript.linter.tslint.config.style.rules

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.project.Project
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CodeStyleSettingsManager

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
    val languageSettings = language(settings) ?: return emptyList()
    val jsCodeStyleSettings = custom(settings) ?: return emptyList()

    return TslintRulesSet.filter { it.isAvailable(project, languageSettings, jsCodeStyleSettings, this) }
  }

  private fun current(project: Project) = CodeStyleSettingsManager.getInstance(project).currentSettings
  private fun language(settings: CodeStyleSettings) = settings.getCommonSettings(JavaScriptSupportLoader.TYPESCRIPT)
  private fun custom(settings: CodeStyleSettings) = settings.getCustomSettings(TypeScriptCodeStyleSettings::class.java)

  fun getCurrentSettings(project: Project, rules: Collection<TsLintSimpleRule<*>>): Map<TsLintSimpleRule<*>, Any?> {
    ApplicationManager.getApplication().assertReadAccessAllowed()
    val settings = current(project)
    val languageSettings = language(settings) ?: return emptyMap()
    val jsCodeStyleSettings = custom(settings) ?: return emptyMap()

    return rules.associate { Pair(it, it.getSettingsValue(languageSettings, jsCodeStyleSettings)) }
  }

  fun applyValues(project: Project, values: Map<TsLintSimpleRule<*>, *>) {
    val settings = current(project)
    val languageSettings = language(settings) ?: return
    val jsCodeStyleSettings = custom(settings) ?: return
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
  fun isTrue(): Boolean {
    if (element.isJsonPrimitive) {
      return element.asBoolean
    }

    if (element.isJsonArray) {
      val jsonArray = element.asJsonArray
      if (jsonArray.count() == 0) {
        return false
      }

      val value = jsonArray[0]

      return value.isJsonPrimitive && value.asBoolean
    }

    return false
  }

  fun getStringValues(): Collection<String> {
    if (element.isJsonArray) {
      val jsonArray = element.asJsonArray
      if (jsonArray.count() == 0) {
        return emptyList()
      }
      val first = jsonArray[0]

      return jsonArray.mapNotNull { if (first != it && it.isJsonPrimitive) it.asString else null }
    }

    return emptyList()
  }


  fun getSecondNumberValue(): Int? {
    if (element.isJsonArray) {
      val jsonArray = element.asJsonArray
      if (jsonArray.count() < 2) {
        return null
      }
      val first = jsonArray[1]

      if (!first.isJsonPrimitive) {
        return null
      }

      return first.asInt
    }

    return null
  }

  fun getSecondIndexValues(): Map<String, String> {
    if (element.isJsonArray) {
      val jsonArray = element.asJsonArray
      if (jsonArray.count() < 2) {
        return emptyMap()
      }
      val first = jsonArray[1]

      if (!first.isJsonObject) {
        return emptyMap()
      }
      val resultObject = first.asJsonObject
      val result = mutableMapOf<String, String>()
      resultObject.entrySet().forEach {
        if (it.value.isJsonPrimitive) {
          result.put(it.key, it.value.asString)
        }
      }

      return result
    }

    return emptyMap()
  }
}