package org.jetbrains.plugins.cucumber.java.resolve;

import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;

public class CucumberJavaTestResolveTest extends BaseCucumberJavaResolveTest {
  public void testNavigationFromStepToStepDef01() {
    doTest("stepResolve_01", "I p<caret>ay 25", "i_pay");
  }
  public void testNavigationFromStepToStepDef02() {
    doTest("stepResolve_01", "the followi<caret>ng groceries", "the_following_groceries");
  }
  public void testNavigationFromStepToStepDef03() {
    doTest("stepResolve_01", "my change sh<caret>ould be 4", "my_change_should_be_");
  }
  
  public void testStepDefinitionWithMatchCount() {
    doTest("stepResolve_01", "I set numb<caret>er to 50", "I_set_number_to");
  }

  public void testStepDefinitionWithMatchDiapason() {
    doTest("stepResolve_01", "I set val<caret>ue to 50", "I_set_value_to");
  }

  public void testNavigationWithQuotes01() {
    doTest("stepResolve_02", "I subtract 5 fr<caret>om 9", "I_subtract_from");
  }

  public void testNavigationWithQuotes02() {
    doTest("stepResolve_02", "the resu<caret>lt is 4", "the_result_is");
  }

  public void testNavigationWithQuotes03() {
    doTest("stepResolve_02", "tes<caret>t \"test\"", "test");
  }

  public void testWordSymbolWithUnicode() {
    doTest("stepResolve_w", "пласт<caret>ик", null);
  }

  public void testWordSymbolWithAZ() {
    doTest("stepResolve_w", "plast<caret>ic", "payment_mode");
  }

  public void testJavaSeveralStepAnnotation() {
    doTest("stepResolve_01", "my anot<caret>her test step", "my_test_step");
  }

  public void testJava8StepDef() {
    doTest("stepResolve_java8", "I have cuk<caret>es", "Given");
  }

  public void testJava8WithConcatenation() {
    doTest("stepResolve_java8", "one<caret> two", "Given");
  }

  public void testNegativeLookBehind() {
    doTest("negativeLookBehind", "c<caret>a", "step_method");
  }

  public void testResolveOfStepWithParameterType() {
    init("stepResolve_ParameterType");
    checkReference("tod<caret>ay", "step_method");
    checkReference("in<caret>t", "step_method");
    checkReference("floa<caret>t", "step_method");
    checkReference("wor<caret>d", "step_method");
    checkReference("strin<caret>g", "step_string_method");
  }

  public void testResolveOfStepWithAdditionalParameterType() {
    init("stepResolve_ParameterType");
    checkReference("bigin<caret>teger", "step_method");
    checkReference("bigde<caret>cimal", "step_method");
    checkReference("sho<caret>rt", "step_method");
    checkReference("by<caret>te", "step_method");
    checkReference("lo<caret>ng", "step_method");
    checkReference("dou<caret>ble", "step_method");
  }

  public void testResolveOfExpressionWithNotNecessaryGroup() {
    init("stepResolve_ParameterType");

    checkReference("I have 10 cucum<caret>bers in my belly", "iHaveCucumbersInMyBelly");
    checkReference("I have 1 cucumb<caret>er in my belly", "iHaveCucumbersInMyBelly");
  }

  public void testResolveWithDollar() {
    init("stepResolve_ParameterType");

    checkReference("I ha<caret>ve $10", "iHaveDollars");
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CucumberJavaTestUtil.createCucumberJava8ProjectDescriptor();
  }
}
