package com.intellij.mdx.react

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

private const val BUNDLE = "messages.MdxReactBundle"

internal object MdxReactBundle {
  private val INSTANCE = DynamicBundle(MdxReactBundle::class.java, BUNDLE)

  @JvmStatic
  @Nls
  fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String = INSTANCE.getMessage(key, *params)
}
