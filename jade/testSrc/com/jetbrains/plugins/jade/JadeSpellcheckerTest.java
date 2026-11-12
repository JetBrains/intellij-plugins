// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.grazie.spellcheck.GrazieSpellCheckingInspection;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class JadeSpellcheckerTest extends BasePlatformTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(new GrazieSpellCheckingInspection());
  }

  public void testAll() {
    myFixture.testHighlighting(false, false, true, getTestName(true) + ".jade");
  }

  @Override
  protected String getTestDataPath() {
    return JadeTestUtil.getBaseTestDataPath() + "/spellchecker/";
  }
}
