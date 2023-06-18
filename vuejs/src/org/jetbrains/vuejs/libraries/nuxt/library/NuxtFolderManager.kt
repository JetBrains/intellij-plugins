// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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
import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.AsyncFileListener.ChangeApplier
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.xmlb.annotations.XCollection
import com.intellij.workspaceModel.ide.WorkspaceModel
import com.intellij.workspaceModel.ide.getInstance
import com.intellij.workspaceModel.ide.toVirtualFileUrl
import com.intellij.workspaceModel.storage.EntityStorage
import com.intellij.workspaceModel.storage.WorkspaceEntity
import com.intellij.workspaceModel.storage.url.VirtualFileUrl
import com.intellij.workspaceModel.storage.url.VirtualFileUrlManager
import org.jetbrains.vuejs.libraries.nuxt.NUXT_OUTPUT_FOLDER

@Service(Service.Level.PROJECT)
@State(name = "DotNuxtFolderManager", storages = [Storage(StoragePathMacros.CACHE_FILE)])
class NuxtFolderManager(private val project: Project) : PersistentStateComponent<NuxtFolderManagerState>, Disposable {
  private val folders = ContainerUtil.newConcurrentSet<VirtualFile>()
  val nuxtFolders: List<VirtualFile>
    get() = folders.filter { it.isValid }

  init {
    VirtualFileManager.getInstance().addAsyncFileListener(NuxtFileListener(), this)
  }

  override fun getState(): NuxtFolderManagerState {
    return NuxtFolderManagerState().also {
      it.folders = folders.map { folder -> folder.path }
    }
  }

  override fun loadState(state: NuxtFolderManagerState) {
    val newFolders: List<VirtualFile> = state.folders.mapNotNull {
      val file = LocalFileSystem.getInstance().findFileByPath(it)
      if (file != null && isAccepted(file, false)) file else null
    }
    folders.clear()
    folders.addAll(newFolders)
  }

  private fun isAccepted(nuxtFolder: VirtualFile, asNewFolder: Boolean): Boolean {
    return nuxtFolder.isValid && isNuxtFolder(nuxtFolder) && ReadAction.compute<Boolean, Throwable> {
      if (asNewFolder) {
        ProjectFileIndex.getInstance(project).isInContent(nuxtFolder)
      }
      else {
        JSLibraryUtil.isInProjectAndOutsideOfLibraryRoots(project, nuxtFolder)
      }
    }
  }

  fun addIfMissing(nuxtFolder: VirtualFile) {
    if (!folders.contains(nuxtFolder) && isAccepted(nuxtFolder, true) && folders.add(nuxtFolder)) {
      addExcludeEntity(nuxtFolder)
      addOrUpdateLibraryEntity(nuxtFolder)
    }
  }

  private fun addExcludeEntity(nuxtFolder: VirtualFile) {
    invokeUnderWriteAction(project) {
      WorkspaceModel.getInstance(project).updateProjectModel("Exclude .nuxt/ for " + nuxtFolder.path) { storage ->
        val virtualFileUrlManager = VirtualFileUrlManager.getInstance(project)
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
      WorkspaceModel.getInstance(project).updateProjectModel("Include library files from .nuxt/ for " + nuxtFolder.path) { storage ->
        val virtualFileUrlManager = VirtualFileUrlManager.getInstance(project)
        val nuxtFolderUrl = nuxtFolder.toVirtualFileUrl(virtualFileUrlManager)
        val entities = findEntities(storage, nuxtFolderUrl)
        entities.forEach(storage::removeEntity)
        storage.addEntity(createEntity(library, virtualFileUrlManager))
      }
    }
  }

  private fun findEntities(storage: EntityStorage, nuxtFolderUrl: VirtualFileUrl): List<WorkspaceEntity> {
    return storage.getVirtualFileUrlIndex().findEntitiesByUrl(nuxtFolderUrl)
      .map(Pair<WorkspaceEntity, String>::first)
      .filter {
        it is NuxtFolderEntity && it.nuxtFolderUrl == nuxtFolderUrl
      }.toList()
  }

  override fun dispose() {}

  companion object {

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

    internal fun createEntity(library: NuxtFolderLibrary, virtualFileUrlManager: VirtualFileUrlManager): NuxtFolderEntity {
      val fileUrls = library.libraryFiles.map { it.toVirtualFileUrl(virtualFileUrlManager) }
      return NuxtFolderEntity(library.nuxtFolder.toVirtualFileUrl(virtualFileUrlManager), fileUrls, NuxtFolderEntity.MyEntitySource)
    }
  }

  private inner class NuxtFileListener : AsyncFileListener {
    override fun prepareChange(events: List<VFileEvent>): ChangeApplier? {
      val relevantEvents = events.filter { isRelevantEvent(it) }
      return if (relevantEvents.isEmpty()) null else object : ChangeApplier {
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


class NuxtFolderManagerState {
  @XCollection(propertyElementName = "nuxtFolders")
  internal var folders: List<String> = listOf()
}
