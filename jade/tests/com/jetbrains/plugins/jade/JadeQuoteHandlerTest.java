// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.openapi.application.PathManager;
import com.intellij.testFramework.EditorTestUtil;
import com.intellij.testFramework.LightPlatformCodeInsightTestCase;
import org.jetbrains.annotations.NotNull;

public class JadeQuoteHandlerTest extends LightPlatformCodeInsightTestCase {

  public static final String RELATIVE_TEST_DATA_PATH = "/plugins/Jade/testData";
  public static final String TEST_DATA_PATH = PathManager.getHomePath() + RELATIVE_TEST_DATA_PATH;

  public void testMinusCode() {
    doTest();
  }

  public void testScriptCode() {
    doTest();
  }

  public void testAttribute1() {
    doTest();
  }

  public void testAttribute2() {
    doTest();
  }

  public void testAttribute3() {
    doTest();
  }

  private void doTest() {
    final String extension = ".jade";
    final String beforeFile = getTestName(true) + "_unquoted" + extension;
    final String afterFile = getTestName(true) + "_quoted" + extension;

    configureByFile(beforeFile);
    EditorTestUtil.performTypingAction(getEditor(), '"');
    checkResultByFile(afterFile);

    configureByFile(afterFile);
    EditorTestUtil.performTypingAction(getEditor(), EditorTestUtil.BACKSPACE_FAKE_CHAR);
    checkResultByFile(beforeFile);
  }

  @NotNull
  @Override
  protected String getTestDataPath() {
    return TEST_DATA_PATH + "/quoteHandler/";
  }
}
