package org.intellij.plugins.postcss.inspections;

import com.intellij.testFramework.TestDataPath;
import org.intellij.plugins.postcss.PostCssFixtureTestCase;
import org.jetbrains.annotations.NotNull;

@TestDataPath("$CONTENT_ROOT/testData/inspections/customMedia/")
public class PostCssCustomMediaInspectionTest extends PostCssFixtureTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(PostCssCustomMediaInspection.class);
  }

  public void testIncorrectCustomMediaName() {
    doTest();
  }

  public void testEmptyCustomMediaName() {
    doTest();
  }

  public void testEmptyCustomMedia() {
    doTest();
  }

  public void testCorrectCustomMedia() {
    doTest();
  }

  private long doTest() {
    return myFixture.testHighlighting(true, false, false, getTestName(true) + ".pcss");
  }

  @NotNull
  @Override
  protected String getTestDataSubdir() {
    return "customMedia";
  }
}