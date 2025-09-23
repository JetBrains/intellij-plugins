package org.jetbrains.plugins.cucumber.java.resolve;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.impl.PomTargetPsiElementImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;
import org.jetbrains.plugins.cucumber.resolve.CucumberResolveTest;

public abstract class BaseCucumberJavaResolveTest extends CucumberResolveTest {
  @Nullable
  @Override
  protected String getStepDefinitionName(@NotNull final PsiElement element) {
    if (element instanceof PsiAnnotation annotation) {
      return PsiTreeUtil.getParentOfType(annotation, PsiMethod.class).getName();
    }
    else if (element instanceof PsiMethodCallExpression methodCallExpression) {
      return methodCallExpression.getMethodExpression().getQualifiedName();
    }
    else if (element instanceof PomTargetPsiElementImpl pomTargetPsiElement) {
      return pomTargetPsiElement.getName();
    }
    return null;
  }

  @Override
  protected String getRelatedTestDataPath() {
    return CucumberJavaTestUtil.RELATED_TEST_DATA_PATH + "resolve";
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CucumberJavaTestUtil.createCucumber2ProjectDescriptor();
  }
}
