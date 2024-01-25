package org.jetbrains.idea.perforce
import com.intellij.openapi.util.Ref
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.VcsConfiguration
import com.intellij.openapi.vcs.VcsShowConfirmationOption
import com.intellij.openapi.vcs.changes.committed.CommittedChangesCache
import com.intellij.openapi.vcs.update.SequentialUpdatesContext
import com.intellij.openapi.vcs.update.UpdateSession
import com.intellij.openapi.vcs.update.UpdatedFiles
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.CollectConsumer
import com.intellij.util.containers.ContainerUtil
import com.intellij.vcsUtil.VcsUtil
import org.jetbrains.idea.perforce.application.PerforceVcs
import org.jetbrains.idea.perforce.perforce.connections.AbstractP4Connection
import org.junit.Before
import org.junit.Test
class PerforceUpdateTest extends PerforceTestCase {
  @Override
  @Before
  void before() throws Exception {
    super.before()
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY)
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.REMOVE, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY)
  }

  @Test
  void testAddAddConflict() {
    VirtualFile dir1 = createDirInCommand(myWorkingCopyDir, "dir1")
    VirtualFile dir2 = createDirInCommand(myWorkingCopyDir, "dir2")
    setupTwoClients(dir1, dir2)

    createFileInCommand(dir1, "a.txt", "hello1")
    submitFileWithClient("test", "//depot/a.txt")
    createFileInCommand(dir2, "a.txt", "hello2")

    UpdateSession session = updateFromVcs(dir2)
    assert session.exceptions?.find { it.message.contains("Can't clobber writable file") && it.message.contains("a.txt") }
  }

  private UpdateSession updateFromVcs(VirtualFile dir2) {
    final UpdatedFiles updatedFiles = UpdatedFiles.create()
    final UpdateSession session = PerforceVcs.getInstance(myProject).getUpdateEnvironment()
      .updateDirectories([VcsUtil.getFilePath(dir2)] as FilePath[], updatedFiles, null, new Ref<SequentialUpdatesContext>())
    return session
  }

  @Test
  void "test bulk committed change query"() throws Exception {
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY)
    
    VirtualFile dir1 = createDirInCommand(myWorkingCopyDir, "dir1")
    VirtualFile dir2 = createDirInCommand(myWorkingCopyDir, "dir2")
    setupTwoClients(dir1, dir2)

    int commitCount = 10
    int modifiedCount = 10
    int fileCount = commitCount * modifiedCount
    List<VirtualFile> files1 = (0..fileCount).collect { createFileInCommand(dir1, "a${it}.txt", "") }
    verify(runP4WithClient((["add"] + files1.collect { it.path }) as String[]))
    submitDefaultList("initial")
    
    assert !updateFromVcs(dir2).exceptions
    dir2.refresh(false, true)
    assert dir2.findChild("a0.txt")
    
    for (i in 1..commitCount) {
      for (j in modifiedCount * (i - 1) ..< modifiedCount * i) {
        openForEdit(files1[j])
        setFileText(files1[j], "text$i")
      }
      submitDefaultList("commit $i")
    }
    
    changeListManager.waitUntilRefreshed()

    List<String> commands = ContainerUtil.createEmptyCOWList()
    AbstractP4Connection.setCommandCallback(new CollectConsumer<String>(commands), myTestRootDisposable)
    
    CommittedChangesCache.getInstance(myProject).refreshAllCachesAsync(true, false)
    CommittedChangesCache.getInstance(myProject).refreshIncomingChanges()
    
    assert commands.size() < modifiedCount * 4
  }


}