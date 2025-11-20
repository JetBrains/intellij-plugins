package com.jetbrains.cidr.cpp.embedded.platformio.ui

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.EDT
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.importing.ProjectResolverPolicy
import com.intellij.openapi.externalSystem.ui.ExternalSystemIconProvider
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioProjectResolvePolicyCleanCache
import com.jetbrains.cidr.cpp.embedded.platformio.project.ID
import com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioWorkspace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlatformioRefreshAction : AnAction() {

  init {
    with(templatePresentation) {
      text = ClionEmbeddedPlatformioBundle.message("action.PlatformioRefreshAction.text")
      icon = ExternalSystemIconProvider.getExtension(ID).reloadIcon
    }
  }

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
  override fun update(e: AnActionEvent) {
    e.presentation.isEnabledAndVisible = with(e.project) { this != null && PlatformioWorkspace.isPlatformioProject(this) }
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project
    if (project == null) return

    e.coroutineScope.launch(Dispatchers.EDT) {
      // Don't refresh untrusted projects
      // Refreshing the project would run pio, which can execute python code via advanced scripting.
      // Just show the untrusted project dialog and do nothing instead.
      ensureProjectIsTrusted(project)
      val rerunSpec = ImportSpecBuilder(project, ID).projectResolverPolicy(PlatformioProjectResolvePolicyCleanCache).build()
      val importSpec = ImportSpecBuilder(project, ID).projectResolverPolicy(PlatformioProjectResolvePolicyCleanCache).withRerunAction {
        ExternalSystemUtil.refreshProject(project.basePath!!, rerunSpec)
      }.build()
      ExternalSystemUtil.refreshProject(project.basePath!!, importSpec)
    }
  }
}

class PlatformioProjectResolvePolicy(val cleanCache: Boolean, val isInitial: Boolean) : ProjectResolverPolicy {
  override fun isPartialDataResolveAllowed(): Boolean = false
}

