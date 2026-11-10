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
public class CucumberJava2ResolveTest {
  private static final TestFixture<LightJavaProject> projectFixture = lightJavaProjectFixture(CucumberJavaTestUtil.createCucumberJava8ProjectDescriptor());
  private final TestFixture<CucumberResolveFixture> cucumber = cucumberJavaResolveFixture(projectFixture);

  @Test
  public void testHighlightingOK() {
    cucumber.get().init("stepResolve_01");
    cucumber.get().getFixture().testHighlighting("ShoppingStepdefs.java");
  }

  @Test
  public void testNavigationFromStepToStepDef01() {
    cucumber.get().doTest("stepResolve_01", "I p<caret>ay 25", "i_pay");
  }

  @Test
  public void testNavigationFromStepToStepDef02() {
    cucumber.get().doTest("stepResolve_01", "the followi<caret>ng groceries", "the_following_groceries");
  }

  @Test
  public void testNavigationFromStepToStepDef03() {
    cucumber.get().doTest("stepResolve_01", "my change sh<caret>ould be 4", "my_change_should_be_");
  }

  @Test
  public void testStepDefinitionWithMatchCount() {
    cucumber.get().doTest("stepResolve_01", "I set numb<caret>er to 50", "I_set_number_to");
  }

  @Test
  public void testStepDefinitionWithMatchDiapason() {
    cucumber.get().doTest("stepResolve_01", "I set val<caret>ue to 50", "I_set_value_to");
  }

  @Test
  public void testNavigationWithQuotes01() {
    cucumber.get().doTest("stepResolve_02", "I subtract 5 fr<caret>om 9", "I_subtract_from");
  }

  @Test
  public void testNavigationWithQuotes02() {
    cucumber.get().doTest("stepResolve_02", "the resu<caret>lt is 4", "the_result_is");
  }

  @Test
  public void testNavigationWithQuotes03() {
    cucumber.get().doTest("stepResolve_02", "tes<caret>t \"test\"", "test");
  }

  @Test
  public void testWordSymbolWithUnicode() {
    //noinspection NonAsciiCharacters
    cucumber.get().doTest("stepResolve_w", "пласт<caret>ик", null);
  }

  @Test
  public void testWordSymbolWithAZ() {
    cucumber.get().doTest("stepResolve_w", "plast<caret>ic", "payment_mode");
  }

  @Test
  public void testCaseSensitivity() {
    cucumber.get().doTest("stepResolve_w", "sensit<caret>ivity", null);
  }

  @Test
  public void testJavaSeveralStepAnnotation() {
    cucumber.get().doTest("stepResolve_01", "my anot<caret>her test step", "my_test_step");
  }

  @Test
  public void testJava8StepDef() {
    cucumber.get().doTest("stepResolve_java8", "I have cuk<caret>es", "Given");
  }

  @Test
  public void testJava8StepDefWithRegex() {
    cucumber.get().doTest("stepResolve_java8", "regex <caret>with number: 10", "When");
  }

  @Test
  public void testJava8WithConcatenation() {
    cucumber.get().doTest("stepResolve_java8", "one<caret> two", "Given");
  }

  @Test
  public void testNegativeLookBehind() {
    cucumber.get().doTest("negativeLookBehind", "c<caret>a", "step_method");
  }

  @Test
  public void testTimeout() {
    cucumber.get().doTest("complexRegex", "wit<caret>h", null);
  }

  /**
   * @see CucumberJava1ResolveTest#testExactStepMatching()
   */
  @Test
  public void testNotExactStepMatching() {
    cucumber.get().init("stepResolve_01");
    cucumber.get().checkReference("test sho<caret>uld pass", null);
  }
}
