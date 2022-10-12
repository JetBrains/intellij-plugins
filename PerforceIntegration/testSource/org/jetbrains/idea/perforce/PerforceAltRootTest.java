package org.jetbrains.idea.perforce;

import com.intellij.openapi.util.io.IoTestUtil;
import com.intellij.openapi.vcs.VcsConfiguration;
import com.intellij.openapi.vcs.VcsDirectoryMapping;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.VcsShowConfirmationOption;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.perforce.P4WhereResult;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;
import org.junit.Test;

import java.util.Arrays;

import static com.intellij.testFramework.UsefulTestCase.assertEmpty;
import static junit.framework.Assert.assertEquals;

public class PerforceAltRootTest extends PerforceTestCase {
  @Override
  public void before() throws Exception {
    super.before();
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY);
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.REMOVE, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY);
  }

  private static String clientSpecWithAltRoot(String mainRoot, String altRoot) {
    String sep = System.lineSeparator();
    return "Client:\ttest" + sep +
           "Root:\t" + mainRoot + sep +
           "AltRoots:\t" + altRoot + sep +
           "View:" + sep + "\t//depot/... //test/..." + sep;
  }

  @Test
  public void testCommitRenameRollback() {
    setupClient(clientSpecWithAltRoot("/foo/bar/goo", myClientRoot.toString()));

    VirtualFile file = createAndCommit();

    renameFileInCommand(file, "bar.txt");
    getChangeListManager().waitUntilRefreshed();
    assertEquals(Change.Type.MOVED, getSingleChange().getType());

    rollbackChange(getSingleChange());
    getChangeListManager().waitUntilRefreshed();
    assertEmpty(getChangeListManager().getAllChanges());
  }

  private VirtualFile createAndCommit() {
    VirtualFile file = createFileInCommand("foo.txt", "");
    getChangeListManager().waitUntilRefreshed();

    assertEmpty(PerforceVcs.getInstance(myProject).getCheckinEnvironment().commit(Arrays.asList(getSingleChange()), "aaa"));
    refreshChanges();
    assertEmpty(getChangeListManager().getAllChanges());
    return file;
  }

  @Test
  public void testEditRollbackDelete() {
    setupClient(clientSpecWithAltRoot("/foo/bar/goo", myClientRoot.toString()));

    VirtualFile file = createAndCommit();

    openForEdit(file);
    getChangeListManager().waitUntilRefreshed();
    getSingleChange();

    rollbackChange(getSingleChange());
    getChangeListManager().waitUntilRefreshed();
    assertEmpty(getChangeListManager().getAllChanges());

    deleteFileInCommand(file);
    getChangeListManager().waitUntilRefreshed();
    getSingleChange();
  }

  @Test
  public void rootAsSymlinkToAltRoot() throws VcsException {
    IoTestUtil.assumeSymLinkCreationIsSupported();

    VirtualFile realRoot = createDirInCommand(createDirInCommand(myWorkingCopyDir, "real"), "root");
    IoTestUtil.createSymLink(realRoot.getPath(), myClientRoot.getPath() + "/root");

    setVcsMappings(new VcsDirectoryMapping(realRoot.getPath(), PerforceVcs.NAME));
    setupClient(clientSpecWithAltRoot(myClientRoot.getPath() + "/root", myClientRoot.getPath() + "/real/root"));

    VirtualFile file = createFileInCommand(realRoot, "a.txt", "");
    
    refreshChanges();
    assertEquals(file, getSingleChange().getVirtualFile());

    P4WhereResult where = PerforceRunner.getInstance(myProject)
      .where(file.getPath(), PerforceConnectionManager.getInstance(myProject).getConnectionForFile(file));
    assertEquals("//depot/a.txt", where.getDepot());
    assertEquals("//test/a.txt", where.getLocalRootDependent());
  }

}
