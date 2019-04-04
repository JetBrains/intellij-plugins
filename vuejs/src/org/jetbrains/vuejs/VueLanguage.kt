// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs

import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.xml.XMLLanguage

class VueLanguage : XMLLanguage(HTMLLanguage.INSTANCE, "Vue") {
  companion object {
    val INSTANCE: VueLanguage = VueLanguage()
  }
}
