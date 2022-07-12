package org.jetbrains.idea.perforce;

import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManagerImpl;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.vcs.DuringChangeListManagerUpdateTestScheme;
import com.intellij.util.concurrency.Semaphore;
import org.jetbrains.idea.perforce.actions.ActionEdit;
import org.jetbrains.idea.perforce.actions.RevertAllUnchangedFilesAction;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.operations.P4AddOperation;
import org.jetbrains.idea.perforce.operations.P4DeleteOperation;
import org.jetbrains.idea.perforce.operations.P4EditOperation;
import org.jetbrains.idea.perforce.perforce.PerforceChangeListHelper;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.jobs.ConnectionSelector;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.intellij.testFramework.UsefulTestCase.*;
import static junit.framework.Assert.assertTrue;

public class PerforceOperationsTest extends PerforceTestCase {
  @Override
  @Before
  public void before() throws Exception {
    super.before();
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY);
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.REMOVE, VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY);
  }


  @Test
  public void testAddOperation() throws Exception {
    VirtualFile file = createFileInCommand("a.txt", "");
    new P4AddOperation("Default", file).execute(myProject);
    verifyOpened("a.txt", "add");
  }

  @Test
  public void testEditOperation() throws Exception {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    VirtualFile fileToEdit = createFileInCommand("a.txt", null);
    submitFile("//depot/a.txt");
    new P4EditOperation("Default", fileToEdit).execute(myProject);
    verifyOpened("a.txt", "edit");
  }

  @Test
  public void testDeleteOperation() throws Exception {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    enableSilentOperation(VcsConfiguration.StandardConfirmation.REMOVE);
    VirtualFile fileToEdit = createFileInCommand("a.txt", null);
    FilePath filePath = VcsContextFactory.getInstance().createFilePathOn(fileToEdit);
    submitFile("//depot/a.txt");
    new P4DeleteOperation("Default", filePath).execute(myProject);
    verifyOpened("a.txt", "delete");
  }

  @Test
  public void testRenameAddedFile() {
    doTestRenameAddedFile();
  }
  private void doTestRenameAddedFile() {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    final VirtualFile fileToAdd = createFileInCommand("a.txt", null);
    verifyOpened("a.txt", "add");

    renameFileInCommand(fileToAdd, "b.txt");

    final List<String> files = getFilesInDefaultChangelist();
    Assert.assertEquals(1, files.size());
    String file = files.get(0);
    Assert.assertEquals("//depot/b.txt\t# add", file);

    ChangeListManagerImpl changeListManager = ChangeListManagerImpl.getInstanceImpl(myProject);
    changeListManager.ensureUpToDate();
    final List<Change> changes = new ArrayList<>(changeListManager.getDefaultChangeList().getChanges());
    Assert.assertEquals(1, changes.size());
    verifyChange(changes.get(0), null, "b.txt");
  }

  @Test
  public void testRenameAddedFile_Old() {
    forceDisableMoveCommand();
    doTestRenameAddedFile();
  }

  @Test
  public void testRenamePackage() throws Exception {
    doTestRenamePackage();
  }
  @Test
  public void testRenamePackage_Old() throws Exception {
    forceDisableMoveCommand();
    doTestRenamePackage();
  }

  private void doTestRenamePackage() throws VcsException {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    VirtualFile dir = createDirInCommand(myWorkingCopyDir, "child");
    final VirtualFile child = createFileInCommand(dir, "a.txt", "test");
    submitFile("//depot/child/a.txt");

    new P4EditOperation("Default", child).execute(myProject);
    renameFileInCommand(dir, "newchild");

    Assert.assertFalse(new File(myWorkingCopyDir.getPath(), "child").exists());   // IDEADEV-18837
    final File newChildDir = new File(myWorkingCopyDir.getPath(), "newchild");
    Assert.assertTrue(newChildDir.exists());
    Assert.assertTrue(new File(newChildDir, "a.txt").canWrite());                 // IDEADEV-18783
    ChangeListManagerImpl changeListManager = ChangeListManagerImpl.getInstanceImpl(myProject);
    changeListManager.ensureUpToDate();
    final List<Change> changes = new ArrayList<>(changeListManager.getDefaultChangeList().getChanges());
    Assert.assertEquals(1, changes.size());
    verifyChange(changes.get(0), "child\\a.txt", "newchild\\a.txt");
  }

  @Test
  public void testDeleteAddedFile() {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    enableSilentOperation(VcsConfiguration.StandardConfirmation.REMOVE);
    VirtualFile fileToEdit = createFileInCommand("a.txt", null);
    Assert.assertEquals(1, getFilesInDefaultChangelist().size());
    deleteFileInCommand(fileToEdit);
    final List<String> files = getFilesInDefaultChangelist();
    Assert.assertEquals(0, files.size());
  }

  @Test
  public void testDeleteDirWithAddedFiles() {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    enableSilentOperation(VcsConfiguration.StandardConfirmation.REMOVE);
    VirtualFile dir = createDirInCommand(myWorkingCopyDir, "test");
    createFileInCommand(dir, "a.txt", null);
    Assert.assertEquals(1, getFilesInDefaultChangelist().size());
    deleteFileInCommand(dir);
    Assert.assertEquals(0, getFilesInDefaultChangelist().size());
  }

  @Test
  public void testAnnotate() throws Exception {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    final VirtualFile file = createFileInCommand("a.txt", "foo: foo\nbar: bar");
    submitDefaultList("initial");
    final FileAnnotation annotation = createTestAnnotation(file);
    assertEquals("1", annotation.getLineRevisionNumber(0).toString());
    assertEquals("1", annotation.getLineRevisionNumber(1).toString());
  }

  @Test
  public void testRevertDeletesEmptyChangelist() throws Exception {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    final VirtualFile file = createFileInCommand("a.txt", "foo: foo\nbar: bar");
    addFile("a.txt");
    createChangeList("xxx", List.of("//depot/a.txt"));
    refreshChanges();

    assertEquals("xxx", assertOneElement(PerforceRunner.getInstance(myProject).getPendingChangeLists(getConnection())).getName());

    assertSize(2, getChangeListManager().getChangeLists());
    assertEmpty(getChangeListManager().findChangeList(getChangeListManager().getDefaultListName()).getChanges());

    Change change = assertOneElement(getChangeListManager().findChangeList("xxx").getChanges());
    assertEquals(file, change.getVirtualFile());

    rollbackChange(change);
    getChangeListManager().waitUntilRefreshed();
    assertEmpty(assertOneElement(getChangeListManager().getChangeLists()).getChanges());

    assertEmpty(PerforceRunner.getInstance(myProject).getPendingChangeLists(getConnection()));
  }

  @Test
  public void testMovingFilesBetweenChangelistsInMultiRootProject() throws VcsException {
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY);
    VirtualFile dir1 = createDirInCommand(myWorkingCopyDir, "dir1");
    VirtualFile dir2 = createDirInCommand(myWorkingCopyDir, "dir2");
    VirtualFile file1 = createFileInCommand(dir1, "a.txt", "");
    VirtualFile file2 = createFileInCommand(dir2, "b.txt", "");
    submitDefaultList("initial");

    setP4ConfigRoots(dir1, dir2);

    LocalChangeList listA = getChangeListManager().addChangeList("A", null);
    LocalChangeList listB = getChangeListManager().addChangeList("B", null);

    getChangeListManager().setDefaultChangeList(listA);
    openForEdit(file1);
    getChangeListManager().waitUntilRefreshed();
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList("A", getChangeListManager(), file1);

    getChangeListManager().setDefaultChangeList(listB);
    openForEdit(file2);
    getChangeListManager().waitUntilRefreshed();
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList("A", getChangeListManager(), file1);
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList("B", getChangeListManager(), file2);

    assertSize(1, ConnectionSelector.getConnections(myProject, getChangeListManager().findChangeList("A")).keySet());
    assertSize(1, ConnectionSelector.getConnections(myProject, getChangeListManager().findChangeList("B")).keySet());

    getChangeListManager().moveChangesTo(listA, getChangeListManager().findChangeList("B").getChanges().toArray(Change.EMPTY_CHANGE_ARRAY));
    getChangeListManager().waitUntilRefreshed();
    refreshChanges();
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList("A", getChangeListManager(), file1, file2);
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList("B", getChangeListManager());

    assertSize(2, PerforceRunner.getInstance(myProject).getPendingChangeLists(getConnection()));
    assertSize(1, ConnectionSelector.getConnections(myProject, getChangeListManager().findChangeList("A")).keySet());

    getChangeListManager().moveChangesTo(listB, getChangeListManager().findChangeList("A").getChanges().toArray(Change.EMPTY_CHANGE_ARRAY));
    getChangeListManager().waitUntilRefreshed();
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList("A", getChangeListManager());
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList("B", getChangeListManager(), file1, file2);

    assertSize(2, PerforceRunner.getInstance(myProject).getPendingChangeLists(getConnection()));
    assertSize(1, ConnectionSelector.getConnections(myProject, getChangeListManager().findChangeList("B")).keySet());
  }

  @Test
  public void testConnectionSelectorForDefaultChangelist() {
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY);
    createFileInCommand("a.txt", "");
    getChangeListManager().waitUntilRefreshed();
    assertSize(1, ConnectionSelector.getConnections(myProject, getChangeListManager().getDefaultChangeList()).keySet());
  }

  @Test public void testCommitRestrictedChangeList() throws VcsException {
    createFileInCommand("a.txt", "");
    addFile("a.txt");
    String spec = PerforceChangeListHelper.createSpecification("aaa", -1, List.of("//depot/a.txt"), null, null, false, true);
    verify(runP4(new String[]{"-c", "test", "change", "-i"}, spec));

    refreshChanges();
    Change change = assertOneElement(getChangeListManager().getAllChanges());
    assertEmpty(PerforceVcs.getInstance(myProject).getCheckinEnvironment().commit(Arrays.asList(change), "aaa"));
    refreshChanges();
    assertEmpty(getChangeListManager().getAllChanges());
  }

  @Test public void testSpecialCharactersInFileName() throws VcsException {
    String name = "a$#@#.txt";
    VirtualFile file = createFileInCommand(name, "");
    new P4AddOperation(getChangeListManager().getDefaultListName(), file).execute(myProject);

    refreshChanges();
    Change change = getSingleChange();
    assertEmpty(PerforceVcs.getInstance(myProject).getCheckinEnvironment().commit(List.of(change), "aaa"));
    refreshChanges();
    assertEmpty(getChangeListManager().getAllChanges());

    moveFileInCommand(file, createDirInCommand(myWorkingCopyDir, "dir"));
    refreshChanges();

    rollbackChange(getSingleChange());
    refreshChanges();
    assertEmpty(getChangeListManager().getAllChanges());

    new P4AddOperation(getChangeListManager().getDefaultListName(), createFileInCommand("a$.txt", "")).execute(myProject);
    new P4AddOperation(getChangeListManager().getDefaultListName(), createFileInCommand("a#.txt", "")).execute(myProject);
    new P4AddOperation(getChangeListManager().getDefaultListName(), createFileInCommand("a%.txt", "")).execute(myProject);
    getChangeListManager().waitUntilRefreshed();
    assertSize(3, getChangeListManager().getAllChanges());

  }

  @Test
  public void testAddAfterDelete() {
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY);
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.REMOVE, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY);
    VirtualFile file = createFileInCommand("a.txt", "");
    submitDefaultList("initial");
    refreshVfs();
    refreshChanges();

    deleteFileInCommand(file);
    assertFalse(file.isValid());
    assertFalse(VfsUtilCore.virtualToIoFile(file).exists());

    getChangeListManager().waitUntilRefreshed();
    assertEquals(Change.Type.DELETED, getSingleChange().getType());

    createFileInCommand("a.txt", "new content");
    getChangeListManager().waitUntilRefreshed();

    assertEquals(Change.Type.MODIFICATION, getSingleChange().getType());

  }

  @Test
  public void testDeleteFromNonDefaultChangeList() {
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY);
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.REMOVE, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY);
    VirtualFile file = createFileInCommand("a.txt", "");
    submitDefaultList("initial");
    refreshVfs();
    refreshChanges();

    openForEdit(file);
    getChangeListManager().waitUntilRefreshed();

    String another = "another";
    getChangeListManager().moveChangesTo(getChangeListManager().addChangeList(another, null), getSingleChange());
    getChangeListManager().waitUntilRefreshed();

    deleteFileInCommand(file);
    getChangeListManager().waitUntilRefreshed();
    assertEmpty(getChangeListManager().getDefaultChangeList().getChanges());
    assertOneElement(getChangeListManager().findChangeList(another).getChanges());
    assertEmpty(getChangeListManager().getDeletedFiles());
  }

  @Test
  public void testEditHonorsIgnoredAndUnversionedFiles() {
    VirtualFile toAdd = createFileInCommand("a.txt", "");
    VirtualFile unversioned = createFileInCommand("c.txt", "");
    addFile("a.txt");
    submitDefaultList("initial");

    refreshChanges();
    assertSameElements(getChangeListManager().getUnversionedFiles(), unversioned);

    EdtTestUtil.runInEdtAndWait((() -> new ActionEdit().processFiles(myProject, myWorkingCopyDir)));
    waitForAsyncRefresh();

    getChangeListManager().waitUntilRefreshed();
    assertEquals(toAdd, getSingleChange().getVirtualFile());
    assertEquals(FileStatus.UNKNOWN, FileStatusManager.getInstance(myProject).getStatus(unversioned));
    assertSameElements(getChangeListManager().getUnversionedFiles(), unversioned);

    ProcessOutput result = runP4WithClient("opened");
    verify(result);
    assertTrue(result.getStdout().contains(toAdd.getName()));
    assertFalse(result.getStdout().contains(unversioned.getName()));
  }

  private static void waitForAsyncRefresh() {
    final Semaphore semaphore = new Semaphore();
    semaphore.down();
    VirtualFileManager.getInstance().asyncRefresh(semaphore::up);
    assertTrue(semaphore.waitFor(100000));
  }

  @Test
  public void testRevertUnchangedFile() {
    final VirtualFile file = createFileInCommand("a.txt", "aaa");
    addFile("a.txt");
    submitDefaultList("foo");

    openForEdit(file);
    assertTrue(VfsUtilCore.virtualToIoFile(file).canWrite());
    EdtTestUtil.runInEdtAndWait(() -> RevertAllUnchangedFilesAction.revertUnchanged(myProject, Collections.singletonList(myWorkingCopyDir), null, null));
    waitForAsyncRefresh();
    assertFalse(VfsUtilCore.virtualToIoFile(file).canWrite());
    getChangeListManager().waitUntilRefreshed();

    assertChangesViewEmpty();
  }

  @Test
  public void testOpenForEditAutomaticallyInAllWriteWorkspace() {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    setupClient(buildTestClientSpec() + "Options:\tallwrite");

    VirtualFile file = createFileInCommand(myWorkingCopyDir, "a.txt", "");
    submitFile("//depot/" + file.getName());

    refreshVfs();
    refreshChanges();

    assertTrue(file.isWritable());

    assertChangesViewEmpty();

    WriteCommandAction.runWriteCommandAction(myProject, () -> {
      FileDocumentManager.getInstance().getDocument(file).setText("new content");
      FileDocumentManager.getInstance().saveAllDocuments();
    });
    getChangeListManager().waitUntilRefreshed();

    assertEquals(file, getSingleChange().getVirtualFile());
    
    rollbackChange(getSingleChange());
    getChangeListManager().waitUntilRefreshed();
    assertChangesViewEmpty();
    
    editFileInCommand(file, "another");
    getChangeListManager().waitUntilRefreshed();
    assertEquals(file, getSingleChange().getVirtualFile());
  }

}