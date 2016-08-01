package org.intellij.plugins.postcss.inspections;

import com.intellij.psi.css.inspections.invalid.CssInvalidImportInspection;
import com.intellij.testFramework.TestDataPath;
import org.intellij.plugins.postcss.PostCssFixtureTestCase;

@TestDataPath("$CONTENT_ROOT/testData/inspections/")
public class PostCssCssInspectionTest extends PostCssFixtureTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(CssInvalidImportInspection.class);
  }

  public void testImportEverywhere() {
    doTest();
  }

  private long doTest() {
    return myFixture.testHighlighting(true, false, false, getTestName(true) + ".pcss");
  }
}