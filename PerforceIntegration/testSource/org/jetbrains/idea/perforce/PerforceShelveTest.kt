package org.jetbrains.idea.perforce

import com.intellij.openapi.vcs.VcsConfiguration
import com.intellij.openapi.vcs.VcsShowConfirmationOption
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.LocalChangeList
import com.intellij.openapi.vcs.changes.ui.RollbackWorker
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.testFramework.UsefulTestCase.assertOneElement
import org.jetbrains.idea.perforce.actions.ShelfUtils
import org.jetbrains.idea.perforce.actions.ShelveAction
import org.jetbrains.idea.perforce.application.PerforceManager
import org.jetbrains.idea.perforce.application.PerforceNumberNameSynchronizer
import org.jetbrains.idea.perforce.application.PerforceShelf
import org.jetbrains.idea.perforce.perforce.P4File
import org.jetbrains.idea.perforce.perforce.PerforceRunner
import org.junit.Assert.*
import org.junit.Test

class PerforceShelveTest : PerforceTestCase() {

  override fun before() {
    super.before()
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY)
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.REMOVE, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY)
  }

  @Test
  fun testReadingShelvedChanges() {
    createFileInCommand("b.txt", "")
    refreshChanges()

    val num = createChangeList("xxx", listOf("//depot/b.txt"))
    runP4WithClient("shelve", "-r", "-c", num.toString())

    refreshChanges()
    assertEquals(2, changeListManager.changeLists.size)

    val xxx = findNotNullChangeList("xxx")
    assertEquals(listOf("//depot/b.txt"), shelf.getShelvedChanges(xxx).map { it.depotPath })
  }

  @Test
  fun testShelveActionRevertsAndCreatesANewChangelist() {
    setP4ConfigRoots(myWorkingCopyDir)

    val file1 = createFileInCommand("a.txt", "")
    refreshChanges()
    submitDefaultList("initial")

    deleteFileInCommand(file1)
    createFileInCommand("b.txt", "")
    changeListManager.waitUntilRefreshed()
    assertEquals(2, changeListManager.allChanges.size)
    assertNotNull(changeListManager.allChanges.find { it.type === Change.Type.NEW })
    assertNotNull(changeListManager.allChanges.find { it.type === Change.Type.DELETED })

    ShelveAction.Handler.shelveChanges(myProject, "my shelf", changeListManager.allChanges)
    changeListManager.waitUntilRefreshed()
    assertTrue(changeListManager.allChanges.isEmpty())
    assertTrue(changeListManager.unversionedFiles.isEmpty())

    val list = findNotNullChangeList("my shelf")
    assertEquals(setOf("//depot/a.txt", "//depot/b.txt"), shelf.getShelvedChanges(list).map { it.depotPath }.toSet())
  }

  @Test
  fun testUnshelveToTheSameChangelist() {
    val file = createFileInCommand("b.txt", "")
    refreshChanges()
    submitDefaultList("initial")
    openForEdit(file)
    editFileInCommand(file, "abc")

    val num = createChangeList("xxx", listOf("//depot/b.txt"))
    runP4WithClient("shelve", "-r", "-c", num.toString())
    runP4WithClient("revert", "//depot/b.txt")
    refreshChanges()
    assertTrue(changeListManager.allChanges.isEmpty())
    VfsUtil.markDirtyAndRefresh(false, false, false, file)
    assertEquals("", VfsUtil.loadText(file))


    val xxx = findNotNullChangeList("xxx")
    ShelfUtils.unshelveChanges(shelf.getShelvedChanges(xxx), myProject, true)
    changeListManager.waitUntilRefreshed()

    assertEquals(2, changeListManager.changeLists.size)
    val change = assertOneElement(findNotNullChangeList("xxx").changes)
    assertEquals(change.type, Change.Type.MODIFICATION)
    assertEquals(file, change.virtualFile)

    assertEquals("abc", VfsUtil.loadText(file))
  }

  @Test
  fun testShelveAndUnshelveRenamedChanges() {
    val file1 = createFileInCommand("a.txt", "")
    refreshChanges()
    submitDefaultList("initial")

    openForEdit(file1)
    renameFileInCommand(file1, "b.txt")
    discardUnversionedCacheAndWaitFullRefresh()
    assertEquals(listOf(Change.Type.MOVED), changeListManager.allChanges.map { it.type })

    ShelveAction.Handler.shelveChanges(myProject, "my shelf", changeListManager.allChanges)
    refreshChanges()
    assertTrue(changeListManager.allChanges.isEmpty())
    assertTrue(changeListManager.unversionedFiles.isEmpty())

    val list = findNotNullChangeList("my shelf")
    assertNotNull(list)

    val shelved = shelf.getShelvedChanges(list)
    assertEquals(setOf("//depot/a.txt", "//depot/b.txt"), shelved.map { it.depotPath }.toSet())

    ShelfUtils.unshelveChanges(shelved, myProject, true)
    refreshChanges()
    assertEquals(listOf(Change.Type.MOVED), changeListManager.allChanges.map { it.type })
  }

  @Test
  fun testShelveFromANamedChangelistWithTheSameCommentAddsToTheExistingShelf() {
    val changes = prepareChangeListWithShelfAndModifiedFile()

    ShelveAction.Handler.shelveChanges(myProject, "my shelf", changes)
    changeListManager.waitUntilRefreshed()

    val list = findNotNullChangeList("my shelf")
    assertEquals(setOf("//depot/a.txt", "//depot/b.txt"), shelf.getShelvedChanges(list).map { it.depotPath }.toSet())
  }

  @Test
  fun testShelveFromANamedChangelistWithADifferentCommentCreatesNewChangelist() {
    val changes = prepareChangeListWithShelfAndModifiedFile()

    ShelveAction.Handler.shelveChanges(myProject, "my shelf2", changes)
    changeListManager.waitUntilRefreshed()

    assertEquals(setOf("//depot/b.txt"), shelf.getShelvedChanges(findNotNullChangeList("my shelf")).map { it.depotPath }.toSet())
    assertEquals(setOf("//depot/a.txt"), shelf.getShelvedChanges(findNotNullChangeList("my shelf2")).map { it.depotPath }.toSet())
  }

  // creates 'my shelf' changelist with shelved 'b.txt' and locally modified 'a.txt'
  private fun prepareChangeListWithShelfAndModifiedFile(): Collection<Change> {
    val file1 = createFileInCommand("a.txt", "")
    refreshChanges()
    submitDefaultList("initial")

    createFileInCommand("b.txt", "")
    changeListManager.waitUntilRefreshed()

    ShelveAction.Handler.shelveChanges(myProject, "my shelf", changeListManager.allChanges)
    changeListManager.waitUntilRefreshed()

    val list = findNotNullChangeList("my shelf")
    assertEquals(setOf("//depot/b.txt"), shelf.getShelvedChanges(list).map { it.depotPath }.toSet())

    openForEdit(file1)
    editFileInCommand(file1, "changed")
    moveToChangelist(PerforceNumberNameSynchronizer.getInstance(myProject).getAllNumbers("my shelf").values().first(), "//depot/a.txt")
    refreshChanges()

    val newList = findNotNullChangeList("my shelf")
    assertOneElement(newList.changes)
    return newList.changes
  }

  private fun findNotNullChangeList(name: String): LocalChangeList {
    val result = changeListManager.changeLists.find { it.name == name }
    assertNotNull(result)
    return result!!
  }

  private val shelf: PerforceShelf
    get() = PerforceManager.getInstance(myProject).shelf

  @Test
  fun testRevertingAllFilesInAChangelistWithShelfDoesntRemoveItFromTheView() {
    createFileInCommand("a.txt", "")
    refreshChanges()

    val num = createChangeList("xxx", listOf("//depot/a.txt"))
    verify(runP4WithClient("shelve", "-c", num.toString(), "//depot/a.txt"))
    refreshChanges()

    val list = findNotNullChangeList("xxx")
    assertOneElement(list.changes)
    assertOneElement(shelf.getShelvedChanges(list))

    RollbackWorker(myProject).doRollback(list.changes, false)
    changeListManager.waitUntilRefreshed()

    assertTrue(findNotNullChangeList("xxx").changes.isEmpty())
  }

  @Test
  fun testParseBaseRevisionInUnshelveConflict() {
    val file = createFileInCommand("a.txt", "")
    refreshChanges()
    submitDefaultList("initial")

    openForEdit(file)
    editFileInCommand(file, "foo")

    val num = createChangeList("xxx", listOf("//depot/a.txt"))
    verify(runP4WithClient("shelve", "-c", num.toString(), "//depot/a.txt"))

    editFileInCommand(file, "bar")
    assertTrue(runP4WithClient("unshelve", "-s", num.toString()).stdout.contains("must resolve //depot/a.txt@=2"))

    val revision = PerforceRunner.getInstance(myProject).getBaseRevision(P4File.create(file))
    assertNotNull(revision)
    assertEquals("//depot/a.txt", revision?.depotPath)
    assertEquals("#1", revision?.revisionNum)
    assertEquals("@=2", revision?.sourceRevision)
  }
}
