// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html

import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.xml.XMLLanguage
import org.jetbrains.annotations.ApiStatus

class VueLanguage : org.jetbrains.vuejs.VueLanguage() {
  companion object {
    val INSTANCE: VueLanguage = VueLanguage()
  }
}

// This class is the original `VueLanguage` class,
// but it's renamed to allow instanceof check through deprecated class from 'vuejs' package
@Deprecated("Public for internal purpose only!")
@ApiStatus.ScheduledForRemoval(inVersion = "2019.3")
open class _VueLanguage : XMLLanguage(HTMLLanguage.INSTANCE, "Vue")
