package org.jetbrains.idea.perforce.application

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
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
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.VFileProperty
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileCopyEvent
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent
import com.intellij.openapi.wm.IdeFrame
import com.intellij.vcsUtil.VcsUtil
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

    VirtualFileManager.getInstance().addAsyncFileListenerBackgroundable(MyVfsListener(), parentDisposable)
    val appConnection = ApplicationManager.getApplication().getMessageBus().connect(parentDisposable)
    appConnection.subscribe<ApplicationActivationListener>(ApplicationActivationListener.TOPIC, myFrameStateListener)
  }

  private fun deactivate() {
    myHasLostFocus = true
  }

  fun addWritableFiles(root: VirtualFile, filesCollection: MutableCollection<VirtualFile>, withIgnored: Boolean) {
    filesCollection.addWritableFilesFromRoot(root, withIgnored, scopeFilter = null)
  }

  fun addWritableFiles(
    root: VirtualFile,
    filesCollection: MutableCollection<VirtualFile>,
    withIgnored: Boolean,
    scopeFilter: VcsDirtyScope,
  ) {
    filesCollection.addWritableFilesFromRoot(root, withIgnored, scopeFilter)
  }

  private fun MutableCollection<VirtualFile>.addWritableFilesFromRoot(
    root: VirtualFile,
    withIgnored: Boolean,
    scopeFilter: VcsDirtyScope?,
  ) {
    val writableFilesUnderRoot = getWritableFilesUnderRoot(root)
    val changeListManager = ChangeListManager.getInstance(myProject)

    for (file in writableFilesUnderRoot) {
      if (shouldIncludeFile(file, withIgnored, scopeFilter, changeListManager)) {
        add(file)
      }
    }
  }

  private fun getWritableFilesUnderRoot(root: VirtualFile): Set<VirtualFile> {
    synchronized(myWritableFiles) {
      // do not collect init files under lock
      myWritableFiles[root]?.let { return HashSet(it) }
    }

    return initializeWritableFiles(root)
  }

  private fun shouldIncludeFile(
    file: VirtualFile,
    withIgnored: Boolean,
    scopeFilter: VcsDirtyScope?,
    changeListManager: ChangeListManager,
  ): Boolean {
    if (scopeFilter?.belongsTo(VcsUtil.getFilePath(file)) == false) return false
    return withIgnored || !changeListManager.isIgnoredFile(file)
  }

  private fun initializeWritableFiles(root: VirtualFile): Set<VirtualFile> {
    val newWritableFiles = buildSet {
      myVcsManager.iterateVfUnderVcsRoot(root) { virtualFile ->
        addFileIfWritable(virtualFile)
        true
      }
    }
    synchronized(myWritableFiles) {
      myWritableFiles[root]?.let { return HashSet(it) }
      myWritableFiles[root] = newWritableFiles.toMutableSet()
      return newWritableFiles
    }
  }

  private fun updateWritableFiles(root: VirtualFile?, isWritable: Boolean, file: VirtualFile) {
    synchronized(myWritableFiles) {
      myWritableFiles[root]?.let { writableFiles ->
        if (isWritable) {
          writableFiles.add(file)
        }
        else {
          writableFiles.remove(file)
        }
      }
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

    for (path in missingFiles) {
      val filePath = VcsUtil.getFilePath(path, false)
      if (FileStatus.DELETED != addGate.getStatus(filePath)) {
        builder.processLocallyDeletedFile(filePath)
      }
    }
  }

  private fun getAddedFilesInCurrentChangesView(addGate: ChangeListManagerGate): Set<FilePath> = buildSet {
    for (list in addGate.getListsCopy()) {
      for (change in list.getChanges()) {
        val afterRevision = change.afterRevision
        if (FileStatus.ADDED == change.fileStatus && afterRevision != null) {
          val file = afterRevision.getFile()
          if (file.fileIsUnderP4Root()) {
            add(file)
          }
        }
      }
    }
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

  private inner class MyVfsListener : AsyncFileListener {
    override fun prepareChange(events: MutableList<out VFileEvent>): AsyncFileListener.ChangeApplier? {
      val beforeActions = ArrayList<VfsAction>()
      val afterActions = ArrayList<VfsAction>()

      for (event in events) {
        ProgressManager.checkCanceled()
        when (event) {
          is VFilePropertyChangeEvent -> {
            if (VirtualFile.PROP_WRITABLE == event.propertyName) {
              val isWritable = event.newValue as? Boolean ?: continue
              event.file.getPathUnderP4Root()?.let { path ->
                val root = myVcsManager.getVcsRootFor(path)
                afterActions.add(WritableChange(path, root, isWritable, event.file))
              }
            }
          }
          is VFileContentChangeEvent -> {
            val file = event.file
            if (!file.isWritable) {
              event.file.getPathUnderP4Root()?.let { path ->
                afterActions.add(ReportRecheck(path))
              }
            }
          }
          is VFileCreateEvent -> {
            val newPath = VcsUtil.getFilePath(event.path, event.isDirectory)
            newPath.takeIf { it.fileIsUnderP4Root() }?.let { path ->
              afterActions.add(ReportRecheck(path))
            }
          }
          is VFileMoveEvent -> {
            val isDirectory = event.file.isDirectory
            val newPath = VcsUtil.getFilePath(event.newPath, isDirectory)
            val oldPath = VcsUtil.getFilePath(event.oldPath, isDirectory)
            newPath.takeIf { it.fileIsUnderP4Root() }?.let { path ->
              afterActions.add(ReportRecheck(path))
            }
            oldPath.takeIf { it.fileIsUnderP4Root() }?.let { path ->
              beforeActions.add(ReportDelete(path))
            }
          }
          is VFileCopyEvent -> {
            val copiedPath = VcsUtil.getFilePath(event.path, event.file.isDirectory)
            copiedPath.takeIf { it.fileIsUnderP4Root() }?.let { path ->
              afterActions.add(ReportRecheck(path))
            }
          }
          is VFileDeleteEvent -> {
            event.file.getPathUnderP4Root()?.let { path ->
              beforeActions.add(ReportDelete(path))
            }
          }
        }
      }

      if (beforeActions.isEmpty() && afterActions.isEmpty()) return null

      return object : AsyncFileListener.ChangeApplier {
        override fun beforeVfsChange() {
          beforeActions.forEach { it.process(this@PerforceReadOnlyFileStateManager) }
        }

        override fun afterVfsChange() {
          afterActions.forEach { it.process(this@PerforceReadOnlyFileStateManager) }
        }
      }
    }

    private fun VirtualFile.getPathUnderP4Root(): FilePath? = VcsUtil.getFilePath(this).takeIf { it.fileIsUnderP4Root() }
  }

  private sealed interface VfsAction {
    fun process(manager: PerforceReadOnlyFileStateManager)
  }

  private data class WritableChange(
    val path: FilePath,
    val root: VirtualFile?,
    val isWritable: Boolean,
    val file: VirtualFile,
  ) : VfsAction {
    override fun process(manager: PerforceReadOnlyFileStateManager) {
      manager.myDirtyFilesHandler.reportRecheck(path)
      manager.updateWritableFiles(root, isWritable, file)
    }
  }

  private data class ReportRecheck(val path: FilePath) : VfsAction {
    override fun process(manager: PerforceReadOnlyFileStateManager) {
      manager.myDirtyFilesHandler.reportRecheck(path)
    }
  }

  private data class ReportDelete(val path: FilePath) : VfsAction {
    override fun process(manager: PerforceReadOnlyFileStateManager) {
      manager.myDirtyFilesHandler.reportDelete(path)
    }
  }

  private fun FilePath.fileIsUnderP4Root(): Boolean {
    if (myProject.isDisposed()) {
      return false
    }

    if (ChangeListManager.getInstance(myProject).isIgnoredFile(this)) {
      return false
    }

    myVcsManager.getVcsFor(this)?.let { return it.keyInstanceMethod == PerforceVcs.getKey() }
    return false
  }

  companion object {
    private val LOG = Logger.getInstance(PerforceReadOnlyFileStateManager::class.java)

    private fun MutableSet<VirtualFile>.addFileIfWritable(vf: VirtualFile) {
      runReadActionBlocking {
        if (vf.isValid() && !vf.isDirectory() && vf.isWritable() && !vf.`is`(VFileProperty.SYMLINK)) {
          add(vf)
        }
      }
    }
  }
}
