package tanvd.grazi.language

import org.languagetool.rules.Rule
import tanvd.grazi.utils.decapitalizeIfNotAbbreviation
import tanvd.grazi.utils.safeSubstring

enum class LangToolFixes(private val ruleId: String, private val fix: (suggestion: String) -> String) {
    ARTICLE_MISSING("ARTICLE_MISSING", {
        val suggestion = it.replace("\\s+".toRegex(), " ")
        when {
            suggestion.startsWith("The") -> "The " + suggestion.safeSubstring(4).decapitalizeIfNotAbbreviation()
            suggestion.startsWith("An") -> "An " + suggestion.safeSubstring(3).decapitalizeIfNotAbbreviation()
            suggestion.startsWith("A") -> "A " + suggestion.safeSubstring(2).decapitalizeIfNotAbbreviation()
            else -> suggestion
        }
    }),
    THE_SUPERLATIVE("THE_SUPERLATIVE", {
        val suggestion = it.replace("\\s+".toRegex(), " ")
        when {
            suggestion.startsWith("The") -> "The " + suggestion.safeSubstring(4).decapitalizeIfNotAbbreviation()
            else -> suggestion
        }
    });

    companion object {
        fun fixSuggestion(ruleId: String, suggestion: String) = values().find { it.ruleId == ruleId }?.let { it.fix(suggestion) } ?: suggestion
        fun fixSuggestion(rule: Rule, suggestion: String) = fixSuggestion(rule.id, suggestion)
    }
}

