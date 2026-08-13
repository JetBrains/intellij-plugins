package org.jetbrains.qodana.dart

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

private const val BUNDLE: @NonNls String = "messages.QodanaDartBundle"

internal object QodanaDartBundle {
  private val instance = DynamicBundle(QodanaDartBundle::class.java, BUNDLE)

  fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): @Nls String = instance.getMessage(key, *params)
}
