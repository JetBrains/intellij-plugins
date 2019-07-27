// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html

import com.intellij.openapi.fileTypes.LanguageFileType
import icons.VuejsIcons
import org.jetbrains.annotations.ApiStatus
import javax.swing.Icon

@Suppress("DEPRECATION")
class VueFileType: org.jetbrains.vuejs.VueFileType() {
  companion object {
    @JvmField
    val INSTANCE: VueFileType = VueFileType()
  }
}

// This class is the original `VueFileType` class,
// but it's renamed to allow instanceof check through deprecated class from 'vuejs' package
@Deprecated("Public for internal purpose only!")
@ApiStatus.ScheduledForRemoval(inVersion = "2019.3")
open class _VueFileType : LanguageFileType(VueLanguage.INSTANCE) {

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
