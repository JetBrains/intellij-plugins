package tanvd.grazi

import com.intellij.openapi.components.BaseComponent
import tanvd.grazi.grammar.GrammarCache
import tanvd.grazi.grammar.GrammarChecker

class GraziPlugin : BaseComponent {
    companion object {
        var isTest: Boolean = false

        fun init() {
            GrammarChecker.init(GraziConfig.state.enabledLanguages)
        }

        fun invalidateCaches() {
            GrammarCache.reset()
            GrammarChecker.clear()
        }
    }

    override fun initComponent() {
        init()
    }
}
