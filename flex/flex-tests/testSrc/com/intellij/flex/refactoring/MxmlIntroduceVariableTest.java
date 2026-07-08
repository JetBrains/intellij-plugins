// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.refactoring;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.refactoring.introduceVariable.JSIntroduceVariableTestCase;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import org.jetbrains.annotations.NotNull;

public class MxmlIntroduceVariableTest extends JSIntroduceVariableTestCase {

  @Override
  protected @NotNull String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("/refactoring/introduceVariable/");
  }

  public void testKeepCData() {
    doTest("created", true, ".mxml");
  }

  public void testNoIntroduceInBinding() {
    assertThrows(CommonRefactoringUtil.RefactoringErrorHintException.class, () -> doTest("created", true, ".mxml"));
  }
}
