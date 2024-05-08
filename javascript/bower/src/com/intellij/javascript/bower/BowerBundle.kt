package com.intellij.javascript.bower

import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

object BowerBundle {
  @NonNls
  private const val BUNDLE: String = "messages.BowerBundle"
  private val INSTANCE: com.intellij.DynamicBundle = com.intellij.DynamicBundle(BowerBundle::class.java, BUNDLE)

  @JvmStatic
  @Nls
  fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String {
    return INSTANCE.getMessage(key, *params)
  }
}
