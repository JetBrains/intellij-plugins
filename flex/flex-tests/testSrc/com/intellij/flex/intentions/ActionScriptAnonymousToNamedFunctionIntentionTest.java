// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.intentions;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.BaseJSIntentionTestCase;
import org.jetbrains.annotations.NotNull;

public class ActionScriptAnonymousToNamedFunctionIntentionTest extends BaseJSIntentionTestCase {

  @Override
  public @NotNull String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("/highlighting/intention/anonymousToNamedFunction");
  }

  public void testAnonymousToNamedFunction_AS() {
    doCompositeNameBeforeAfterTest("as", false);
  }

  public void testAnonymousToNamedFunction_ActionScriptClassMember() {
    doCompositeNameBeforeAfterTest("as", false);
  }
}
