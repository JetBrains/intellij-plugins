package com.jetbrains.lang.dart;

import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.refactoring.move.moveFilesOrDirectories.MoveFilesOrDirectoriesProcessor;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.util.DartTestUtils;

import java.util.ArrayList;
import java.util.Collection;

abstract public class DartMoveTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return DartTestUtils.BASE_TEST_DATA_PATH + getBasePath();
  }

  @Override
  protected String getBasePath() {
    return FileUtil.toSystemDependentName("/move/");
  }

  //Both names are relative to root directory
  private void doTest(String toMove, final String targetDirName) throws Exception {
    doTest(new String[]{toMove}, targetDirName);
  }

  //Both names are relative to root directory
  private void doTest(final String[] toMove, final String targetDirName) throws Exception {
    myFixture.copyDirectoryToProject(getTestName(true) + "/before", getTestName(true));
    Collection<PsiElement> files = new ArrayList<>();
    for (String s : toMove) {
      final VirtualFile child = myFixture.findFileInTempDir(getTestName(true) + "/" + s);
      assertNotNull("Neither class nor file " + s + " not found", child);
      files.add(myFixture.getPsiManager().findFile(child));
    }
    final VirtualFile child1 = myFixture.findFileInTempDir(getTestName(true) + "/" + targetDirName);
    assertNotNull("Target dir " + targetDirName + " not found", child1);
    final PsiDirectory targetDirectory = myFixture.getPsiManager().findDirectory(child1);
    assertNotNull(targetDirectory);

    new MoveFilesOrDirectoriesProcessor(myFixture.getProject(), PsiUtilCore.toPsiElementArray(files), targetDirectory,
                                        false, true, null, null).run();
    FileDocumentManager.getInstance().saveAllDocuments();

    VirtualFile expected = LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + getTestName(true) + "/after");
    PlatformTestUtil.assertDirectoriesEqual(expected, myFixture.findFileInTempDir(getTestName(true)));
  }

  public void testMoveFile2() throws Exception {
    doTest("bar/Foo.dart", "");
  }

  public void testMoveFile1() throws Exception {
    doTest("Foo.dart", "bar");
  }
}
