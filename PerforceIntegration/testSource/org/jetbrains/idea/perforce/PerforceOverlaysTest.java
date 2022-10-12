package org.jetbrains.idea.perforce;

import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.vcs.VcsConfiguration;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.VcsShowConfirmationOption;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vfs.VirtualFile;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PerforceOverlaysTest extends PerforceTestCase {

  @Override
  @Before
  public void before() throws Exception {
    super.before();
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY);
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.REMOVE, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY);
  }

  @Test
  public void testDeleteCheckedIn() {
    VirtualFile file = setupOverlays();
    deleteFileInCommand(file);

    getChangeListManager().waitUntilRefreshed();
    rollbackChange(getSingleChange());
    getChangeListManager().waitUntilRefreshed();

    assertChangesViewEmpty();
  }

  @Test
  public void fileFromOverlayingMapping() throws VcsException {
    VirtualFile file = setupOverlays();

    openForEdit(file);
    refreshChanges();

    assertEquals("src2", getSingleChange().getBeforeRevision().getContent());
  }

  @Test
  public void fileFromOverlaidMapping() throws VcsException {
    VirtualFile file = createFileInCommand(createDirInCommand(myWorkingCopyDir, "src"), "a.txt", "src");
    submitDefaultList("initial");

    setupClient(buildTestClientSpec() + "\t+//depot/src2/...\t//test/src/...");
    openForEdit(file);
    refreshChanges();

    assertEquals("src", getSingleChange().getBeforeRevision().getContent());
  }

  @Test
  public void testDeleteEdited() {
    VirtualFile file = setupOverlays();
    openForEdit(file);
    deleteFileInCommand(file);

    getChangeListManager().waitUntilRefreshed();
    rollbackChange(getSingleChange());
    getChangeListManager().waitUntilRefreshed();

    assertChangesViewEmpty();
  }

  private VirtualFile setupOverlays() {
    VirtualFile file = createFileInCommand(createDirInCommand(myWorkingCopyDir, "src"), "a.txt", "src");
    createFileInCommand(createDirInCommand(myWorkingCopyDir, "src2"), "a.txt", "src2");
    submitDefaultList("initial");

    setupClient(buildTestClientSpec() + "\t+//depot/src2/...\t//test/src/...");
    
    verify(runP4WithClient("sync"));
    refreshVfs();
    refreshChanges();
    
    return file;
  }

  @Test
  public void testRevertWithOverlayMapping() {
    VirtualFile file = setupOverlays();
    openForEdit(file);
    editFileInCommand(file, "bbb");
    getChangeListManager().waitUntilRefreshed();
    
    rollbackChange(getSingleChange());
    getChangeListManager().waitUntilRefreshed();
    
    assertChangesViewEmpty();
    assertEquals("src2", LoadTextUtil.loadText(file).toString());
  }

  @Test
  public void testDiff() throws VcsException {
    VirtualFile file = setupOverlays();
    openForEdit(file);
    editFileInCommand(file, "new content");
    getChangeListManager().waitUntilRefreshed();

    assertEquals("src2", getSingleChange().getBeforeRevision().getContent());
  }

  @Test
  public void testRename() throws VcsException {
    VirtualFile file = createFileInCommand("a.txt", "src");
    refreshChanges();
    submitDefaultList("initial");

    verify(runP4(new String[]{"depot", "-i"}, "Depot: someDepot\nOwner: test\nType: local\nMap: some/...\n"));

    verify(runP4(new String[] { "client", "-i" }, buildTestClientSpec() + "\t+//someDepot/x/...\t//test/..."));
    verify(runP4WithClient("sync"));
    refreshVfs();
    refreshChanges();

    renameFileInCommand(file, "b.txt");
    discardUnversionedCache();
    refreshChanges();

    Change change = getSingleChange();
    assertEquals(Change.Type.MOVED, change.getType());
    assertEquals("a.txt", change.getBeforeRevision().getFile().getName());
    assertEquals("b.txt", change.getAfterRevision().getFile().getName());
    assertEquals("src", change.getBeforeRevision().getContent());
  }

}
