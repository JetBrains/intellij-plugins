// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.formatter;

import com.intellij.psi.formatter.FormatterTestCase;
import com.jetbrains.lang.dart.util.DartTestUtils;

public class DartFormatterInHtmlTest extends FormatterTestCase {

  @Override
  protected String getFileExtension() {
    return "html";
  }

  @Override
  protected String getTestDataPath() {
    return DartTestUtils.BASE_TEST_DATA_PATH;
  }

  @Override
  protected String getBasePath() {
    return "formatter/html";
  }

  @Override
  protected void doTest(String resultNumber) throws Exception {
    String testName = getTestName(false);
    doTest(testName + "." + getFileExtension(), testName + "_after." + getFileExtension(), resultNumber);
  }

  public void testDefault() throws Exception {
    doTest();
  }
}

