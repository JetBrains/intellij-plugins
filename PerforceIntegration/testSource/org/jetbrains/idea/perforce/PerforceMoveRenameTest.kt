package org.jetbrains.idea.perforce

import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vcs.VcsConfiguration
import com.intellij.openapi.vcs.VcsException
import com.intellij.openapi.vcs.VcsShowConfirmationOption
import com.intellij.openapi.vcs.actions.VcsContextFactory
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.diff.DiffProvider
import com.intellij.openapi.vcs.history.VcsFileRevision
import com.intellij.openapi.vcs.history.VcsHistorySession
import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.openapi.vcs.impl.AbstractVcsHelperImpl
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.RefreshWorker
import com.intellij.openapi.vfs.newvfs.impl.VfsData
import com.intellij.openapi.vfs.newvfs.persistent.PersistentFS
import com.intellij.testFramework.EdtTestUtil
import com.intellij.testFramework.TestLoggerFactory
import com.intellij.util.Consumer
import org.jetbrains.idea.perforce.application.PerforceFileRevision
import org.jetbrains.idea.perforce.application.PerforceVcs
import org.junit.Assert
import org.junit.Test

import static com.intellij.testFramework.UsefulTestCase.*
import static junit.framework.TestCase.*

class PerforceMoveRenameTest extends PerforceTestCase {
  private static final Logger LOG = Logger.getInstance(PerforceMoveRenameTest.class)
  @Override
  void before() throws Exception {
    super.before()
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY)
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.REMOVE, VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY)
  }

  private void doTestIdeaRenameDirectory(boolean old) throws IOException, VcsException {
    if (old) {
      forceDisableMoveCommand()
    }

    final VirtualFile dir = createDirectoryWithFiveFiles("foo")

    submitDefaultList("comment1")

    renameFileInCommand(dir, "bar")

    getChangeListManager().waitUntilRefreshed()

    assertEmpty(getChangeListManager().getUnversionedFiles())

    submitDefaultList("comment2")
    refreshChanges()

    final List<VcsFileRevision> revisionList = getFileHistory(dir.findChild("e.txt"))
    assertEquals(2, revisionList.size())
    assertEquals(old ? "add" : "move/add", ((PerforceFileRevision)revisionList.get(0)).getAction())
  }

  private VirtualFile createDirectoryWithFiveFiles(final String name) throws IOException {
    final VirtualFile dir = createDirInCommand(myWorkingCopyDir, name)
    addFile(name + "/" + createFileInCommand(dir, "a.txt", "").getName())
    addFile(name + "/" + createFileInCommand(dir, "b.txt", "").getName())
    addFile(name + "/" + createFileInCommand(dir, "c.txt", "").getName())
    addFile(name + "/" + createFileInCommand(dir, "d.txt", "").getName())
    addFile(name + "/" + createFileInCommand(dir, "e.txt", "").getName())
    return dir
  }

  @Test
  void testIdeaRenameDirectory() throws IOException, VcsException {
    doTestIdeaRenameDirectory(false)
  }

  @Test
  void testIdeaRenameDirectory_Old() throws IOException, VcsException {
    doTestIdeaRenameDirectory(true)
  }


  @Test
  void testIdeaRename() throws Exception { testIdeaRenameOrMove(true, false) }
  @Test
  void testIdeaMove() throws Exception { testIdeaRenameOrMove(false, false) }
  @Test
  void testIdeaRename_Old() throws Exception { testIdeaRenameOrMove(true, true) }
  @Test
  void testIdeaMove_Old() throws Exception { testIdeaRenameOrMove(false, true) }

  private void testIdeaRenameOrMove(boolean rename, boolean old) throws IOException, VcsException {
    if (old) {
      forceDisableMoveCommand()
    }

    final VirtualFile file = createFileInCommand("a.txt", "")
    addFile("a.txt")

    submitDefaultList("comment1")

    Assert.assertEquals(1, getFileHistory(file).size())

    openForEdit(file)
    if (rename) {
      renameFileInCommand(file, "b.txt")
    }
    else {
      moveFileInCommand(file, createDirInCommand(myWorkingCopyDir, "newParent"))
    }

    getChangeListManager().waitUntilRefreshed()

    Change change = getSingleChange()
    Assert.assertEquals(Change.Type.MOVED, change.getType())
    verifyChange(change, "a.txt", rename ? "b.txt" : "newParent/a.txt")

    submitDefaultList("comment2")
    refreshChanges()

    final List<VcsFileRevision> revisionList = getFileHistory(file)
    assertEquals(2, revisionList.size())
    assertEquals(old ? "add" : "move/add", ((PerforceFileRevision)revisionList.get(0)).getAction())
  }

  @Test
  void testCurrentRevisionAfterRename() throws IOException, VcsException {
    doTestCurrentRevisionAfterRename()
  }

  @Test
  void testCurrentRevisionAfterRename_Old() throws IOException, VcsException {
    forceDisableMoveCommand()
    doTestCurrentRevisionAfterRename()
  }

  private void doTestCurrentRevisionAfterRename() throws IOException, VcsException {
    final VirtualFile file = createFileInCommand("a.txt", "")
    addFile("a.txt")
    submitDefaultList("comment1")
    refreshChanges()

    openForEdit(file)
    editFileInCommand(file, "abc")
    submitDefaultList("comment2")
    refreshChanges()

    renameFileInCommand(file, "b.txt")
    submitDefaultList("comment3")
    refreshChanges()

    final VcsHistorySession session = PerforceVcs.getInstance(myProject).getVcsHistoryProvider().createSessionFor(VcsContextFactory.instance.createFilePathOn(file))
    assert session != null
    assertNotNull(session.getCurrentRevisionNumber())
    assertOrderedCollection(session.getRevisionList(), new Consumer<VcsFileRevision>() {
      @Override
      void consume(VcsFileRevision revision) {
        assertTrue(revision.getCommitMessage().endsWith("comment3"))
        assertTrue(session.isCurrentRevision(revision.getRevisionNumber()))
      }
    }, new Consumer<VcsFileRevision>() {
      @Override
      void consume(VcsFileRevision revision) {
        assertTrue(revision.getCommitMessage().endsWith("comment2"))
        assertFalse(session.isCurrentRevision(revision.getRevisionNumber()))
      }
    }, new Consumer<VcsFileRevision>() {
      @Override
      void consume(VcsFileRevision revision) {
        assertTrue(revision.getCommitMessage().endsWith("comment1"))
        assertFalse(session.isCurrentRevision(revision.getRevisionNumber()))
      }
    })
  }

  @Test
  void testRevertAfterRename() throws IOException, VcsException {
    doTestRevertAfterRename()
  }

  @Test
  void testRevertAfterRename_Old() throws IOException, VcsException {
    forceDisableMoveCommand()
    doTestRevertAfterRename()
  }

  private void doTestRevertAfterRename() throws IOException, VcsException {
    final VirtualFile foo = createDirectoryWithFiveFiles("foo")
    final VirtualFile bar = createDirectoryWithFiveFiles("bar")
    refreshChanges()
    assertEquals(10, assertOneElement(getChangeListManager().getChangeLists()).getChanges().size())
    assertEmpty(getChangeListManager().getUnversionedFiles())

    submitDefaultList("initial")
    refreshChanges()
    assertEmpty(assertOneElement(getChangeListManager().getChangeLists()).getChanges())
    assertEmpty(getChangeListManager().getUnversionedFiles())

    renameFileInCommand(foo, "foo1")
    renameFileInCommand(bar, "bar1")
    getChangeListManager().waitUntilRefreshed()

    assertEmpty(getChangeListManager().getUnversionedFiles())
    final Collection<Change> changes = assertOneElement(getChangeListManager().getChangeLists()).getChanges()
    assertEquals(10, changes.size())

    rollbackChanges(changes as List)
    getChangeListManager().waitUntilRefreshed()

    assertEmpty(assertOneElement(getChangeListManager().getChangeLists()).getChanges())
    assertEmpty(getChangeListManager().getUnversionedFiles())
  }

  @Test
  void testDiffRevision() throws IOException, VcsException {
    doTestDiffRevision()
  }

  @Test
  void testDiffRevision_Old() throws IOException, VcsException {
    forceDisableMoveCommand()
    doTestDiffRevision()
  }

  private void doTestDiffRevision() throws IOException, VcsException {
    final VirtualFile file = createFileInCommand("a.txt", "foo")
    addFile("a.txt")
    submitDefaultList("comment1")
    refreshChanges()

    openForEdit(file)
    renameFileInCommand(file, "b.txt")
    getChangeListManager().waitUntilRefreshed()

    editFileInCommand(file, "bar")
    refreshChanges()

    DiffProvider diffProvider = PerforceVcs.getInstance(myProject).getDiffProvider()
    assert diffProvider != null
    VcsRevisionNumber currentRevision = diffProvider.getCurrentRevision(file)
    assertNotNull(currentRevision)
    assertEquals("foo", diffProvider.createFileContent(currentRevision, file).getContent())
    assertNotNull(diffProvider.getLastRevision(file))
  }

  @Test
  void testMoveBetweenRootsInSameWorkspace() {
    doTestMoveBetweenRootsInSameWorkspace(false)
  }

  @Test
  void testMoveBetweenRootsInSameWorkspace_Old() {
    forceDisableMoveCommand()
    doTestMoveBetweenRootsInSameWorkspace(true)
  }

  private void doTestMoveBetweenRootsInSameWorkspace(boolean old) {
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY)
    VirtualFile dir1 = createDirInCommand(myWorkingCopyDir, "dir1")
    VirtualFile dir2 = createDirInCommand(myWorkingCopyDir, "dir2")
    VirtualFile file = createFileInCommand(dir1, "a.txt", "")
    submitDefaultList("initial")

    setP4ConfigRoots(dir1, dir2)

    openForEdit(file)
    moveFileInCommand(file, dir2)
    getChangeListManager().waitUntilRefreshed()

    assertEquals(Change.Type.MOVED, getSingleChange().getType())
    submitDefaultList("moved")
    refreshChanges()

    final List<VcsFileRevision> revisionList = getFileHistory(file)
    assertEquals(2, revisionList.size())
    assertEquals(old ? "add" : "move/add", ((PerforceFileRevision)revisionList.get(0)).getAction())
  }

  @Test
  void testDoubleRenameBC_AB() {
    doTestRenameBC_AB(true)
  }

  @Test
  void testDoubleRenameBC_AB_Old() {
    forceDisableMoveCommand()
    doTestRenameBC_AB(true)
  }

  @Test
  void testSingleRenameBC_AB() {
    doTestRenameBC_AB(false)
  }

  @Test
  void testSingleRenameBC_AB_Old() {
    forceDisableMoveCommand()
    doTestRenameBC_AB(false)
  }

  // see https://youtrack.jetbrains.com/issue/IDEA-207595
  private void ensureVfsIsReallyUpToDateAfterPreviousConcurrentRefreshes() {
    refreshVfs()
  }

  private void doTestRenameBC_AB(final boolean doubleRename) {
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY)
    VcsException hadException = null
    AbstractVcsHelperImpl.setCustomExceptionHandler(myProject, { t -> hadException = t } as Consumer<VcsException>)
    VirtualFile file1 = createFileInCommand("foo.txt", "")
    VirtualFile file2 = createFileInCommand("bar.txt", "")
    submitDefaultList("initial")

    VfsUtil.markDirty(true, true, myWorkingCopyDir)
    refreshVfs()
    assert !file1.writable
    assert !file2.writable
    refreshChanges()
    assertEmpty(getChangeListManager().getAllChanges())

    openForEdit(file1)
    openForEdit(file2)

    if (doubleRename) {
      renameFileInCommand(file1, "foo1.txt")
    }

    String barPath = file2.getPath()

    ensureVfsIsReallyUpToDateAfterPreviousConcurrentRefreshes()
    renameFileInCommand(file2, "bar1.txt")
    assert !hadException
    ensureVfsIsReallyUpToDateAfterPreviousConcurrentRefreshes()
    renameFileInCommand(file1, "bar.txt")
    assert hadException?.message?.contains(PerforceBundle.message("exception.text.cannot.assure.no.file.being.on.server", barPath))
    hadException = null
    getChangeListManager().waitUntilRefreshed()

    def changes = changeListManager.allChanges
    if (doubleRename) {
      assert changes.find { it.type == Change.Type.DELETED && it.beforeRevision.file.path.endsWith('foo.txt') }
      assert changes.find { it.type == Change.Type.NEW && it.afterRevision.file.path.endsWith('foo1.txt') }
    }
    assert changes.find { it.type == Change.Type.MOVED && it.beforeRevision.file.path.endsWith('bar.txt') && it.afterRevision.file.path.endsWith('bar1.txt') }
    assertSize(doubleRename ? 3 : 1, changes)
    assertEmpty(getChangeListManager().getUnversionedFiles())
    assertEmpty(getChangeListManager().getModifiedWithoutEditing())
    assert assertOneElement(getChangeListManager().getDeletedFiles()).path.path.endsWith(doubleRename ? 'foo1.txt' : 'foo.txt')

    EdtTestUtil.runInEdtAndWait({
      UndoManager.getInstance(myProject).undo(null)
    })
    getChangeListManager().waitUntilRefreshed()

    //assert getChangeListManager().getAllChanges().size() == (old ? 3 : 2)

    assertEmpty(getChangeListManager().getUnversionedFiles())
    assertEmpty(getChangeListManager().getModifiedWithoutEditing())
    assertEmpty(getChangeListManager().getDeletedFiles())

    TestLoggerFactory.enableDebugLogging(myTestRootDisposable, RefreshWorker.class, PersistentFS.class, VfsData.class)

    submitDefaultList("move")
    VfsUtil.markDirty(true, false, file1, file2)
    refreshVfs()

    LOG.info("checking")

    assert VfsUtil.virtualToIoFile(file1).exists()
    assert VfsUtil.virtualToIoFile(file2).exists()

    assert !VfsUtil.virtualToIoFile(file1).canWrite()
    assert !VfsUtil.virtualToIoFile(file2).canWrite()

    LOG.info("file2 id " + file2.id)

    assert file1.valid
    assert file2.valid

    assert !file1.writable
    assert !file2.writable

    refreshChanges()

    assertChangesViewEmpty()

    renameFileInCommand(file1, "bar.txt")
    changeListManager.waitUntilRefreshed()
    getSingleChange()

    assert !hadException
  }

  @Test
  void testRenameChangingCase() {
    doTestRenameChangingCase()
  }

  @Test
  void testRenameChangingCase_Old() {
    forceDisableMoveCommand()
    doTestRenameChangingCase()
  }

  void doTestRenameChangingCase() {
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY)

    def dir = createDirInCommand(myWorkingCopyDir, "dir")
    createFileInCommand(dir, 'a.txt', 'asdf')
    changeListManager.waitUntilRefreshed()
    assert singleChange

    renameFileInCommand(dir, 'Dir')
    assert dir.valid
    changeListManager.waitUntilRefreshed()
    assert singleChange
  }

  @Test
  void testMoveUnversionedIntoAnotherModule() {
    doTestMoveUnversionedIntoAnotherModule()
  }

  @Test
  void testMoveUnversionedIntoAnotherModule_Old() {
    forceDisableMoveCommand()
    doTestMoveUnversionedIntoAnotherModule()
  }

  private void doTestMoveUnversionedIntoAnotherModule() {
    def dir1 = createDirInCommand(myWorkingCopyDir, "dir1")
    def dir2 = createDirInCommand(myWorkingCopyDir, "dir2")
    setP4ConfigRoots(dir1, dir2)
    def file = createFileInCommand(dir1, 'foo.txt', '')
    refreshChanges()
    assertOneElement(changeListManager.unversionedFiles)

    moveFileInCommand(file, dir2)
    changeListManager.waitUntilRefreshed()
    assertOneElement(changeListManager.unversionedFiles)
  }
}
