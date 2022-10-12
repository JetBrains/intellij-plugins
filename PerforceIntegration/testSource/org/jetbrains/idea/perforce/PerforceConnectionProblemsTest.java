package org.jetbrains.idea.perforce;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.vcs.VcsConfiguration;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.impl.DebugUtil;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.operations.VcsOperationLog;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.junit.Test;

import static com.intellij.testFramework.UsefulTestCase.assertOneElement;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class PerforceConnectionProblemsTest extends PerforceTestCase {
  @Override
  public void before() throws Exception {
    super.before();
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    enableSilentOperation(VcsConfiguration.StandardConfirmation.REMOVE);
    assertTrue(PerforceSettings.getSettings(myProject).ENABLED);
  }

  @Test
  public void testTroublesWhenGoingOnline() {
    goOffline();
    createFileInCommand("a.txt", null);
    stopPerforceServer();
    goOnline();
    assertFalse(PerforceSettings.getSettings(myProject).ENABLED);
    assertOneElement(VcsOperationLog.getInstance(myProject).getPendingOperations());
  }

  @Test
  public void testAddWhenDisconnected() {
    DebugUtil.sleep(3000); //todo ensure PerforceBaseInfoWorker.refreshInfo passes

    stopPerforceServer();
    createFileInCommand("a.txt", null);

    assertSingleOfflineChange();
  }

  @Test
  public void testRenameWhenDisconnected() {
    VirtualFile file = createFileInCommand("a.txt", "original");
    submitDefaultList("initial");
    refreshVfs();
    refreshChanges();

    stopPerforceServer();
    renameFileInCommand(file, "b.txt");

    assertSingleOfflineChange();
  }

  @Test
  public void testEditWhenDisconnected() throws VcsException {
    final VirtualFile file = createFileInCommand("a.txt", "original");
    submitDefaultList("initial");
    refreshVfs();
    refreshChanges();

    stopPerforceServer();
    WriteCommandAction.writeCommandAction(myProject)
                      .run(() -> PerforceVcs.getInstance(myProject).getEditFileProvider().editFiles(new VirtualFile[]{file}));
    assertSingleOfflineChange();
  }

  @Test
  public void testDeleteWhenDisconnected() {
    final VirtualFile file = createFileInCommand("a.txt", "original");
    submitDefaultList("initial");
    refreshVfs();
    refreshChanges();

    stopPerforceServer();
    deleteFileInCommand(file);

    assertSingleOfflineChange();
  }

  @Test
  public void testMoveToAnotherChangeListWhenDisconnected() {
    createFileInCommand("a.txt", "original");
    refreshChanges();

    stopPerforceServer();
    String clName = "another";
    getChangeListManager().moveChangesTo(getChangeListManager().addChangeList(clName, "zoo"), getSingleChange());

    refreshChanges();
    assertFalse(PerforceSettings.getSettings(myProject).ENABLED);
    assertOneElement(VcsOperationLog.getInstance(myProject).getPendingOperations());
    assertOneElement(getChangeListManager().findChangeList(clName).getChanges());
  }

  @Test
  public void testRenameWithWrongClient() {
    VirtualFile file = createFileInCommand("a.txt", "original");
    submitDefaultList("initial");
    refreshVfs();
    refreshChanges();

    PerforceSettings.getSettings(myProject).client = "someWrongClient";
    renameFileInCommand(file, "b.txt");

    assertSingleOfflineChange();
  }

  @Test
  public void testRenameWithWrongSecurityLevel() {
    VirtualFile file = createFileInCommand("a.txt", "original");
    submitDefaultList("initial");
    refreshVfs();
    refreshChanges();

    verify(runP4WithClient("configure", "set", "security=3"));
    renameFileInCommand(file, "b.txt");

    assertSingleOfflineChange();
  }

  private void assertSingleOfflineChange() {
    refreshChanges();
    getSingleChange();
    assertFalse(PerforceSettings.getSettings(myProject).ENABLED);
    assertOneElement(VcsOperationLog.getInstance(myProject).getPendingOperations());
  }
}
