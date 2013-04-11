package com.dmarcotte.handlebars.editor.folding;

import com.dmarcotte.handlebars.util.HbTestUtils;
import com.intellij.testFramework.PlatformTestCase;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

import java.io.File;

public class HbFoldingBuilderTest extends LightPlatformCodeInsightFixtureTestCase {
  private static final String TEST_DATA_PATH = new File(HbTestUtils.BASE_TEST_DATA_PATH, "folding").getAbsolutePath();

  public HbFoldingBuilderTest() {
    PlatformTestCase.initPlatformLangPrefix();
  }

  public void testFoldsWithUnclosedBlocks() {
    doTest();
  }

  public void testMultiLineOpenStacheFolds() {
    doTest();
  }

  public void testMultipleFolds() {
    doTest();
  }

  public void testSloppyEndBlockFolds() {
    doTest();
  }

  public void testUnclosedOpenStache() {
    doTest();
  }

  public void testCommentFolds() {
    doTest();
  }

  public void testInverseBlockCodeFolds() {
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
    myFixture.testFolding(new File(TEST_DATA_PATH, getTestName(true) + ".hbs").getAbsolutePath());
  }
}
