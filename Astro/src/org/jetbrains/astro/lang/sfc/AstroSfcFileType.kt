// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.sfc

import com.intellij.openapi.fileTypes.LanguageFileType
import org.jetbrains.astro.AstroBundle
import org.jetbrains.astro.AstroIcons
import javax.swing.Icon

class AstroSfcFileType : LanguageFileType(AstroSfcLanguage.INSTANCE) {

  override fun getName(): String {
    return "Astro"
  }

  override fun getDescription(): String {
    return AstroBundle.message("astro.language.name")
  }

  override fun getDefaultExtension(): String {
    return ".astro"
  }

  override fun getIcon(): Icon {
    return AstroIcons.Astro
  }

  companion object {
    @JvmField
    val INSTANCE: LanguageFileType = AstroSfcFileType()
  }
}
