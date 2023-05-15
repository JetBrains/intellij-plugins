// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.util.function.Supplier

class Angular2Bundle : DynamicBundle(BUNDLE) {

  companion object {
    const val BUNDLE: @NonNls String = "messages.Angular2Bundle"
    private val INSTANCE: Angular2Bundle = Angular2Bundle()

    @JvmStatic
    fun message(key: @PropertyKey(resourceBundle = BUNDLE) String, vararg params: Any): @Nls String {
      return INSTANCE.getMessage(key, *params)
    }

    @JvmStatic
    fun messagePointer(key: @PropertyKey(resourceBundle = BUNDLE) String,
                       vararg params: Any): Supplier<String> {
      return INSTANCE.getLazyMessage(key, *params)
    }
  }
}