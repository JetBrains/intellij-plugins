package tanvd.grazi

import com.intellij.openapi.components.BaseComponent
import tanvd.grazi.grammar.GrammarCache
import tanvd.grazi.grammar.GrammarEngine
import tanvd.grazi.language.LangChecker
import tanvd.grazi.spellcheck.SpellDictionary

class GraziPlugin : BaseComponent {
    companion object {
        var isTest: Boolean = false

        fun init() {
            SpellDictionary.graziFolder = GraziConfig.state.graziFolder
            GrammarEngine.enabledLangs = GraziConfig.state.enabledLanguages.toList()
            GrammarEngine.spellCheckEnabled = GraziConfig.state.enabledSpellcheck

            LangChecker.init(GraziConfig.state.enabledLanguages)
        }

        fun invalidateCaches() {
            GrammarCache.reset()
            LangChecker.clear()
        }
    }

    override fun initComponent() {
        init()
    }
}
