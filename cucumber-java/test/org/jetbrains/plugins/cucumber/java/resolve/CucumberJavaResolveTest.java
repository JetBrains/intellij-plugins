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
  public void testNavigationFromStepToStepDef() throws Exception {
    doTest("stepResolve_01", "I p<caret>ay 25", "i_pay");
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
