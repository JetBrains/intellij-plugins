// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.steps;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;

import java.util.HashSet;
import java.util.Set;

public class JavaAnnotatedStepDefinition extends AbstractJavaStepDefinition {
  private final @NotNull String myAnnotationValue;

  public JavaAnnotatedStepDefinition(@NotNull PsiElement stepDef, @NotNull String annotationValue) {
    super(stepDef);
    myAnnotationValue = annotationValue;
  }

  @Override
  protected @Nullable String getCucumberRegexFromElement(PsiElement element) {
    // NOTE(bartekpacia): Name of this method is invalid because it can return either a regex or a cukex.
    if (!(element instanceof PsiMethod)) return null;
    return myAnnotationValue;
  }

  @Override
  public void setValue(@NotNull String newValue) {
    if (!(getElement() instanceof PsiMethod method)) {
      return;
    }
    final Module module = ModuleUtilCore.findModuleForPsiElement(method);
    if (module == null) {
      return;
    }
    final GlobalSearchScope dependenciesScope = module.getModuleWithDependenciesAndLibrariesScope(true);
    final Set<PsiClass> allStepAnnotations = new HashSet<>(CucumberJavaUtil.getAllStepAnnotationClasses(module, dependenciesScope));

    for (PsiAnnotation annotation : method.getAnnotations()) {
      PsiClass annotationClass = annotation.resolveAnnotationType();
      if (annotationClass == null) continue;
      if (allStepAnnotations.contains(annotationClass)) {
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(method.getProject());
        String newValueEscaped = CucumberJavaUtil.unescapeCucumberRegex(newValue);
        annotation.setDeclaredAttributeValue("value", factory.createExpressionFromText("\"" + newValueEscaped + "\"", null));
      }
    }
  }

  @Override
  public String toString() {
    return "JavaAnnotatedStepDefinition{stepDef: " + getElement() + "}";
  }
}
