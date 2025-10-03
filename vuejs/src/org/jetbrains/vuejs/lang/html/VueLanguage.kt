// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html

import com.intellij.javascript.web.html.WebFrameworkHtmlDialect
import org.jetbrains.annotations.ApiStatus

object VueLanguage : WebFrameworkHtmlDialect("Vue") {
  @Deprecated("Use VueLanguage instead", ReplaceWith("VueLanguage"))
  @ApiStatus.ScheduledForRemoval
  val INSTANCE: VueLanguage = VueLanguage

  object Companion {
    @Deprecated("Use VueLanguage instead", ReplaceWith("VueLanguage"))
    @ApiStatus.ScheduledForRemoval
    val INSTANCE: VueLanguage = VueLanguage
  }
}
