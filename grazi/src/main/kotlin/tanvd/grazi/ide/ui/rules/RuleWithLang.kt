package tanvd.grazi.ide.ui.rules

import org.languagetool.rules.Category
import org.languagetool.rules.Rule
import tanvd.grazi.GraziConfig
import tanvd.grazi.language.Lang
import tanvd.grazi.language.LangTool
import java.util.*
import kotlin.Comparator

data class RuleWithLang(val rule: Rule, val lang: Lang, val enabled: Boolean, var enabledInTree: Boolean) {
    val category: Category = rule.category
}

typealias RulesMap = Map<Lang, Map<Category, List<RuleWithLang>>>

fun LangTool.allRulesWithLangs(): RulesMap {
    val state = GraziConfig.get()

    val result = TreeMap<Lang, SortedMap<Category, MutableList<RuleWithLang>>>()
    state.enabledLanguages.forEach { lang ->
        val categories = TreeMap<Category, MutableList<RuleWithLang>>(Comparator.comparing(Category::getName))

        with(get(lang)) {
            val activeRules = allActiveRules.toSet()

            fun Rule.isActive() = (id in state.userEnabledRules && id !in state.userDisabledRules)
                    || (id !in state.userDisabledRules && id !in state.userEnabledRules && this in activeRules)

            allRules.distinctBy { it.id }.forEach {
                categories.getOrPut(it.category, ::LinkedList).add(RuleWithLang(it, lang, enabled = it.isActive(), enabledInTree = it.isActive()))
            }

            if (categories.isNotEmpty()) result[lang] = categories
        }
    }

    return result
}
