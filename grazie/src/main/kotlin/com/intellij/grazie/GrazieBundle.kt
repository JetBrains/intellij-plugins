// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie

import com.intellij.AbstractBundle
import com.intellij.CommonBundle
import org.jetbrains.annotations.PropertyKey
import java.util.*

object GrazieBundle {
  const val bundleName = "messages.GrazieBundle"

  private val bundle by lazy { ResourceBundle.getBundle(bundleName) }

  fun message(@PropertyKey(resourceBundle = bundleName) key: String, vararg params: String): String {
    return AbstractBundle.message(bundle, key, *params)
  }
}
