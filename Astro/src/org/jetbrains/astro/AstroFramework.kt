// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro

import com.intellij.javascript.web.WebFramework
import com.intellij.javascript.web.html.WebFrameworkHtmlFileType
import org.jetbrains.astro.lang.sfc.AstroSfcFileType
import javax.swing.Icon

class AstroFramework: WebFramework() {

  override val displayName: String = "Astro"
  override val icon: Icon = AstroIcons.Astro
  override val standaloneFileType: WebFrameworkHtmlFileType = AstroSfcFileType.INSTANCE

  companion object {
    val instance get() = get(ID)
    const val ID = "astro"
  }
}