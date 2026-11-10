package org.jetbrains.plugins.cucumber.java.resolve;

import com.intellij.testFramework.fixtures.LightJavaProject;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;
import org.jetbrains.plugins.cucumber.resolve.CucumberResolveFixture;
import org.junit.jupiter.api.Test;

import static com.intellij.testFramework.fixtures.LightJavaFixturesKt.lightJavaProjectFixture;
import static org.jetbrains.plugins.cucumber.java.resolve.CucumberJavaResolveFixtures.cucumberJavaResolveFixture;

@TestApplication
public class CucumberJavaSrcResolveTest {
  private static final TestFixture<LightJavaProject> projectFixture = lightJavaProjectFixture(CucumberJavaTestUtil.createCucumber2ProjectDescriptor());
  private final TestFixture<CucumberResolveFixture> cucumber = cucumberJavaResolveFixture(projectFixture);

  @Test
  public void testNavigationToSrc() {
    cucumber.get().doTest("stepResolve_03", "tes<caret>t \"test\"", "test");
    cucumber.get().getFixture().testHighlighting("ShoppingStepdefs.java");
  }

  @Test
  public void testResolveToStepWithStringConcatenation() {
    cucumber.get().doTest("stepResolveStringConcatenation", "subt<caret>ract", "I_subtract_from");
    cucumber.get().getFixture().testHighlighting("ShoppingStepdefs.java");
  }

  @Test
  public void testResolveToStepWithTimeout() {
    cucumber.get().doTest("resolveToStepWithTimeout", "subt<caret>ract", "I_subtract_from");
    cucumber.get().getFixture().testHighlighting("ShoppingStepdefs.java");
  }

  @Test
  public void testStrictStartAndEndRegexOptions() {
    cucumber.get().doTest("strictStartAndEndRegexOptions", "I have sh<caret>ort step", "I_have_short_step");
    cucumber.get().getFixture().testHighlighting("ShoppingStepdefs.java");
  }

  @Test
  public void testStepDefContainerMarkedWithStepDefAnnotation() {
    cucumber.get().doTest("stepDefContainerMarkedWithStepDefAnnotation", "the follo<caret>wing grocerie", "the_following_groceries");
    cucumber.get().getFixture().testHighlighting("ShoppingStepdefs.java");
  }
}
