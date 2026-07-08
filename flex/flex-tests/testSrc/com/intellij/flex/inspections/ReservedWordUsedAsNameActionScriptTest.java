// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.inspections;

import com.intellij.flex.util.FlexTestUtils;
import com.sixrr.inspectjs.InspectionJSTestCase;
import com.sixrr.inspectjs.validity.ReservedWordUsedAsNameJSInspection;
import org.jetbrains.annotations.NotNull;

public class ReservedWordUsedAsNameActionScriptTest extends InspectionJSTestCase {

  @Override
  protected @NotNull String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("/inspections/ReservedWordUsedAsName/");
  }

  public void test() {
    myFixture.enableInspections(ReservedWordUsedAsNameJSInspection.class);
    myFixture.testHighlighting("test.as");
  }
}
