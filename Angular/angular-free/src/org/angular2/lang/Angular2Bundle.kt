package org.angular2.lang

import com.ibm.icu.text.MessageFormat
import com.intellij.DynamicBundle
import com.intellij.openapi.diagnostic.logger
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.util.function.Supplier

object Angular2Bundle {
  const val BUNDLE: @NonNls String = "messages.Angular2Bundle"
  private val instance = DynamicBundle(Angular2Bundle::class.java, BUNDLE)

  @JvmStatic
  fun message(key: @PropertyKey(resourceBundle = BUNDLE) String, vararg params: Any): @Nls String {
    return instance.getMessage(key, *params)
  }

  @JvmStatic
  fun htmlMessage(key: @PropertyKey(resourceBundle = BUNDLE) String, vararg params: Any): @Nls String {
    return "<html>" + instance.getMessage(key, *params) + "</html>"
  }

  @JvmStatic
  fun icuHtmlMessage(key: @PropertyKey(resourceBundle = BUNDLE) String, vararg params: Pair<String, Any>): @Nls String {
    return try {
      "<html>" + MessageFormat.format(instance.getMessage(key), params.toMap()) + "</html>"
    }
    catch (e: IllegalArgumentException) {
      logger<Angular2Bundle>().error(e)
      "Bad message: $key"
    }
  }

  @JvmStatic
  fun messagePointer(key: @PropertyKey(resourceBundle = BUNDLE) String,
                     vararg params: Any): Supplier<String> {
    return instance.getLazyMessage(key, *params)
  }
}