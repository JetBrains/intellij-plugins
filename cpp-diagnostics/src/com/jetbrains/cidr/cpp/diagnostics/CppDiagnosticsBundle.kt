package com.jetbrains.cidr.cpp.diagnostics

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

private const val BUNDLE = "messages.CppDiagnosticsBundle"

object CppDiagnosticsBundle {
  private val INSTANCE = DynamicBundle(javaClass, BUNDLE)

  fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): @Nls String {
    return INSTANCE.getMessage(key, *params)
  }
}