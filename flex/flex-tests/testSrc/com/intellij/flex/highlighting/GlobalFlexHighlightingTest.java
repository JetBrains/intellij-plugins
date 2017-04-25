package com.intellij.flex.highlighting;

import com.intellij.codeInsight.daemon.impl.DefaultHighlightVisitorBasedInspection;
import com.intellij.codeInspection.ex.InspectionProfileImpl;
import com.intellij.codeInspection.ex.InspectionToolRegistrar;
import com.intellij.codeInspection.ex.InspectionToolWrapper;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.javascript.flex.css.FlexStylesIndexableSetContributor;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.inspections.JSUnresolvedVariableInspection;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.profile.codeInspection.InspectionProjectProfileManager;
import com.intellij.testFramework.InspectionTestCase;
import com.intellij.testFramework.InspectionsKt;

import java.util.List;

import static com.intellij.openapi.vfs.VfsUtilCore.convertFromUrl;
import static com.intellij.openapi.vfs.VfsUtilCore.urlToPath;

public class GlobalFlexHighlightingTest extends InspectionTestCase {

  @Override
  protected void setUp() throws Exception {
    VfsRootAccess.allowRootAccess(getTestRootDisposable(),
                                  urlToPath(convertFromUrl(FlexStylesIndexableSetContributor.class.getResource("FlexStyles.as"))));
    super.setUp();
  }

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
    final InspectionProfileImpl profile = InspectionProjectProfileManager.getInstance(myProject).getCurrentProfile();
    InspectionsKt.runInInitMode(() -> {
      profile.enableTool(JSUnresolvedVariableInspection.SHORT_NAME, myProject);
      return null;
    });
    try {
      doTest(getTestName(false), new DefaultHighlightVisitorBasedInspection.AnnotatorBasedInspection());
    }
    finally {
      profile.disableTool(JSUnresolvedVariableInspection.SHORT_NAME, myProject);
    }
  }

  @Override
  public String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("global_inspections");
  }
}
