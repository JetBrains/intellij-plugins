package tanvd.grazi

import com.intellij.CommonBundle
import org.jetbrains.annotations.PropertyKey
import java.util.*

object GraziBundle {
    private const val bundleName = "messages.GraziBundle"

    private val bundle by lazy { ResourceBundle.getBundle(bundleName) }

    fun message(@PropertyKey(resourceBundle = bundleName) key: String, vararg params: String): String {
        return CommonBundle.message(bundle, key, *params)
    }
}
