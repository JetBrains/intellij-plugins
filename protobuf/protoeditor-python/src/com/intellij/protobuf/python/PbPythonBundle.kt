package com.intellij.protobuf.python

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls
private const val BUNDLE = "messages.PbPythonBundle"

internal object PbPythonBundle {
  private val bundle = DynamicBundle(PbPythonBundle::class.java, BUNDLE)

  @Nls
  fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String {
    return bundle.getMessage(key, *params)
  }
}
