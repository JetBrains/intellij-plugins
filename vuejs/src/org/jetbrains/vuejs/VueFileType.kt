// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs

import com.intellij.openapi.fileTypes.LanguageFileType
import icons.VuejsIcons
import javax.swing.Icon

class VueFileType : LanguageFileType(VueLanguage.INSTANCE) {
  companion object {
    val INSTANCE: VueFileType = VueFileType()
  }

  override fun getName(): String {
    return "Vue.js"
  }

  override fun getDescription(): String {
    return "Vue.js template"
  }

  override fun getDefaultExtension(): String {
    return "vue"
  }

  override fun getIcon(): Icon? {
    return VuejsIcons.Vue
  }
}
