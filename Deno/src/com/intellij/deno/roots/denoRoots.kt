package com.intellij.deno.roots

import com.intellij.deno.DenoSettings
import com.intellij.deno.entities.DenoEntity
import com.intellij.deno.entities.DenoEntityBuilder
import com.intellij.deno.entities.DenoEntitySource
import com.intellij.deno.service.DenoTypings
import com.intellij.deno.useDenoLibrary
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.backend.workspace.storeAndGet
import com.intellij.platform.workspace.storage.MutableEntityStorage
import com.intellij.util.concurrency.annotations.RequiresEdt
import org.jetbrains.annotations.ApiStatus

/**
 * Makes the [DenoEntity] in the workspace model match [createDenoEntity].
 * Call it after a change of the Deno settings or of the Deno cache.
 *
 * `WorkspaceEntityLifecycleSupporterUtils.ensureEntitiesInWorkspaceAreAsProviderDefined` does the same work, but it is a suspend function.
 * The callers run on the EDT and expect the entity to be up to date when this function returns, so the change to it is postponed.
 */
@RequiresEdt
@ApiStatus.Internal
fun updateDenoEntity(project: Project) {
  if (project.isDefault) return
  val expected = createDenoEntity(project)
  val workspaceModel = WorkspaceModel.getInstance(project)
  val current = workspaceModel.currentSnapshot.entities(DenoEntity::class.java).toList()
  val upToDate = if (expected == null) current.isEmpty() else current.size == 1 && expected.hasSameRoots(current.single())
  if (upToDate) return

  val builder = MutableEntityStorage.create()
  if (expected != null) {
    builder.addEntity(expected)
  }
  ApplicationManager.getApplication().runWriteAction {
    workspaceModel.updateProjectModel("Update Deno entity") { storage ->
      storage.replaceBySource({ it is DenoEntitySource }, builder)
    }
  }
}

/**
 * Creates the [DenoEntity] with the Deno cache folders and the Deno typings, or returns `null` when the project does not use the Deno library.
 *
 * The cache folders are stored by path, so the file index picks them up when `deno cache` creates them later.
 */
@ApiStatus.Internal
fun createDenoEntity(project: Project): DenoEntityBuilder? {
  if (!useWorkspaceModel() || !useDenoLibrary(project)) return null

  val settings = DenoSettings.getService(project)
  val urlManager = WorkspaceModel.getInstance(project).getVirtualFileUrlManager()
  val typings = DenoTypings.getInstance(project).getDenoTypingsVirtualFile()
  // The cache path comes from a settings field and can end with a slash. A URL keeps an empty segment, so a doubled slash would give a
  // URL that differs from the URL of the folder.
  fun folderUrl(path: String) = urlManager.storeAndGet(VfsUtilCore.pathToUrl(FileUtil.toCanonicalPath(path)))
  return DenoEntity(DenoEntitySource) {
    depsFile = folderUrl(settings.getDenoCacheDeps())
    npmFolder = folderUrl(settings.getDenoNpm())
    denoTypes = typings?.let { urlManager.storeAndGet(it) }
  }
}

@ApiStatus.Internal
fun DenoEntityBuilder.hasSameRoots(entity: DenoEntity): Boolean {
  return depsFile == entity.depsFile && npmFolder == entity.npmFolder && denoTypes == entity.denoTypes
}

internal fun useWorkspaceModel(): Boolean {
  return Registry.`is`("deno.use.workspace.model.roots", true)
}
