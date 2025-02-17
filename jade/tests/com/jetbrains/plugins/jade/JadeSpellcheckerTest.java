// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.openapi.application.PathManager;
import com.intellij.spellchecker.inspections.SpellCheckingInspection;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class JadeSpellcheckerTest extends BasePlatformTestCase {

  public static final String RELATIVE_TEST_DATA_PATH = "/plugins/Jade/testData";
  public static final String TEST_DATA_PATH = PathManager.getHomePath() + RELATIVE_TEST_DATA_PATH;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(new SpellCheckingInspection());
  }

  public void testAll() {
    myFixture.testHighlighting(false, false, true, getTestName(true) + ".jade");
  }

  @Override
  protected String getTestDataPath() {
    return TEST_DATA_PATH + "/spellchecker/";
  }
}
