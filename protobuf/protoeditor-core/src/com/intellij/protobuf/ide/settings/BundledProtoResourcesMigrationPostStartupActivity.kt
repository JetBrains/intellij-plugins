package com.intellij.protobuf.ide.settings

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.util.io.createFile
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
    val protoInJarPath = DefaultConfigurator::class.java.classLoader.getResource("include")?.toURI()?.path
    if (protoInJarPath == null) {
      thisLogger().warn("Unable to detect old style bundled protos path. Abort migration.")
      return null
    }

    val protoVfsUrl = VfsUtilCore.pathToUrl(protoInJarPath)
    return oldSettings.firstOrNull { it.location == protoVfsUrl }
  }

  private fun extractBundledProtoToTempDirectory() {
    try {
      for (bundledProtoFile in bundledProtoFiles) {
        val extractedFilePath = DefaultConfigurator.getExtractedProtoPath().resolve("google/protobuf/$bundledProtoFile")
        if (!extractedFilePath.exists()) {
          extractedFilePath.createFile()
          this::class.java.classLoader.getResource("include/google/protobuf/$bundledProtoFile")?.openStream().use {
            if (it == null) {
              thisLogger().warn("Bundled resource '$bundledProtoFile' is not found in plugin distributive")
            }
            else {
              FileUtil.copy(it, extractedFilePath.outputStream(false))
            }
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