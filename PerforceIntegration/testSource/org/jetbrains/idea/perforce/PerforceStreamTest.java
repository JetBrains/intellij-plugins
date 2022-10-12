package org.jetbrains.idea.perforce;

import com.intellij.openapi.vcs.VcsConfiguration;
import com.intellij.openapi.vcs.VcsShowConfirmationOption;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import org.junit.Test;

import java.io.IOException;

import static com.intellij.testFramework.UsefulTestCase.*;

public class PerforceStreamTest extends PerforceTestCase {

  @Override
  public void before() throws Exception {
    super.before();
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY);
  }

  @Override
  protected String getPerforceVersion() {
    return "2015.1";
  }

  @Override
  protected void setupWorkspace() {
    ensureNoEnvP4Config();

    verify(runP4(new String[]{"depot", "-i"}, """
      Depot: streams
      Owner: test
      Type: stream
      Map: streams/..."""));
    setupClient(buildTestClientSpecCore("test", myClientRoot.toString()) + " //streams/... //test/...");
    verify(runP4(new String[]{"stream", "-i"}, """
      Stream: //streams/MAIN
      Name: MAIN
      Parent: none
      Owner: test
      Paths: share ...
      Options: allsubmit unlocked notoparent nofromparent mergedown
      Type: mainline"""));
    verify(runP4WithClient("workspace", "-s", "-f", "-S", "//streams/MAIN"));
    verify(runP4WithClient("sync", "..."));
  }

  @Test
  public void test() throws IOException {
    VirtualFile file = createFileInCommand("a.txt", "a");
    addFile("a.txt");
    submitDefaultList("initial");

    assertTrue(VfsUtilCore.virtualToIoFile(file).exists());
    verify(runP4(new String[]{"stream", "-i"}, """
      Stream: //streams/DEV
      Name: DEV
      Owner: test
      Type: development
      Paths: share ...
      Options: allsubmit unlocked toparent fromparent mergedown
      Parent: //streams/MAIN"""));
    assertTrue(VfsUtilCore.virtualToIoFile(file).exists());
    switchToStream("DEV");

    verify(runP4WithClient("merge", "-S", "//streams/DEV", "-r"));
    verify(runP4WithClient("resolve", "-at", "..."));
    submitDefaultList("branching");
    refreshVfs();

    assertTrue(VfsUtilCore.virtualToIoFile(file).exists());
    file = LocalFileSystem.getInstance().findFileByPath(file.getPath());
    assertNotNull(file);

    openForEdit(file);
    editFileInCommand(file, "ab");
    submitDefaultList("ab");

    createIOFile(TEST_P4CONFIG, createP4Config("test"));
    setUseP4Config();
    refreshChanges();
    assertChangesViewEmpty();

    switchToStream("MAIN");
    verify(runP4WithClient("sync", "..."));
    refreshVfs();

    assertEquals("a", VfsUtilCore.loadText(file));
    openForEdit(file);

    refreshChanges();
    assertEmpty(getChangeListManager().getModifiedWithoutEditing());
    assertEquals(file, getSingleChange().getVirtualFile());
  }

  private void switchToStream(String name) {
    verify(runP4WithClient("workspace", "-s", "-S", "//streams/" + name));
  }
}
