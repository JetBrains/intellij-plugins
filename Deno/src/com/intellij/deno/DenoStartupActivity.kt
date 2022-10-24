package com.intellij.deno

import com.intellij.deno.roots.createDenoEntity
import com.intellij.deno.roots.useWorkspaceModel
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity

class DenoStartupActivity : StartupActivity {
  override fun runActivity(project: Project) {
    if (!useWorkspaceModel()) return
    val service = DenoSettings.getService(project)
    if (!service.isUseDeno()) return

    createDenoEntity(project)
  }
}