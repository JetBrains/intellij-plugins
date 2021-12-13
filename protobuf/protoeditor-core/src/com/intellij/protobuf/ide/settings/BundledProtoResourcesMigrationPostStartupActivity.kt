package com.intellij.protobuf.ide.settings

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.util.io.exists
import com.intellij.util.io.outputStream
import java.io.IOException

internal class BundledProtoResourcesMigrationPostStartupActivity : StartupActivity.Background {
  private val bundledProtoFiles = setOf(
    "any.proto",
    "api.proto",
    "duration.proto",
    "descriptor.proto",
    "empty.proto",
    "field_mask.proto",
    "source_context.proto",
    "struct.proto",
    "timestamp.proto",
    "type.proto",
    "wrappers.proto"
  )

  override fun runActivity(project: Project) {
    val baseExtractionPath = DefaultConfigurator.getExtractedProtoPath()
    if (!bundledProtoFiles.all { baseExtractionPath.resolve("google/protobuf/$it").exists() }) {
      extractBundledProtoToTempDirectory()
    }

    val extractedProtoPath = DefaultConfigurator().builtInIncludeEntry ?: return
    val oldSettings = PbProjectSettings.getInstance(project).importPathEntries

    if (oldSettings.contains(extractedProtoPath)) return

    val oldStyleEntry = findBundledInJarProtoPath(oldSettings) ?: return
    val newSettings = oldSettings.filter { it != oldStyleEntry }.plus(extractedProtoPath)
    PbProjectSettings.getInstance(project).importPathEntries = newSettings
    PbProjectSettings.notifyUpdated(project)
  }

  private fun findBundledInJarProtoPath(oldSettings: List<PbProjectSettings.ImportPathEntry>): PbProjectSettings.ImportPathEntry? {
    val bundledProtoFiles = this::class.java.classLoader.getResource("include")!!
    val protoVfsUrl = runReadAction { VfsUtil.findFileByURL(bundledProtoFiles)?.url } ?: run {
      thisLogger().warn("Bundled proto files are not found in plugin distributive")
      return null
    }

    return oldSettings.firstOrNull { it.location == protoVfsUrl }
  }

  private fun extractBundledProtoToTempDirectory() {
    try {
      for (bundledProtoFile in bundledProtoFiles) {
        val extractedFilePath = DefaultConfigurator.getExtractedProtoPath().resolve("google/protobuf/$bundledProtoFile")
        if (!extractedFilePath.exists()) {
          val resourceUrl = this::class.java.classLoader.getResource("include/google/protobuf/$bundledProtoFile")!!
          resourceUrl.openStream().use {
            FileUtil.copy(it, extractedFilePath.outputStream(false))
          }
        }
      }
    }
    catch (exception: IOException) {
      thisLogger().warn("Unable to create temp binary file", exception)
      FileUtil.delete(DefaultConfigurator.getExtractedProtoPath())
    }
  }
}