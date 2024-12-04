package org.jetbrains.idea.perforce

import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vcs.VcsConfiguration
import com.intellij.openapi.vcs.VcsException
import com.intellij.openapi.vcs.VcsShowConfirmationOption
import com.intellij.openapi.vcs.actions.VcsContextFactory
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.impl.AbstractVcsHelperImpl
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.impl.VfsData
import com.intellij.openapi.vfs.newvfs.persistent.PersistentFS
import com.intellij.testFramework.EdtTestUtil
import com.intellij.testFramework.TestLoggerFactory
import com.intellij.testFramework.UsefulTestCase.*
import com.intellij.util.Consumer
import com.intellij.util.ThrowableRunnable
import org.jetbrains.idea.perforce.application.PerforceFileRevision
import org.jetbrains.idea.perforce.application.PerforceVcs
import org.junit.Assert
import org.junit.Test

open class PerforceMoveRenameTest : PerforceTestCase() {
  private val LOG = Logger.getInstance(PerforceMoveRenameTest::class.java)

  override fun before() {
    super.before()
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY)
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.REMOVE, VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY)
  }

  private fun doTestIdeaRenameDirectory(old: Boolean) {
    if (old) {
      forceDisableMoveCommand()
    }

    val dir = createDirectoryWithFiveFiles("foo")

    submitDefaultList("comment1")

    renameFileInCommand(dir, "bar")
    refreshVfs()
    refreshChanges()

    assertEmpty(changeListManager.unversionedFiles)

    submitDefaultList("comment2")
    refreshChanges()

    val revisionList = getFileHistory(dir.findChild("e.txt"))
    assertEquals(2, revisionList.size)
    assertEquals(if (old) "add" else "move/add", (revisionList[0] as PerforceFileRevision).action)
  }

  private fun createDirectoryWithFiveFiles(name: String): VirtualFile {
    val dir = createDirInCommand(myWorkingCopyDir, name)
    addFile("$name/" + createFileInCommand(dir, "a.txt", "").name)
    addFile("$name/" + createFileInCommand(dir, "b.txt", "").name)
    addFile("$name/" + createFileInCommand(dir, "c.txt", "").name)
    addFile("$name/" + createFileInCommand(dir, "d.txt", "").name)
    addFile("$name/" + createFileInCommand(dir, "e.txt", "").name)
    return dir
  }

  @Test
  fun testIdeaRenameDirectory() {
    doTestIdeaRenameDirectory(false)
  }

  @Test
  fun testIdeaRenameDirectory_Old() {
    doTestIdeaRenameDirectory(true)
  }


  @Test
  fun testIdeaRename() {
    testIdeaRenameOrMove(true, false)
  }

  @Test
  fun testIdeaMove() {
    testIdeaRenameOrMove(false, false)
  }

  @Test
  fun testIdeaRename_Old() {
    testIdeaRenameOrMove(true, true)
  }

  @Test
  fun testIdeaMove_Old() {
    testIdeaRenameOrMove(false, true)
  }

  private fun testIdeaRenameOrMove(rename: Boolean, old: Boolean) {
    if (old) {
      forceDisableMoveCommand()
    }

    val file = createFileInCommand("a.txt", "")
    addFile("a.txt")

    submitDefaultList("comment1")

    Assert.assertEquals(1, getFileHistory(file).size)

    openForEdit(file)
    if (rename) {
      renameFileInCommand(file, "b.txt")
    }
    else {
      moveFileInCommand(file, createDirInCommand(myWorkingCopyDir, "newParent"))
      refreshChanges()
    }

    changeListManager.waitUntilRefreshed()

    val change = getSingleChange()
    Assert.assertEquals(Change.Type.MOVED, change.type)
    verifyChange(change, "a.txt", if (rename) "b.txt" else "newParent/a.txt")

    submitDefaultList("comment2")
    refreshChanges()

    val revisionList = getFileHistory(file)
    assertEquals(2, revisionList.size)
    assertEquals(if (old) "add" else "move/add", (revisionList[0] as PerforceFileRevision).action)
  }

  @Test
  fun testCurrentRevisionAfterRename() {
    doTestCurrentRevisionAfterRename()
  }

  @Test
  fun testCurrentRevisionAfterRename_Old() {
    forceDisableMoveCommand()
    doTestCurrentRevisionAfterRename()
  }

  private fun doTestCurrentRevisionAfterRename() {
    val file = createFileInCommand("a.txt", "")
    addFile("a.txt")
    refreshChanges()

    submitDefaultList("comment1")
    refreshChanges()

    openForEdit(file)
    editFileInCommand(file, "abc")
    submitDefaultList("comment2")
    refreshChanges()

    renameFileInCommand(file, "b.txt")
    refreshVfs()
    refreshChanges()

    submitDefaultList("comment3")
    refreshChanges()

    val session = PerforceVcs.getInstance(myProject).vcsHistoryProvider!!.createSessionFor(
      VcsContextFactory.getInstance().createFilePathOn(file))
    Assert.assertNotNull(session)
    assertNotNull(session!!.currentRevisionNumber)
    assertOrderedCollection(session.revisionList, Consumer { revision ->
      assertTrue(revision.commitMessage!!.endsWith("comment3"))
      assertTrue(session.isCurrentRevision(revision.revisionNumber))
    }, Consumer { revision ->
      assertTrue(revision.commitMessage!!.endsWith("comment2"))
      assertFalse(session.isCurrentRevision(revision.revisionNumber))
    }, Consumer { revision ->
        assertTrue(revision.commitMessage!!.endsWith("comment1"))
        assertFalse(session.isCurrentRevision(revision.revisionNumber))
    })
  }

  @Test
  fun testRevertAfterRename() {
    doTestRevertAfterRename()
  }

  @Test
  fun testRevertAfterRename_Old() {
    forceDisableMoveCommand()
    doTestRevertAfterRename()
  }

  private fun doTestRevertAfterRename() {
    val foo = createDirectoryWithFiveFiles("foo")
    val bar = createDirectoryWithFiveFiles("bar")
    refreshChanges()
    assertEquals(10, assertOneElement(changeListManager.changeLists).changes.size)
    assertTrue(changeListManager.unversionedFilesPaths.isEmpty())

    submitDefaultList("initial")
    refreshChanges()
    assertTrue(assertOneElement(changeListManager.changeLists).changes.isEmpty())
    assertTrue(changeListManager.unversionedFilesPaths.isEmpty())

    renameFileInCommand(foo, "foo1")
    renameFileInCommand(bar, "bar1")
    refreshChanges()

    assertTrue(changeListManager.unversionedFilesPaths.isEmpty())
    val changes = assertOneElement(changeListManager.changeLists).changes
    assertEquals(10, changes.size)

    rollbackChanges(changes.toMutableList())
    refreshChanges()

    assertTrue(assertOneElement(changeListManager.changeLists).changes.isEmpty())
    assertTrue(changeListManager.unversionedFilesPaths.isEmpty())
  }

  @Test
  fun testDiffRevision() {
    doTestDiffRevision()
  }

  @Test
  fun testDiffRevision_Old() {
    forceDisableMoveCommand()
    doTestDiffRevision()
  }

  private fun doTestDiffRevision() {
    val file = createFileInCommand("a.txt", "foo")
    addFile("a.txt")
    submitDefaultList("comment1")
    refreshChanges()

    openForEdit(file)
    renameFileInCommand(file, "b.txt")
    refreshChanges()

    editFileInCommand(file, "bar")
    refreshChanges()

    val diffProvider = PerforceVcs.getInstance(myProject).diffProvider
    assertNotNull(diffProvider)
    val currentRevision = diffProvider!!.getCurrentRevision(file)
    assertNotNull(currentRevision)
    assertEquals("foo", diffProvider.createFileContent(currentRevision, file)!!.content)
    assertNotNull(diffProvider.getLastRevision(file))
  }

  @Test
  fun testMoveBetweenRootsInSameWorkspace() {
    doTestMoveBetweenRootsInSameWorkspace(false)
  }

  @Test
  fun testMoveBetweenRootsInSameWorkspace_Old() {
    forceDisableMoveCommand()
    doTestMoveBetweenRootsInSameWorkspace(true)
  }

  private fun doTestMoveBetweenRootsInSameWorkspace(old: Boolean) {
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY)
    val dir1 = createDirInCommand(myWorkingCopyDir, "dir1")
    val dir2 = createDirInCommand(myWorkingCopyDir, "dir2")
    val file = createFileInCommand(dir1, "a.txt", "")
    refreshChanges()

    submitDefaultList("initial")

    setP4ConfigRoots(dir1, dir2)

    openForEdit(file)
    moveFileInCommand(file, dir2)
    refreshChanges()

    assertEquals(Change.Type.MOVED, getSingleChange().type)
    submitDefaultList("moved")
    refreshChanges()

    val revisionList = getFileHistory(file)
    assertEquals(2, revisionList.size)
    assertEquals(if (old) "add" else "move/add", (revisionList[0] as PerforceFileRevision).action)
  }

  @Test
  fun testDoubleRenameBC_AB() {
    doTestRenameBC_AB(true)
  }

  @Test
  fun testDoubleRenameBC_AB_Old() {
    forceDisableMoveCommand()
    doTestRenameBC_AB(true)
  }

  @Test
  fun testSingleRenameBC_AB() {
    doTestRenameBC_AB(false)
  }

  @Test
  fun testSingleRenameBC_AB_Old() {
    forceDisableMoveCommand()
    doTestRenameBC_AB(false)
  }

  // see https://youtrack.jetbrains.com/issue/IDEA-207595
  private fun ensureVfsIsReallyUpToDateAfterPreviousConcurrentRefreshes() {
    refreshVfs()
  }

  private fun doTestRenameBC_AB(doubleRename: Boolean) {
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY)
    var hadException: VcsException? = null
    AbstractVcsHelperImpl.setCustomExceptionHandler(myProject) { t -> hadException = t }
    val file1 = createFileInCommand("foo.txt", "")
    val file2 = createFileInCommand("bar.txt", "")
    refreshChanges()

    submitDefaultList("initial")

    VfsUtil.markDirty(true, true, myWorkingCopyDir)
    refreshVfs()
    assertNotNull(file1)
    assertNotNull(file2)
    refreshChanges()
    assertTrue(changeListManager.allChanges.isEmpty())

    openForEdit(file1)
    openForEdit(file2)

    if (doubleRename) {
      renameFileInCommand(file1, "foo1.txt")
    }

    val barPath = file2.path

    ensureVfsIsReallyUpToDateAfterPreviousConcurrentRefreshes()
    renameFileInCommand(file2, "bar1.txt")
    assertNull(hadException)
    ensureVfsIsReallyUpToDateAfterPreviousConcurrentRefreshes()
    renameFileInCommand(file1, "bar.txt")
    assertTrue(hadException?.message?.contains(PerforceBundle.message("exception.text.cannot.assure.no.file.being.on.server", barPath)) == true)
    hadException = null
    refreshChanges()

    val changes = changeListManager.allChanges

    if (doubleRename) {
      assertNotNull(changes.find { it.type == Change.Type.DELETED && it.beforeRevision!!.file.path.endsWith("foo.txt") })
      assertNotNull(changes.find { it.type == Change.Type.NEW && it.afterRevision!!.file.path.endsWith("foo1.txt") })
    }
    assertNotNull(changes.find {
      it.type == Change.Type.MOVED && it.beforeRevision!!.file.path.endsWith("bar.txt") && it.afterRevision!!.file.path.endsWith("bar1.txt")
    })
    assertEquals((if (doubleRename) 3 else 1), changes.size)
    assertTrue(changeListManager.unversionedFilesPaths.isEmpty())
    assertTrue(changeListManager.modifiedWithoutEditing.isEmpty())
    assertNotNull(assertOneElement(changeListManager.deletedFiles).path.path.endsWith(if (doubleRename) "foo1.txt" else "foo.txt"))

    EdtTestUtil.runInEdtAndWait(ThrowableRunnable {
      UndoManager.getInstance(myProject).undo(null)
    })
    refreshChanges()

    //assert getChangeListManager().getAllChanges().size() == (old ? 3 : 2)

    assertEmpty(changeListManager.unversionedFilesPaths)
    assertEmpty(changeListManager.modifiedWithoutEditing)
    assertEmpty(changeListManager.deletedFiles)

    TestLoggerFactory.enableDebugLogging(myTestRootDisposable, PersistentFS::class.java, VfsData::class.java)

    submitDefaultList("move")
    VfsUtil.markDirty(true, false, file1, file2)
    refreshVfs()

    LOG.info("checking")

    assertTrue(VfsUtil.virtualToIoFile(file1).exists())
    assertTrue(VfsUtil.virtualToIoFile(file2).exists())

    assertFalse(VfsUtil.virtualToIoFile(file1).canWrite())
    assertFalse(VfsUtil.virtualToIoFile(file2).canWrite())

    assertTrue(file1.isValid)
    assertTrue(file2.isValid)

    assertFalse(file1.isWritable)
    assertFalse(file2.isWritable)

    refreshChanges()

    assertChangesViewEmpty()

    renameFileInCommand(file1, "bar.txt")
    refreshChanges()
    getSingleChange()

    assertNull(hadException)
  }

  @Test
  fun testRenameChangingCase() {
    doTestRenameChangingCase()
  }

  @Test
  fun testRenameChangingCase_Old() {
    forceDisableMoveCommand()
    doTestRenameChangingCase()
  }

  fun doTestRenameChangingCase() {
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY)

    val dir = createDirInCommand(myWorkingCopyDir, "dir")
    createFileInCommand(dir, "a.txt", "asdf")
    refreshChanges()
    assertNotNull(getSingleChange())

    renameFileInCommand(dir, "Dir")
    assertTrue(dir.isValid)
    assertNotNull(getSingleChange())
  }

  @Test
  fun testMoveUnversionedIntoAnotherModule() {
    doTestMoveUnversionedIntoAnotherModule()
  }

  @Test
  fun testMoveUnversionedIntoAnotherModule_Old() {
    forceDisableMoveCommand()
    doTestMoveUnversionedIntoAnotherModule()
  }

  private fun doTestMoveUnversionedIntoAnotherModule() {
    val dir1 = createDirInCommand(myWorkingCopyDir, "dir1")
    val dir2 = createDirInCommand(myWorkingCopyDir, "dir2")
    setP4ConfigRoots(dir1, dir2)
    val file = createFileInCommand(dir1, "foo.txt", "")
    refreshChanges()
    assertOneElement(changeListManager.unversionedFiles)

    moveFileInCommand(file, dir2)
    changeListManager.waitUntilRefreshed()
    assertOneElement(changeListManager.unversionedFiles)
  }
}
