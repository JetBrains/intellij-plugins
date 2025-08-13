// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.libraries.nuxt.library

import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.lang.javascript.psi.util.JSProjectUtil
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.AsyncFileListener.ChangeApplier
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.backend.workspace.toVirtualFileUrl
import com.intellij.platform.backend.workspace.workspaceModel
import com.intellij.platform.workspace.storage.EntityStorage
import com.intellij.platform.workspace.storage.MutableEntityStorage
import com.intellij.platform.workspace.storage.WorkspaceEntity
import com.intellij.platform.workspace.storage.url.VirtualFileUrl
import com.intellij.platform.workspace.storage.url.VirtualFileUrlManager
import com.intellij.util.concurrency.annotations.RequiresBlockingContext
import com.intellij.util.containers.nullize
import com.intellij.util.xmlb.annotations.XCollection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.annotations.TestOnly
import org.jetbrains.vuejs.libraries.nuxt.NUXT_OUTPUT_FOLDER
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Service(Service.Level.PROJECT)
@State(name = "DotNuxtFolderManager", storages = [Storage(StoragePathMacros.CACHE_FILE)])
internal class NuxtFolderManager(
  private val project: Project,
  internal val coroutineScope: CoroutineScope
) : PersistentStateComponent<NuxtFolderManagerState>, Disposable {
  private val folders = ConcurrentHashMap.newKeySet<VirtualFile>()
  val nuxtFolders: List<VirtualFile>
    get() = folders.filter { it.isValid }

  private val nextUpdateId: AtomicInteger = AtomicInteger()
  private val updatesInProgress: MutableSet<String> = ConcurrentHashMap.newKeySet()

  init {
    VirtualFileManager.getInstance().addAsyncFileListener(NuxtFileListener(), this)
  }

  override fun getState(): NuxtFolderManagerState {
    return NuxtFolderManagerState(folders.map { folder -> folder.path })
  }

  override fun loadState(state: NuxtFolderManagerState) {
    setFolders(state.folders.mapNotNull {
      LocalFileSystem.getInstance().findFileByPath(it)?.takeIf { file ->
        isAccepted(file, false)
      }
    })
  }

  internal fun setFolders(newFolders: List<VirtualFile>) {
    folders.clear()
    folders.addAll(newFolders)
  }

  private fun isAccepted(nuxtFolder: VirtualFile, asNewFolder: Boolean): Boolean {
    if (nuxtFolder.isValid && isNuxtFolder(nuxtFolder)) {
      return if (asNewFolder) {
        val projectFileIndex = ProjectFileIndex.getInstance(project)
        ReadAction.compute<Boolean, Throwable> { projectFileIndex.isInContent(nuxtFolder) }
      }
      else {
        ReadAction.compute<Boolean, Throwable> { JSLibraryUtil.isInProjectAndOutsideOfLibraryRoots(project, nuxtFolder) }
      }
    }
    return false
  }

  fun addIfMissing(nuxtFolder: VirtualFile) {
    if (!folders.contains(nuxtFolder) && isAccepted(nuxtFolder, true) && folders.add(nuxtFolder)) {
      LOG.info("Added .nuxt/ folder (${nuxtFolder.path}), total folders: ${folders.size}")
      addExcludeEntity(nuxtFolder)
      addOrUpdateLibraryEntity(nuxtFolder)
    }
  }

  /**
   * Update the Workspace Model asynchronously.
   *
   * In tests, use `waitCoroutinesBlocking(getDotNuxtFolderManagerCoroutineScope(project))`
   * to wait for the Workspace Model update: in particular, it will wait for the
   * [WorkspaceFileIndex][com.intellij.workspaceModel.core.fileIndex.WorkspaceFileIndex] to be updated,
   * but not for the file index, use `IndexingTestUtil.waitUntilIndexesAreReady(project)` for that.
   */
  internal fun updateWorkspaceModel(description: String, action: (WorkspaceModel, MutableEntityStorage) -> Unit) {
    val update = "#${nextUpdateId.incrementAndGet()}: $description"
    updatesInProgress.add(update)

    LOG.debug { "Starting workspace model update: '$update', total updates running (${updatesInProgress.size}): $updatesInProgress" }
    coroutineScope.launch {
      val workspaceModel = project.workspaceModel
      workspaceModel.update(description) { storage ->
        action(workspaceModel, storage)
        updatesInProgress.remove(update)
        LOG.debug { "Finished workspace model update: '$update', total updates running: ${updatesInProgress.size}" }
      }
    }
  }

  private fun addExcludeEntity(nuxtFolder: VirtualFile) {
    updateWorkspaceModel("Exclude .nuxt/ for " + nuxtFolder.path) { workspaceModel, storage ->
      val virtualFileUrlManager = workspaceModel.getVirtualFileUrlManager()
      val nuxtFolderUrl = nuxtFolder.toVirtualFileUrl(virtualFileUrlManager)
      val entities = findEntities(storage, nuxtFolderUrl)
      entities.forEach(storage::removeEntity)
      storage.addEntity(NuxtFolderEntity(nuxtFolderUrl, emptyList(), NuxtFolderEntity.MyEntitySource))
    }
  }

  private fun addOrUpdateLibraryEntity(nuxtFolder: VirtualFile) {
    val library = NuxtFolderLibrary(nuxtFolder)
    updateWorkspaceModel("Include library files from .nuxt/ for " + nuxtFolder.path) { workspaceModel, storage ->
      val virtualFileUrlManager = workspaceModel.getVirtualFileUrlManager()
      val nuxtFolderUrl = nuxtFolder.toVirtualFileUrl(virtualFileUrlManager)
      val entities = findEntities(storage, nuxtFolderUrl)
      entities.forEach(storage::removeEntity)
      storage.addEntity(createEntity(library, virtualFileUrlManager))
    }
  }

  private fun findEntities(storage: EntityStorage, nuxtFolderUrl: VirtualFileUrl): List<WorkspaceEntity> {
    return storage.getVirtualFileUrlIndex().findEntitiesByUrl(nuxtFolderUrl)
      .filter {
        it is NuxtFolderEntity && it.nuxtFolderUrl == nuxtFolderUrl
      }.toList()
  }

  override fun dispose() {}

  companion object {
    @RequiresBlockingContext
    fun getInstance(project: Project): NuxtFolderManager = project.service<NuxtFolderManager>()

    private fun isNuxtFolder(file: VirtualFile): Boolean = file.isDirectory && file.name == NUXT_OUTPUT_FOLDER

    internal fun createEntity(library: NuxtFolderLibrary, virtualFileUrlManager: VirtualFileUrlManager): NuxtFolderEntity.Builder {
      val fileUrls = library.libraryFiles.map { it.toVirtualFileUrl(virtualFileUrlManager) }
      return NuxtFolderEntity(library.nuxtFolder.toVirtualFileUrl(virtualFileUrlManager), fileUrls, NuxtFolderEntity.MyEntitySource)
    }
  }

  private inner class NuxtFileListener : AsyncFileListener {
    override fun prepareChange(events: List<VFileEvent>): ChangeApplier? {
      val relevantEvents = events.filter(::isRelevantEvent).nullize() ?: return null
      return object : ChangeApplier {
        override fun afterVfsChange() {
          for (event in relevantEvents) {
            when (event) {
              is VFileCreateEvent -> {
                findNuxtFolder(event.parent)?.let {
                  addOrUpdateLibraryEntity(it)
                }
              }
            }
          }
        }
      }
    }

    private fun findNuxtFolder(file: VirtualFile): VirtualFile? {
      return JSProjectUtil.traverseUpAndFindFirst(file) { if (folders.contains(it)) it else null }
    }

    private fun isRelevantEvent(event: VFileEvent): Boolean {
      when (event) {
        is VFileCreateEvent -> {
          return findNuxtFolder(event.parent) != null
        }
      }
      return false
    }
  }
}

internal class NuxtFolderManagerState @JvmOverloads constructor(
  @field:XCollection(propertyElementName = "nuxtFolders")
  @JvmField
  internal val folders: List<String> = listOf(),
)

internal val LOG: Logger = logger<NuxtFolderManager>()

/**
 * Reset [NuxtFolderManager] asynchronously in tests.
 * Workaround for the following limitations of light tests:
 * 1. No state reset for a project service implementing `PersistentStateComponent` at light test startup.
 * 2. No project startup activities are run at light test startup.
 *
 * Tests should await separately for its finish using `waitCoroutinesBlocking(getDotNuxtFolderManagerCoroutineScope(project))`.
 */
@TestOnly
fun resetDotNuxtFolderManager(project: Project) {
  NuxtFolderManager.getInstance(project).setFolders(emptyList())
  NuxtFolderModelSynchronizer(project).sync()
}

@TestOnly
fun getDotNuxtFolderManagerCoroutineScope(project: Project): CoroutineScope = NuxtFolderManager.getInstance(project).coroutineScope
