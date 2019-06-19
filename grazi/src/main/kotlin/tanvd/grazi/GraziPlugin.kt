package tanvd.grazi

import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.components.BaseComponent
import com.intellij.openapi.extensions.PluginId
import org.jetbrains.annotations.TestOnly
import tanvd.grazi.language.LangTool
import tanvd.grazi.spellcheck.GraziSpellchecker

class GraziPlugin : BaseComponent {
    companion object {
        @TestOnly
        var isTest: Boolean = false

        const val id: String = "tanvd.grazi"

        val version: String
            get() = PluginManager.getPlugin(PluginId.getId(id))!!.version

        fun init() {
            LangTool.init(GraziConfig.state.enabledLanguages)
        }

        fun invalidateCaches() {
            LangTool.reset()
            GraziSpellchecker.reset()
        }

        fun reinit() {
            invalidateCaches()
            init()
        }
    }

    override fun initComponent() {
        init()
    }
}
