package com.dmarcotte.handlebars.highlighting;

import com.dmarcotte.handlebars.util.HbTestUtils;
import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection;
import com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;

/**
 * Created by fedorkorotkov.
 */
@TestApplication
public class HbHighlightingTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> fixture = sharedCodeInsightFixture(projectFixture);
  private String myTestName;

  @BeforeEach
  void setUp(TestInfo testInfo) {
    myTestName = PlatformTestUtil.getTestName(testInfo.getTestMethod().orElseThrow().getName(), true);
    fixture.get().setTestDataPath(HbTestUtils.BASE_TEST_DATA_PATH + "/highlighting");
    enableInspections();
  }

  private void enableInspections() {
    fixture.get().enableInspections(HtmlUnknownTagInspection.class, HtmlUnknownAttributeInspection.class);
  }

  private void doTest(String extension) {
    CodeInsightTestFixture myFixture = fixture.get();
    myFixture.configureByFile(myTestName + "." + extension);
    myFixture.checkHighlighting(true, false, true);
  }

  @Test
  public void testScriptTag() {
    doTest("html");
  }

  @Test
  public void testUncompletedTag() {
    doTest("hbs");
  }

  @Test
  public void testUncompletedTagInHandlebars() {
    doTest("hbs");
  }

  @Test
  public void testInvalidElementStackOverflow() {
    fixture.get().configureByFile(myTestName + ".hbs");
  }

  @Test
  public void testTagWithAttributeValue() {
    doTest("hbs");
  }
}
