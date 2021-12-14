// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.refactoring;

import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.javascript.web.WebTestUtil;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings;
import com.intellij.openapi.ui.TestDialog;
import com.intellij.openapi.ui.TestDialogManager;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.rename.RenameProcessor;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import org.angular2.Angular2MultiFileFixtureTestCase;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;

public class RenameTest extends Angular2MultiFileFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "rename";
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      TestDialogManager.setTestDialog(TestDialog.DEFAULT);
    }
    catch (Throwable e) {
      addSuppressedException(e);
    }
    finally {
      super.tearDown();
    }
  }

  public void testRenameComponentFromStringUsage() {
    doMultiFileTest("test.component.ts", "newName");
  }

  public void testComponentFieldFromTemplate() {
    doMultiFileTest("test.component.html", "newName");
  }

  public void testI18nAttribute() {
    doMultiFileTest("directive.ts", "new-name");
  }

  public void testLocalInTemplate() {
    doMultiFileTest("test.component.html", "newName");
  }

  public void testReferenceFromTS() {
    doMultiFileTest("test.component.ts", "newReference");
  }

  public void testReferenceFromHTML() {
    doMultiFileTest("test.component.html", "newReference");
  }

  public void testReferenceFromTSNoStrings() {
    doMultiFileTest("test.component.ts", "newReference", false);
  }

  public void testReferenceFromHTMLNoStrings() {
    doMultiFileTest("test.component.html", "newReference", false);
  }

  public void testPipeFromHTML() {
    doMultiFileTest("test.component.html", "bar");
  }

  public void testPipeFromHTMLNoStrings() {
    doMultiFileTest("test.component.html", "bar", false);
  }

  public void testPipeFromTS() {
    doMultiFileTest("foo.pipe.ts", "bar");
  }

  public void testPipeFromTS2() {
    doMultiFileTest("foo.pipe.ts", "bar");
  }

  public void testPipeFromTS2NoStrings() {
    doMultiFileTest("foo.pipe.ts", "bar", false);
  }

  public void testComponentWithRelatedFiles() {
    TestDialogManager.setTestDialog(TestDialog.OK);
    JSTestUtils.testWithTempCodeStyleSettings(getProject(), t -> {
      t.getCustomSettings(TypeScriptCodeStyleSettings.class).FILE_NAME_STYLE = JSCodeStyleSettings.JSFileNameStyle.PASCAL_CASE;
      doMultiFileTest("foo-bar.component.ts", "NewNameComponent");
    });
  }

  public void testComponentToNonComponentName() {
    TestDialogManager.setTestDialog(TestDialog.OK);
    doMultiFileTest("foo-bar.component.ts", "NewNameSomething");
  }

  public void testModuleToNameWithoutPrefix() {
    TestDialogManager.setTestDialog(TestDialog.OK);
    doMultiFileTest("foo.module.ts", "Module");
  }

  public void testInjectionReparse() {
    TestDialogManager.setTestDialog(TestDialog.OK);
    doMultiFileTest("foo.component.html", "product");
  }

  public void testNgContentSelector() {
    doMultiFileTest("slots.component.ts", "new-tag");
  }

  private void doMultiFileTest(String mainFile, String newName) {
    doMultiFileTest(mainFile, newName, true);
  }

  private void doMultiFileTest(String mainFile, String newName, boolean searchCommentsAndText) {
    doTest((rootDir, rootAfter) -> {
      myFixture.configureFromTempProjectFile(mainFile);
      if (WebTestUtil.canRenameWebSymbolAtCaret(myFixture)) {
        WebTestUtil.renameWebSymbol(myFixture, newName);
      }
      else {
        PsiElement targetElement = TargetElementUtil.findTargetElement(
          myFixture.getEditor(),
          TargetElementUtil.ELEMENT_NAME_ACCEPTED | TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED);
        targetElement = RenamePsiElementProcessor.forElement(targetElement).substituteElementToRename(targetElement, myFixture.getEditor());
        RenameProcessor renameProcessor =
          new RenameProcessor(myFixture.getProject(), targetElement, newName, searchCommentsAndText, searchCommentsAndText);
        renameProcessor.run();
      }
    });
  }

  @NotNull
  @Override
  protected String getTestRoot() {
    return "/";
  }
}
