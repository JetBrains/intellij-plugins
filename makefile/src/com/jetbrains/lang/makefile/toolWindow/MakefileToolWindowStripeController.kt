package com.jetbrains.lang.makefile.toolWindow

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project

interface MakefileToolWindowStripeController {
  fun shouldHideStripeIconFor(project: Project): Boolean

  companion object {
    val EP_NAME = ExtensionPointName<MakefileToolWindowStripeController>("com.intellij.makefile.toolWindowStripeController")
  }
}
