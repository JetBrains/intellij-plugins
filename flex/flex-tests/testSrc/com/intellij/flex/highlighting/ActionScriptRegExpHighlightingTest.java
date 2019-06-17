// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.highlighting;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

public class ActionScriptRegExpHighlightingTest extends BasePlatformTestCase {

  public void testLookBehind() {
    myFixture.testHighlighting(getTestName(false) + ".as");
  }

  public void testNamedGroup() {
    myFixture.testHighlighting(getTestName(false) + ".as");
  }

  @NotNull
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("as_regexp");
  }
}
