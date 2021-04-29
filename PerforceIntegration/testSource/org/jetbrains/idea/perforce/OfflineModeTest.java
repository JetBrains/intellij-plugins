package org.jetbrains.idea.perforce;

import com.intellij.diagnostic.ThreadDumper;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vfs.*;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.application.PerforceFileRevision;
import org.jetbrains.idea.perforce.operations.P4EditOperation;
import org.jetbrains.idea.perforce.operations.P4MoveRenameOperation;
import org.jetbrains.idea.perforce.operations.VcsOperation;
import org.jetbrains.idea.perforce.operations.VcsOperationLog;
import org.jetbrains.idea.perforce.perforce.PerforceCachingContentRevision;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.intellij.testFramework.UsefulTestCase.*;

@SuppressWarnings("SameParameterValue")
public class OfflineModeTest extends PerforceTestCase {
  private static final Logger LOG = Logger.getInstance(OfflineModeTest.class);
  @Override
  @Before
  public void before() throws Exception {
    super.before();
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY);
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.REMOVE, VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY);
  }

  @Test
  public void testOfflineAdd() {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    goOffline();
    createFileInCommand("a.txt", null);
    goOnline();
    verifyOpened("a.txt", "add");
  }

  @Test
  public void testOfflineDelete() {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.REMOVE);
    VirtualFile file = createAndSubmit("a.txt", "original content");

    goOffline();
    deleteFileInCommand(file);
    goOnline();
    verifyOpened("a.txt", "delete");
  }

  @Test
  public void testOfflineChanges() throws IOException {
    WriteAction.computeAndWait(() -> myWorkingCopyDir.createChildData(this, "a.txt"));

    addFile("a.txt");
    ChangeListManagerImpl changeListManager = ChangeListManagerImpl.getInstanceImpl(myProject);
    changeListManager.ensureUpToDate();
    LocalChangeList list = changeListManager.getDefaultChangeList();
    Assert.assertEquals(1, list.getChanges().size());
    // this is needed since last successful update tracker gets notified after local changes update
    // (on the same thread)
    changeListManager.ensureUpToDate();

    goOffline();

    VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    changeListManager.ensureUpToDate();
    list = changeListManager.getDefaultChangeList();
    Collection<Change> changes = list.getChanges();
    Assert.assertEquals(1, changes.size());
    Change c = changes.iterator().next();
    Assert.assertNull(c.getBeforeRevision());
    ContentRevision afterRevision = c.getAfterRevision();
    Assert.assertTrue(afterRevision instanceof CurrentContentRevision);
  }

  @Test
  public void testChangesForAddDoneOffline() {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    goOffline();
    createFileInCommand("a.txt", null);
    refreshChanges();
    Collection<Change> changes = ChangeListManagerImpl.getInstanceImpl(myProject).getDefaultChangeList().getChanges();
    Assert.assertEquals(1, changes.size());
  }

  @Test
  public void testChangesForEditDoneOffline() throws Exception {
    final VirtualFile fileToEdit = createAndSubmit("a.txt", "original content");
    refreshChanges();

    goOffline();
    refreshChanges();

    openForEdit(fileToEdit);
    setFileText(fileToEdit, "new content");
    refreshChanges();

    assertEquals(FileStatus.MODIFIED, FileStatusManager.getInstance(myProject).getStatus(fileToEdit));
    Change c = getSingleChange();
    Assert.assertTrue(c.getBeforeRevision() instanceof PerforceCachingContentRevision);
    Assert.assertTrue(c.getAfterRevision() instanceof CurrentContentRevision);
    Assert.assertEquals("original content", c.getBeforeRevision().getContent());
    Assert.assertEquals("new content", c.getAfterRevision().getContent());
  }

  @Test
  public void testChangesForDeleteDoneOffline() {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.REMOVE);
    final VirtualFile fileToEdit = createAndSubmit("a.txt", "original content");

    goOffline();
    deleteFileInCommand(fileToEdit);
    refreshChanges();

    Change c = getSingleChange();
    Assert.assertTrue(c.getBeforeRevision() instanceof PerforceCachingContentRevision);
    Assert.assertNull(c.getAfterRevision());
    // TODO[yole]: implement when LocalVCS allows to retrieve content for deleted files
    //Assert.assertEquals("original content", c.getBeforeRevision().getContent());
  }

  @Test
  public void testChangesForRenameDoneOffline() {
    final VirtualFile fileToEdit = doTestChangesForRenameDoneOffline();

    final List<VcsFileRevision> revisionList = getFileHistory(fileToEdit);
    assertEquals(2, revisionList.size());
    assertEquals("move/add", ((PerforceFileRevision)revisionList.get(0)).getAction());
  }

  private VirtualFile doTestChangesForRenameDoneOffline() {
    final VirtualFile fileToEdit = createAndSubmit("a.txt", "original content");

    goOffline();
    renameFileInCommand(fileToEdit, "b.txt");
    refreshChanges();

    Change c = getSingleChange();
    Assert.assertTrue(c.getBeforeRevision().getFile().getPath().endsWith("a.txt"));
    Assert.assertTrue(c.getAfterRevision().getFile().getPath().endsWith("b.txt"));

    goOnline();
    submitDefaultList("comment");
    refreshChanges();
    return fileToEdit;
  }

  @Test
  public void testPreserveModifiedWithoutCheckout() {
    VirtualFile file = createAndSubmit("a.txt", "something");
    refreshVfs();
    getChangeListManager().waitUntilRefreshed();
    assertEmpty(getChangeListManager().getModifiedWithoutEditing());

    editExternally(file, "something else");
    getChangeListManager().waitUntilRefreshed();

    assertOrderedEquals(getChangeListManager().getModifiedWithoutEditing(), file);

    goOffline();
    refreshChanges();
    assertOrderedEquals(getChangeListManager().getModifiedWithoutEditing(), file);

    goOnline();
    refreshChanges();
    assertOrderedEquals(getChangeListManager().getModifiedWithoutEditing(), file);
  }

  @Test
  public void testExternalChangeAsModifiedWithoutCheckout() throws Exception {
    VirtualFile file = createAndSubmit("a.txt", "something");
    goOffline();
    refreshChanges();
    assertEmpty(getChangeListManager().getModifiedWithoutEditing());

    editExternally(file, "something else");

    refreshChanges();
    assertOrderedEquals(getChangeListManager().getModifiedWithoutEditing(), file);

    rollbackModifiedWithoutCheckout(file);
    assertEquals("something", LoadTextUtil.loadText(file).toString());
    assertFalse(file.isWritable());
    getChangeListManager().waitUntilRefreshed();
    assertEmpty(getChangeListManager().getModifiedWithoutEditing());
  }

  @Test
  public void testTwoQuickChanges() {
    VirtualFile file = createAndSubmit("a.txt", "something");
    refreshChanges();

    goOffline();
    refreshChanges();
    assertEmpty(getChangeListManager().getModifiedWithoutEditing());

    openForEdit(file);
    editFileInCommand(file, "something1");
    editFileInCommand(file, "something2");

    refreshChanges();
    rollbackChange(getSingleChange());
    assertEquals("something", LoadTextUtil.loadText(file).toString());
  }

  @Test
  public void testRevertCheckedOut() {
    VirtualFile file = createAndSubmit("a.txt", "something");
    refreshChanges();

    goOffline();
    refreshChanges();
    assertEmpty(getChangeListManager().getModifiedWithoutEditing());

    openForEdit(file);
    refreshChanges();
    assertEmpty(getChangeListManager().getModifiedWithoutEditing());

    rollbackChange(getSingleChange());
    assertEquals("something", LoadTextUtil.loadText(file).toString());
  }

  @Test
  public void testOfflineRevertForEditDoneOutsideIdea() throws Exception {
    final File ioFile = new File(myClientRoot, "a.txt");
    assertTrue(ioFile.createNewFile());
    addFile("a.txt");
    FileUtil.writeToFile(ioFile, "original content");
    submitFile("//depot/a.txt");

    verify(runP4WithClient("edit", ioFile.getAbsolutePath()));
    FileUtil.writeToFile(ioFile, "new content");

    final VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(ioFile);
    refreshChanges();
    getSingleChange();

    goOffline();
    refreshChanges();

    rollbackChange(getSingleChange());
    assertEquals("original content", getFileText(file));
  }

  @Test
  public void testOfflineContentForRenameDoneOnline() throws Exception {
    final VirtualFile fileToEdit = createAndSubmit("a.txt", "original content");
    refreshChanges();

    renameFileInCommand(fileToEdit, "b.txt");
    refreshChanges();
    getSingleChange();

    goOffline();
    refreshChanges();

    final Change change = getSingleChange();
    assertNotNull(change.getBeforeRevision().getContent());
    assertNotNull(change.getAfterRevision().getContent());
  }

  @Test
  public void testChangesForRenameDoneOffline_Old() {
    forceDisableMoveCommand();
    final VirtualFile fileToEdit = doTestChangesForRenameDoneOffline();

    final List<VcsFileRevision> revisionList = getFileHistory(fileToEdit);
    assertEquals(2, revisionList.size());
    assertEquals("add", ((PerforceFileRevision)revisionList.get(0)).getAction());
  }

  @Test
  public void testSeveralRenameMoveOperationsOffline() {
    final VirtualFile fileToEdit = doTestSeveralRenameMoveOperationsOffline();

    final List<VcsFileRevision> revisionList = getFileHistory(fileToEdit);
    assertEquals(2, revisionList.size());
    assertEquals("move/add", ((PerforceFileRevision)revisionList.get(0)).getAction());
  }

  private VirtualFile doTestSeveralRenameMoveOperationsOffline() {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    final VirtualFile file1 = createFileInCommand("a.txt", "original content");
    final VirtualFile file2 = createFileInCommand("b.txt", "original content");
    submitDefaultList("initial");
    refreshChanges();

    goOffline();
    watchVfsEvents();

    final VirtualFile dir1 = createDirInCommand(myWorkingCopyDir, "dir1");
    moveFileInCommand(file1, dir1);
    renameFileInCommand(file1, "b.txt");
    moveFileInCommand(dir1, createDirInCommand(myWorkingCopyDir, "dir2"));

    assertNull(myWorkingCopyDir.findChild("dir1"));
    renameFileInCommand(file1, "c.txt");
    moveFileInCommand(file2, createDirInCommand(myWorkingCopyDir, "dir1"));
    refreshChanges();

    Collection<Change> changes = ChangeListManagerImpl.getInstanceImpl(myProject).getDefaultChangeList().getChanges();
    assertUnorderedCollection(changes, c -> {
      Assert.assertTrue(c.getBeforeRevision().getFile().getPath().endsWith("a.txt"));
      Assert.assertTrue(c.getAfterRevision().getFile().getPath().endsWith("dir2/dir1/c.txt"));
    }, c -> {
                                               Assert.assertTrue(c.getBeforeRevision().getFile().getPath().endsWith("b.txt"));
                                               Assert.assertTrue(c.getAfterRevision().getFile().getPath().endsWith("dir1/b.txt"));
                                             }
    );

    goOnline();
    submitDefaultList("comment");
    refreshChanges();
    return file1;
  }

  @Test
  public void testSeveralRenameMoveOperationsOffline_Old() {
    forceDisableMoveCommand();
    final VirtualFile fileToEdit = doTestSeveralRenameMoveOperationsOffline();

    final List<VcsFileRevision> revisionList = getFileHistory(fileToEdit);
    assertEquals(2, revisionList.size());
    assertEquals("add", ((PerforceFileRevision)revisionList.get(0)).getAction());
  }

  @Test
  public void testOfflineRevertForOnlineEdit() throws Exception {
    final VirtualFile fileToEdit = createAndSubmit("a.txt", "original content");
    refreshChanges();

    openForEdit(fileToEdit);
    setFileText(fileToEdit, "new content");
    refreshChanges();
    assertEquals(FileStatus.MODIFIED, FileStatusManager.getInstance(myProject).getStatus(fileToEdit));
    ensureContentCached(getSingleChange());

    goOffline();
    refreshChanges();
    rollbackChange(getSingleChange());

    Assert.assertEquals("original content", getFileText(fileToEdit));
    Assert.assertFalse(fileToEdit.isWritable());
    refreshChanges();
    Assert.assertEquals(0, ChangeListManagerImpl.getInstanceImpl(myProject).getDefaultChangeList().getChanges().size());

    goOnline();
    refreshChanges();
    Assert.assertEquals(0, getFilesInDefaultChangelist().size());
  }

  @Test
  public void testOfflineRevertForOfflineEdit() throws Exception {
    final VirtualFile fileToEdit = createAndSubmit("a.txt", "original content");
    refreshChanges();
    goOffline();
    openForEdit(fileToEdit);
    setFileText(fileToEdit, "new content");
    refreshChanges();

    Change c = getSingleChange();
    rollbackChange(c);
    Assert.assertEquals("original content", getFileText(fileToEdit));

    refreshChanges();
    Assert.assertEquals(0, ChangeListManagerImpl.getInstanceImpl(myProject).getDefaultChangeList().getChanges().size());
  }

  @Test
  public void testOfflineRevertForOnlineAdd() {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    createFileInCommand("a.txt", "content");
    refreshChanges();

    goOffline();
    rollbackChange(getSingleChange());
    refreshChanges();
    Assert.assertEquals(0, ChangeListManagerImpl.getInstanceImpl(myProject).getDefaultChangeList().getChanges().size());

    goOnline();
    Assert.assertEquals(0, getFilesInDefaultChangelist().size());
  }

  @Test
  public void testOfflineRevertForOfflineAdd() {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    goOffline();
    createFileInCommand("a.txt", "content");
    refreshChanges();
    rollbackChange(getSingleChange());
    refreshChanges();
    Assert.assertEquals(0, ChangeListManagerImpl.getInstanceImpl(myProject).getDefaultChangeList().getChanges().size());
    Assert.assertEquals(0, VcsOperationLog.getInstance(myProject).getPendingOperations().size());
  }

  @Test
  public void testRenameMoveRevert() {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);

    final VirtualFile foo = createDirInCommand(myWorkingCopyDir, "foo");
    final VirtualFile file = createFileInCommand(foo, "a.txt", "");
    refreshChanges();
    getSingleChange();

    submitDefaultList("initial");
    refreshVfs();
    refreshChanges();

    goOffline();
    refreshChanges();

    assertEmpty(getChangeListManager().getModifiedWithoutEditing());
    assertEmpty(getChangeListManager().getUnversionedFiles());
    assertEmpty(getChangeListManager().getDefaultChangeList().getChanges());

    renameFileInCommand(file, "b.txt");
    assertNotNull(LastUnchangedContentTracker.getLastUnchangedContent(file));
    getChangeListManager().waitUntilRefreshed();
    assertNotNull(LastUnchangedContentTracker.getLastUnchangedContent(file));

    watchVfsEvents();

    renameFileInCommand(foo, "bar");

    assertTrue(file.isValid());
    assertNotNull(LastUnchangedContentTracker.getLastUnchangedContent(file));

    getChangeListManager().waitUntilRefreshed();

    assertTrue(file.isValid());
    assertNotNull(LastUnchangedContentTracker.getLastUnchangedContent(file));

    assertInstanceOf(assertOneElement(VcsOperationLog.getInstance(myProject).getPendingOperations()), P4MoveRenameOperation.class);

    Change c = getSingleChange();
    assertEquals(Change.Type.MOVED, c.getType());
    rollbackChange(c);
    getChangeListManager().waitUntilRefreshed();

    assertEmpty(assertOneElement(getChangeListManager().getChangeListsCopy()).getChanges());

    assertNotNull(myWorkingCopyDir.findFileByRelativePath("foo/a.txt"));
    assertNull(myWorkingCopyDir.findFileByRelativePath("bar/a.txt"));
    assertNull(myWorkingCopyDir.findFileByRelativePath("bar/b.txt"));
    assertNull(myWorkingCopyDir.findFileByRelativePath("foo/b.txt"));
  }

  private void watchVfsEvents() {
    myProject.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
      @Override
      public void after(@NotNull List<? extends @NotNull VFileEvent> events) {
        LOG.debug("OfflineModeTest.after: " + events);
        LOG.debug(ThreadDumper.dumpThreadsToString());
      }
    });
  }

  @Test
  public void testOfflineRevertForOfflineRename() {
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.REMOVE, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY);

    final VirtualFile file = createAndSubmit("a.txt", "content");
    refreshChanges();
    goOffline();
    openForEdit(file);
    renameFileInCommand(file, "b.txt");

    refreshChanges();
    Change c = getSingleChange();
    rollbackChange(c);
    refreshChanges();

    Assert.assertEquals("a.txt", file.getName());
    Assert.assertEquals(0, ChangeListManagerImpl.getInstanceImpl(myProject).getDefaultChangeList().getChanges().size());
    Assert.assertEquals(0, VcsOperationLog.getInstance(myProject).getPendingOperations().size());

    goOnline();

    final VirtualFile anotherFile = createFileInCommand("c.txt", "");
    getChangeListManager().waitUntilRefreshed();
    deleteFileInCommand(anotherFile);
    getChangeListManager().waitUntilRefreshed();

    Assert.assertEquals(0, ChangeListManagerImpl.getInstanceImpl(myProject).getDefaultChangeList().getChanges().size());
  }

  @Test
  public void testOfflineDeleteAfterOfflineAdd() {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    enableSilentOperation(VcsConfiguration.StandardConfirmation.REMOVE);

    goOffline();
    final VirtualFile file = createFileInCommand("a.txt", "content");
    deleteFileInCommand(file);

    refreshChanges();
    Assert.assertEquals(0, ChangeListManagerImpl.getInstanceImpl(myProject).getDefaultChangeList().getChanges().size());
    Assert.assertEquals(0, VcsOperationLog.getInstance(myProject).getPendingOperations().size());
  }

  @Test
  public void testMergeAddAndRename() {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    goOffline();
    final VirtualFile file = createFileInCommand("a.txt", "content");
    renameFileInCommand(file, "b.txt");

    refreshChanges();
    Change c = getSingleChange();
    Assert.assertTrue(c.getAfterRevision().getFile().getPath().endsWith("b.txt"));
    Assert.assertEquals(1, VcsOperationLog.getInstance(myProject).getPendingOperations().size());
  }

  @Test
  public void testMergeTwoRenames() {
    final VirtualFile file = createAndSubmit("a.txt", "content");
    refreshChanges();
    goOffline();
    renameFileInCommand(file, "b.txt");
    renameFileInCommand(file, "c.txt");

    refreshChanges();
    Change c = getSingleChange();
    Assert.assertTrue(c.getAfterRevision().getFile().getPath().endsWith("c.txt"));
    Assert.assertEquals(1, VcsOperationLog.getInstance(myProject).getPendingOperations().size());
  }

  @Test
  public void testCyclicRename() {
    final VirtualFile file = createAndSubmit("a.txt", "content");
    refreshChanges();
    goOffline();
    renameFileInCommand(file, "b.txt");
    renameFileInCommand(file, "a.txt");

    refreshChanges();
    Change c = getSingleChange();
    final ContentRevision beforeRevision = c.getBeforeRevision();
    assertNotNull(c.toString(), beforeRevision);
    Assert.assertTrue(beforeRevision.getFile().getPath().endsWith("a.txt"));
    Assert.assertTrue(c.getAfterRevision().getFile().getPath().endsWith("a.txt"));
    final List<VcsOperation> pendingOps = VcsOperationLog.getInstance(myProject).getPendingOperations();
    Assert.assertEquals(1, pendingOps.size());
    Assert.assertTrue(pendingOps.get(0) instanceof P4EditOperation);
  }

  @Test
  public void testRevertCyclicRename() throws Exception {
    final VirtualFile file = createAndSubmit("a.txt", "content");
    refreshChanges();
    goOffline();
    openForEdit(file);
    renameFileInCommand(file, "b.txt");
    setFileText(file, "new content");
    renameFileInCommand(file, "a.txt");

    refreshChanges();
    Change c = getSingleChange();
    rollbackChange(c);
    Assert.assertEquals("content", getFileText(file));
  }

  @Test
  public void testAddInChangelist() {
    goOffline();
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    final LocalChangeList oldDefaultList = switchToChangeList("Second");
    createFileInCommand("a.txt", "new content");
    switchToChangeList(oldDefaultList);

    refreshChanges();
    final ChangeListManager clManager = ChangeListManagerImpl.getInstanceImpl(myProject);
    final List<LocalChangeList> lists = clManager.getChangeListsCopy();
    Assert.assertEquals(2, lists.size());
    final LocalChangeList list = clManager.findChangeList("Second");
    Assert.assertEquals(1, list.getChanges().size());

    goOnline();
    verifyOpenedInList("Second", "a.txt");
  }

  @Test
  public void testEditInChangelist() {
    VirtualFile f = createAndSubmit("a.txt", "old content");

    goOffline();
    final LocalChangeList oldDefaultList = switchToChangeList("Second");
    openForEdit(f);
    switchToChangeList(oldDefaultList);

    goOnline();
    verifyOpenedInList("Second", "a.txt");
  }

  @Test
  public void testDeleteInChangelist() {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.REMOVE);
    VirtualFile f = createAndSubmit("a.txt", "old content");

    goOffline();
    final LocalChangeList oldDefaultList = switchToChangeList("Second");
    deleteFileInCommand(f);
    switchToChangeList(oldDefaultList);

    goOnline();
    verifyOpenedInList("Second", "a.txt");
  }

  @Test
  public void testRenameInChangelist() {
    doTestRenameInChangelist();
  }
  private void doTestRenameInChangelist() {
    final VirtualFile fileToEdit = createAndSubmit("a.txt", "original content");

    goOffline();
    final LocalChangeList oldDefaultList = switchToChangeList("Second");
    renameFileInCommand(fileToEdit, "b.txt");
    switchToChangeList(oldDefaultList);

    goOnline();
    refreshChanges();
    verifyOpenedInList("Second", "a.txt");
    verifyOpenedInList("Second", "b.txt");
  }

  @Test
  public void testRenameInChangelist_Old() {
    forceDisableMoveCommand();
    doTestRenameInChangelist();
  }

  @Test
  public void testReopen() {
    final VirtualFile file = createAndSubmit("a.txt", "original content");
    openForEdit(file);
    refreshChanges();
    getSingleChange();
    refreshChanges();
    goOffline();
    refreshChanges();
    Change c = getSingleChange();

    ChangeListManager clManager = ChangeListManagerImpl.getInstanceImpl(myProject);
    final LocalChangeList list = clManager.addChangeList("Second", "");
    clManager.moveChangesTo(list, c);

    refreshChanges();
    final List<LocalChangeList> lists = clManager.getChangeListsCopy();
    Assert.assertEquals(2, lists.size());
    Assert.assertEquals(1, clManager.findChangeList("Second").getChanges().size());
    final List<VcsOperation> pendingOps = VcsOperationLog.getInstance(myProject).getPendingOperations();
    Assert.assertEquals(1, pendingOps.size());

    goOnline();
    verifyOpenedInList("Second", "a.txt");
  }

  @Test
  public void testMergeReopen() {
    final VirtualFile file = createAndSubmit("a.txt", "original content");
    refreshChanges();
    goOffline();
    openForEdit(file);

    refreshChanges();
    Change c = getSingleChange();
    ChangeListManager clManager = ChangeListManagerImpl.getInstanceImpl(myProject);
    final LocalChangeList list = clManager.addChangeList("Second", "");
    clManager.moveChangesTo(list, c);

    final List<LocalChangeList> lists = clManager.getChangeListsCopy();
    Assert.assertEquals(2, lists.size());
    Assert.assertEquals(1, clManager.findChangeList("Second").getChanges().size());
    final List<VcsOperation> pendingOps = VcsOperationLog.getInstance(myProject).getPendingOperations();
    Assert.assertEquals(1, pendingOps.size());
  }

  @Test
  public void testMergeReopenRename() {
    final VirtualFile file = createAndSubmit("a.txt", "original content");
    refreshChanges();
    goOffline();
    openForEdit(file);
    renameFileInCommand(file, "b.txt");

    refreshChanges();
    Change c = getSingleChange();
    ChangeListManager clManager = ChangeListManagerImpl.getInstanceImpl(myProject);
    final LocalChangeList list = clManager.addChangeList("Second", "");
    clManager.moveChangesTo(list, c);

    final List<LocalChangeList> lists = clManager.getChangeListsCopy();
    Assert.assertEquals(2, lists.size());
    Assert.assertEquals(1, clManager.findChangeList("Second").getChanges().size());
    final List<VcsOperation> pendingOps = VcsOperationLog.getInstance(myProject).getPendingOperations();
    Assert.assertEquals(1, pendingOps.size());
  }

  @Test
  public void testOfflineCopy() {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    final VirtualFile file = createAndSubmit("a.txt", "content");
    goOffline();

    copyFileInCommand(file, "b.txt");
    goOnline();
    verifyOpened("b.txt", "add");
    ProcessOutput result = runP4WithClient("resolved");
    String stdout = result.getStdout().trim();
    Assert.assertTrue(stdout.endsWith("b.txt - branch from //depot/a.txt#1"));
  }

  @Test
  public void testUnversionedVsHijacked() {
    final VirtualFile fileA = createFileInCommand("a.txt", "content");
    addFile("a.txt");
    submitDefaultList("initial");
    refreshVfs();

    VirtualFile fileB = createFileInCommand("b.txt", "");

    getChangeListManager().waitUntilRefreshed();

    assertSameElements(getChangeListManager().getUnversionedFiles(), fileB);
    assertEmpty(getChangeListManager().getModifiedWithoutEditing());

    goOffline();
    refreshChanges();

    assertSameElements(getChangeListManager().getUnversionedFiles(), fileB);
    assertEmpty(getChangeListManager().getModifiedWithoutEditing());

    editExternally(fileA, "hijacked");
    VirtualFile fileC = createFileInCommand("c.txt", "");
    getChangeListManager().waitUntilRefreshed();

    assertSameElements(getChangeListManager().getUnversionedFiles(), fileB, fileC);
    assertSameElements(getChangeListManager().getModifiedWithoutEditing(), fileA);
  }

  @Test
  public void testIgnoredInOfflineMode() {
    createFileInCommand("a.txt", "content");
    addFile("a.txt");
    submitDefaultList("initial");
    refreshVfs();

    goOffline();
    refreshChanges();
    VirtualFile fileB = createFileInCommand("b.txt", "");
    VcsDirtyScopeManager.getInstance(myProject).fileDirty(VcsUtil.getFilePath(new File(myClientRoot, "b.txt")));
    refreshVfs();
    refreshChanges();
    getChangeListManager().waitUntilRefreshed();
    assertSameElements(getChangeListManager().getUnversionedFiles(), fileB);
    assertSameElements(getChangeListManager().getIgnoredFiles(), VfsUtil.findFileByIoFile(myP4IgnoreFile, true));
  }

  private VirtualFile createAndSubmit(final String fileName, final String content) {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    final VirtualFile fileToEdit = createFileInCommand(fileName, content);
    submitFile("//depot/" + fileName);
    fileToEdit.refresh(false, false);
    refreshChanges();
    return fileToEdit;
  }

  private static String getFileText(final VirtualFile fileToEdit) throws IOException {
    return CharsetToolkit.bytesToString(fileToEdit.contentsToByteArray(), StandardCharsets.UTF_8);
  }

  private static void ensureContentCached(final Change c) throws VcsException {
    final ContentRevision beforeRevision = c.getBeforeRevision();
    assert beforeRevision != null;
    beforeRevision.getContent();
  }

  private LocalChangeList switchToChangeList(final String name) {
    final ChangeListManager clManager = ChangeListManagerImpl.getInstanceImpl(myProject);
    final LocalChangeList list = clManager.addChangeList(name, "");
    final LocalChangeList oldDefaultList = clManager.getDefaultChangeList();
    clManager.setDefaultChangeList(list);
    return oldDefaultList;
  }

  private void switchToChangeList(final LocalChangeList oldDefaultList) {
    ChangeListManagerImpl.getInstanceImpl(myProject).setDefaultChangeList(oldDefaultList);
  }

  private void verifyOpenedInList(final String changeListName, final String path) {
    ProcessOutput execResult = runP4WithClient("changes", "-i", "-t", "-s", "pending");
    final String stdout = execResult.getStdout().trim();

    Pattern pattern = Pattern.compile("Change (\\d+).+'(.+) '");
    Matcher m = pattern.matcher(stdout);
    Assert.assertTrue("Unexpected pending changes: " + stdout, m.matches());
    Assert.assertEquals(changeListName, m.group(2));

    execResult = runP4WithClient("describe", "-s", m.group(1));
    Assert.assertTrue(execResult.getStdout().contains("... //depot/" + path));
  }

  @Test
  public void testAddAfterDelete() {
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY);
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.REMOVE, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY);
    VirtualFile file = createFileInCommand("a.txt", "");
    submitDefaultList("initial");
    refreshVfs();
    refreshChanges();

    goOffline();
    refreshChanges();

    deleteFileInCommand(file);
    assertFalse(file.isValid());
    assertFalse(VfsUtilCore.virtualToIoFile(file).exists());

    getChangeListManager().waitUntilRefreshed();
    Assert.assertEquals(Change.Type.DELETED, getSingleChange().getType());

    createFileInCommand("a.txt", "new content");
    getChangeListManager().waitUntilRefreshed();
    Assert.assertEquals(Change.Type.MODIFICATION, getSingleChange().getType());

    goOnline();
    refreshChanges();
    Assert.assertEquals(Change.Type.MODIFICATION, getSingleChange().getType());
  }

}
