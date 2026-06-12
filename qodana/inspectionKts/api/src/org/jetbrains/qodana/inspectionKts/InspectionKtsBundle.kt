package org.jetbrains.qodana.inspectionKts

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

private const val BUNDLE = "messages.InspectionKtsBundle"

object InspectionKtsBundle {
  private val INSTANCE = DynamicBundle(InspectionKtsBundle::class.java, BUNDLE)

  @JvmStatic
  fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): @Nls String {
    return INSTANCE.getMessage(key, *params)
  }
}
