// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.editor;

import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.CodeInsightTestUtil;
import org.intellij.terraform.TerraformTestUtils;

@TestDataPath("$CONTENT_ROOT/selectWord")
public class TerraformWordSelectionerTest extends BasePlatformTestCase {

  public void testStringLiteral1() { doTest(); }

  public void testStringLiteral2() { doTest(); }

  private void doTest() {
    CodeInsightTestUtil.doWordSelectionTestOnDirectory(myFixture, getTestName(true), "tf");
  }

  @Override
  protected String getTestDataPath() {
    return TerraformTestUtils.getTestDataPath() + "/selectWord";
  }
}
