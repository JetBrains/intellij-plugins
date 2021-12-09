// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html

import com.intellij.javascript.web.lang.html.WebFrameworkHtmlFileType
import com.intellij.openapi.fileTypes.LanguageFileType
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.VuejsIcons
import javax.swing.Icon

class VueFileType private constructor(): WebFrameworkHtmlFileType(VueLanguage.INSTANCE, "Vue.js", "vue") {
  companion object {
    @JvmField
    val INSTANCE: VueFileType = VueFileType()
  }
}
