package org.jetbrains.plugins.cucumber.java.resolve;

import com.intellij.openapi.application.PathManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.resolve.CucumberResolveTest;

/**
 * User: Andrey.Vokin
 * Date: 7/20/12
 */
public class CucumberJavaResolveTest extends CucumberResolveTest {
  public void testNavigationFromStepToStepDef01() throws Exception {
    doTest("stepResolve_01", "I p<caret>ay 25", "i_pay");
  }
  public void testNavigationFromStepToStepDef02() throws Exception {
    doTest("stepResolve_01", "the followi<caret>ng groceries", "the_following_groceries");
  }
  public void testNavigationFromStepToStepDef03() throws Exception {
    doTest("stepResolve_01", "my change sh<caret>ould be 4", "my_change_should_be_");
  }

  @Nullable
  @Override
  protected String getStepDefinitionName(PsiElement stepDefinition) {
    if (stepDefinition instanceof PsiMethod) {
      return ((PsiMethod)stepDefinition).getName();
    }
    return null;
  }

  @Override
  protected String getTestDataPath() {
    return PathManager.getHomePath() + "\\contrib\\cucumber-java\\testData\\resolve\\";
  }
}
