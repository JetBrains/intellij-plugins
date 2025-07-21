// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.libraries.nuxt.library

import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.lang.javascript.psi.util.JSProjectUtil
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.components.*
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
import com.intellij.platform.workspace.storage.EntityStorage
import com.intellij.platform.workspace.storage.WorkspaceEntity
import com.intellij.platform.workspace.storage.url.VirtualFileUrl
import com.intellij.platform.workspace.storage.url.VirtualFileUrlManager
import com.intellij.util.concurrency.annotations.RequiresBlockingContext
import com.intellij.util.containers.nullize
import com.intellij.util.xmlb.annotations.XCollection
import org.jetbrains.vuejs.libraries.nuxt.NUXT_OUTPUT_FOLDER
import java.util.concurrent.ConcurrentHashMap

@Service(Service.Level.PROJECT)
@State(name = "DotNuxtFolderManager", storages = [Storage(StoragePathMacros.CACHE_FILE)])
internal class NuxtFolderManager(private val project: Project) : PersistentStateComponent<NuxtFolderManagerState>, Disposable {
  private val folders = ConcurrentHashMap.newKeySet<VirtualFile>()
  val nuxtFolders: List<VirtualFile>
    get() = folders.filter { it.isValid }

  init {
    VirtualFileManager.getInstance().addAsyncFileListener(NuxtFileListener(), this)
  }

  override fun getState(): NuxtFolderManagerState {
    return NuxtFolderManagerState(folders.map { folder -> folder.path })
  }

  override fun loadState(state: NuxtFolderManagerState) {
    val newFolders = state.folders.mapNotNull {
      val file = LocalFileSystem.getInstance().findFileByPath(it)
      if (file != null && isAccepted(file, false)) {
        file
      }
      else null
    }
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
      addExcludeEntity(nuxtFolder)
      addOrUpdateLibraryEntity(nuxtFolder)
    }
  }

  private fun addExcludeEntity(nuxtFolder: VirtualFile) {
    invokeUnderWriteAction(project) {
      val workspaceModel = WorkspaceModel.getInstance(project)
      workspaceModel.updateProjectModel("Exclude .nuxt/ for " + nuxtFolder.path) { storage ->
        val virtualFileUrlManager = workspaceModel.getVirtualFileUrlManager()
        val nuxtFolderUrl = nuxtFolder.toVirtualFileUrl(virtualFileUrlManager)
        val entities = findEntities(storage, nuxtFolderUrl)
        entities.forEach(storage::removeEntity)
        storage.addEntity(NuxtFolderEntity(nuxtFolderUrl, emptyList(), NuxtFolderEntity.MyEntitySource))
      }
    }
  }

  private fun addOrUpdateLibraryEntity(nuxtFolder: VirtualFile) {
    val runnable = {
      doCreateLibrary(nuxtFolder)
    }
    val application = ApplicationManager.getApplication()
    if (application.isUnitTestMode || !application.isDispatchThread && !application.isReadAccessAllowed) {
      runnable()
    }
    else {
      application.executeOnPooledThread(runnable)
    }
  }

  private fun doCreateLibrary(nuxtFolder: VirtualFile) {
    val library = NuxtFolderLibrary(nuxtFolder)
    invokeUnderWriteAction(project) {
      val workspaceModel = WorkspaceModel.getInstance(project)
      workspaceModel.updateProjectModel("Include library files from .nuxt/ for " + nuxtFolder.path) { storage ->
        val virtualFileUrlManager = workspaceModel.getVirtualFileUrlManager()
        val nuxtFolderUrl = nuxtFolder.toVirtualFileUrl(virtualFileUrlManager)
        val entities = findEntities(storage, nuxtFolderUrl)
        entities.forEach(storage::removeEntity)
        storage.addEntity(createEntity(library, virtualFileUrlManager))
      }
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

    fun invokeUnderWriteAction(project: Project, runnable: Runnable) {
      val application = ApplicationManager.getApplication()
      if (application.isUnitTestMode) {
        WriteAction.runAndWait<Throwable> {
          runnable.run()
        }
      }
      val runnableUnderWriteAction = { application.runWriteAction(runnable) }
      if (application.isWriteIntentLockAcquired) {
        runnableUnderWriteAction()
      }
      else {
        application.invokeLater(runnableUnderWriteAction, application.defaultModalityState, project.disposed)
      }
    }

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
