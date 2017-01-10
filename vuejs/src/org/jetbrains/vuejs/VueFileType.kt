package org.jetbrains.vuejs

import com.intellij.lang.html.HTMLLanguage
import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

class VueFileType : LanguageFileType(VueLanguage.INSTANCE) {
  companion object {
    val INSTANCE = VueFileType();
  }

  override fun getName(): String {
    return "Vue.js"
  }

  override fun getDescription(): String {
    return "Vue.js templates"
  }

  override fun getDefaultExtension(): String {
    return "vue"
  }

  override fun getIcon(): Icon? {
    return HTMLLanguage.INSTANCE.associatedFileType!!.icon
  }
}