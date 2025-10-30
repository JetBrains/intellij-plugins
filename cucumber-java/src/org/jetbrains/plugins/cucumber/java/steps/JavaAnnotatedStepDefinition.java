// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.steps;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;

import java.util.*;

@NotNullByDefault
public class JavaAnnotatedStepDefinition extends AbstractJavaStepDefinition {

  public JavaAnnotatedStepDefinition(PsiAnnotation element) {
    super(element);
  }

  public static JavaAnnotatedStepDefinition create(PsiAnnotation element) {
    final JavaAnnotatedStepDefinition stepDefinition = CachedValuesManager.getCachedValue(element, () -> {
      return CachedValueProvider.Result.create(new JavaAnnotatedStepDefinition(element), element);
    });
    return stepDefinition;
  }

  @Override
  protected @Nullable String getCucumberRegexFromElement(@Nullable PsiElement element) {
    // NOTE(bartekpacia): This implementation doesn't conform to this method's name because it can return either a regex or a cukex.
    //  However, it has been like this for many years, and it seems to work fine. If possible, consider refactoring in the future.
    if (!(element instanceof PsiAnnotation annotation)) return null;
    return CucumberJavaUtil.getAnnotationValue(annotation);
  }

  @Override
  public void setValue(String newValue) {
    if (!(getElement() instanceof PsiAnnotation annotation)) {
      return;
    }
    final Module module = ModuleUtilCore.findModuleForPsiElement(annotation);
    if (module == null) {
      return;
    }
    final GlobalSearchScope dependenciesScope = module.getModuleWithDependenciesAndLibrariesScope(true);
    final Set<PsiClass> allStepAnnotations = new HashSet<>(CucumberJavaUtil.getAllStepAnnotationClasses(module, dependenciesScope));
    final PsiClass annotationClass = annotation.resolveAnnotationType();
    if (annotationClass == null) {
      return;
    }
    if (allStepAnnotations.contains(annotationClass)) {
      PsiElementFactory factory = JavaPsiFacade.getElementFactory(annotation.getProject());
      String newValueEscaped = CucumberJavaUtil.unescapeCucumberRegex(newValue);
      annotation.setDeclaredAttributeValue("value", factory.createExpressionFromText("\"" + newValueEscaped + "\"", null));
    }
  }

  @Override
  public @Nullable PsiAnnotation getElement() {
    final PsiElement element = super.getElement();
    if (element == null) return null;
    return (PsiAnnotation)element;
  }

  @Override
  public List<String> getVariableNames() {
    PsiElement element = getElement();
    if (element instanceof PsiAnnotation annotation) {
      PsiMethod method = PsiTreeUtil.getParentOfType(annotation, PsiMethod.class);
      if (method == null) {
        return Collections.emptyList();
      }
      PsiParameter[] parameters = method.getParameterList().getParameters();
      ArrayList<String> result = new ArrayList<>();
      for (PsiParameter parameter : parameters) {
        result.add(parameter.getName());
      }
      return result;
    }
    return Collections.emptyList();
  }

  @Override
  public String toString() {
    return "JavaAnnotatedStepDefinition{backed by element: " + getElement() + "}";
  }
}
