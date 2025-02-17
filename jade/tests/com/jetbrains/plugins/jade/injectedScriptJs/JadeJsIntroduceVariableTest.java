package com.jetbrains.plugins.jade.injectedScriptJs;

import com.intellij.lang.javascript.refactoring.introduceVariable.JSIntroduceVariableTestCase;
import com.jetbrains.plugins.jade.JadeHighlightingTest;
import org.jetbrains.annotations.NotNull;

public class JadeJsIntroduceVariableTest extends JSIntroduceVariableTestCase {

  public void testIntroduceVar() {
    doTest("a", false, ".jade");
  }

  @NotNull
  @Override
  protected String getTestDataPath() {
    return JadeHighlightingTest.TEST_DATA_PATH + "/injectedScriptJs/";
  }
}
