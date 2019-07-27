package com.dmarcotte.handlebars.editor.folding;

import com.dmarcotte.handlebars.util.HbTestUtils;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.io.File;

public class HbFoldingBuilderTest extends BasePlatformTestCase {
  private static final String TEST_DATA_PATH = new File(HbTestUtils.BASE_TEST_DATA_PATH, "folding").getAbsolutePath();

  public void testFoldsWithUnclosedBlocks() {
    doTest();
  }

  public void testMultiLineOpenStacheFold() {
    doTest();
  }

  public void testMultipleFolds() {
    doTest();
  }

  public void testSloppyEndBlockFold() {
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
    myFixture.testFolding(new File(TEST_DATA_PATH, getTestName(true) + ".hbs").getAbsolutePath());
  }
}
