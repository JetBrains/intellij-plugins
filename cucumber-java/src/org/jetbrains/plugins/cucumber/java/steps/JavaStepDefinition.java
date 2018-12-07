package org.jetbrains.plugins.cucumber.java.steps;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.ParameterTypeManager;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;
import org.jetbrains.plugins.cucumber.java.CucumberJavaVersionUtil;

import static org.jetbrains.plugins.cucumber.CucumberUtil.buildRegexpFromCucumberExpression;
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
    String definitionText = getStepDefinitionText();
    if (definitionText == null) {
      return null;
    }
    final Module module = ModuleUtilCore.findModuleForPsiElement(element);
    if (module != null) {
      ParameterTypeManager parameterTypes = getAllParameterTypes(module);
      if (!CucumberJavaVersionUtil.isCucumber3OrMore(element)) {
        return definitionText;
      }
      return buildRegexpFromCucumberExpression(definitionText, parameterTypes);
    }

    return definitionText;
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
