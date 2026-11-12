package com.dmarcotte.handlebars.inspections;


import com.dmarcotte.handlebars.util.HbTestUtils;
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;

@TestApplication
public class HbInspectionsTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> fixture = sharedCodeInsightFixture(projectFixture);
  private String myTestName;

  @BeforeEach
  void setUp(TestInfo testInfo) {
    myTestName = PlatformTestUtil.getTestName(testInfo.getTestMethod().orElseThrow().getName(), false);
    fixture.get().setTestDataPath(HbTestUtils.BASE_TEST_DATA_PATH + "/inspections");
  }

  @Test
  public void testEmptyBlock() {
    doTest();
  }

  private void doTest() {
    CodeInsightTestFixture myFixture = fixture.get();
    EdtTestUtil.runInEdtAndWait(() -> myFixture.testInspection(myTestName, new LocalInspectionToolWrapper(new HbEmptyBlockInspection())));
  }
}
