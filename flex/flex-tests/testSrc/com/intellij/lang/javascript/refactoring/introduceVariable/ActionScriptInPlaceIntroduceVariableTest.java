package com.intellij.lang.javascript.refactoring.introduceVariable;

import com.intellij.flex.FlexTestUtils;
import org.jetbrains.annotations.NotNull;

public class ActionScriptInPlaceIntroduceVariableTest extends JSInplaceIntroduceVariableTestCase {

  @NotNull
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("refactoring/introduceVariable/");
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass());
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
