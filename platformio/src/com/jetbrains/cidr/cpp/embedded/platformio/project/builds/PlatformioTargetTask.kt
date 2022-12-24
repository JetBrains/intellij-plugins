package com.jetbrains.cidr.cpp.embedded.platformio.project.builds

import com.intellij.openapi.util.NlsSafe
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle
import com.jetbrains.cidr.execution.build.tasks.CidrAbstractConfigurationTask
import org.jetbrains.annotations.Nls

class PlatformioTargetTask(@Nls private val presentationName: String?, @NlsSafe vararg val args: String)
  : CidrAbstractConfigurationTask(PlatformioBuildConfiguration) {

  override fun getPresentableName(): @Nls String =
    presentationName ?: ClionEmbeddedPlatformioBundle.message("execution.pio.target.title", args.joinToString(" "))
}