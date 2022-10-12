package org.jetbrains.idea.perforce
import com.intellij.openapi.vcs.VcsConfiguration
import com.intellij.openapi.vcs.VcsShowConfirmationOption
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.LocalChangeList
import com.intellij.openapi.vcs.changes.ui.RollbackWorker
import com.intellij.openapi.vfs.VfsUtil
import org.jetbrains.idea.perforce.actions.ShelfUtils
import org.jetbrains.idea.perforce.actions.ShelveAction
import org.jetbrains.idea.perforce.application.PerforceManager
import org.jetbrains.idea.perforce.application.PerforceNumberNameSynchronizer
import org.jetbrains.idea.perforce.application.PerforceShelf
import org.jetbrains.idea.perforce.perforce.P4File
import org.jetbrains.idea.perforce.perforce.PerforceRunner
import org.junit.Test

import static com.intellij.testFramework.UsefulTestCase.assertOneElement
class PerforceShelveTest extends PerforceTestCase {

  @Override
  void before() throws Exception {
    super.before()
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY)
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.REMOVE, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY)
  }

  @Test
  void "reading shelved changes"() {
    createFileInCommand("b.txt", "")
    long num = createChangeList("xxx", ["//depot/b.txt"])
    runP4WithClient("shelve", '-r', "-c", num as String)

    refreshChanges()
    assert changeListManager.changeLists.size() == 2

    def xxx = findNotNullChangeList('xxx')
    assert shelf.getShelvedChanges(xxx).collect { it.depotPath } == ["//depot/b.txt"]
  }

  @Test
  void "shelve action reverts and creates a new changelist"() {
    setP4ConfigRoots(myWorkingCopyDir)
    
    def file1 = createFileInCommand("a.txt", "")
    submitDefaultList('initial')

    deleteFileInCommand(file1)
    createFileInCommand("b.txt", "")
    changeListManager.waitUntilRefreshed()
    assert changeListManager.allChanges.size() == 2
    assert changeListManager.allChanges.find { it.type == Change.Type.NEW }
    assert changeListManager.allChanges.find { it.type == Change.Type.DELETED }

    ShelveAction.shelveChanges(myProject, 'my shelf', changeListManager.allChanges)
    changeListManager.waitUntilRefreshed()
    assert !changeListManager.allChanges
    assert !changeListManager.unversionedFiles

    def list = findNotNullChangeList 'my shelf'
    assert shelf.getShelvedChanges(list).collect { it.depotPath } as Set == ["//depot/a.txt", "//depot/b.txt"] as Set
  }

  @Test
  void "unshelve to the same changelist"() {
    def file = createFileInCommand("b.txt", "")
    submitDefaultList('initial')
    openForEdit(file)
    editFileInCommand(file, 'abc')
    
    long num = createChangeList("xxx", ["//depot/b.txt"])
    runP4WithClient("shelve", '-r', "-c", num as String)
    runP4WithClient("revert", "//depot/b.txt")
    refreshChanges()
    assert !changeListManager.allChanges
    VfsUtil.markDirtyAndRefresh(false, false, false, file)
    assert VfsUtil.loadText(file) == ''

    def xxx = findNotNullChangeList('xxx')
    ShelfUtils.unshelveChanges(shelf.getShelvedChanges(xxx), myProject, true)
    changeListManager.waitUntilRefreshed()
    
    assert changeListManager.changeLists.size() == 2
    def change = assertOneElement(findNotNullChangeList('xxx').changes)
    assert change.type == Change.Type.MODIFICATION
    assert change.virtualFile == file

    assert VfsUtil.loadText(file) == 'abc'
  }

  @Test
  void "shelve and unshelve renamed changes"() {
    def file1 = createFileInCommand("a.txt", "")
    submitDefaultList('initial')

    openForEdit(file1)
    renameFileInCommand(file1, 'b.txt')
    changeListManager.waitUntilRefreshed()
    assert changeListManager.allChanges.collect { it.type } == [Change.Type.MOVED]

    ShelveAction.shelveChanges(myProject, 'my shelf', changeListManager.allChanges)
    changeListManager.waitUntilRefreshed()
    assert !changeListManager.allChanges
    assert !changeListManager.unversionedFiles

    def list = findNotNullChangeList('my shelf')
    assert list

    def shelved = shelf.getShelvedChanges(list)
    assert shelved.collect { it.depotPath } as Set == ["//depot/a.txt", "//depot/b.txt"] as Set

    ShelfUtils.unshelveChanges(shelved, myProject, true)
    changeListManager.waitUntilRefreshed()
    assert changeListManager.allChanges.collect { it.type } == [Change.Type.MOVED]
  }

  @Test
  void "shelve from a named changelist with the same comment adds to the existing shelf"() {
    def changes = prepareChangeListWithShelfAndModifiedFile()

    ShelveAction.shelveChanges(myProject, 'my shelf', changes)
    changeListManager.waitUntilRefreshed()

    def list = findNotNullChangeList('my shelf')
    assert shelf.getShelvedChanges(list).collect { it.depotPath } as Set == ["//depot/a.txt", "//depot/b.txt"] as Set
  }

  @Test
  void "shelve from a named changelist with a different comment creates new changelist"() {
    def changes = prepareChangeListWithShelfAndModifiedFile()

    ShelveAction.shelveChanges(myProject, 'my shelf2', changes)
    changeListManager.waitUntilRefreshed()

    assert shelf.getShelvedChanges(findNotNullChangeList('my shelf')).collect { it.depotPath } as Set == ["//depot/b.txt"] as Set
    assert shelf.getShelvedChanges(findNotNullChangeList('my shelf2')).collect { it.depotPath } as Set == ["//depot/a.txt"] as Set
  }

  // creates 'my shelf' changelist with shelved 'b.txt' and locally modified 'a.txt'
  private Collection<Change> prepareChangeListWithShelfAndModifiedFile() {
    def file1 = createFileInCommand("a.txt", "")
    submitDefaultList('initial')

    createFileInCommand("b.txt", "")
    changeListManager.waitUntilRefreshed()

    ShelveAction.shelveChanges(myProject, 'my shelf', changeListManager.allChanges)
    changeListManager.waitUntilRefreshed()

    def list = findNotNullChangeList('my shelf')
    assert shelf.getShelvedChanges(list).collect { it.depotPath } as Set == ["//depot/b.txt"] as Set

    openForEdit(file1)
    editFileInCommand(file1, 'changed')
    moveToChangelist(PerforceNumberNameSynchronizer.getInstance(myProject).getAllNumbers('my shelf').values().first(), '//depot/a.txt')
    refreshChanges()

    list = findNotNullChangeList('my shelf')
    assertOneElement(list.changes)
    return list.changes
  }

  private LocalChangeList findNotNullChangeList(String name) {
    def result = changeListManager.changeLists.find { it.name == name }
    assert result
    result
  }

  private PerforceShelf getShelf() { PerforceManager.getInstance(myProject).shelf }

  @Test
  void "reverting all files in a changelist with shelf doesn't remove it from the view"() {
    createFileInCommand("a.txt", "")

    long num = createChangeList("xxx", ["//depot/a.txt"])
    verify(runP4WithClient("shelve", "-c", num as String, '//depot/a.txt'))
    refreshChanges()
    
    def list = findNotNullChangeList('xxx')
    assertOneElement(list.changes)
    assertOneElement(shelf.getShelvedChanges(list))

    new RollbackWorker(myProject).doRollback(list.changes, false, null, null)
    changeListManager.waitUntilRefreshed()
    
    assert findNotNullChangeList('xxx').changes.empty
  }
  
  @Test
  void "parse base revision in unshelve conflict"() {
    def file = createFileInCommand("a.txt", "")
    submitDefaultList('initial')
    
    openForEdit(file)
    editFileInCommand(file, 'foo')
    long num = createChangeList("xxx", ["//depot/a.txt"])
    verify(runP4WithClient("shelve", "-c", num as String, '//depot/a.txt'))
    
    editFileInCommand(file, 'bar')
    assert runP4WithClient("unshelve", "-s", num as String).stdout.contains('must resolve //depot/a.txt@=2')

    def revision = PerforceRunner.getInstance(myProject).getBaseRevision(P4File.create(file))
    assert revision
    assert revision.depotPath == '//depot/a.txt'
    assert revision.revisionNum == '#1'
    assert revision.sourceRevision == '@=2'
  }
}
