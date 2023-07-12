// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.refactoring;

import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy;
import com.intellij.testFramework.fixtures.TempDirTestFixture;
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl;
import org.angular2.Angular2MultiFileFixtureTestCase;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;

public class Angular2ExtractComponentTest extends Angular2MultiFileFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "extractComponent";
  }

  public void testSingleElementMultiLineFromCaret() {
    doMultiFileTest();
  }

  public void testSingleElementSingleLine() {
    doMultiFileTest();
  }

  public void testMultiElement() {
    doMultiFileTest();
  }

  public void testNoElement() {
    doMultiFileTest();
  }

  public void testNameClashes() {
    doMultiFileTest();
  }

  public void testExtractFromInlineTemplate() {
    doMultiFileTest("src/app/app.component.ts");
  }

  public void testUnsupportedSelection() {
    doFailedTest();
  }

  public void testUnsupportedSelection2() {
    doFailedTest();
  }

  public void testUnsupportedSelection3() {
    doFailedTest();
  }

  public void testUnsupportedSelection4() {
    doFailedTest();
  }

  private void doMultiFileTest() {
    doMultiFileTest("src/app/app.component.html");
  }

  private void doMultiFileTest(String source) {
    doTest((rootDir, rootAfter) -> {
      myFixture.configureFromTempProjectFile(source);
      myFixture.performEditorAction("Angular2ExtractComponentAction");
    });
  }

  private void doFailedTest() {
    assertThrows(CommonRefactoringUtil.RefactoringErrorHintException.class, () -> doMultiFileTest());
  }

  @NotNull
  @Override
  protected String getTestRoot() {
    return "/";
  }

  @Override
  protected TempDirTestFixture createTempDirTestFixture() {
    IdeaTestExecutionPolicy policy = IdeaTestExecutionPolicy.current();
    return policy != null
           ? policy.createTempDirTestFixture()
           : new LightTempDirTestFixtureImpl(false);
  }
}
