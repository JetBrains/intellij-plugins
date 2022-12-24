package com.jetbrains.cidr.cpp.embedded.platformio.project

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.concurrency.annotations.RequiresEdt
import com.jetbrains.cidr.CidrProjectOpenProcessor
import com.jetbrains.cidr.ProjectOpenHelper
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioFileType
import com.jetbrains.cidr.cpp.external.system.linkExternalProject
import com.jetbrains.cidr.external.system.fus.CidrExternalUsageUtil

class PlatformioProjectOpenProcessor : CidrProjectOpenProcessor(PlatformioProjectOpenHelper) {
  override val name: String = "PlatformIO"

  override fun doOpenProject(virtualFile: VirtualFile, projectToClose: Project?, forceOpenInNewFrame: Boolean): Project? {
    val platformioIniFile = PlatformioProjectOpenHelper.findSupportedSubFile(virtualFile) ?: return null

    // CPP-16000 - don't re-create project from specified file there's already valid project there
    val existingProject = PlatformioProjectOpenHelper.openExistingProjectInDirectory(platformioIniFile, projectToClose, forceOpenInNewFrame)
    if (existingProject != null) {
      return existingProject
    }

    val spec = OpenProjectSpec(platformioIniFile)
    val project = PlatformioProjectOpenHelper.openProject(platformioIniFile, projectToClose, forceOpenInNewFrame, spec) ?: return null

    CidrExternalUsageUtil.logProjectCreated(ID, project)

    val externalProjectPath = platformioIniFile.parent
    linkPlatformioProject(project, externalProjectPath)
    return project
  }

  @RequiresEdt
  fun linkPlatformioProject(project: Project, projectPath: VirtualFile) {
    // default project settings
    val settings = PlatformioProjectSettings.default()
    settings.externalProjectPath = projectPath.path

    val workspace = project.service<PlatformioWorkspace>()

    linkExternalProject(project, ID, settings, workspace)
  }

  companion object {
    private val DATA_KEY: Key<OpenProjectSpec> = Key.create("PLATFORMIO_FILE_TO_OPEN_KEY")

    object PlatformioProjectOpenHelper : ProjectOpenHelper<OpenProjectSpec>(DATA_KEY, SupportedFileChecker)

    data class OpenProjectSpec(val platformioIni: VirtualFile)

    object SupportedFileChecker : ProjectOpenHelper.SupportedFileChecker {
      override fun isSupportedFile(file: VirtualFile): Boolean {
        return when {
          file.isDirectory -> file.children.any(PlatformioFileType::isFileOfType)
          else -> PlatformioFileType.isFileOfType(file)
        }
      }
    }
  }
}