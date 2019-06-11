package com.intellij.flex.refactoring;

import com.intellij.flex.editor.FlexProjectDescriptor;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.refactoring.introduceVariable.JSInplaceIntroduceVariableTestCase;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;

public class ActionScriptInPlaceIntroduceVariableTest extends JSInplaceIntroduceVariableTestCase {

  @NotNull
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("refactoring/introduceVariable/");
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return FlexProjectDescriptor.DESCRIPTOR;
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    FlexTestUtils.allowFlexVfsRootsFor(myFixture.getTestRootDisposable(), "refactoring/introduceVariable/");
    FlexTestUtils.setupFlexSdk(getModule(), getTestName(false), getClass(), myFixture.getTestRootDisposable());
  }

  public void testInplaceBasicAS() {
    doTest(getTestName(false), ".as");
  }

  public void testInplaceSecondOccurrence() {
    doTest(getTestName(false), ".as");
  }

  public void testInplaceWithNamespace() {
    String name1 = "test1/" + getTestName(false) + "1";
    String name2 = "test2/" + getTestName(false) + "2";
    myFixture.configureByFiles(name2 + ".as", name1 + ".as");
    performActionIntroduce();
    myFixture.checkResultByFile(getTestName(false) + "2_after.as");
  }
}
