package com.intellij.lang.javascript.linter.tslint;

import com.intellij.openapi.project.Project;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.jetbrains.jsonSchema.impl.inspections.JsonSchemaComplianceInspection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;

@TestApplication
public class TsLintConfigHighlightingTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsightFixture = sharedCodeInsightFixture(projectFixture);
  private String testName;

  @BeforeEach
  void setUp(TestInfo testInfo) {
    testName = PlatformTestUtil.getTestName(testInfo.getTestMethod().orElseThrow().getName(), true);
    CodeInsightTestFixture myFixture = codeInsightFixture.get();
    myFixture.setTestDataPath(TsLintTestUtil.BASE_TEST_DATA_PATH + "/config/highlighting/");
    myFixture.enableInspections(JsonSchemaComplianceInspection.class);
  }

  @Test
  public void testDisabled() {
    doTest();
  }

  private long doTest() {
    return codeInsightFixture.get().testHighlighting(testName + "/tslint.json");
  }

  @Test
  public void testAlignParameters() {
    doTest();
  }

  @Test
  public void testOneLine() {
    doTest();
  }

  @Test
  public void testTypedefWhitespace() {
    doTest();
  }

  @Test
  public void testWhitespace() {
    doTest();
  }

  @Test
  public void testWrongWhitespaceType() {
    doTest();
  }

  @Test
  public void testAlignWrongParameters() {
    doTest();
  }

  @Test
  public void testPreferSwitchPreferTemplate() {
    doTest();
  }
}
