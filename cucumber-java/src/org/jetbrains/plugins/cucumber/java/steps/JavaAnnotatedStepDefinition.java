package org.jetbrains.plugins.cucumber.java.steps;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;

public class JavaAnnotatedStepDefinition extends AbstractJavaStepDefinition {
  private final String myAnnotationClassName;

  public JavaAnnotatedStepDefinition(@NotNull PsiElement stepDef, @NotNull String annotationClassName) {
    super(stepDef);
    myAnnotationClassName = annotationClassName;
  }

  @Nullable
  @Override
  public String getStepDefinitionText() {
    PsiElement element = getElement();
    if (element == null) {
      return null;
    }

    if (!(element instanceof PsiMethod)) {
      return null;
    }
    String patternText = CucumberJavaUtil.getStepAnnotationValue((PsiMethod)element, myAnnotationClassName);
    if (patternText != null && patternText.length() > 1) {
      return patternText.replace("\\\\", "\\").replace("\\\"", "\"");
    }
    return null;
  }
}
