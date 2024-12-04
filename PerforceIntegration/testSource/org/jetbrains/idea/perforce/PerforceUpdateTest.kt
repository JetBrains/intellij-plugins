package org.jetbrains.idea.perforce

import com.intellij.openapi.util.Ref
import com.intellij.openapi.vcs.VcsConfiguration
import com.intellij.openapi.vcs.VcsShowConfirmationOption
import com.intellij.openapi.vcs.changes.committed.CommittedChangesCache
import com.intellij.openapi.vcs.update.UpdateSession
import com.intellij.openapi.vcs.update.UpdatedFiles
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.CollectConsumer
import com.intellij.util.containers.ContainerUtil
import com.intellij.vcsUtil.VcsUtil
import org.jetbrains.idea.perforce.application.PerforceVcs
import org.jetbrains.idea.perforce.perforce.connections.AbstractP4Connection
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PerforceUpdateTest : PerforceTestCase() {

  @Before
  override fun before() {
    super.before()
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY)
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.REMOVE, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY)
  }

  @Test
  fun testAddAddConflict() {
    val dir1 = createDirInCommand(myWorkingCopyDir, "dir1")
    val dir2 = createDirInCommand(myWorkingCopyDir, "dir2")
    setupTwoClients(dir1, dir2)

    createFileInCommand(dir1, "a.txt", "hello1")
    refreshChanges()
    submitFileWithClient("test", "//depot/a.txt")
    createFileInCommand(dir2, "a.txt", "hello2")

    val session = updateFromVcs(dir2)
    assertNotNull(
      session.exceptions?.find { it.message.contains("Can't clobber writable file") && it.message.contains("a.txt") })
  }

  private fun updateFromVcs(dir2: VirtualFile): UpdateSession {
    val updatedFiles = UpdatedFiles.create()
    val session = PerforceVcs.getInstance(myProject).updateEnvironment
      .updateDirectories(arrayOf(VcsUtil.getFilePath(dir2)), updatedFiles, null, Ref())
    return session
  }

  @Test
  fun `test bulk committed change query`() {
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY)

    val dir1 = createDirInCommand(myWorkingCopyDir, "dir1")
    val dir2 = createDirInCommand(myWorkingCopyDir, "dir2")
    setupTwoClients(dir1, dir2)

    val commitCount = 10
    val modifiedCount = 10
    val fileCount = commitCount * modifiedCount
    val files1 = (0..fileCount).map { createFileInCommand(dir1, "a${it}.txt", "") }
    verify(runP4WithClient(*(listOf("add") + files1.map { it.path }).toTypedArray()))
    submitDefaultList("initial")

    assertTrue(updateFromVcs(dir2).exceptions.isEmpty())
    dir2.refresh(false, true)
    assertNotNull(dir2.findChild("a0.txt"))

    for (i in 1..commitCount) {
      for (j in modifiedCount * (i - 1) until modifiedCount * i) {
        openForEdit(files1[j])
        setFileText(files1[j], "text$i")
      }
      submitDefaultList("commit $i")
    }

    changeListManager.waitUntilRefreshed()

    val commands = ContainerUtil.createConcurrentList<String>()
    AbstractP4Connection.setCommandCallback(CollectConsumer(commands), myTestRootDisposable)

    CommittedChangesCache.getInstance(myProject).refreshAllCachesAsync(true, false)
    CommittedChangesCache.getInstance(myProject).refreshIncomingChanges()

    assertTrue(commands.size < modifiedCount * 4)
  }
}
