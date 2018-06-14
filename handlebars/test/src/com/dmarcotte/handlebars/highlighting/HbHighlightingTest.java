package com.dmarcotte.handlebars.highlighting;

import com.dmarcotte.handlebars.util.HbTestUtils;
import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection;
import com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

/**
 * Created by fedorkorotkov.
 */
public class HbHighlightingTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getBasePath() {
    return "/highlighting";
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    enableInspections();
  }

  private void enableInspections() {
    myFixture.enableInspections(HtmlUnknownTagInspection.class, HtmlUnknownAttributeInspection.class);
  }

  @NotNull
  @Override
  protected String getTestDataPath() {
    return HbTestUtils.BASE_TEST_DATA_PATH + getBasePath();
  }

  private void doTest(String extension) {
    myFixture.configureByFile(getTestName(true) + "." + extension);
    myFixture.checkHighlighting(true, false, true);
  }

  public void testScriptTag() {
    doTest("html");
  }

  public void testUncompletedTag() {
    doTest("hbs");
  }

  public void testUncompletedTagInHandlebars() {
    doTest("hbs");
  }

  public void testInvalidElementStackOverflow() {
    myFixture.configureByFile(getTestName(true) + ".hbs");
  }

  public void testTagWithAttributeValue() {
    doTest("hbs");
  }
}
