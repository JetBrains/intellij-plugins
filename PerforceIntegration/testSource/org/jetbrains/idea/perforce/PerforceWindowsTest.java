package org.jetbrains.idea.perforce;

import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.VcsConfiguration;
import com.intellij.openapi.vcs.VcsDirectoryMapping;
import com.intellij.openapi.vcs.VcsShowConfirmationOption;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;
import org.junit.rules.TestName;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static com.intellij.testFramework.UsefulTestCase.*;

public class PerforceWindowsTest extends TestCase {
  @Override
  protected void runTest() throws Throwable {
    if (SystemInfo.isWindows) {
      super.runTest();
    }
  }

  public void testNullClientRoot() throws Exception {
    PerforceTestCase tc = new PerforceTestCase() {
      @Override
      protected String buildTestClientSpec() {
        String clientRoot = StringUtil.decapitalize(FileUtil.toSystemIndependentName(myClientRoot.toString()));
        return buildTestClientSpec("test", "null", "\"//test/" + clientRoot + "/...\"");
      }
    };

    doSanityTest(tc);
  }

  private void doSanityTest(PerforceTestCase tc) throws Exception {
    tc.name = new TestName(){
      @Override
      public String getMethodName() {
        return PerforceWindowsTest.this.getName();
      }
    };
    tc.before();

    try {
      tc.setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY);

      tc.refreshInfoAndClient();

      VirtualFile file = tc.createFileInCommand(tc.createDirInCommand(tc.getWorkingCopyDir(), "a b"), "a.txt", "aaa");
      tc.getChangeListManager().waitUntilRefreshed();
      assertSameElements(tc.getChangeListManager().getUnversionedFiles(), file);

      tc.addFile("a b/a.txt");
      tc.refreshChanges();

      assertEquals(file, tc.getSingleChange().getVirtualFile());
      assertEmpty(tc.getChangeListManager().getUnversionedFiles());

      tc.rollbackChange(tc.getSingleChange());
      tc.getChangeListManager().waitUntilRefreshed();
      assertEmpty(tc.getChangeListManager().getAllChanges());
      assertSameElements(tc.getChangeListManager().getUnversionedFiles(), file);
    }
    finally {
      tc.after();
    }
  }

  public void testUnixAltRoot() throws Exception {
    PerforceTestCase tc = new PerforceTestCase() {
      @Override
      protected String buildTestClientSpec() {
        String clientRoot = FileUtil.toSystemIndependentName(myClientRoot.toString());
        assertEquals(clientRoot, clientRoot.charAt(1), ':');

        String sep = System.lineSeparator();
        return "Client:\ttest" + sep +
               "Root:\t" + myClientRoot + sep +
               "AltRoots:\t" + clientRoot.substring(2) + sep +
               "View:" + sep + "\t//depot/... //test/..." + sep;
      }
    };

    doSanityTest(tc);
  }

  public void _testRootClient() throws Exception {
    final String drive = findTestDrive();
    PerforceTestCase tc = new PerforceTestCase() {
      @Override
      protected String buildTestClientSpec() {
        return buildTestClientSpec("test", drive + ":", "//test/...");
      }

      @Override
      protected void initProject(@NotNull File clientRoot, String testName) throws Exception {
        File newClient = new File(drive + ":\\");
        assertFalse(newClient.exists());
        Runtime.getRuntime().exec(new String[]{"subst", drive + ":", clientRoot.getPath()}).waitFor();
        assertTrue(newClient.exists());
        myClientRoot = clientRoot = newClient;

        super.initProject(clientRoot, testName);
      }
    };

    tc.before();
    try {
      new File(tc.myClientRoot, "foo").mkdirs();
      new File(tc.myClientRoot, "foo/b.txt").createNewFile();
      tc.addFile("foo/b.txt");
      tc.submitDefaultList("initial");
      new File(tc.myClientRoot, "foo/a.txt").createNewFile();
      LocalFileSystem.getInstance().refresh(false);

      tc.refreshInfoAndClient();
      tc.refreshChanges();
      assertOneElement(tc.getChangeListManager().getUnversionedFiles());
      assertEmpty(tc.getChangeListManager().getDeletedFiles());

      tc.setVcsMappings(new VcsDirectoryMapping(drive + ":", "Perforce"));
      tc.refreshInfoAndClient();
      tc.refreshChanges();
      assertOneElement(tc.getChangeListManager().getUnversionedFiles());
      assertEmpty(tc.getChangeListManager().getDeletedFiles());

      tc.addFile("foo/a.txt");
      tc.refreshChanges();
      assertEmpty(tc.getChangeListManager().getUnversionedFiles());
      tc.getSingleChange();
      assertEmpty(tc.getChangeListManager().getDeletedFiles());

      tc.setVcsMappings(new VcsDirectoryMapping(drive + ":\\", "Perforce"));
      tc.refreshChanges();
      assertEmpty(tc.getChangeListManager().getUnversionedFiles());
      tc.getSingleChange();
      assertEmpty(tc.getChangeListManager().getDeletedFiles());
    }
    finally {
      try {
        VirtualFile root = LocalFileSystem.getInstance().findFileByPath(drive + ":/");
        if (root != null) {
          for (VirtualFile file : root.getChildren()) {
            file.delete(this);
          }
        }
      }
      finally {
        Runtime.getRuntime().exec(new String[]{"subst", drive + ":", "/D"}).waitFor();
        tc.after();
      }
    }
  }

  private static String findTestDrive() {
    List<File> roots = Arrays.asList(File.listRoots());
    for (char c = 'D'; c <= 'Z'; c++) {
      File candidate = new File(c + ":/");
      if (!roots.contains(candidate) && !candidate.exists()) {
        return String.valueOf(c);
      }
    }
    throw new AssertionError("Cannot find an empty drive letter");
  }

}

