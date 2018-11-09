package org.jetbrains.vuejs

import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.xml.XMLLanguage

class VueLanguage : XMLLanguage(HTMLLanguage.INSTANCE, "Vue") {
  companion object {
    val INSTANCE: VueLanguage = VueLanguage()
  }
}