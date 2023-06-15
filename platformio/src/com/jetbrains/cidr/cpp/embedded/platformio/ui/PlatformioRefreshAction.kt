package com.jetbrains.cidr.cpp.embedded.platformio.ui

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.importing.ImportSpecImpl
import com.intellij.openapi.externalSystem.importing.ProjectResolverPolicy
import com.intellij.openapi.externalSystem.ui.ExternalSystemIconProvider
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioProjectResolvePolicyCleanCache
import com.jetbrains.cidr.cpp.embedded.platformio.project.ID
import com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioWorkspace

class PlatformioRefreshAction : AnAction(ExternalSystemIconProvider.getExtension(ID).reloadIcon) {

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
  override fun update(e: AnActionEvent) {
    e.presentation.isEnabledAndVisible = with(e.project) { this != null && PlatformioWorkspace.isPlatformioProject(this) }
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project
    if (project != null) {
      val importSpec = ImportSpecBuilder(project, ID).projectResolverPolicy(PlatformioProjectResolvePolicyCleanCache).build()
      val rerunSpec = ImportSpecBuilder(project, ID).projectResolverPolicy(PlatformioProjectResolvePolicyCleanCache).build()
      if (importSpec is ImportSpecImpl) {
        importSpec.rerunAction = Runnable { ExternalSystemUtil.refreshProject(project.basePath!!, rerunSpec) }
      }
      ExternalSystemUtil.refreshProject(project.basePath!!, importSpec)
    }
  }

}

class PlatformioProjectResolvePolicy(val cleanCache: Boolean) : ProjectResolverPolicy {
  override fun isPartialDataResolveAllowed(): Boolean = false
}

