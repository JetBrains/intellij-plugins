package org.jetbrains.plugins.cucumber.java.resolve;

/**
 * User: Andrey.Vokin
 * Date: 3/4/13
 */
public class CucumberJavaSrcResolveTest extends BaseCucumberJavaResolveTest {
  public void testNavigationToSrc() throws Exception {
    doTest("stepResolve_03", "tes<caret>t \"test\"", "test");
  }

  public void testResolveToStepWithStringConcatenation() throws Exception {
    doTest("stepResolveStringConcatenation", "subt<caret>ract", "I_subtract_from");
  }

  public void testResolveToStepWithTimeout() throws Exception {
    doTest("resolveToStepWithTimeout", "subt<caret>ract", "I_subtract_from");
  }

}
