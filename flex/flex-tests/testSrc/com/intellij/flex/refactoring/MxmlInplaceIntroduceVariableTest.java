// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.refactoring;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.refactoring.introduceVariable.JSInplaceIntroduceVariableTestCase;
import org.jetbrains.annotations.NotNull;

public class MxmlInplaceIntroduceVariableTest extends JSInplaceIntroduceVariableTestCase {

  @Override
  protected @NotNull String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("/refactoring/introduceVariable/");
  }

  public void testInitialOccurrenceSelected() {
    final String testName = getTestName(false);
    doTest("tmp", testName, ".mxml", testName);
  }

  public void testInjectedAllOccurrences() {
    doTest("created", getTestName(false), ".mxml", true, null, null, getTestName(false));
  }
}
