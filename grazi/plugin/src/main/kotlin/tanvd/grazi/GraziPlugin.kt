package tanvd.grazi

import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginManager
import com.intellij.ide.plugins.cl.PluginClassLoader
import com.intellij.openapi.extensions.PluginId
import java.io.File

object GraziPlugin {
    const val id: String = "tanvd.grazi"

    private val descriptor: IdeaPluginDescriptor
        get() = PluginManager.getPlugin(PluginId.getId(id))!!

    val version: String
        get() = descriptor.version

    val classLoader: PluginClassLoader
        get() = descriptor.pluginClassLoader as PluginClassLoader

    val path: File
        get() = descriptor.path
}
