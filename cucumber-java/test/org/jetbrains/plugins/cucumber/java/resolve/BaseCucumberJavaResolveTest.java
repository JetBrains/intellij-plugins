package org.jetbrains.plugins.cucumber.java.resolve;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;
import org.jetbrains.plugins.cucumber.resolve.CucumberResolveTest;

/**
 * User: Andrey.Vokin
 * Date: 8/9/12
 */
public abstract class BaseCucumberJavaResolveTest extends CucumberResolveTest {
  @Nullable
  @Override
  protected String getStepDefinitionName(@NotNull final PsiElement stepDefinition) {
    if (stepDefinition instanceof PsiMethod) {
      return ((PsiMethod)stepDefinition).getName();
    }
    return null;
  }

  @Override
  protected String getRelatedTestDataPath() {
    return CucumberJavaTestUtil.RELATED_TEST_DATA_PATH + "resolve";
  }
}
