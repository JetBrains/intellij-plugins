package com.dmarcotte.handlebars.editor.folding;

import com.dmarcotte.handlebars.util.HbTestUtils;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.io.File;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;

@TestApplication
public class HbFoldingBuilderTest {
  private static final String TEST_DATA_PATH = new File(HbTestUtils.BASE_TEST_DATA_PATH, "folding").getAbsolutePath();

  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> fixture = sharedCodeInsightFixture(projectFixture);
  private String myTestName;

  @BeforeEach
  void setUp(TestInfo testInfo) {
    myTestName = PlatformTestUtil.getTestName(testInfo.getTestMethod().orElseThrow().getName(), true);
  }

  @Test
  public void testFoldsWithUnclosedBlocks() {
    doTest();
  }

  @Test
  public void testMultiLineOpenStacheFold() {
    doTest();
  }

  @Test
  public void testMultipleFolds() {
    doTest();
  }

  @Test
  public void testSloppyEndBlockFold() {
    doTest();
  }

  @Test
  public void testUnclosedOpenStache() {
    doTest();
  }

  @Test
  public void testCommentFolds() {
    doTest();
  }

  @Test
  public void testInverseBlockCodeFolds() {
    doTest();
  }

  @Test
  public void testEmptyCommentFold() {
    // regression test for https://github.com/dmarcotte/idea-handlebars/issues/80.  Expect no folds in this case.
    doTest();
  }

  /**
   * Test folding based by validating against a the file in {@link #TEST_DATA_PATH} who
   * names matches the test.<br/>
   * <br/>
   * Test data files contain &lt;form&gt; and &lt;/form&gt; tags to indictate the beginning and end
   * of expected folded areas
   */
  private void doTest() {
    fixture.get().testFolding(new File(TEST_DATA_PATH, myTestName + ".hbs").getAbsolutePath());
  }
}
