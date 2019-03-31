package tanvd.grazi

import com.intellij.openapi.components.BaseComponent
import tanvd.grazi.grammar.GrammarCache
import tanvd.grazi.language.LangChecker

class GraziPlugin : BaseComponent {
    companion object {
        var isTest: Boolean = false

        fun init() {
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
