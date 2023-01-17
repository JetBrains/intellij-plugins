package com.intellij.lang.javascript.linter.tslint;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.jetbrains.jsonSchema.impl.inspections.JsonSchemaComplianceInspection;

public class TsLintConfigHighlightingTest extends BasePlatformTestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(JsonSchemaComplianceInspection.class);
  }

  @Override
  protected String getBasePath() {
    return "/config/highlighting/";
  }

  @Override
  protected String getTestDataPath() {
    return TsLintTestUtil.BASE_TEST_DATA_PATH + getBasePath();
  }

  public void testDisabled() {
    doTest();
  }

  private long doTest() {
    return myFixture.testHighlighting(getTestName(true) + "/tslint.json");
  }

  public void testAlignParameters() {
    doTest();
  }

  public void testOneLine() {
    doTest();
  }

  public void testTypedefWhitespace() {
    doTest();
  }

  public void testWhitespace() {
    doTest();
  }

  public void testWrongWhitespaceType() {
    doTest();
  }

  public void testAlignWrongParameters() {
    doTest();
  }

  public void testPreferSwitchPreferTemplate() {
    doTest();
  }
}
