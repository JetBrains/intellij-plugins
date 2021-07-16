package org.jetbrains.idea.perforce;

import com.intellij.idea.Bombed;
import com.intellij.openapi.vcs.VcsConfiguration;
import com.intellij.openapi.vcs.VcsTestUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.junit.Test;

import java.util.Calendar;

import static com.intellij.testFramework.UsefulTestCase.assertEmpty;
import static junit.framework.Assert.assertEquals;

public class PerforceListenerTest extends PerforceTestCase {
  @Test
  public void testCopyAddedFile() {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    VirtualFile a = createFileInCommand("a.txt", "a");
    copyFileInCommand(a, "b.txt");
    verifyOpened("b.txt", "add");
  }

  @Test
  @Bombed(year = 2021, month = Calendar.AUGUST, day = 20, user = "AMPivovarov")
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
    getChangeListManager().waitUntilRefreshed();
    assertEquals(file2, getSingleChange().getVirtualFile());
    assertEmpty(getChangeListManager().getUnversionedFiles());
  }
}