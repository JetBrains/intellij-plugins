package org.jetbrains.plugins.cucumber.java.steps;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.ParameterTypeManager;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;

import static org.jetbrains.plugins.cucumber.CucumberUtil.buildRegexpFromCucumberExpression;
import static org.jetbrains.plugins.cucumber.CucumberUtil.isCucumberExpression;
import static org.jetbrains.plugins.cucumber.java.CucumberJavaUtil.getAllParameterTypes;

public class JavaStepDefinition extends AbstractJavaStepDefinition {
  private final String myAnnotationClassName;

  public JavaStepDefinition(@NotNull PsiElement stepDef, @NotNull String annotationClassName) {
    super(stepDef);
    myAnnotationClassName = annotationClassName;
  }

  @Nullable
  @Override
  protected String getCucumberRegexFromElement(PsiElement element) {
    if (!(element instanceof PsiMethod)) {
      return null;
    }
    String patternText = CucumberJavaUtil.getStepAnnotationValue((PsiMethod)element, myAnnotationClassName);
    if (patternText != null &&patternText.length() > 1) {
      final Module module = ModuleUtilCore.findModuleForPsiElement(element);
      if (module != null) {
        ParameterTypeManager parameterTypes = getAllParameterTypes(module);
        String escapedPattern = patternText.replace("\\\\", "\\").replace("\\\"", "\"");
        if (!isCucumberExpression(escapedPattern)) {
          return escapedPattern;
        }
        return buildRegexpFromCucumberExpression(escapedPattern, parameterTypes);
      }
    }

    return null;
  }
}
