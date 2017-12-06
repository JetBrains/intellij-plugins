package org.angularjs.codeInsight;

import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

import java.io.IOException;

public class RenameTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "rename";
  }

  public void testRenameClassFromStringUsage() throws IOException {
    JSTestUtils.testES6(getProject(), () -> doMultiFileTest("test.component.html", "newName"));
  }

  @SuppressWarnings("SameParameterValue")
  private void doMultiFileTest(String mainFile, String newName) throws IOException {
    final String testName = getTestName(true);
    final VirtualFile dir1 = myFixture.copyDirectoryToProject(testName + "/before", "");
    PsiDocumentManager.getInstance(myFixture.getProject()).commitAllDocuments();
    myFixture.configureFromTempProjectFile(mainFile);
    myFixture.renameElement(myFixture.getElementAtCaret(), newName, true, true);
    String afterPath = getTestDataPath() + "/" + testName + "/after";
    VirtualFile dirAfter = LocalFileSystem.getInstance().findFileByPath(afterPath);
    if (dirAfter == null) {
      throw new RuntimeException("Cannot find 'after' dir from path: " + afterPath);
    }
    PlatformTestUtil.assertDirectoriesEqual(dirAfter, dir1);
  }
}
