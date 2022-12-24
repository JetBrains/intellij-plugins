package com.jetbrains.cidr.cpp.embedded.platformio.project.migration

import com.intellij.conversion.*
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.io.delete
import com.intellij.util.io.isDirectory
import com.intellij.util.io.write
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioFileType
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

class PlatformioProjectMigrationProvider : ConverterProvider() {
  override fun getConversionDescription(): String =
    ClionEmbeddedPlatformioBundle.message("project.convert.description")

  override fun createConverter(context: ConversionContext): ProjectConverter = PlatformioProjectMigrationConverter(context)
}

private class PlatformioProjectMigrationConverter(val context: ConversionContext) : ProjectConverter() {
  private val filesToDelete = mutableListOf<Path>()

  override fun isConversionNeeded(): Boolean {
    if (context.projectBaseDir.listDirectoryEntries(PlatformioFileType.FILE_NAME).isEmpty())
      return false
    return listCmakeFiles()
      .filter {
        val content = FileUtil.loadFile(it.toFile())
        return content.contains("https://docs.platformio.org/")
      }
      .count() == 2
  }

  private fun listCmakeFiles() = context.projectBaseDir.listDirectoryEntries("CMake*.txt")
    .filter { it.fileName.toString() in arrayOf("CMakeLists.txt", "CMakeListsPrivate.txt") }

  override fun getAdditionalAffectedFiles(): MutableCollection<Path> {
    if (filesToDelete.isEmpty()) {
      context.settingsBaseDir?.apply {
        filesToDelete.addAll(listCmakeFiles())
        listDirectoryEntries().forEach {
          if (it.extension == "iml" ||
              it.name == "modules.xml" ||
              it.name == "misc.xml") {
            filesToDelete.add(it)
          }
        }
        context.projectBaseDir.listDirectoryEntries("cmake-build*")
          .forEach { if (it.isDirectory()) filesToDelete.add(it) }
      }
    }
    return filesToDelete
  }

  override fun createWorkspaceFileConverter(): ConversionProcessor<WorkspaceSettings> =
    object : ConversionProcessor<WorkspaceSettings>() {
      override fun isConversionNeeded(settings: WorkspaceSettings?): Boolean = this@PlatformioProjectMigrationConverter.isConversionNeeded

      override fun process(settings: WorkspaceSettings) {
        arrayOf("CMakePresetLoader", "CMakeReloadState", "CMakeRunConfigurationManager")
          .forEach {
            settings.getComponentElement(it)?.apply { parent.removeContent(this) }
          }
      }
    }

  override fun preProcessingFinished() {
    filesToDelete.forEach { it.delete(true) }
    context.settingsBaseDir?.resolve("misc.xml")?.write(
      """
<?xml version="1.0" encoding="UTF-8"?>
<project version="4">
  <component name="ExternalStorageConfigurationManager" enabled="true" />
  <component name="PlatformIOSettings">
    <option name="linkedExternalProjectsSettings">
      <PlatformioProjectSettings>
        <option name="externalProjectPath" value="${'$'}PROJECT_DIR${'$'}" />
        <option name="modules">
          <set>
            <option value="${'$'}PROJECT_DIR${'$'}" />
          </set>
        </option>
      </PlatformioProjectSettings>
    </option>
  </component>
  <component name="PlatformIOWorkspace" PROJECT_DIR="${'$'}PROJECT_DIR${'$'}" />
</project>
      """.trimIndent()
    )
  }
}
