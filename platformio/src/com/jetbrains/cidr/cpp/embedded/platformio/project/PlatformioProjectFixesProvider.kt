package com.jetbrains.cidr.cpp.embedded.platformio.project

import com.intellij.clion.projectStatus.popup.ProjectFixesProvider
import com.intellij.clion.projectStatus.popup.asProjectFixAction
import com.intellij.clion.projectStatus.ui.isProjectAwareFile
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.cidr.cpp.embedded.platformio.ui.PlatformioRefreshAction
import com.jetbrains.cidr.lang.daemon.OCFileScopeProvider

class PlatformioProjectFixesProvider : ProjectFixesProvider {
  override suspend fun collectFixes(project: Project, file: VirtualFile?, context: DataContext): List<AnAction> =
    if (file != null
        && PlatformioWorkspace.isPlatformioProject(project)
        && readAction { isProjectAwareFile(file, project) }
        && OCFileScopeProvider.waitGetProjectSourceLocationKind(project, file).isOutOfProject())
      listOf(PlatformioRefreshAction().asProjectFixAction())
    else emptyList()
}