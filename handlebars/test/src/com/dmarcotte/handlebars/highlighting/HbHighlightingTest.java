package com.dmarcotte.handlebars.highlighting;

import com.dmarcotte.handlebars.util.HbTestUtils;
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

  @NotNull
  @Override
  protected String getTestDataPath() {
    return HbTestUtils.BASE_TEST_DATA_PATH + getBasePath();
  }

  @Override
  protected boolean isWriteActionRequired() {
    return false;
  }

  private void doTest(String extension) {
    myFixture.configureByFile(getTestName(true) + "." + extension);
    myFixture.checkHighlighting(true, false, true);
  }

  public void testScriptTag() {
    myFixture.enableInspections(HtmlUnknownTagInspection.class);
    doTest("html");
  }
}
