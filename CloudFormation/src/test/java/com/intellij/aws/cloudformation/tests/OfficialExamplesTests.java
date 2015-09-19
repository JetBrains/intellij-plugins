package com.intellij.aws.cloudformation.tests;

import com.google.common.io.Files;
import com.intellij.aws.cloudformation.inspections.FormatViolationInspection;
import com.intellij.aws.cloudformation.inspections.UnresolvedReferencesInspection;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.testFramework.InspectionFixtureTestCase;

import java.io.File;

public class OfficialExamplesTests extends InspectionFixtureTestCase {
  private static final String EXAMPLES_FOLDER_NAME = "officialExamples";

  public void testFormatViolationInspection() throws Exception {
    runInspection("format", new FormatViolationInspection());
  }

  public void testUnresolvedReferencesInspection() throws Exception {
    runInspection("resolve", new UnresolvedReferencesInspection());
  }

  private void runInspection(String inspectionMoniker, LocalInspectionTool inspectionTool) throws Exception {
    File examplesFolder = TestUtil.getTestDataFile(EXAMPLES_FOLDER_NAME);

    File expectedFile = new File(examplesFolder, "expected.xml");
    File expectedFilePerInspection = new File(examplesFolder, "expected-" + inspectionMoniker + ".xml");

    Files.copy(expectedFilePerInspection, expectedFile);

    try {
      doTest(TestUtil.getTestDataPathRelativeToIdeaHome(EXAMPLES_FOLDER_NAME), inspectionTool);
    } finally {
      expectedFile.delete();
    }
  }
}
