// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.util.function.Supplier

@NonNls
private const val BUNDLE = "messages.AstroBundle"

object AstroBundle {
  private val instance = DynamicBundle(AstroBundle::class.java, BUNDLE)

  @JvmStatic
  @Nls
  fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String =
    if (instance.containsKey(key)) instance.getMessage(key, *params) else AstroDeprecatedMessagesBundle.message(key, *params)

  @JvmStatic
  fun messagePointer(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): Supplier<@Nls String> =
    if (instance.containsKey(key)) instance.getLazyMessage(key, *params) else AstroDeprecatedMessagesBundle.messagePointer(key, *params)
}
