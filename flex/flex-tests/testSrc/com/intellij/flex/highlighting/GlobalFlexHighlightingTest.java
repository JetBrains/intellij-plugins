// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.highlighting;

import com.intellij.codeInsight.daemon.impl.DefaultHighlightVisitorBasedInspection;
import com.intellij.codeInspection.ex.InspectionToolRegistrar;
import com.intellij.codeInspection.ex.InspectionToolWrapper;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.testFramework.JavaInspectionTestCase;

import java.util.List;

public class GlobalFlexHighlightingTest extends JavaInspectionTestCase {
  @Override
  protected void setUp() throws Exception {
    FlexTestUtils.allowFlexVfsRootsFor(getTestRootDisposable(), "global_inspections");
    super.setUp();
  }

  public void testAvailability() {
    boolean foundSyntaxCheckInspection = false;
    boolean foundAnnotatorInspection = false;

    List<InspectionToolWrapper<?, ?>> tools = InspectionToolRegistrar.getInstance().createTools();
    for (InspectionToolWrapper tool : tools) {
      String shortName = tool.getShortName();
      foundAnnotatorInspection |= shortName.equals("Annotator");
      foundSyntaxCheckInspection |= shortName.equals("SyntaxError");
    }

    assertTrue("Should have global syntax inspection provided", foundSyntaxCheckInspection);
    assertTrue("Should have global annotator inspection provided", foundAnnotatorInspection);
  }

  public void testReportingSyntaxProblemsInMxml() {
    doSyntaxErrorsTest();
  }

  public void testReportingSyntaxProblemsInActionScript() {
    doSyntaxErrorsTest();
  }

  private void doSyntaxErrorsTest() {
    doTest(getTestName(false), new DefaultHighlightVisitorBasedInspection.SyntaxErrorInspection());
  }

/*
  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testReportingAnnotatorProblemsInMxml() {
    doAnnotatorTest();
  }

  public void testReportingAnnotatorProblemsInActionScript() {
    doAnnotatorTest();
  }


  private void doAnnotatorTest() {
    final InspectionProfileImpl profile = InspectionProjectProfileManager.getInstance(myProject).getCurrentProfile();
    InspectionsKt.runInInitMode(() -> {
      profile.enableTool(JSUnresolvedVariableInspection.SHORT_NAME, myProject);
      return null;
    });
    try {
      doTest(getTestName(false), new DefaultHighlightVisitorBasedInspection.AnnotatorBasedInspection());
    }
    finally {
      profile.setToolEnabled(JSUnresolvedVariableInspection.SHORT_NAME, false);
    }
  }
*/
  @Override
  public String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("global_inspections");
  }
}
