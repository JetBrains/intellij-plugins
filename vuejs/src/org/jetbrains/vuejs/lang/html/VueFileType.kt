// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html

import com.intellij.openapi.fileTypes.LanguageFileType
import icons.VuejsIcons
import org.jetbrains.vuejs.VueBundle
import javax.swing.Icon

class VueFileType : LanguageFileType(VueLanguage.INSTANCE) {
  companion object {
    @JvmField
    val INSTANCE: VueFileType = VueFileType()

    const val VUE_EXTENSION = "vue"
  }

  override fun getName(): String {
    return "Vue.js"
  }

  override fun getDescription(): String {
    return VueBundle.message("vue.file.type.description")
  }

  override fun getDefaultExtension(): String {
    return VUE_EXTENSION
  }

  override fun getIcon(): Icon? {
    return VuejsIcons.Vue
  }
}
