package org.jetbrains.idea.perforce;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.VcsConfiguration;
import com.intellij.openapi.vcs.VcsDirectoryMapping;
import com.intellij.openapi.vcs.VcsShowConfirmationOption;
import com.intellij.openapi.vfs.StandardFileSystems;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.junit5.TestApplication;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static com.intellij.openapi.vcs.changes.ChangeListManagerExtensionsKt.getUnversionedFiles;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestApplication
@EnabledOnOs(OS.WINDOWS)
public class PerforceWindowsTest {
  private TestInfo myTestInfo;

  @BeforeEach
  public void captureTestInfo(TestInfo testInfo) {
    myTestInfo = testInfo;
  }

  @Test
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
    tc.setUpPerforce(myTestInfo);

    try {
      tc.setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD,
                                 VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY);

      tc.refreshInfoAndClient();

      VirtualFile file = tc.createFileInCommand(tc.createDirInCommand(tc.getWorkingCopyDir(), "a b"), "a.txt", "aaa");
      tc.getChangeListManager().waitUntilRefreshed();
      assertThat(getUnversionedFiles(tc.getChangeListManager())).containsExactlyInAnyOrder(file);

      tc.addFile("a b/a.txt");
      tc.refreshChanges();

      assertEquals(file, tc.getSingleChange().getVirtualFile());
      assertThat(getUnversionedFiles(tc.getChangeListManager())).isEmpty();

      tc.rollbackChange(tc.getSingleChange());
      tc.getChangeListManager().waitUntilRefreshed();
      assertThat(tc.getChangeListManager().getAllChanges()).isEmpty();
      assertThat(getUnversionedFiles(tc.getChangeListManager())).containsExactlyInAnyOrder(file);
    }
    finally {
      tc.tearDownPerforce();
    }
  }

  @Test
  public void testUnixAltRoot() throws Exception {
    PerforceTestCase tc = new PerforceTestCase() {
      @Override
      protected String buildTestClientSpec() {
        String clientRoot = FileUtil.toSystemIndependentName(myClientRoot.toString());
        assertEquals(clientRoot.charAt(1), ':', clientRoot);

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
      StandardFileSystems.local().refresh(false);

      tc.refreshInfoAndClient();
      tc.refreshChanges();
      assertThat(getUnversionedFiles(tc.getChangeListManager())).hasSize(1);
      assertThat(tc.getChangeListManager().getDeletedFiles()).isEmpty();

      tc.setVcsMappings(new VcsDirectoryMapping(drive + ":", "Perforce"));
      tc.refreshInfoAndClient();
      tc.refreshChanges();
      assertThat(getUnversionedFiles(tc.getChangeListManager())).hasSize(1);
      assertThat(tc.getChangeListManager().getDeletedFiles()).isEmpty();

      tc.addFile("foo/a.txt");
      tc.refreshChanges();
      assertThat(getUnversionedFiles(tc.getChangeListManager())).isEmpty();
      tc.getSingleChange();
      assertThat(tc.getChangeListManager().getDeletedFiles()).isEmpty();

      tc.setVcsMappings(new VcsDirectoryMapping(drive + ":\\", "Perforce"));
      tc.refreshChanges();
      assertThat(getUnversionedFiles(tc.getChangeListManager())).isEmpty();
      tc.getSingleChange();
      assertThat(tc.getChangeListManager().getDeletedFiles()).isEmpty();
    }
    finally {
      try {
        VirtualFile root = StandardFileSystems.local().findFileByPath(drive + ":/");
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

