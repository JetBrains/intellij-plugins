// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.metadata

import com.intellij.lang.DependentLanguage
import com.intellij.lang.Language

class MetadataJsonLanguage private constructor() : Language("Metadata JSON", "application/json"), DependentLanguage {
  companion object {
    @JvmStatic
    val INSTANCE = MetadataJsonLanguage()
  }
}
