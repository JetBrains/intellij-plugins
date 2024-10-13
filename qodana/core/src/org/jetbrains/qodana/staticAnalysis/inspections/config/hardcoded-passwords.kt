package org.jetbrains.qodana.staticAnalysis.inspections.config

import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import java.util.regex.PatternSyntaxException

data class HardcodedPasswordsConfig(
  val reportDefaultSuspiciousVariableNames: Boolean? = null,

  val ignoreVariableNames: List<String> = emptyList(),
  val ignoreVariableValues: List<String> = emptyList(),
  val ignoreVariableNamesValues: List<NameAndValue> = emptyList(),

  val variableNames: List<String> = emptyList(),
  val variableValues: List<String> = emptyList(),
  val variableNamesValues: List<NameAndValue> = emptyList(),
) {
  data class NameAndValue(
    val name: String? = null,
    val value: String? = null
  )
}

data class HardcodedPasswords(
  val reportDefaultSuspiciousVariableNames: Boolean?,

  val ignoreVariableNames: List<Regex>,
  val ignoreVariableValues: List<Regex>,
  val ignoreVariableNamesValues: List<NameAndValue>,

  val variableNames: List<Regex>,
  val variableValues: List<Regex>,
  val variableNamesValues: List<NameAndValue>,
) {
  data class NameAndValue(
    val name: Regex,
    val value: Regex
  )

  companion object {
    fun fromConfig(config: HardcodedPasswordsConfig): HardcodedPasswords {
      return HardcodedPasswords(
        reportDefaultSuspiciousVariableNames = config.reportDefaultSuspiciousVariableNames,

        ignoreVariableNames = config.ignoreVariableNames.map(::parseRegex),
        ignoreVariableValues = config.ignoreVariableValues.map(::parseRegex),
        ignoreVariableNamesValues = config.ignoreVariableNamesValues.map { parseNameAndValueFromConfig(it, "ignoreVariableNamesValues") },

        variableNames = config.variableNames.map(::parseRegex),
        variableValues = config.variableValues.map(::parseRegex),
        variableNamesValues = config.variableNamesValues.map { parseNameAndValueFromConfig(it, "variableNamesValues") }
      )
    }

    private fun parseNameAndValueFromConfig(entry: HardcodedPasswordsConfig.NameAndValue, section: String): NameAndValue {
      if (entry.name == null || entry.value == null) {
        throw QodanaException("hardcodedPasswords $section.$entry pattern does not specify \"name\" and \"value\"")
      }
      return NameAndValue(parseRegex(entry.name), parseRegex(entry.value))
    }

    private fun parseRegex(regexString: String): Regex {
      return try {
        Regex(regexString)
      }
      catch (e : PatternSyntaxException) {
        throw QodanaException("Rule $regexString is invalid regular expression, ${e.message}")
      }
    }
  }
}