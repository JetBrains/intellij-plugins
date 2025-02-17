// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.injectedScriptJs;

import com.intellij.lang.javascript.refactoring.introduceVariable.JSIntroduceVariableTestCase;
import com.jetbrains.plugins.jade.JadeTestUtil;
import org.jetbrains.annotations.NotNull;

public class JadeJsIntroduceVariableTest extends JSIntroduceVariableTestCase {

  public void testIntroduceVar() {
    doTest("a", false, ".jade");
  }

  @NotNull
  @Override
  protected String getTestDataPath() {
    return JadeTestUtil.getBaseTestDataPath() + "/injectedScriptJs/";
  }
}
