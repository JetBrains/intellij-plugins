package org.jetbrains.idea.perforce.application

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.ThrowableComputable
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.VcsException
import com.intellij.openapi.vcs.VcsMappingListener
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.vcsUtil.VcsUtil
import org.jetbrains.idea.perforce.perforce.PerforceSettings

class PerforceDirtyFilesHandler(private val myProject: Project,
                                private val myUnversionedTracker: PerforceUnversionedTracker) {
  private val LOG = Logger.getInstance(PerforceDirtyFilesHandler::class.java)

  private val myDirtyFiles: MutableSet<FilePath> = mutableSetOf()
  private val myScannerLock = Object()
  private val myDirtyScopeManager: VcsDirtyScopeManager = VcsDirtyScopeManager.getInstance(myProject)
  private val ourFilesThreshold = 200

  private var myTotalRescanThresholdPassed = true

  @Volatile
  private var isActive = false

  @Volatile
  private var myPreviousRescanProblem = false

  init {
    myDirtyScopeManager.markEverythingDirty()
  }

  fun activate(parentDisposable: Disposable) {
    Disposer.register(parentDisposable) { deactivate() }

    myProject.messageBus.connect(parentDisposable).apply {
      subscribe(ProjectLevelVcsManager.VCS_CONFIGURATION_CHANGED, VcsMappingListener { cancelAndRescheduleTotalRescan() })
      subscribe(PerforceSettings.OFFLINE_MODE_EXITED, Runnable { cancelAndRescheduleTotalRescan() })
    }
  }

  private fun deactivate() {
    isActive = false
  }

  private fun cancelAndRescheduleTotalRescan() {
    isActive = false
    scheduleTotalRescan()
  }

  fun rescanIfProblems() {
    if (myPreviousRescanProblem) {
      scheduleTotalRescan()
    }
  }

  fun scheduleTotalRescan() {
    LOG.debug("totalRescan scheduled")
    synchronized(myScannerLock) {
      myTotalRescanThresholdPassed = true
      myDirtyFiles.clear()
      myDirtyScopeManager.markEverythingDirty()
    }
  }

  fun scanAndGetMissingFiles(progress: ProgressIndicator): Set<String> {
    val scanner = createScanner()

    progress.checkCanceled()
    val result: UnversionedScopeScanner.ScanResult = rescan(scanner)
    progress.checkCanceled()

    myUnversionedTracker.markUnknown(result.allLocalFiles)
    myUnversionedTracker.markUnversioned(result.localOnly)

    myUnversionedTracker.scheduleUpdate()
    return result.missingFiles
  }

  private fun createScanner(): ThrowableComputable<UnversionedScopeScanner.ScanResult, VcsException> {
    val scanner: UnversionedScopeScanner = object : UnversionedScopeScanner(myProject) {
      override fun checkCanceled() {
        if (!isActive) {
          throw ProcessCanceledException()
        }
      }
    }
    synchronized(myScannerLock) {
      return if (myTotalRescanThresholdPassed) {
        myTotalRescanThresholdPassed = false
        ThrowableComputable<UnversionedScopeScanner.ScanResult, VcsException> {
          scanner.doRescan(UnversionedScopeScanner.createEverythingDirtyScope(myProject), true)
        }
      }
      else {
        val dirtyFiles: Set<FilePath> = HashSet(myDirtyFiles)
        myDirtyFiles.clear()
        ThrowableComputable<UnversionedScopeScanner.ScanResult, VcsException> { scanner.doRescan(dirtyFiles, false) }
      }
    }
  }

  @Throws(VcsException::class)
  private fun rescan(scanner: ThrowableComputable<UnversionedScopeScanner.ScanResult, VcsException>): UnversionedScopeScanner.ScanResult {
    isActive = true
    myPreviousRescanProblem = false
    return try {
      scanner.compute()
    }
    catch (e: VcsException) {
      myPreviousRescanProblem = true
      throw e
    }
  }

  fun reportRecheck(file: VirtualFile?) {
    myUnversionedTracker.markUnknown(file)
    if (addDirtyFile(VcsUtil.getFilePath(file!!))) {
      myDirtyScopeManager.fileDirty(file)
    }
  }

  fun reportDelete(file: VirtualFile?) {
    if (addDirtyFile(VcsUtil.getFilePath(file!!))) {
      myDirtyScopeManager.fileDirty(file)
    }
  }

  fun reportRecheck(targets: Set<VirtualFile?>) {
    for (target in targets) {
      if (!addDirtyFile(VcsUtil.getFilePath(target!!))) {
        return
      }
    }
    myDirtyScopeManager.filesDirty(targets, null)
  }

  private fun addDirtyFile(holder: FilePath): Boolean {
    synchronized(myScannerLock) {
      if (myTotalRescanThresholdPassed) {
        return false
      }
      LOG.debug("addDirtyFile: $holder")
      myDirtyFiles.add(holder)
      if (myDirtyFiles.size > ourFilesThreshold) {
        scheduleTotalRescan()
        return false
      }
      return true
    }
  }
}