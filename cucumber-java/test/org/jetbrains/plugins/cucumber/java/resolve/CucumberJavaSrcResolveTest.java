package org.jetbrains.plugins.cucumber.java.resolve;

public class CucumberJavaSrcResolveTest extends BaseCucumberJavaResolveTest {
  public void testNavigationToSrc() {
    doTest("stepResolve_03", "tes<caret>t \"test\"", "test");
    myFixture.testHighlighting("ShoppingStepdefs.java");
  }

  public void testResolveToStepWithStringConcatenation() {
    doTest("stepResolveStringConcatenation", "subt<caret>ract", "I_subtract_from");
    myFixture.testHighlighting("ShoppingStepdefs.java");
  }

  public void testResolveToStepWithTimeout() {
    doTest("resolveToStepWithTimeout", "subt<caret>ract", "I_subtract_from");
    myFixture.testHighlighting("ShoppingStepdefs.java");
  }

  public void testStrictStartAndEndRegexOptions() {
    doTest("strictStartAndEndRegexOptions", "I have sh<caret>ort step", "I_have_short_step");
    myFixture.testHighlighting("ShoppingStepdefs.java");
  }

  public void testStepDefContainerMarkedWithStepDefAnnotation() {
    doTest("stepDefContainerMarkedWithStepDefAnnotation", "the follo<caret>wing grocerie", "the_following_groceries");
    myFixture.testHighlighting("ShoppingStepdefs.java");
  }
}
