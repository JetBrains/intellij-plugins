package com.intellij.lang.javascript.linter.tslint.codestyle.rules

import com.google.gson.Gson
import com.intellij.application.options.CodeStyle
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lang.javascript.formatter.JSCodeStyleUtil
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.util.*
import org.yaml.snakeyaml.Yaml

private val LOG = Logger.getInstance(TsLintConfigWrapper::class.java)

internal class TsLintConfigWrapper(private val rules: Map<String, TslintJsonOption>, private val extends: List<String>) {
  fun hasExtends(): Boolean = extends.isNotEmpty()

  private fun getOption(name: String): TslintJsonOption? = rules[name]

  fun getRulesToApply(project: Project): Collection<TsLintRule> {
    ApplicationManager.getApplication().assertReadAccessAllowed()
    val settings = current(project)
    val languageSettings = language(settings)
    val jsCodeStyleSettings = custom(settings)

    return TslintRulesSet.filter {
      val option = getOption(it.optionId)
      option != null && option.isEnabled() && it.isAvailable(languageSettings, jsCodeStyleSettings, option)
    }
  }

  private fun current(project: Project) = CodeStyle.getSettings(project)
  private fun language(settings: CodeStyleSettings) = settings.getCommonSettings(JavaScriptSupportLoader.TYPESCRIPT)
  private fun custom(settings: CodeStyleSettings) = settings.getCustomSettings(TypeScriptCodeStyleSettings::class.java)

  fun applyRules(project: Project, rules: Collection<TsLintRule>) {
    JSCodeStyleUtil.updateProjectCodeStyle(project) { newSettings ->
      WriteAction.run<RuntimeException> {
        val newLanguageSettings = language(newSettings)
        val newJsCodeStyleSettings = custom(newSettings)
        rules.forEach {
          val option = getOption(it.optionId)
          if (option != null) {
            it.apply(newLanguageSettings, newJsCodeStyleSettings, option)
          }
        }
      }
    }
  }

  companion object {
    private val RULES_CACHE_KEY = Key.create<ParameterizedCachedValue<TsLintConfigWrapper, PsiFile>>("tslint.cache.key.config.json")

    private val CACHED_VALUE_PROVIDER: ParameterizedCachedValueProvider<TsLintConfigWrapper, PsiFile> = ParameterizedCachedValueProvider {
      if (it == null || PsiTreeUtil.hasErrorElements(it)) {
        CachedValueProvider.Result.create(null, it)
      }
      CachedValueProvider.Result.create(getConfigFromText(it.text), it)
    }

    fun getConfigForFile(psiFile: PsiFile?): TsLintConfigWrapper? {
      if (psiFile == null) return null
      return CachedValuesManager.getManager(psiFile.project)
               .getParameterizedCachedValue(psiFile, RULES_CACHE_KEY, CACHED_VALUE_PROVIDER, false, psiFile) ?: return null
    }

    fun getConfigFromText(text: String?): TsLintConfigWrapper? {
      val map = jsonAsMap(text) ?: yamlAsMap(text)
      if (map != null) {
        val rulesObject = map["rules"]
        if (rulesObject is Map<*, *>) {
          @Suppress("UNCHECKED_CAST")
          val typed = rulesObject as Map<String, Any>
          return TsLintConfigWrapper(typed.mapValues { TslintJsonOption(it.value) }, asStringArrayOrSingleString(map["extends"]))
        }
      }
      return null
    }

    private fun jsonAsMap(text: String?): Map<String, Any>? {
      return try {
        Gson().fromJson<MutableMap<String, Any>>(text, MutableMap::class.java)
      }
      catch (e: Exception) {
        LOG.debug("Could not parse JSON TSLint config from $text", e)
        null
      }
    }

    private fun yamlAsMap(text: String?): Map<String, Any>? {
      return try {
        Yaml().load<Map<String, Any>>(text)
      }
      catch (e: Exception) {
        LOG.debug("Could not parse YAML TSLint config from $text", e)
        null
      }
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

  fun getStringValues(): List<String> {
    return getOptionsList().filterIsInstance(String::class.java)
  }

  fun getOptionsList(): List<Any> {
    val options = getRuleOptions()
    if (options is List<*>) {
      return options.filterNotNull()
    }
    if (options != null) {
      return listOf(options)
    }
    return emptyList()
  }

  fun getNumberValue(): Int? {
    return asInt(getRuleOptions())
  }

  fun getStringMapValue(): Map<String, String> {
    return asStringMap(getRuleOptions())
  }

  private fun getRuleOptions(): Any? {
    if (this.element is List<*>) {
      if (this.element.size == 2) {
        return this.element[1]
      }
      return element.drop(1)
    }
    if (this.element is Map<*, *>) {
      val optionsElement = this.element["options"]
      if (optionsElement is List<*> && optionsElement.size == 1) {
        return optionsElement[0]
      }
      return optionsElement
    }
    return null
  }
}

private fun asInt(element: Any?): Int? {
  if (element is Number) {
    return element.toInt()
  }
  return null
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

private fun asStringArrayOrSingleString(element: Any?): List<String> {
  if (element is String) {
    return listOf(element)
  }
  if (element is List<*>) {
    return element.mapNotNull { it as? String }
  }
  return emptyList()
}