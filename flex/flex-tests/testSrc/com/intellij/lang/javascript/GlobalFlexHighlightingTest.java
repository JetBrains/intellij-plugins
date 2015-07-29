package com.intellij.lang.javascript;

import com.intellij.codeInspection.DefaultHighlightVisitorBasedInspection;
import com.intellij.codeInspection.ex.InspectionProfileImpl;
import com.intellij.codeInspection.ex.InspectionToolRegistrar;
import com.intellij.codeInspection.ex.InspectionToolWrapper;
import com.intellij.flex.FlexTestUtils;
import com.intellij.lang.javascript.inspections.JSUnresolvedVariableInspection;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Computable;
import com.intellij.profile.codeInspection.InspectionProjectProfileManager;
import com.intellij.testFramework.InspectionTestCase;

import java.util.List;

public class GlobalFlexHighlightingTest extends InspectionTestCase {
  protected Sdk getTestProjectJdk() {
    return getTestProjectSdk();
  }

  @Override
  protected Sdk getTestProjectSdk() {
    final JSTestUtils.TestDescriptor testDescriptor = new JSTestUtils.TestDescriptor(this);
    if (JSTestUtils.testMethodHasOption(testDescriptor, JSTestOption.WithFlexSdk)) {
      return FlexTestUtils.getSdk(testDescriptor);
    }
    return super.getTestProjectSdk();
  }

  public void testAvailability() throws Exception {
    boolean foundSyntaxCheckInspection = false;
    boolean foundAnnotatorInspection = false;

    List<InspectionToolWrapper> tools = InspectionToolRegistrar.getInstance().createTools();
    for (InspectionToolWrapper tool : tools) {
      String shortName = tool.getShortName();
      foundAnnotatorInspection |= shortName.equals("Annotator");
      foundSyntaxCheckInspection |= shortName.equals("SyntaxError");
    }

    assertTrue("Should have global syntax inspection provided", foundSyntaxCheckInspection);
    assertTrue("Should have global annotator inspection provided", foundAnnotatorInspection);
  }

  public void testReportingSyntaxProblemsInMxml() throws Exception {
    doSyntaxErrorsTest();
  }

  public void testReportingSyntaxProblemsInActionScript() throws Exception {
    doSyntaxErrorsTest();
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testReportingAnnotatorProblemsInMxml() throws Exception {
    doAnnotatorTest();
  }

  public void testReportingAnnotatorProblemsInActionScript() throws Exception {
    doAnnotatorTest();
  }

  private void doSyntaxErrorsTest() throws Exception {
    doTest(getTestName(false), new DefaultHighlightVisitorBasedInspection.SyntaxErrorInspection());
  }

  private void doAnnotatorTest() throws Exception {
    final InspectionProfileImpl profile = (InspectionProfileImpl)InspectionProjectProfileManager.getInstance(myProject).getInspectionProfile();
    InspectionProfileImpl.initAndDo(new Computable<Object>() {
      @Override
      public Object compute() {
        profile.enableTool(JSUnresolvedVariableInspection.SHORT_NAME, myProject);
        return null;
      }
    });
    try {
      doTest(getTestName(false), new DefaultHighlightVisitorBasedInspection.AnnotatorBasedInspection());
    } finally {
      profile.disableTool(JSUnresolvedVariableInspection.SHORT_NAME, myProject);
    }
  }

  @Override
  public String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("global_inspections");
  }
}
