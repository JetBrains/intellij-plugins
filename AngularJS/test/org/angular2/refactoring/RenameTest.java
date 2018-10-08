// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.refactoring;

import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.LightPlatformMultiFileFixtureTestCase;
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TestDialog;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.rename.RenameProcessor;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;

public class RenameTest extends LightPlatformMultiFileFixtureTestCase {
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

  public void testRenameComponentFromStringUsage() {
    doMultiFileTest("test.component.ts", "newName");
  }

  public void testComponentFieldFromTemplate() {
    doMultiFileTest("test.component.html", "newName");
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

  public void testPipeFromHTML() {
    doMultiFileTest("test.component.html", "bar");
  }

  public void testPipeFromTS() {
    doMultiFileTest("foo.pipe.ts", "bar");
  }

  public void testPipeFromTS2() {
    doMultiFileTest("foo.pipe.ts", "bar");
  }

  public void testComponentWithRelatedFiles() {
    Messages.setTestDialog(TestDialog.OK);
    JSTestUtils.testWithTempCodeStyleSettings(getProject(), t -> {
      t.getCustomSettings(TypeScriptCodeStyleSettings.class).FILE_NAME_STYLE = JSCodeStyleSettings.JSFileNameStyle.PASCAL_CASE;
      doMultiFileTest("foo-bar.component.ts", "NewNameComponent");
    });
  }

  public void testComponentToNonComponentName() {
    Messages.setTestDialog(TestDialog.OK);
    doMultiFileTest("foo-bar.component.ts", "NewNameSomething");
  }

  public void testModuleToNameWithoutPrefix() {
    Messages.setTestDialog(TestDialog.OK);
    doMultiFileTest("foo.module.ts", "Module");
  }

  public void testInjectionReparse() {
    Messages.setTestDialog(TestDialog.OK);
    doMultiFileTest("foo.component.html", "product");
  }

  private void doMultiFileTest(String mainFile, String newName) {
    doTest((rootDir, rootAfter) -> {
      myFixture.configureFromTempProjectFile(mainFile);
      PsiElement targetElement = TargetElementUtil.findTargetElement(myFixture.getEditor(), TargetElementUtil.ELEMENT_NAME_ACCEPTED
                                                                                            | TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED);
      targetElement = RenamePsiElementProcessor.forElement(targetElement).substituteElementToRename(targetElement, myFixture.getEditor());
      RenameProcessor renameProcessor = new RenameProcessor(myFixture.getProject(), targetElement, newName, true, true);
      renameProcessor.run();
    });
  }

  @NotNull
  @Override
  protected String getTestRoot() {
    return "/";
  }
}
