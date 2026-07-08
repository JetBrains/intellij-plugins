// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.inspections;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.inspections.JSUnresolvedReferenceInspection;
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

public class ActionScriptSuppressInspectionsTest extends BasePlatformTestCase {

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("/inspections/Suppress");
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(new JSUnresolvedReferenceInspection());
  }

  public void testJSUnresolvedFunctionForJS2() {
    myFixture.enableInspections(new JSUnusedLocalSymbolsInspection());
    doTest("js2", "unusedLocalSymbols/");
  }

  private void doTest(final String ext, final @NotNull String dirName) {
    String name = getTestName(false);
    myFixture.configureByFile(dirName + name + "_before." + ext);
    IntentionAction action = ContainerUtil.getFirstItem(myFixture.filterAvailableIntentions("Suppress for statement"));
    assertNotNull(action);
    myFixture.launchAction(action);
    myFixture.checkResultByFile(dirName + name + "_after." + ext);
  }
}
