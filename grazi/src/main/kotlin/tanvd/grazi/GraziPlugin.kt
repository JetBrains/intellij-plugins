package tanvd.grazi

import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.components.BaseComponent
import com.intellij.openapi.extensions.PluginId
import tanvd.grazi.grammar.GrammarCache
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.spellcheck.SpellChecker

class GraziPlugin : BaseComponent {
    companion object {
        var isTest: Boolean = false

        const val id: String = "tanvd.grazi"

        val version: String
            get() = PluginManager.getPlugin(PluginId.getId(id))!!.version

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
