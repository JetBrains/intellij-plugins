// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html

import com.intellij.lang.html.HTMLLanguage
import com.intellij.openapi.fileTypes.LanguageFileType

class VueLanguage : HTMLLanguage(HTMLLanguage.INSTANCE, "Vue") {
  companion object {
    val INSTANCE: VueLanguage = VueLanguage()
  }

  override fun getAssociatedFileType(): LanguageFileType = VueFileType.INSTANCE
}
