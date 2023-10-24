package com.intellij.deno

import com.intellij.deno.roots.createDenoEntity
import com.intellij.deno.roots.useWorkspaceModel
import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

private class DenoStartupActivity : ProjectActivity {
  override suspend fun execute(project: Project) {
    if (!useWorkspaceModel()) {
      return
    }

    val service = project.serviceAsync<DenoSettings>()
    if (!service.isUseDeno()) {
      return
    }

    project.serviceAsync<DumbService>().runWhenSmart {
      createDenoEntity(project)
    }
  }
}