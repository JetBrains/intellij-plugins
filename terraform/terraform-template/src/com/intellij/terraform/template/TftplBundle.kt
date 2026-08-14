package com.intellij.terraform.template

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

object TftplBundle {
  @NonNls
  private const val BUNDLE: String = "messages.TftplBundle"
  private val instance = DynamicBundle(TftplBundle::class.java, BUNDLE)

  fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): @Nls String {
    return instance.getMessage(key, *params)
  }
}