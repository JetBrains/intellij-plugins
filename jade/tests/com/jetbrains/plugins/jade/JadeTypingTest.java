// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

public class JadeTypingTest extends BasePlatformTestCase {

  @NotNull
  @Override
  protected String getTestDataPath() {
    return JadeHighlightingTest.TEST_DATA_PATH + "/typing/";
  }

  public void testEnterDoctype() {
    doTest("\n");
  }

  public void testEnterTag() {
    doTest("\n");
  }

  public void testEnterComment() {
    doTest("\n");
  }

  public void testEnterEmbeddedJS() {
    doTest("\n");
  }

  public void testEnterEmbeddedJSToplevel() {
    doTest("\n");
  }

  public void testBackspace() {
    doTest("\b\b");
  }

  private void doTest(String toType) {
    myFixture.configureByFile('/' + getTestName(true) + ".jade");
    myFixture.type(toType);
    myFixture.checkResultByFile('/' + getTestName(true) + "_after.jade");
  }

}
