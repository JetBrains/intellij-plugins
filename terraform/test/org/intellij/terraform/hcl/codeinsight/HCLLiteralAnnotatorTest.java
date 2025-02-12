// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.codeinsight;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import org.intellij.terraform.TfTestUtils;

public class HCLLiteralAnnotatorTest extends CodeInsightFixtureTestCase {
  private String myExtension;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myExtension = "hcl";
    myFixture.setTestDataPath(getBasePath());
  }

  private void terraform() {
    myExtension = "tf";
  }

  @Override
  protected String getBasePath() {
    return TfTestUtils.getTestDataPath() + "/terraform/annotator/";
  }

  protected String getExtension() {
    return myExtension;
  }

  protected void doTestHighlighting(boolean checkInfo, boolean checkWeakWarning, boolean checkWarning) {
    myFixture.testHighlighting(checkWarning, checkInfo, checkWeakWarning, getTestName(false) + "." + getExtension());
  }

  public void testNumbers() throws Exception {
    doTestHighlighting(false, true, true);
  }

  public void testMethodCalls() throws Exception {
    doTestHighlighting(false, true, true);
  }

  public void testForEllipsis() throws Exception {
    doTestHighlighting(false, true, true);
  }

  public void testHCL1StringKeys() throws Exception {
    terraform();
    doTestHighlighting(false, true, true);
  }

  public void test324() throws Exception {
    terraform();
    doTestHighlighting(false, true, true);
  }
}
