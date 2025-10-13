package com.intellij.deno

import com.intellij.deno.roots.createDenoEntity
import com.intellij.deno.roots.useWorkspaceModel
import com.intellij.deno.settings.useDeno
import com.intellij.javascript.runtime.settings.JSRuntimeConfiguration
import com.intellij.javascript.runtime.settings.isJavaScriptRuntimeSettingsPageEnabled
import com.intellij.openapi.application.UiWithModelAccess
import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private class DenoStartupActivity : ProjectActivity {
  override suspend fun execute(project: Project) {
    if (isJavaScriptRuntimeSettingsPageEnabled) {
      // sync selected "Preferred runtime" with `DenoSetting`
      val newUseDeno = JSRuntimeConfiguration.getInstance(project).runtimeType.useDeno
      val denoSettings = DenoSettings.getService(project)
      if (denoSettings.getUseDeno() != newUseDeno) {
        withContext(Dispatchers.UiWithModelAccess) {
          denoSettings.setUseDenoAndReload(newUseDeno)
        }
      }
    }

    if (!useWorkspaceModel()) {
      return
    }

    if (!useDenoLibrary(project)) {
      return
    }

    project.serviceAsync<DumbService>().runWhenSmart {
      createDenoEntity(project)
    }
  }
}