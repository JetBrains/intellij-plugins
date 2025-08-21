// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.psi.JSElement
import org.jetbrains.vuejs.model.source.EntityContainerInfoProvider.InitializedContainerInfoProvider.ListAccessor

// Temp workaround for WEB-74310
internal fun <T> ListAccessor<T>.toSafeAccessor(): ListAccessor<T> =
  object : ListAccessor<T>() {
    override fun build(declaration: JSElement): List<T> =
      try {
        this@toSafeAccessor.build(declaration)
      }
      catch (_: IllegalArgumentException) {
        emptyList()
      }
  }
