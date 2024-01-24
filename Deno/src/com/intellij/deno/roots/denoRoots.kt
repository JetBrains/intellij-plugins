package com.intellij.deno.roots

import com.intellij.deno.DenoSettings
import com.intellij.deno.entities.DenoEntity
import com.intellij.deno.entities.DenoEntitySource
import com.intellij.deno.service.DenoTypings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.backend.workspace.toVirtualFileUrl
import com.intellij.platform.workspace.storage.MutableEntityStorage

internal fun createDenoEntity(project: Project) {
  if (!useWorkspaceModel()) {
    return
  }

  val (depsVirtualFile, denoTypingsVirtualFile) = getRoots(project)

  val virtualFileUrlManager = WorkspaceModel.getInstance(project).getVirtualFileUrlManager()

  val builder = MutableEntityStorage.create()
  builder addEntity DenoEntity(DenoEntitySource) {
    this.denoTypes = denoTypingsVirtualFile?.toVirtualFileUrl(virtualFileUrlManager)
    this.depsFile = depsVirtualFile?.toVirtualFileUrl(virtualFileUrlManager)
  }

  ApplicationManager.getApplication().runWriteAction {
    WorkspaceModel.getInstance(project).updateProjectModel("Create Deno entity") { newBuilder ->
      newBuilder.replaceBySource({ it is DenoEntitySource }, builder)
    }
  }
}

internal fun removeDenoEntity(project: Project) {
  if (!useWorkspaceModel()) {
    return
  }

  ApplicationManager.getApplication().runWriteAction {
    WorkspaceModel.getInstance(project).updateProjectModel("Remove Deno entity") { builder ->
      builder.entitiesBySource { it is DenoEntitySource }.forEach { builder.removeEntity(it) }
    }
  }
}

internal fun getRoots(project: Project): Pair<VirtualFile?, VirtualFile?> {
  val service = DenoSettings.getService(project)
  if (!service.isUseDeno()) return null to null

  val denoPackages = service.getDenoCacheDeps()
  val typings = DenoTypings.getInstance(project)
  val depsVirtualFile = LocalFileSystem.getInstance().findFileByPath(denoPackages)
  val denoTypingsVirtualFile = typings.getDenoTypingsVirtualFile()
  return depsVirtualFile to denoTypingsVirtualFile
}

internal fun useWorkspaceModel(): Boolean {
  return Registry.`is`("deno.use.workspace.model.roots", false)
}
