package org.angularjs.refactoring;

import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TestDialog;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.rename.RenameProcessor;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

import java.io.IOException;

public class RenameTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "rename";
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      Messages.setTestDialog(TestDialog.DEFAULT);
    }
    finally {
      super.tearDown();
    }
  }

  public void testRenameComponentFromStringUsage() throws IOException {
    doMultiFileTest("test.component.html", "newName");
  }

  public void testComponentWithRelatedFiles() throws Exception {
    Messages.setTestDialog(TestDialog.OK);
    JSTestUtils.testWithTempCodeStyleSettings(getProject(), t -> {
      t.getCustomSettings(TypeScriptCodeStyleSettings.class).FILE_NAME_STYLE = JSCodeStyleSettings.JSFileNameStyle.PASCAL_CASE;
      doMultiFileTest("foo-bar.component.ts", "NewNameComponent");
    });
  }

  public void testComponentToNonComponentName() throws IOException {
    Messages.setTestDialog(TestDialog.OK);
    doMultiFileTest("foo-bar.component.ts", "NewNameSomething");
  }

  public void testModuleToNameWithoutPrefix() throws IOException {
    Messages.setTestDialog(TestDialog.OK);
    doMultiFileTest("foo.module.ts", "Module");
  }

  private void doMultiFileTest(String mainFile, String newName) throws IOException {
    final String testName = getTestName(true);
    final VirtualFile dir1 = myFixture.copyDirectoryToProject(testName + "/before", "");
    PsiDocumentManager.getInstance(myFixture.getProject()).commitAllDocuments();
    myFixture.configureFromTempProjectFile(mainFile);
    PsiElement targetElement = TargetElementUtil.findTargetElement(myFixture.getEditor(), TargetElementUtil.ELEMENT_NAME_ACCEPTED
                                                                                          | TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED);
    targetElement = RenamePsiElementProcessor.forElement(targetElement).substituteElementToRename(targetElement, myFixture.getEditor());
    RenameProcessor renameProcessor = new RenameProcessor(myFixture.getProject(), targetElement, newName, true, true);
    renameProcessor.run();
    String afterPath = getTestDataPath() + "/" + testName + "/after";
    VirtualFile dirAfter = LocalFileSystem.getInstance().findFileByPath(afterPath);
    if (dirAfter == null) {
      throw new RuntimeException("Cannot find 'after' dir from path: " + afterPath);
    }
    PlatformTestUtil.assertDirectoriesEqual(dirAfter, dir1);
  }
}
