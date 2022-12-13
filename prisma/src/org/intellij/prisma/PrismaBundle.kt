package org.intellij.prisma

import com.intellij.DynamicBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls
const val PRISMA_BUNDLE = "messages.PrismaBundle"

object PrismaBundle : DynamicBundle(PRISMA_BUNDLE) {

  @Suppress("SpreadOperator")
  @JvmStatic
  fun message(@PropertyKey(resourceBundle = PRISMA_BUNDLE) key: String, vararg params: Any) =
    getMessage(key, *params)

  @Suppress("SpreadOperator", "unused")
  @JvmStatic
  fun messagePointer(@PropertyKey(resourceBundle = PRISMA_BUNDLE) key: String, vararg params: Any) =
    getLazyMessage(key, *params)
}
