package com.jetbrains.cidr.cpp.embedded.platformio.project

import com.intellij.clion.projectStatus.fus.ProjectFixKinds
import com.intellij.clion.projectStatus.popup.ProjectFixesProvider
import com.intellij.clion.projectStatus.popup.asProjectFixAction
import com.intellij.clion.projectStatus.ui.isProjectAwareFile
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioFileType
import com.jetbrains.cidr.cpp.embedded.platformio.ui.PlatformioRefreshAction
import com.jetbrains.cidr.cpp.external.system.project.ui.ExternalProjectStatusAndFixesProviderBase
import com.jetbrains.cidr.cpp.external.system.project.ui.SelectAndLoadProjectActionBase
import com.jetbrains.cidr.lang.OCFileTypeHelpers
import com.jetbrains.cidr.lang.daemon.OCFileScopeProvider

/**
 * Provides fix for file out of project warning.
 */
class PlatformioProjectFixesProvider : ProjectFixesProvider {
  override suspend fun collectFixes(project: Project, file: VirtualFile?, context: DataContext): List<AnAction> =
    if (file != null
        && PlatformioWorkspace.isPlatformioProject(project)
        && readAction { isProjectAwareFile(file, project) }
        && OCFileTypeHelpers.isSourceFile(file.name)
        && OCFileScopeProvider.waitGetProjectSourceLocationKind(project, file).isOutOfProject())
      listOf(PlatformioRefreshAction().asProjectFixAction(ProjectFixKinds.PLATFORMIO_RELOAD_PROJECT))
    else emptyList()
}

/**
 * Provides fixes for when no project model is loaded
 */
class ExternalProjectStatusAndFixesProvider : ExternalProjectStatusAndFixesProviderBase() {
  override fun isBuildFile(file: VirtualFile): Boolean = PlatformioFileType.isFileOfType(file)

  override fun createLoadAction(project: Project, buildFile: VirtualFile): AnAction = LoadPlatformioAction(project, buildFile).asProjectFixAction(ProjectFixKinds.PLATFORMIO_LOAD_PROJECT)

  private class LoadPlatformioAction(private val project: Project, private val buildFile: VirtualFile)
    : AnAction(ClionEmbeddedPlatformioBundle.message("action.PlatformioLoadProjectAction.text")) {
    override fun actionPerformed(e: AnActionEvent) {
      PlatformioWorkspace.linkProject(project, buildFile.parent)
    }
  }

  override fun createSelectAndLoadAction(project: Project, rootDirectory: VirtualFile): AnAction = SelectAndLoadPlatformioAction(project, rootDirectory).asProjectFixAction(ProjectFixKinds.PLATFORMIO_SELECT_AND_LOAD_PROJECT)

  private class SelectAndLoadPlatformioAction(project: Project, rootDirectory: VirtualFile) : SelectAndLoadProjectActionBase(project, rootDirectory, @Suppress("DialogTitleCapitalization") ClionEmbeddedPlatformioBundle.message("project.status.action.select")) {
    override fun isBuildFile(file: VirtualFile): Boolean = PlatformioFileType.isFileOfType(file)

    override fun linkProject(project: Project, rootDirectory: VirtualFile) {
      PlatformioWorkspace.linkProject(project, rootDirectory)
    }
  }
}