package org.jetbrains.plugins.cucumber.java.resolve;

/**
 * User: Andrey.Vokin
 * Date: 7/20/12
 */
public class CucumberJavaResolveTest extends BaseCucumberJavaResolveTest {
  public void testNavigationFromStepToStepDef01() throws Exception {
    doTest("stepResolve_01", "I p<caret>ay 25", "i_pay");
  }
  public void testNavigationFromStepToStepDef02() throws Exception {
    doTest("stepResolve_01", "the followi<caret>ng groceries", "the_following_groceries");
  }
  public void testNavigationFromStepToStepDef03() throws Exception {
    doTest("stepResolve_01", "my change sh<caret>ould be 4", "my_change_should_be_");
  }

  public void testNavigationWithQuotes01() throws Exception {
    doTest("stepResolve_02", "I subtract 5 fr<caret>om 9", "I_subtract_from");
  }

  public void testNavigationWithQuotes02() throws Exception {
    doTest("stepResolve_02", "the resu<caret>lt is 4", "the_result_is");
  }

  public void testNavigationWithQuotes03() throws Exception {
    doTest("stepResolve_02", "tes<caret>t \"test\"", "test");
  }
}
