package com.dmarcotte.handlebars.inspections;

import com.dmarcotte.handlebars.util.HbTestUtils;
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
public class HbBlockMismatchFixTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> fixture = sharedCodeInsightFixture(projectFixture);
  private String myTestName;

  @BeforeEach
  void setUp(TestInfo testInfo) {
    myTestName = PlatformTestUtil.getTestName(testInfo.getTestMethod().orElseThrow().getName(), false);
    fixture.get().setTestDataPath(HbTestUtils.BASE_TEST_DATA_PATH);
  }

  @Test
  public void testWrongCloseBlock1() {
    doTest("Change block end");
  }

  @Test
  public void testWrongCloseBlock2() {
    doTest("Change block end");
  }

  @Test
  public void testWrongOpenBlock1() {
    doTest("Change block start");
  }

  @Test
  public void testWrongOpenBlock2() {
    doTest("Change block start");
  }

  @Test
  public void testWrongOpenRawBlock() {
    doTest("Change block start");
  }

  private void doTest(String intentionHint) {
    CodeInsightTestFixture myFixture = fixture.get();
    myFixture.configureByFile("inspections/before" + myTestName + ".hbs");
    EdtTestUtil.runInEdtAndWait(() -> myFixture.launchAction(myFixture.findSingleIntention(intentionHint)));
    myFixture.checkResultByFile("inspections/after" + myTestName + ".hbs");
  }
}
