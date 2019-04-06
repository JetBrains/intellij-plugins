package tanvd.grazi

import com.beust.klaxon.Klaxon
import com.intellij.CommonBundle
import org.jetbrains.annotations.PropertyKey
import java.util.*

object GraziBundle {
    private const val bundleName = "messages.GraziBundle"

    private val bundle by lazy { ResourceBundle.getBundle(bundleName) }

    val langConfigs by lazy { Properties().also { it.load(GraziBundle::class.java.classLoader.getResourceAsStream("data/lang_config.properties")) } }

    fun message(@PropertyKey(resourceBundle = bundleName) key: String, vararg params: String): String {
        return CommonBundle.message(bundle, key, *params)
    }

    inline fun <reified T> langConfig(key: String) = Klaxon().parse<T>(langConfigs.getProperty(key))!!
    inline fun <reified T> langConfigSet(key: String) = Klaxon().parseArray<T>(langConfigs.getProperty(key))!!.toSet()
}
