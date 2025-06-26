// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.steps;

import com.intellij.openapi.module.Module;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;

public class JavaAnnotatedStepDefinition extends AbstractJavaStepDefinition {
  private final @NotNull String myAnnotationValue;

  public JavaAnnotatedStepDefinition(@NotNull PsiElement stepDef, @NotNull Module module, @NotNull String annotationValue) {
    super(stepDef, module);
    myAnnotationValue = annotationValue;
  }

  @Override
  protected @Nullable String getCucumberRegexFromElement(PsiElement element) {
    // NOTE(bartekpacia): Name of this method is invalid because it can return either a regex or a cukex.
    if (!(element instanceof PsiMethod)) return null;
    if (myAnnotationValue.length() > 1) {
      return CucumberJavaUtil.escapeCucumberRegex(myAnnotationValue);
    }
    return null;
  }

  private static final String[] CUCUMBER_ANNOTATION_NAMES = {
    "io.cucumber.java.en.Given",
    "io.cucumber.java.en.When",
    "io.cucumber.java.en.Then"
  };

  @Override
  public void setValue(@NotNull String newValue) {
    if (!(getElement() instanceof PsiMethod method)) return;
    final PsiAnnotation[] annotations = method.getAnnotations();

    for (PsiAnnotation annotation : annotations) {
      String qualifiedName = annotation.getQualifiedName();
      if (qualifiedName != null) {
        for (String cucumberAnnotation : CUCUMBER_ANNOTATION_NAMES) {
          if (qualifiedName.equals(cucumberAnnotation)) {
            PsiElementFactory factory = JavaPsiFacade.getElementFactory(getElement().getProject());
            String newValueEscaped = CucumberJavaUtil.unescapeCucumberRegex(newValue);
            annotation.setDeclaredAttributeValue("value", factory.createExpressionFromText("\"" + newValueEscaped + "\"", null));
          }
        }
      }
    }
  }

  @Override
  public String toString() {
    return "JavaAnnotatedStepDefinition{stepDef: " + getElement() + "}";
  }
}
