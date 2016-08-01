package org.intellij.plugins.postcss.inspections;

import com.intellij.psi.css.inspections.invalid.CssInvalidPseudoSelectorInspection;
import com.intellij.testFramework.TestDataPath;
import org.intellij.plugins.postcss.PostCssFixtureTestCase;
import org.jetbrains.annotations.NotNull;

@TestDataPath("$CONTENT_ROOT/testData/inspections/customSelectors/")
public class PostCssCustomSelectorInspectionTest extends PostCssFixtureTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(PostCssCustomSelectorInspection.class, CssInvalidPseudoSelectorInspection.class);
  }

  public void testIncorrectCustomSelectorName() {
    doTest();
  }

  public void testEmptyCustomSelectorName() {
    doTest();
  }

  public void testEmptySelectorList() {
    doTest();
  }

  public void testEmptySelectorListTwoInLine() {
    doTest();
  }

  public void testEmptyCustomSelector() {
    doTest();
  }

  public void testCustomSelectorWithoutDashes() {
    doTest();
  }

  public void testCustomSelectorWithInvalidTokenAt() {
    doTest();
  }

  public void testCustomSelectorWithInvalidTokenSlash() {
    doTest();
  }

  private long doTest() {
    return myFixture.testHighlighting(true, false, false, getTestName(true) + ".pcss");
  }

  @NotNull
  @Override
  protected String getTestDataSubdir() {
    return "customSelectors";
  }
}