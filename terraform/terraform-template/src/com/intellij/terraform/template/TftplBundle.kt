package com.intellij.terraform.template

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

object TftplBundle : DynamicBundle(TftplBundle::class.java, TftplBundle.BUNDLE) {

  @NonNls
  private const val BUNDLE: String = "messages.TftplBundle"

  fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): @Nls String {
    return getMessage(key, *params)
  }
}