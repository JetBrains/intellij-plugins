package com.intellij.lang.javascript.linter.eslint

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.util.function.Supplier

@NonNls
private const val BUNDLE = "messages.EslintBundle"

object EslintBundle {
  private val instance = DynamicBundle(EslintBundle::class.java, BUNDLE)

  @JvmStatic
  fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): @Nls String =
    instance.getMessage(key, *params)

  @JvmStatic
  fun messagePointer(
    @PropertyKey(resourceBundle = BUNDLE) key: String,
    vararg params: Any,
  ): Supplier<@Nls String> = instance.getLazyMessage(key, *params)
}
