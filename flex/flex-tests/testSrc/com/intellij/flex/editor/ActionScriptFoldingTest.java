// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.editor;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class ActionScriptFoldingTest extends BasePlatformTestCase {

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  private void doTest(final String extension) {
    myFixture.testFolding(getTestDataPath() + "/folding/" + getTestName(false) + "." + extension);
  }

  public void testClass() {
    doTest("as");
  }

  public void testPackage() {
    doTest("as");
  }
}
