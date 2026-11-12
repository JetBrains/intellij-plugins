// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class JadeFoldingTest extends BasePlatformTestCase {

  public void testFolding() {
    defaultTest();
  }

  public void testWeb17111() {
    defaultTest();
  }

  public void defaultTest() {
    myFixture.testFolding(getTestDataPath() + getTestName(true) + ".jade");
  }

  @Override
  protected String getTestDataPath() {
    return JadeTestUtil.getBaseTestDataPath() + "/folding/";
  }
}
