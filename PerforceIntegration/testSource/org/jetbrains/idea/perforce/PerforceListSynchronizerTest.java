package org.jetbrains.idea.perforce;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vcs.VcsConfiguration;
import com.intellij.openapi.vcs.VcsDirectoryMapping;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.VcsShowConfirmationOption;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.ChangeListManagerImpl;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.vcs.DuringChangeListManagerUpdateTestScheme;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.idea.perforce.application.ConnectionKey;
import org.jetbrains.idea.perforce.application.PerforceNumberNameSynchronizer;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManagerI;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.intellij.testFramework.UsefulTestCase.*;
import static org.junit.Assert.assertEquals;

public class PerforceListSynchronizerTest extends PerforceTestCase {
  private PerforceNumberNameSynchronizer mySynch;
  private PerforceConnectionManagerI myConnMan;

  @Override
  public void before() throws Exception {
    super.before();

    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY);
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.REMOVE, VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY);
    myConnMan = PerforceConnectionManager.getInstance(myProject);
    mySynch = PerforceNumberNameSynchronizer.getInstance(myProject);
  }

  @Test
  public void testAlienExists() throws IOException {
    final Ref<VirtualFile> refA = new Ref<>();
    final Ref<VirtualFile> refB = new Ref<>();

    WriteAction.runAndWait(() -> {
      refA.set(myWorkingCopyDir.createChildData(this, "a.txt"));
      refB.set(myWorkingCopyDir.createChildData(this, "b.txt"));
    });

    addFile("a.txt");
    addFile("b.txt");

    final ChangeListManager clManager = ChangeListManager.getInstance(myProject);
    refreshChanges();

    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(LocalChangeList.getDefaultName(), clManager, refA.get(), refB.get());

    final String descr = "123";
    final long listNumber = createChangeList(descr, Arrays.asList("//depot/a.txt", "//depot/b.txt"));

    refreshChanges();

    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(descr, clManager, refA.get(), refB.get());

    assertMappedNumber(descr, listNumber);
  }

  @Test
  public void testTwoNativeInOneAreDetected() throws IOException {
    final Ref<VirtualFile> refA = new Ref<>();
    final Ref<VirtualFile> refB = new Ref<>();

    WriteAction.runAndWait(() -> {
      refA.set(myWorkingCopyDir.createChildData(this, "a.txt"));
      refB.set(myWorkingCopyDir.createChildData(this, "b.txt"));
    });

    addFile("a.txt");
    addFile("b.txt");

    final ChangeListManager clManager = ChangeListManager.getInstance(myProject);
    refreshChanges();

    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(LocalChangeList.getDefaultName(), clManager, refA.get(), refB.get());

    final String descr = "123";
    final long listNumber = createChangeList(descr, Arrays.asList("//depot/a.txt"));
    final long listNumber2 = createChangeList(descr, Arrays.asList("//depot/b.txt"));

    refreshChanges();

    checkDuplicateDescriptionLists(descr, refA.get(), refB.get(), listNumber, listNumber2);
  }

  @Test
  public void testTwoNativeInOneOneEdited() throws Throwable {
    final Ref<VirtualFile> refA = new Ref<>();
    final Ref<VirtualFile> refB = new Ref<>();

    WriteAction.runAndWait(() -> {
      refA.set(myWorkingCopyDir.createChildData(this, "a.txt"));
      refB.set(myWorkingCopyDir.createChildData(this, "b.txt"));
    });

    addFile("a.txt");
    addFile("b.txt");

    final ChangeListManager clManager = ChangeListManager.getInstance(myProject);
    refreshChanges();

    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(LocalChangeList.getDefaultName(), clManager, refA.get(), refB.get());

    final String descr = "123";
    final long listNumber = createChangeList(descr, Arrays.asList("//depot/a.txt"));
    final long listNumber2 = createChangeList(descr, Arrays.asList("//depot/b.txt"));

    refreshChanges();

    checkDuplicateDescriptionLists(descr, refA.get(), refB.get(), listNumber, listNumber2);
    final String newDescr = "321";
    editListDescription(listNumber, newDescr);

    refreshChanges();

    assertMappedNumber(descr, listNumber2);
    assertMappedNumber(newDescr, listNumber);
  }

  @Test
  public void testTwoNativeRenameSame() throws Throwable {
    final Ref<VirtualFile> refA = new Ref<>();
    final Ref<VirtualFile> refB = new Ref<>();

    WriteAction.runAndWait(() -> {
      refA.set(myWorkingCopyDir.createChildData(this, "a.txt"));
      refB.set(myWorkingCopyDir.createChildData(this, "b.txt"));
    });

    addFile("a.txt");
    addFile("b.txt");

    final ChangeListManager clManager = ChangeListManager.getInstance(myProject);
    refreshChanges();

    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(LocalChangeList.getDefaultName(), clManager, refA.get(), refB.get());

    final String descr = "123";
    final long listNumber = createChangeList(descr, Arrays.asList("//depot/a.txt"));
    final long listNumber2 = createChangeList(descr, Arrays.asList("//depot/b.txt"));

    refreshChanges();

    checkDuplicateDescriptionLists(descr, refA.get(), refB.get(), listNumber, listNumber2);

    final String newDescr = "321";
    editListDescription(listNumber, newDescr);
    editListDescription(listNumber2, newDescr);

    refreshChanges();

    checkDuplicateDescriptionLists(newDescr, refA.get(), refB.get(), listNumber, listNumber2);
  }

  private void checkDuplicateDescriptionLists(String descr, VirtualFile file1, VirtualFile file2, long number1, long number2) {
    ChangeListManagerImpl clManager = getChangeListManager();
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(descr, clManager, file1);
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(descr + " (2)", clManager, file2);
    assertMappedNumber(descr, number1);
    assertMappedNumber(descr + " (2)", number2);
  }

  @Test public void testRenameChangelist() throws Exception {
    VirtualFile file = createFileInCommand("a.txt", "foo");
    addFile("a.txt");
    final long listNumber = createChangeList("descr1", Arrays.asList("//depot/a.txt"));
    refreshChanges();

    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList("descr1", getChangeListManager(), file);
    assertSize(2, getChangeListManager().getChangeLists());
    getChangeListManager().setDefaultChangeList(getChangeListManager().findChangeList("descr1"));

    editListDescription(listNumber, "descr2");
    refreshChanges();

    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList("descr2", getChangeListManager(), file);
    assertEquals("descr2", getChangeListManager().getDefaultChangeList().getName());
    assertSize(2, getChangeListManager().getChangeLists());
  }

  @Test
  public void testTwoNativeRenameDifferent() throws Throwable {
    final Ref<VirtualFile> refA = new Ref<>();
    final Ref<VirtualFile> refB = new Ref<>();

    WriteAction.runAndWait(() -> {
      refA.set(myWorkingCopyDir.createChildData(this, "a.txt"));
      refB.set(myWorkingCopyDir.createChildData(this, "b.txt"));
    });

    addFile("a.txt");
    addFile("b.txt");

    final ChangeListManager clManager = ChangeListManager.getInstance(myProject);
    refreshChanges();

    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(LocalChangeList.getDefaultName(), clManager, refA.get(), refB.get());

    final String descr = "123";
    final long listNumber = createChangeList(descr, Arrays.asList("//depot/a.txt"));
    final long listNumber2 = createChangeList(descr, Arrays.asList("//depot/b.txt"));

    refreshChanges();

    checkDuplicateDescriptionLists(descr, refA.get(), refB.get(), listNumber, listNumber2);

    final String newDescr = "321";
    final String newDescr2 = "321*";
    editListDescription(listNumber, newDescr);
    editListDescription(listNumber2, newDescr2);

    refreshChanges();

    assertMappedNumber(newDescr, listNumber);
    assertMappedNumber(newDescr2, listNumber2);
  }

  @Test
  public void testTwoMergedIntoOneNative() throws Throwable {
    final Ref<VirtualFile> refA = new Ref<>();
    final Ref<VirtualFile> refB = new Ref<>();

    WriteAction.runAndWait(() -> {
      refA.set(myWorkingCopyDir.createChildData(this, "a.txt"));
      refB.set(myWorkingCopyDir.createChildData(this, "b.txt"));
    });

    addFile("a.txt");
    addFile("b.txt");

    final ChangeListManager clManager = ChangeListManager.getInstance(myProject);
    refreshChanges();

    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(LocalChangeList.getDefaultName(), clManager, refA.get(), refB.get());

    final String descr = "123";
    final String descr2 = "123->";
    final long listNumber = createChangeList(descr, Arrays.asList("//depot/a.txt"));
    final long listNumber2 = createChangeList(descr2, Arrays.asList("//depot/b.txt"));

    refreshChanges();

    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(descr, clManager, refA.get());
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(descr2, clManager, refB.get());

    assertMappedNumber(descr, listNumber);
    assertMappedNumber(descr2, listNumber2);

    editListDescription(listNumber2, descr);

    refreshChanges();

    assertMappedNumber(descr, listNumber);
    assertMappedNumber(descr + " (2)", listNumber2);
  }

  @Test
  public void testNativeCommitRemovedList() throws IOException {
    final Ref<VirtualFile> refA = new Ref<>();
    final Ref<VirtualFile> refB = new Ref<>();

    WriteAction.runAndWait(() -> {
      refA.set(myWorkingCopyDir.createChildData(this, "a.txt"));
      refB.set(myWorkingCopyDir.createChildData(this, "b.txt"));
    });

    addFile("a.txt");
    addFile("b.txt");

    final ChangeListManager clManager = ChangeListManager.getInstance(myProject);
    refreshChanges();

    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(LocalChangeList.getDefaultName(), clManager, refA.get(), refB.get());

    final String descr = "123";
    final long listNumber = createChangeList(descr, Arrays.asList("//depot/a.txt", "//depot/b.txt"));

    refreshChanges();
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(descr, clManager, refA.get(), refB.get());
    assertMappedNumber(descr, listNumber);

    submitList(listNumber);

    refreshChanges();

    assertNull(clManager.findChangeList(descr));
    final P4Connection connection = myConnMan.getConnectionForFile(myWorkingCopyDir);
    final ConnectionKey key = connection.getConnectionKey();
    assertNull(mySynch.getNumber(key, descr));
    assertNull(mySynch.getName(key, listNumber));
  }

  @Test
  public void testMoveIntoPreExisting() throws IOException {
    final Ref<VirtualFile> refA = new Ref<>();
    final Ref<VirtualFile> refB = new Ref<>();

    WriteAction.runAndWait(() -> {
      refA.set(myWorkingCopyDir.createChildData(this, "a.txt"));
      refB.set(myWorkingCopyDir.createChildData(this, "b.txt"));
    });

    addFile("a.txt");
    addFile("b.txt");

    final ChangeListManager clManager = ChangeListManager.getInstance(myProject);
    refreshChanges();

    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(LocalChangeList.getDefaultName(), clManager, refA.get(), refB.get());

    final String descr = "123";
    final String descr2 = "123->";
    final long listNumber = createChangeList(descr, null);
    final long listNumber2 = createChangeList(descr2, null);

    refreshChanges();
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(LocalChangeList.getDefaultName(), clManager, refA.get(), refB.get());

    final LocalChangeList defaultList = clManager.getDefaultChangeList();
    final Collection<Change> changeCollection = defaultList.getChanges();
    final Change[] changeArr = changeCollection.toArray(new Change[0]);

    LocalChangeList l1 = clManager.addChangeList(descr, null);
    LocalChangeList l2 = clManager.addChangeList(descr2, null);

    clManager.moveChangesTo(l1, changeArr);
    refreshChanges();
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(descr, clManager, refA.get(), refB.get());
    assertMappedNumber(descr, listNumber);

    clManager.moveChangesTo(l2, changeArr);
    refreshChanges();
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(descr2, clManager, refA.get(), refB.get());
    assertMappedNumber(descr2, listNumber2);

    l1 = clManager.addChangeList(descr, null);
    clManager.moveChangesTo(l1, changeArr);
    refreshChanges();
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(descr, clManager, refA.get(), refB.get());
    assertMappedNumber(descr, listNumber);

    l2 = clManager.addChangeList(descr2, null);
    clManager.moveChangesTo(l2, changeArr);
    refreshChanges();
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(descr2, clManager, refA.get(), refB.get());
    assertMappedNumber(descr2, listNumber2);

    l1 = clManager.addChangeList(descr, null);
    clManager.moveChangesTo(l1, changeArr);
    refreshChanges();
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(descr, clManager, refA.get(), refB.get());
    assertMappedNumber(descr, listNumber);

    l2 = clManager.addChangeList(descr2, null);
    clManager.moveChangesTo(l2, changeArr);
    refreshChanges();
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(descr2, clManager, refA.get(), refB.get());
    assertMappedNumber(descr2, listNumber2);
  }

  @Test
  public void testTwoNativeInOneAreDetectedDifferentComments() throws IOException {
    final Ref<VirtualFile> refA = new Ref<>();
    final Ref<VirtualFile> refB = new Ref<>();

    WriteAction.runAndWait(() -> {
      refA.set(myWorkingCopyDir.createChildData(this, "a.txt"));
      refB.set(myWorkingCopyDir.createChildData(this, "b.txt"));
    });

    addFile("a.txt");
    addFile("b.txt");

    final ChangeListManager clManager = ChangeListManager.getInstance(myProject);
    refreshChanges();

    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(LocalChangeList.getDefaultName(), clManager, refA.get(), refB.get());

    final String descr1 = "123\n456\n789";
    final String descr2 = "123\nabc\ndef";
    final long listNumber = createChangeList(descr1, Arrays.asList("//depot/a.txt"));
    final long listNumber2 = createChangeList(descr2, Arrays.asList("//depot/b.txt"));

    refreshChanges();

    checkDuplicateDescriptionLists("123...", refA.get(), refB.get(), listNumber, listNumber2);
  }
  
  @Test
  public void testChangeSecondLineOfDescription() throws VcsException {
    VirtualFile file = createFileInCommand(myWorkingCopyDir, "a.txt", "");

    addFile("a.txt");

    String title = "title...";
    long listNumber = createChangeList("title\n1", Arrays.asList("//depot/a.txt"));

    refreshChanges();

    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(title, getChangeListManager(), file);
    assertMappedNumber(title, listNumber);

    editListDescription(listNumber, "title\n2");

    refreshChanges();

    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(title, getChangeListManager(), file);
    assertMappedNumber(title, listNumber);
  }

  @Test
  public void testChangeSecondLineOfDescriptionInAutoGeneratedChangeList() throws VcsException {
    VirtualFile file = createFileInCommand(myWorkingCopyDir, "a.txt", "");

    addFile("a.txt");

    createChangeList("title\n0", Collections.emptyList());

    String title = "title... (2)";
    long listNumber = createChangeList("title\n1", Arrays.asList("//depot/a.txt"));

    refreshChanges();

    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(title, getChangeListManager(), file);
    assertMappedNumber(title, listNumber);

    editListDescription(listNumber, "title\n2");

    refreshChanges();

    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(title, getChangeListManager(), file);
    assertMappedNumber(title, listNumber);
  }


  private void assertMappedNumber(String listName, Long number) {
    final P4Connection connection = myConnMan.getConnectionForFile(myWorkingCopyDir);
    assertEquals(number, mySynch.getNumber(connection.getConnectionKey(), listName));
  }

  @Test
  public void testShowEmptyChangelistsInIdea() {
    createChangeList("xxx", Collections.emptyList());
    refreshChanges();

    assertSize(2, getChangeListManager().getChangeLists());
    assertEmpty(getChangeListManager().findChangeList(getChangeListManager().getDefaultListName()).getChanges());
    assertEmpty(getChangeListManager().findChangeList("xxx").getChanges());
  }

  @Test
  public void testNoChangelistsForChangesOutsideRoots() {
    VirtualFile dir1 = createDirInCommand(myWorkingCopyDir, "dir1");
    VirtualFile file1 = createFileInCommand(dir1, "a.txt", "");
    VirtualFile file2 = createFileInCommand("b.txt", "");
    addFile("dir1/a.txt");
    addFile("b.txt");
    createChangeList("xxx", Arrays.asList("//depot/b.txt"));

    setVcsMappings(new VcsDirectoryMapping(dir1.getPath(), PerforceVcs.getInstance(myProject).getName()));
    refreshChanges();

    assertEmpty(getChangeListManager().getUnversionedFiles());
    assertEquals(file1, getSingleChange().getVirtualFile());
    assertEquals(getChangeListManager().getDefaultListName(), assertOneElement(getChangeListManager().getChangeLists()).getName());
  }

  @Test
  public void testHideEmptyChangelistsRemovedFromIdea() {
    VirtualFile file = createFileInCommand("b.txt", "");
    addFile("b.txt");
    long num = createChangeList("xxx", Arrays.asList("//depot/b.txt"));
    runP4WithClient("shelve", "-r", "-c", String.valueOf(num)); // prevent changelist from being deleted automatically

    refreshChanges();

    assertEquals(file, assertOneElement(getChangeListManager().getAllChanges()).getVirtualFile());
    assertSize(2, getChangeListManager().getChangeLists());
    
    getChangeListManager().removeChangeList("xxx");
    refreshChanges();
    assertSize(1, getChangeListManager().getChangeLists());
    assertEquals(file, getSingleChange().getVirtualFile());
    
    moveToChangelist(num, "//depot/b.txt");
    refreshChanges();
    assertSize(2, getChangeListManager().getChangeLists()); // xxx is non-empty, so show it again
    
    moveToDefaultChangelist("//depot/b.txt");
    refreshChanges();
    assertSize(2, getChangeListManager().getChangeLists()); // still show xxx, because it wasn't deleted explicitly
  }

  @Test
  public void testSubmitOneFileFromInactiveChangelist() {
    createFileInCommand("a.txt", "");
    createFileInCommand("b.txt", "");
    addFile("a.txt");
    addFile("b.txt");

    createChangeList("xxx", Arrays.asList("//depot/a.txt", "//depot/b.txt"));
    refreshChanges();

    Collection<Change> changes = getChangeListManager().findChangeList("xxx").getChanges();
    assertSize(2, changes);
    assertEmpty(PerforceVcs.getInstance(myProject).getCheckinEnvironment().commit(Collections.singletonList(changes.iterator().next()), "single file"));

    refreshVfs();
    getChangeListManager().waitUntilRefreshed();
    assertSameElements(getAllChangeListNames(), LocalChangeList.getDefaultName(), "xxx");
  }

  @Test
  public void testSubmitOneFileFromActiveChangelistWithDifferingComment() {
    createFileInCommand("a.txt", "");
    createFileInCommand("b.txt", "");
    addFile("a.txt");
    addFile("b.txt");

    createChangeList("xxx", Arrays.asList("//depot/a.txt", "//depot/b.txt"));
    refreshChanges();

    getChangeListManager().setDefaultChangeList(getChangeListManager().findChangeList("xxx"));
    getChangeListManager().editComment("xxx", "comment");
    refreshChanges();

    Collection<Change> changes = getChangeListManager().findChangeList("xxx").getChanges();
    assertSize(2, changes);
    assertEmpty(PerforceVcs.getInstance(myProject).getCheckinEnvironment().commit(Collections.singletonList(changes.iterator().next()), "single file"));

    refreshVfs();
    getChangeListManager().waitUntilRefreshed();
    assertSameElements(getAllChangeListNames(), LocalChangeList.getDefaultName(), "xxx");
    assertEquals("xxx", getChangeListManager().getDefaultListName());
  }

  @Test
  public void testReCreateDefaultChangeList() {
    getChangeListManager().addChangeList("foo", "bar");
    getChangeListManager().setDefaultChangeList("foo");
    getChangeListManager().removeChangeList(LocalChangeList.getDefaultName());

    refreshChanges();
    assertSameElements(getAllChangeListNames(), "foo");

    VirtualFile file = createFileInCommand("a.txt", "");
    addFile("a.txt");
    
    refreshChanges();

    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(LocalChangeList.getDefaultName(), getChangeListManager(), file);
    assertSameElements(getAllChangeListNames(), LocalChangeList.getDefaultName(), "foo");
  }

  private List<String> getAllChangeListNames() {
    return ContainerUtil.map(getChangeListManager().getChangeLists(), LocalChangeList::getName);
  }
}
