package org.jetbrains.idea.perforce.application

import com.intellij.dvcs.repo.VcsManagedFilesHolderBase
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.changes.VcsManagedFilesHolder
import com.intellij.openapi.vfs.VirtualFile

class PerforceIgnoredFilesHolder(private val unversionedTracker: PerforceUnversionedTracker) : VcsManagedFilesHolderBase() {

  override fun isInUpdatingMode() = unversionedTracker.isInUpdateMode

  override fun containsFile(file: FilePath, vcsRoot: VirtualFile): Boolean {
    return unversionedTracker.isIgnored(file)
  }

  override fun values(): Collection<FilePath> {
    return unversionedTracker.ignoredFiles
  }

  class Provider(val project: Project) : VcsManagedFilesHolder.Provider {
    private val vcs = PerforceVcs.getInstance(project)

    override fun getVcs(): PerforceVcs = vcs
    override fun createHolder() = PerforceIgnoredFilesHolder(vcs.onlineChangeProvider.unversionedTracker)
  }
}