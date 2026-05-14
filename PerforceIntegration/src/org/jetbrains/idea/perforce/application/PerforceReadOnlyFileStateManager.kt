package org.jetbrains.idea.perforce.application

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.FileStatus
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.VcsException
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vcs.changes.ChangeListManagerGate
import com.intellij.openapi.vcs.changes.ChangelistBuilder
import com.intellij.openapi.vcs.changes.VcsDirtyScope
import com.intellij.openapi.vfs.VFileProperty
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileCopyEvent
import com.intellij.openapi.vfs.VirtualFileEvent
import com.intellij.openapi.vfs.VirtualFileListener
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.VirtualFileMoveEvent
import com.intellij.openapi.vfs.VirtualFilePropertyEvent
import com.intellij.openapi.wm.IdeFrame
import com.intellij.vcsUtil.VcsUtil
import java.io.File
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.collections.MutableCollection
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.concurrent.Volatile

class PerforceReadOnlyFileStateManager(private val myProject: Project, private val myDirtyFilesHandler: PerforceDirtyFilesHandler) {
  private val myVcsManager = ProjectLevelVcsManager.getInstance(myProject)
  private val myLock = Any()
  private val myFrameStateListener = object : ApplicationActivationListener {
    override fun applicationDeactivated(ideFrame: IdeFrame) {
      processFocusLost()
    }
  }
  private val myPreviousAddedSnapshot: MutableSet<FilePath> = HashSet()

  private val myWritableFiles: MutableMap<VirtualFile, MutableSet<VirtualFile>> = HashMap()

  @Volatile
  private var myHasLostFocus = false

  fun activate(parentDisposable: Disposable) {
    Disposer.register(parentDisposable) { this.deactivate() }

    VirtualFileManager.getInstance().addVirtualFileListener(MyVfsListener(), parentDisposable)
    val appConnection = ApplicationManager.getApplication().getMessageBus().connect(parentDisposable)
    appConnection.subscribe<ApplicationActivationListener>(ApplicationActivationListener.TOPIC, myFrameStateListener)
  }

  private fun deactivate() {
    myHasLostFocus = true
  }

  fun addWritableFiles(root: VirtualFile, writableFiles: MutableCollection<VirtualFile>, withIgnored: Boolean) {
    var writablesUnderRoot: MutableSet<VirtualFile> = HashSet()
    var needInit = false
    synchronized(myWritableFiles) {
      // do not collect init files under lock
      if (!myWritableFiles.containsKey(root)) {
        needInit = true
      }

      val currentFilesUnderRoot = myWritableFiles[root]
      if (currentFilesUnderRoot != null) {
        writablesUnderRoot = HashSet(currentFilesUnderRoot)
      }
    }

    if (needInit) {
      writablesUnderRoot = initializeWritableFiles(root)
    }

    for (vf in writablesUnderRoot) {
      if (withIgnored || !ChangeListManager.getInstance(myProject).isIgnoredFile(vf)) {
        writableFiles.add(vf)
      }
    }
  }

  fun addWritableFiles(
    root: VirtualFile, writableFiles: MutableCollection<VirtualFile>, withIgnored: Boolean,
    scopeFilter: VcsDirtyScope,
  ) {
    var writablesUnderRoot: MutableSet<VirtualFile> = HashSet()
    var needInit = false
    synchronized(myWritableFiles) {
      if (!myWritableFiles.containsKey(root)) {
        needInit = true
      }
      val currentFilesUnderRoot = myWritableFiles[root]
      if (currentFilesUnderRoot != null) {
        writablesUnderRoot = HashSet(currentFilesUnderRoot)
      }
    }

    if (needInit) {
      writablesUnderRoot = initializeWritableFiles(root)
    }

    for (vf in writablesUnderRoot) {
      if (!scopeFilter.belongsTo(VcsUtil.getFilePath(vf))) continue
      if (withIgnored || !ChangeListManager.getInstance(myProject).isIgnoredFile(vf)) {
        writableFiles.add(vf)
      }
    }
  }

  private fun initializeWritableFiles(root: VirtualFile): MutableSet<VirtualFile> {
    val newWritableFiles: MutableSet<VirtualFile> = HashSet()
    myVcsManager.iterateVfUnderVcsRoot(root) { vf: VirtualFile ->
      addFileIfWritable(newWritableFiles, vf)
      true
    }
    synchronized(myWritableFiles) {
      if (!myWritableFiles.containsKey(root)) {
        myWritableFiles[root] = newWritableFiles
      }
      return HashSet(myWritableFiles[root]!!)
    }
  }

  @Throws(VcsException::class)
  fun getChanges(
    dirtyScope: VcsDirtyScope, builder: ChangelistBuilder, progress: ProgressIndicator,
    addGate: ChangeListManagerGate,
  ) {
    val newAdded = getAddedFilesInCurrentChangesView(addGate)
    synchronized(myLock) {
      progress.checkCanceled()
      recheckPreviouslyAddedFiles(newAdded)
      recheckWhatUnversionedRefreshNeeded(dirtyScope)
    }

    val missingFiles = myDirtyFilesHandler.scanAndGetMissingFiles(addGate, progress)

    val locallyDeleted = findLocallyDeletedMissingFiles(addGate, missingFiles)
    for (path in locallyDeleted) {
      builder.processLocallyDeletedFile(VcsUtil.getFilePath(path, false))
    }
  }

  private fun getAddedFilesInCurrentChangesView(addGate: ChangeListManagerGate): Set<FilePath> {
    val set = HashSet<FilePath>()
    for (list in addGate.getListsCopy()) {
      for (change in list.getChanges()) {
        val afterRevision = change.afterRevision
        if (FileStatus.ADDED == change.fileStatus && afterRevision != null) {
          val file = afterRevision.getFile()
          if (fileIsUnderP4Root(file)) {
            set.add(file)
          }
        }
      }
    }
    return set
  }

  private fun recheckWhatUnversionedRefreshNeeded(dirtyScope: VcsDirtyScope) {
    if (myHasLostFocus && dirtyScope.wasEveryThingDirty()) {
      LOG.info("--- recheck missing")
      myHasLostFocus = false
      myDirtyFilesHandler.rescanIfProblems()
    }
  }

  private fun recheckPreviouslyAddedFiles(newAdded: Set<FilePath>) {
    val copy = HashSet(myPreviousAddedSnapshot)
    copy.removeAll(newAdded)
    myPreviousAddedSnapshot.clear()
    myPreviousAddedSnapshot.addAll(newAdded)
    if (!copy.isEmpty()) {
      myDirtyFilesHandler.reportRecheck(copy)
    }
  }

  fun processFocusLost() {
    myHasLostFocus = true
  }

  fun discardUnversioned() {
    synchronized(myWritableFiles) {
      myWritableFiles.clear()
    }
    myDirtyFilesHandler.scheduleTotalRescan()
  }

  private inner class MyVfsListener : VirtualFileListener {
    override fun propertyChanged(event: VirtualFilePropertyEvent) {
      val file = event.file
      val path = VcsUtil.getFilePath(file)
      if (VirtualFile.PROP_WRITABLE == event.propertyName && fileIsUnderP4Root(path)) {
        myDirtyFilesHandler.reportRecheck(path)

        val root = myVcsManager.getVcsRootFor(path)
        synchronized(myWritableFiles) {
          if (myWritableFiles.containsKey(root)) {
            val writableFiles = myWritableFiles[root]!!
            if (event.newValue as Boolean) {
              writableFiles.add(file)
            }
            else {
              writableFiles.remove(file)
            }
          }
        }
      }
    }

    override fun contentsChanged(event: VirtualFileEvent) {
      val file = event.file
      val path = VcsUtil.getFilePath(file)
      if (!file.isWritable() && fileIsUnderP4Root(path)) {
        myDirtyFilesHandler.reportRecheck(path)
      }
    }

    override fun fileCreated(event: VirtualFileEvent) {
      val file = event.file
      val path = VcsUtil.getFilePath(file)
      if (!fileIsUnderP4Root(path)) return
      myDirtyFilesHandler.reportRecheck(path)
    }

    override fun fileMoved(event: VirtualFileMoveEvent) {
      val file = event.file
      val path = VcsUtil.getFilePath(file)
      if (!fileIsUnderP4Root(path)) return
      myDirtyFilesHandler.reportRecheck(path)
    }

    override fun fileCopied(event: VirtualFileCopyEvent) {
      val file = event.file
      val path = VcsUtil.getFilePath(file)
      if (!fileIsUnderP4Root(path)) return
      myDirtyFilesHandler.reportRecheck(path)
    }

    override fun beforeFileDeletion(event: VirtualFileEvent) {
      val file = event.file
      val path = VcsUtil.getFilePath(file)
      if (!fileIsUnderP4Root(path)) return
      myDirtyFilesHandler.reportDelete(path)
    }

    override fun beforeFileMovement(event: VirtualFileMoveEvent) {
      val file = event.file
      val path = VcsUtil.getFilePath(file)
      if (!fileIsUnderP4Root(path)) return
      myDirtyFilesHandler.reportDelete(path)
    }
  }

  private fun fileIsUnderP4Root(file: FilePath): Boolean {
    if (myProject.isDisposed()) {
      return false
    }

    if (ChangeListManager.getInstance(myProject).isIgnoredFile(file)) {
      return false
    }

    val vcs = myVcsManager.getVcsFor(file)
    return vcs != null && PerforceVcs.getKey() == vcs.keyInstanceMethod
  }

  companion object {
    private val LOG = Logger.getInstance(PerforceReadOnlyFileStateManager::class.java)

    private fun addFileIfWritable(collection: MutableSet<VirtualFile>, vf: VirtualFile) {
      ApplicationManager.getApplication().runReadAction {
        if (!vf.isValid() || vf.isDirectory() || !vf.isWritable() || vf.`is`(VFileProperty.SYMLINK)) {
          return@runReadAction
        }
        collection.add(vf)
      }
    }

    private fun findLocallyDeletedMissingFiles(addGate: ChangeListManagerGate, missingFiles: Set<String>): Set<String> {
      val locallyDeleted = HashSet<String>()
      for (path in missingFiles) {
        if (FileStatus.DELETED != addGate.getStatus(VcsUtil.getFilePath(File(path)))) {
          locallyDeleted.add(path)
        }
      }
      return locallyDeleted
    }
  }
}
