package org.jetbrains.plugins.cucumber.java.steps;

import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JavaAnnotatedStepDefinition extends AbstractJavaStepDefinition {
  private final @NotNull String myAnnotationValue;

  public JavaAnnotatedStepDefinition(@NotNull PsiElement stepDef, @NotNull Module module, @NotNull String annotationValue) {
    super(stepDef, module);
    myAnnotationValue = annotationValue;
  }

  @Nullable
  @Override
  protected String getCucumberRegexFromElement(PsiElement element) {
    if (element == null) {
      return null;
    }

    if (!(element instanceof PsiMethod)) {
      return null;
    }
    if (myAnnotationValue.length() > 1) {
      return myAnnotationValue.replace("\\\\", "\\").replace("\\\"", "\"");
    }
    return null;
  }
}
