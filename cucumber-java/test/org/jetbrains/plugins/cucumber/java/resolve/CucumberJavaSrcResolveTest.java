package org.jetbrains.plugins.cucumber.java.resolve;

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

  public void testStrictStartAndEndRegexOptions() throws Exception {
    doTest("strictStartAndEndRegexOptions", "I have sh<caret>ort step", "I_have_short_step");
  }

  public void testStepDefContainerMarkedWithStepDefAnnotation() throws Exception {
    doTest("stepDefContainerMarkedWithStepDefAnnotation", "the follo<caret>wing grocerie", "the_following_groceries");
  }
}
