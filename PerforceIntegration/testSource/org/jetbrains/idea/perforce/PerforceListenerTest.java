package org.jetbrains.idea.perforce;

import com.intellij.openapi.vcs.VcsConfiguration;
import com.intellij.openapi.vcs.VcsTestUtil;
import com.intellij.openapi.vcs.changes.ChangeListManagerExtensionsKt;
import com.intellij.openapi.vfs.VirtualFile;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PerforceListenerTest extends PerforceTestCase {
  @Test
  public void testCopyAddedFile() {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    VirtualFile a = createFileInCommand("a.txt", "a");
    copyFileInCommand(a, "b.txt");
    refreshChanges();
    verifyOpened("b.txt", "add");
  }

  @Test
  public void testCopyToAnotherWorkspace() {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);

    VirtualFile dir1 = createDirInCommand(myWorkingCopyDir, "dir1");
    VirtualFile dir2 = createDirInCommand(myWorkingCopyDir, "dir2");
    setupTwoClients(dir1, dir2);

    VirtualFile file = createFileInCommand(dir1, "a.txt", "xxx");
    getChangeListManager().waitUntilRefreshed();
    assertEquals(file, getSingleChange().getVirtualFile());

    submitFile("//depot/a.txt");
    refreshVfs();
    refreshChanges();

    VirtualFile file2 = VcsTestUtil.copyFileInCommand(myProject, file, dir2, "b.txt");
    refreshChanges();
    assertEquals(file2, getSingleChange().getVirtualFile());
    assertThat(ChangeListManagerExtensionsKt.getUnversionedFiles(getChangeListManager())).isEmpty();
  }
}
