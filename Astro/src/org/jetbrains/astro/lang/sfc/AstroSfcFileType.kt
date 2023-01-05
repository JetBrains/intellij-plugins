// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.sfc

import com.intellij.javascript.web.html.WebFrameworkHtmlFileType

class AstroSfcFileType private constructor() : WebFrameworkHtmlFileType(AstroSfcLanguage.INSTANCE, "Astro", "astro") {
  companion object {
    @JvmField
    val INSTANCE: AstroSfcFileType = AstroSfcFileType()
  }
}
