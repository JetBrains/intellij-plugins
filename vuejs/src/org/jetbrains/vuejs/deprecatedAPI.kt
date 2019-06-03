// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs

import org.jetbrains.annotations.ApiStatus
import org.jetbrains.vuejs.lang.html._VueFileType
import org.jetbrains.vuejs.lang.html._VueLanguage

@Deprecated("Class moved, kept here for compatibility with Aegis Code Check Plugin")
@ApiStatus.ScheduledForRemoval(inVersion = "2019.3")
open class VueFileType : _VueFileType() {
  companion object {
    val INSTANCE: VueFileType = org.jetbrains.vuejs.lang.html.VueFileType.INSTANCE
  }
}

@Deprecated("Class moved, kept here for compatibility with Aegis Code Check Plugin")
@ApiStatus.ScheduledForRemoval(inVersion = "2019.3")
open class VueLanguage : _VueLanguage() {
  companion object {
    val INSTANCE: VueLanguage = org.jetbrains.vuejs.lang.html.VueLanguage.INSTANCE
  }
}
