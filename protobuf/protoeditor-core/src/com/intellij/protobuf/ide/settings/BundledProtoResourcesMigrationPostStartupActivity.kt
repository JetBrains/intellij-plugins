package com.intellij.protobuf.ide.settings

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectPostStartupActivity
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.io.outputStream
import java.io.IOException
import kotlin.io.path.exists

internal class BundledProtoResourcesMigrationPostStartupActivity : ProjectPostStartupActivity {
  private val bundledProtoFiles = setOf(
    "google/protobuf/any.proto",
    "google/protobuf/api.proto",
    "google/protobuf/duration.proto",
    "google/protobuf/descriptor.proto",
    "google/protobuf/empty.proto",
    "google/protobuf/field_mask.proto",
    "google/protobuf/source_context.proto",
    "google/protobuf/struct.proto",
    "google/protobuf/timestamp.proto",
    "google/protobuf/type.proto",
    "google/protobuf/wrappers.proto"
  )

  override suspend fun execute(project: Project) {
    val baseExtractionPath = DefaultConfigurator.getExtractedProtoPath()
    if (!bundledProtoFiles.all { baseExtractionPath.resolve(it).exists() }) {
      extractBundledProtoToTempDirectory()
    }

    val extractedProtoPath = DefaultConfigurator().builtInIncludeEntry ?: return
    val oldSettings = PbProjectSettings.getInstance(project).importPathEntries

    if (oldSettings.contains(extractedProtoPath)) return

    val oldStyleEntry = findBundledInJarProtoPath(oldSettings) ?: findBundledPathWithUnknownClassloader(oldSettings)
    val newSettings = oldSettings.filter { it != oldStyleEntry }.plus(extractedProtoPath)
    PbProjectSettings.getInstance(project).importPathEntries = newSettings
    PbProjectSettings.notifyUpdated(project)
  }

  private fun findBundledInJarProtoPath(oldSettings: List<PbProjectSettings.ImportPathEntry>): PbProjectSettings.ImportPathEntry? {
    val bundledDirectory = this::class.java.classLoader.getResource("include")
    val protoDirectoryUrl = runReadAction { bundledDirectory?.let(VfsUtil::findFileByURL)?.url } ?: run {
      thisLogger().warn("Bundled proto files directory is not found in plugin distributive")
      return null
    }

    return oldSettings.firstOrNull { it.location == protoDirectoryUrl }
  }

  private fun findBundledPathWithUnknownClassloader(oldSettings: List<PbProjectSettings.ImportPathEntry>): PbProjectSettings.ImportPathEntry? {
    val anyBundledProto = "include/${bundledProtoFiles.first()}"
    val parentDirectoriesCount = anyBundledProto.split('/').size
    val bundledProtoFile = this::class.java.classLoader.getResource("include/google/protobuf/any.proto")
    val protoFile = runReadAction { bundledProtoFile?.let(VfsUtil::findFileByURL) } ?: run {
      thisLogger().warn("Bundled proto files are not found in plugin distributive")
      return null
    }
    val protoDirectoryUrl = generateSequence(protoFile, VirtualFile::getParent).take(parentDirectoriesCount).lastOrNull()?.url
    return oldSettings.firstOrNull { it.location == protoDirectoryUrl }
  }

  private fun extractBundledProtoToTempDirectory() {
    try {
      for (bundledProtoFile in bundledProtoFiles) {
        val extractedFilePath = DefaultConfigurator.getExtractedProtoPath().resolve(bundledProtoFile)
        if (!extractedFilePath.exists()) {
          val resourceUrl = this::class.java.classLoader.getResource("include/$bundledProtoFile")!!
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