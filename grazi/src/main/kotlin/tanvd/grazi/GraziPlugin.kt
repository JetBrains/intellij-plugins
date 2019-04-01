package tanvd.grazi

import com.intellij.openapi.components.BaseComponent
import tanvd.grazi.grammar.GrammarCache
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.spellcheck.SpellChecker

class GraziPlugin : BaseComponent {
    companion object {
        var isTest: Boolean = false

        fun init() {
            GrammarChecker.init(GraziConfig.state.enabledLanguages)
        }

        fun invalidateCaches() {
            GrammarCache.reset()
            GrammarChecker.clear()
            SpellChecker.reset()
        }
    }

    override fun initComponent() {
        init()
    }
}
