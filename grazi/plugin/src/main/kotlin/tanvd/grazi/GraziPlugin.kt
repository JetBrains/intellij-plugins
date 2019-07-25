package tanvd.grazi

import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.extensions.PluginId

object GraziPlugin {
    const val id: String = "tanvd.grazi"

    val version: String
        get() = PluginManager.getPlugin(PluginId.getId(id))!!.version
}
