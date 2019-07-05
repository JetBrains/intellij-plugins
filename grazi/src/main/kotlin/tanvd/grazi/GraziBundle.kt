package tanvd.grazi

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.CommonBundle
import org.jetbrains.annotations.PropertyKey
import java.util.*

object GraziBundle {
    private val json = ObjectMapper()
    private val setTypeRef = object : TypeReference<Set<String>>() {}

    const val bundleName = "messages.GraziBundle"

    private val bundle by lazy { ResourceBundle.getBundle(bundleName) }

    private val langConfigs by lazy { Properties().also { it.load(GraziBundle::class.java.classLoader.getResourceAsStream("data/lang_config.properties")) } }

    fun message(@PropertyKey(resourceBundle = bundleName) key: String, vararg params: String): String {
        return CommonBundle.message(bundle, key, *params)
    }

    fun langConfig(key: String): Set<String> {
        return json.readValue(langConfigs.getProperty(key)!!, setTypeRef)
    }
}
