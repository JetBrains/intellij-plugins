// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.codeInsight;

import com.intellij.dupLocator.DefaultDuplocatorState;
import com.intellij.dupLocator.DuplicatesTestCase;
import com.intellij.dupLocator.util.DuplocatorUtil;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.flex.FlexSupportLoader;
import org.jetbrains.annotations.NotNull;

public class ActionScriptDupLocatorTest extends DuplicatesTestCase {

  public void testAs1() throws Exception {
    doTest("asdups1.as", false, false, true, 1, 1, "", 2);
  }

  public void testAs2() throws Exception {
    doTest("asdups2.as", false, false, true, 3, 0, "", 4);
  }

  @Override
  protected Language[] getLanguages() {
    return new Language[]{FlexSupportLoader.ECMA_SCRIPT_L4};
  }

  @Override
  protected @NotNull String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("duplicates/");
  }

  @Override
  protected void findAndCheck(String fileName,
                              boolean distinguishVars,
                              boolean distinguishFunctions,
                              boolean distinguishListerals,
                              int patternCount, String suffix, int lowerBound) throws Exception {
    final DefaultDuplocatorState asState = (DefaultDuplocatorState)DuplocatorUtil.registerAndGetState(
      FlexSupportLoader.ECMA_SCRIPT_L4);
    final boolean asOldFuncs = asState.DISTINGUISH_FUNCTIONS;
    final boolean asOldLits = asState.DISTINGUISH_LITERALS;
    final boolean asOldVars = asState.DISTINGUISH_VARIABLES;
    final int asOldLowerBound = asState.LOWER_BOUND;

    try {
      asState.DISTINGUISH_FUNCTIONS = distinguishFunctions;
      asState.DISTINGUISH_LITERALS = distinguishListerals;
      asState.DISTINGUISH_VARIABLES = distinguishVars;
      asState.LOWER_BOUND = lowerBound;

      doFindAndCheck(fileName, patternCount, suffix);
    }
    finally {
      asState.DISTINGUISH_FUNCTIONS = asOldFuncs;
      asState.DISTINGUISH_LITERALS = asOldLits;
      asState.DISTINGUISH_VARIABLES = asOldVars;
      asState.LOWER_BOUND = asOldLowerBound;
    }
  }
}
