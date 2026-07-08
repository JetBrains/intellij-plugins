// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.intentions;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.BaseJSIntentionTestCase;
import org.jetbrains.annotations.NotNull;

public class ActionScriptSplitDeclarationIntentionTest extends BaseJSIntentionTestCase {

  @Override
  public @NotNull String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("/highlighting/intention/splitDeclaration");
  }

  public void testSplitDeclaration() {
    //before.as / after.as
    doCompositeNameBeforeAfterTest("as", false);
  }
}
