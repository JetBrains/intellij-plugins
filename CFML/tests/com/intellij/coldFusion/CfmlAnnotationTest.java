package com.intellij.coldFusion;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.coldFusion.UI.inspections.CfmlReferenceInspection;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;

/**
 * User: Nadya.Zabrodina
 */
public class CfmlAnnotationTest extends CfmlCodeInsightFixtureTestCase {
  @Override
  protected String getBasePath() {
    return "/annotation";
  }

  @Override
  protected boolean isWriteActionRequired() {
    return false;
  }

  private void doAnnotationTest(boolean infos, Class<? extends LocalInspectionTool>... inspectionClasses) throws Exception {
    String inputDataFileName = Util.getInputDataFileName(getTestName(true));
    myFixture.configureByFiles(inputDataFileName);
    myFixture.testHighlighting(false, infos, false);
  }


  public void testActionNameAnnotation() throws Throwable {
    doAnnotationTest(true, CfmlReferenceInspection.class);
  }
}
