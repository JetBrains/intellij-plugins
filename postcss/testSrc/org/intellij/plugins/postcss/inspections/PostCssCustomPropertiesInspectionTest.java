package org.intellij.plugins.postcss.inspections;

import com.intellij.testFramework.TestDataPath;
import org.intellij.plugins.postcss.PostCssFixtureTestCase;
import org.jetbrains.annotations.NotNull;

@TestDataPath("$CONTENT_ROOT/testData/inspections/customPropertiesSet/")
public class PostCssCustomPropertiesInspectionTest extends PostCssFixtureTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(PostCssCustomPropertiesInspection.class);
  }

  public void testCorrectApply() {
    doTest();
  }

  public void testInsideRoot() {
    doTest();
  }

  public void testInsideAtRule() {
    doTest();
  }

  public void testOutsideRoot() {
    doTest();
  }

  public void testOutsideRootProperty() {
    doTest();
  }

  private void doTest() {
    myFixture.testHighlighting(true, false, false, getTestName(true) + ".pcss");
  }

  @NotNull
  @Override
  protected String getTestDataSubdir() {
    return "customPropertiesSet";
  }
}