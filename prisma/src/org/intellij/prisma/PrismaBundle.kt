package org.intellij.prisma

import com.intellij.DynamicBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls
internal const val PRISMA_BUNDLE = "messages.PrismaBundle"

object PrismaBundle {
  private val instance = DynamicBundle(PrismaBundle::class.java, PRISMA_BUNDLE)

  @Suppress("SpreadOperator")
  @JvmStatic
  fun message(@PropertyKey(resourceBundle = PRISMA_BUNDLE) key: String, vararg params: Any) =
    instance.getMessage(key, *params)

  @Suppress("SpreadOperator", "unused")
  @JvmStatic
  fun messagePointer(@PropertyKey(resourceBundle = PRISMA_BUNDLE) key: String, vararg params: Any) =
    instance.getLazyMessage(key, *params)
}
