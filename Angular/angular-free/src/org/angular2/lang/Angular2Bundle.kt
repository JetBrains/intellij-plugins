package org.angular2.lang

import com.intellij.DynamicBundle
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
  fun messagePointer(key: @PropertyKey(resourceBundle = BUNDLE) String,
                     vararg params: Any): Supplier<String> {
    return instance.getLazyMessage(key, *params)
  }
}