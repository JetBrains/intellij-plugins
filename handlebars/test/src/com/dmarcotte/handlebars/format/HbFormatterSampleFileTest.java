package com.dmarcotte.handlebars.format;

import com.intellij.application.options.CodeStyle;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static com.dmarcotte.handlebars.format.HbFormatterTestUtil.doFileBasedTest;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;

@TestApplication
public class HbFormatterSampleFileTest implements HbFormattingModelBuilderTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> fixture = sharedCodeInsightFixture(projectFixture);
  private FormatterTestSettings formatterTestSettings;
  private String myTestName;

  @BeforeEach
  void setUp(TestInfo testInfo) {
    myTestName = PlatformTestUtil.getTestName(testInfo.getTestMethod().orElseThrow().getName(), false);
    formatterTestSettings = new FormatterTestSettings(CodeStyle.getSettings(fixture.get().getProject()));
    formatterTestSettings.setUp();
  }

  @AfterEach
  void tearDown() {
    if (formatterTestSettings != null) {
      formatterTestSettings.tearDown();
    }
  }

  @Test
  public void testContactsSampleFile()
    throws Exception {
    doFileBasedTest(fixture.get(), myTestName + ".html");
  }

  @Test
  public void testTodosSampleFile()
    throws Exception {
    doFileBasedTest(fixture.get(), myTestName + ".html");
  }

  @Test
  public void testHbTagHashAlignment()
    throws Exception {
    doFileBasedTest(fixture.get(), myTestName + ".hbs");
  }

  @Test
  public void testHbScript()
    throws Exception {
    doFileBasedTest(fixture.get(), myTestName + ".hbs");
  }

  /**
   * Test out formatting with a non-HTML template data language
   *
   */
  @Test
  public void testSampleFileWithCustomTemplateDataLang()
    throws Exception {
    doFileBasedTest(fixture.get(), "JSSampleFile.hbs", JavaScriptFileType.INSTANCE);
  }
}
