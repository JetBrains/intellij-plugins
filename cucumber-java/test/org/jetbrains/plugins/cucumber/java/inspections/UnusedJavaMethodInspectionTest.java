package org.jetbrains.plugins.cucumber.java.inspections;

import com.intellij.codeInspection.deadCode.UnusedDeclarationInspection;
import com.intellij.codeInspection.unusedSymbol.UnusedSymbolLocalInspection;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;

/**
 * User: Andrey.Vokin
 * Date: 10/20/12
 */
public class UnusedJavaMethodInspectionTest extends CucumberJavaBaseInspectionTest {
  protected void doTest(final String file) {
    myFixture.enableInspections(new UnusedDeclarationInspection(), new UnusedSymbolLocalInspection());
    myFixture.configureByFile(file);
    myFixture.testHighlighting(true, false, true);
  }

  public void testStepDefinition() {
    doTest("ShoppingStepdefs.java");
  }

  public void testHooks() {
    doTest("Hooks.java");
  }

  @Override
  protected String getBasePath() {
    return CucumberJavaTestUtil.RELATED_TEST_DATA_PATH + "inspections\\unusedMethod";
  }
}
