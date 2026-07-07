package org.jetbrains.qodana.inspectionKts.mcp

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

internal object InspectionKtsMcpBundle {
  private const val BUNDLE: String = "messages.InspectionKtsMcpBundle"
  private val INSTANCE: DynamicBundle = DynamicBundle(InspectionKtsMcpBundle::class.java, BUNDLE)

  @JvmStatic
  fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): @Nls String = INSTANCE.getMessage(key, *params)
}
