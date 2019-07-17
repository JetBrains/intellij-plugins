package tanvd.grazi.grammar

import org.languagetool.rules.Rule

enum class LangToolFixes(val ruleId: String, val fixSuggestion: (suggestion: String) -> String = { it }) {
    ARTICLE_MISSING("ARTICLE_MISSING", { suggestion ->
        when {
            suggestion.startsWith("The") -> "The " + suggestion[4].toLowerCase() + suggestion.substring(5)
            suggestion.startsWith("A") -> "A " + suggestion[2].toLowerCase() + suggestion.substring(3)
            else -> suggestion
        }
    }),
    IDENTITY("", { it });

    companion object {
        operator fun get(ruleId: String): LangToolFixes = values().find { it.ruleId == ruleId } ?: IDENTITY
        operator fun get(rule: Rule): LangToolFixes = values().find { it.ruleId == rule.id } ?: IDENTITY
    }
}

