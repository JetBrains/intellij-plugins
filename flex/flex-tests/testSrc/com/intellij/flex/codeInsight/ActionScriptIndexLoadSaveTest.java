// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.codeInsight;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInspection.htmlInspections.RequiredAttributesInspection;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.inspections.JSDeprecatedSymbolsInspection;
import com.intellij.lang.javascript.inspections.JSUndeclaredVariableInspection;
import com.intellij.lang.javascript.inspections.JSUnresolvedReferenceInspection;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class ActionScriptIndexLoadSaveTest extends BasePlatformTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    JSTestUtils.configureRecursionAssertions(this);
  }

  private void runSingleTest(String @NotNull ... fileNames) {
    myFixture.enableInspections(new RequiredAttributesInspection(),
                                new JSUnresolvedReferenceInspection(),
                                new JSUndeclaredVariableInspection(),
                                new JSDeprecatedSymbolsInspection()
                                );
    JSTestUtils.initJSIndexes(getProject());

    myFixture.configureByFiles(fileNames);

    final Collection<HighlightInfo> collection = myFixture.doHighlighting(HighlightSeverity.WEAK_WARNING);

    JSResolveUtil.clearResolveCaches(myFixture.getFile());
    List<HighlightInfo> actual = myFixture.doHighlighting(HighlightSeverity.WEAK_WARNING);
    assertEquals(collection.size(), actual.size());
  }

  public final void testSimpleJs2() {
    runSingleTest("Simple.js2");
  }

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("/loadsave/");
  }
}
