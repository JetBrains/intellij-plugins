package org.jetbrains.idea.perforce;

import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.VcsConfiguration;
import com.intellij.openapi.vcs.VcsShowConfirmationOption;
import com.intellij.openapi.vcs.changes.ChangeListManagerImpl;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.idea.perforce.application.PerforceChangeProvider;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UnversionedTest extends PerforceTestCase {

  private ChangeListManagerImpl myChangeListManager;
  private VcsDirtyScopeManager myDirtyScopeManager;

  @Override
  @Before
  public void before() throws Exception {
    super.before();
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY);
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.REMOVE, VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY);
    myChangeListManager = ChangeListManagerImpl.getInstanceImpl(myProject);
    myDirtyScopeManager = VcsDirtyScopeManager.getInstance(myProject);
  }

  // todo ?? special tests for initial scan?

  @Test
  public void testAddUnversionedAndRevertBack() {
    final VirtualFile a = createFileInCommand("a.txt", null);
    final VirtualFile b = createFileInCommand("b.txt", null);

    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate();
    assertEquals(FileStatus.UNKNOWN, myChangeListManager.getStatus(a));
    assertEquals(FileStatus.UNKNOWN, myChangeListManager.getStatus(b));

    final File ioA = new File(a.getPath());
    final File ioB = new File(b.getPath());
    assertTrue(ioA.setReadOnly());
    assertTrue(ioB.setReadOnly());
    a.refresh(false, false);
    b.refresh(false, false);

    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate();
    assertEquals(FileStatus.UNKNOWN, myChangeListManager.getStatus(a));
    assertEquals(FileStatus.UNKNOWN, myChangeListManager.getStatus(b));

    addFile("a.txt");

    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate();
    assertEquals(FileStatus.ADDED, myChangeListManager.getStatus(a));
    assertEquals(FileStatus.UNKNOWN, myChangeListManager.getStatus(b));

    ProcessOutput result = runP4WithClient("revert", ioA.getPath());
    verify(result);

    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate();
    assertEquals(FileStatus.UNKNOWN, myChangeListManager.getStatus(a));
    assertEquals(FileStatus.UNKNOWN, myChangeListManager.getStatus(b));
  }

  /*(do one test for add r/o, one for add r/w)
  2 unversioned (r/o+r/w), check, natively add, commit, lost focus, total refresh, check
  */
  @Test
  public void testDetectUnversionedToNotChanged() {
    final VirtualFile a = createFileInCommand("a.txt", null);
    final VirtualFile b = createFileInCommand("b.txt", null);

    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate();
    assertEquals(FileStatus.UNKNOWN, myChangeListManager.getStatus(a));
    assertEquals(FileStatus.UNKNOWN, myChangeListManager.getStatus(b));

    final File ioA = new File(a.getPath());
    final File ioB = new File(b.getPath());
    assertTrue(ioA.setReadOnly());
    assertTrue(ioB.setReadOnly());
    a.refresh(false, false);
    b.refresh(false, false);

    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate();
    assertEquals(FileStatus.UNKNOWN, myChangeListManager.getStatus(a));
    assertEquals(FileStatus.UNKNOWN, myChangeListManager.getStatus(b));

    verify(runP4WithClient("add", new File(myClientRoot, "a.txt").toString()));
    submitFile("//depot/a.txt");

    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate();
    ((PerforceChangeProvider) PerforceVcs.getInstance(myProject).getChangeProvider()).imitateLostFocus();
    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate();

    assertEquals(FileStatus.UNKNOWN, myChangeListManager.getStatus(a));
    assertEquals(FileStatus.UNKNOWN, myChangeListManager.getStatus(b));

    // and only after force
    ((PerforceChangeProvider) PerforceVcs.getInstance(myProject).getChangeProvider()).discardCache();
    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate();
    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate();
    assertEquals(FileStatus.NOT_CHANGED, myChangeListManager.getStatus(a));
    assertEquals(FileStatus.UNKNOWN, myChangeListManager.getStatus(b));
  }

}
