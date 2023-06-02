package com.intellij.deno.roots

import com.intellij.deno.DenoSettings
import com.intellij.deno.entities.DenoEntity
import com.intellij.deno.entities.DenoEntitySource
import com.intellij.deno.service.DenoTypings
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.workspaceModel.ide.WorkspaceModel
import com.intellij.workspaceModel.ide.getInstance
import com.intellij.workspaceModel.ide.toVirtualFileUrl
import com.intellij.platform.workspaceModel.storage.MutableEntityStorage
import com.intellij.platform.workspaceModel.storage.url.VirtualFileUrlManager


internal fun createDenoEntity(project: Project) {
  if (!useWorkspaceModel()) return
  val (depsVirtualFile, denoTypingsVirtualFile) = getRoots(project)

  val virtualFileUrlManager = VirtualFileUrlManager.getInstance(project)

  val builder = MutableEntityStorage.create()
  builder addEntity DenoEntity(DenoEntitySource) {
    this.denoTypes = denoTypingsVirtualFile?.toVirtualFileUrl(virtualFileUrlManager)
    this.depsFile = depsVirtualFile?.toVirtualFileUrl(virtualFileUrlManager)
  }

  runWriteAction {
    WorkspaceModel.getInstance(project).updateProjectModel("Create Deno entity") { newBuilder ->
      newBuilder.replaceBySource({ it is DenoEntitySource }, builder)
    }
  }
}

internal fun removeDenoEntity(project: Project) {
  if (!useWorkspaceModel()) return
  runWriteAction {
    WorkspaceModel.getInstance(project).updateProjectModel("Remove Deno entity") { builder ->
      builder.entitiesBySource { it is DenoEntitySource }.values.flatMap { it.values }.flatten().forEach { builder.removeEntity(it) }
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
