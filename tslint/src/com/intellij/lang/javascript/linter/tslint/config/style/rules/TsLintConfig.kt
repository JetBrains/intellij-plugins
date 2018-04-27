package com.intellij.lang.javascript.linter.tslint.config.style.rules

import com.google.gson.Gson
import com.intellij.application.options.CodeStyle
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CodeStyleSettingsManager
import com.intellij.psi.util.*
import org.yaml.snakeyaml.Yaml

class TsLintConfigWrapper(private val options: Map<String, TslintJsonOption>) {

  fun getOption(name: String): TslintJsonOption? = options[name]

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

  companion object {
    private val RULES_CACHE_KEY = Key.create<ParameterizedCachedValue<TsLintConfigWrapper, PsiFile>>("tslint.cache.key.config.json")

    private val CACHED_VALUE_PROVIDER: ParameterizedCachedValueProvider<TsLintConfigWrapper, PsiFile> = ParameterizedCachedValueProvider {
      if (it == null || PsiTreeUtil.hasErrorElements(it)) {
        CachedValueProvider.Result.create(null, it)
      }
      CachedValueProvider.Result.create(parseConfigFile(it), it)
    }

    private fun parseConfigFile(it: PsiFile): TsLintConfigWrapper? {
      val map = jsonAsMap(it) ?: yamlAsMap(it)
      if (map != null) {
        val rulesObject = map["rules"]
        if (rulesObject is Map<*, *>) {
          @Suppress("UNCHECKED_CAST")
          val typed = rulesObject as Map<String, Any>
          return TsLintConfigWrapper(typed.mapValues { TslintJsonOption(it.value) })
        }
      }
      return null
    }

    private fun jsonAsMap(it: PsiFile): Map<String, Any>? {
      return try {
        Gson().fromJson<MutableMap<String, Any>>(it.text, MutableMap::class.java)
      }
      catch (e: Exception) {
        null
      }
    }

    private fun yamlAsMap(file: PsiFile): Map<String, Any>? {
      return try {
        Yaml().load<Map<String, Any>>(file.text)
      }
      catch (e: Exception) {
        null
      }
    }

    fun getConfigForFile(psiFile: PsiFile?): TsLintConfigWrapper? {
      if (psiFile == null) return null
      return CachedValuesManager.getManager(psiFile.project)
               .getParameterizedCachedValue(psiFile, RULES_CACHE_KEY, CACHED_VALUE_PROVIDER, false, psiFile) ?: return null
    }
  }
}

class TslintJsonOption(private val element: Any?) {
  fun isEnabled(): Boolean {
    if (element is Boolean) {
      return element
    }
    if (element is List<*> && element.size > 0 && element[0] is Boolean) {
      return element[0] as Boolean
    }
    if (element is Map<*, *>) {
      val severityValue = element["severity"]
      return !("none" == severityValue || "off" == severityValue)
    }

    return false
  }

  fun getStringValues(): Collection<String> {
    if (element is List<*>) {
      return element.drop(1).mapNotNull { it as? String }
    }
    if (element is Map<*, *>) {
      return asStringArrayOrSingleString(element["options"])
    }

    return emptyList()
  }

  fun getNumberValue(): Int? {
    if (element is List<*> && element.size > 1 && element[1] is Number) {
      return (element[1] as Number).toInt()
    }
    if (element is Map<*, *>) {
      val optionsValue = element["options"]
      if (optionsValue is Number) {
        return optionsValue.toInt()
      }
      if (optionsValue is List<*> && optionsValue.size > 0 && optionsValue[0] is Number) {
        return (optionsValue[0] as Number).toInt()
      }
    }

    return null
  }

  fun getStringMapValue(): Map<String, String> {
    if (element is List<*> && element.size > 1) {
      return asStringMap(element[1])
    }
    if (element is Map<*, *>) {
      return asStringMap(element["options"])
    }

    return emptyMap()
  }

  private fun asStringArrayOrSingleString(element: Any?): List<String> {
    if (element is String) {
      return listOf(element)
    }
    if (element is List<*>) {
      return element.mapNotNull { it as? String }
    }
    return emptyList()
  }

  private fun asStringMap(element: Any?): Map<String, String> {
    if (element is Map<*, *>) {
      val result = mutableMapOf<String, String>()
      element.forEach {
        if (it.key is String && it.value is String) {
          result[it.key as String] = it.value as String
        }
      }
      return result
    }
    return emptyMap()
  }
}