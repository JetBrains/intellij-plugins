package com.intellij.flex.refactoring;

import com.intellij.flex.base.FlexInplaceIntroduceVariableTestCase;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.module.ModuleType;
import org.jetbrains.annotations.NotNull;

public class ActionScriptInPlaceIntroduceVariableTest extends FlexInplaceIntroduceVariableTestCase {

  @NotNull
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("refactoring/introduceVariable/");
  }

  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @Override
  protected void setUp() throws Exception {
    FlexTestUtils.allowFlexVfsRootsFor(getTestRootDisposable(), "refactoring/introduceVariable/");
    super.setUp();
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(), getTestRootDisposable());
  }

  public void testInplaceBasicAS() throws Exception {
    doTest(getTestName(false), ".as");
  }

  public void testInplaceSecondOccurrence() throws Exception {
    doTest(getTestName(false), ".as");
  }

  public void testInplaceWithNamespace() throws Exception {
    String name1 = "test1/" + getTestName(false) + "1";
    String name2 = "test2/" + getTestName(false) + "2";
    configureByFiles(null, name2 + ".as", name1 + ".as");
    performActionIntroduce();
    checkResultByFile(getTestName(false) + "2_after.as");
  }
}
