package org.jetbrains.qodana.jvm.gradle

import com.intellij.openapi.externalSystem.util.ExternalSystemConstants
import com.intellij.openapi.externalSystem.util.Order
import com.intellij.util.PlatformUtils
import org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension

@Order(ExternalSystemConstants.UNORDERED)
class QodanaGradleProjectResolver : AbstractProjectResolverExtension() {
  override fun getExtraCommandLineArgs(): List<String> {
    val disableDownloadSources =
      if (PlatformUtils.isQodana()) listOf ("-Didea.gradle.download.sources.force=false") else emptyList()

    return System.getProperty("qodana.gradle.extra.command.line.args", "").split(";") + disableDownloadSources
  }
}

