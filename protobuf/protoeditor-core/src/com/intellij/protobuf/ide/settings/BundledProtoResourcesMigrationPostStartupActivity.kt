package com.intellij.protobuf.ide.settings

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.util.io.exists
import java.io.IOException
import java.net.URL
import java.nio.file.Path

internal class BundledProtoResourcesMigrationPostStartupActivity : StartupActivity.Background {
  private val bundledProtoUrl: URL?
    get() = DefaultConfigurator::class.java.classLoader.getResource("include")

  private val anyProtoPreciseUrl: Path
    get() = DefaultConfigurator.getExtractedProtoPath().resolve("google/protobuf/any.proto")

  override fun runActivity(project: Project) {
    if (!anyProtoPreciseUrl.exists()) extractBundledProtoToTempDirectory()

    val extractedProtoPath = DefaultConfigurator().builtInIncludeEntry ?: return
    val oldSettings = PbProjectSettings.getInstance(project).importPathEntries

    if (oldSettings.contains(extractedProtoPath)) return

    val oldStyleEntry = findBundledInJarProtoPath(oldSettings) ?: return
    val newSettings = oldSettings.filter { it != oldStyleEntry }.plus(extractedProtoPath)
    PbProjectSettings.getInstance(project).importPathEntries = newSettings
  }

  private fun findBundledInJarProtoPath(oldSettings: List<PbProjectSettings.ImportPathEntry>): PbProjectSettings.ImportPathEntry? {
    val protoInJarPath = bundledProtoUrl?.toURI()?.path
    if (protoInJarPath == null) {
      thisLogger().warn("Unable to detect old style bundled protos path. Abort migration.")
      return null
    }

    val protoVfsUrl = VfsUtilCore.pathToUrl(protoInJarPath)
    return oldSettings.firstOrNull { it.location == protoVfsUrl }
  }

  private fun extractBundledProtoToTempDirectory() {
    try {
      PbBundledResourcesUtil.extractResources(bundledProtoUrl, DefaultConfigurator.getExtractedProtoPath())
    }
    catch (exception: IOException) {
      Logger.getInstance(DefaultConfigurator::class.java).warn("Unable to create temp binary file", exception)
    }
  }
}