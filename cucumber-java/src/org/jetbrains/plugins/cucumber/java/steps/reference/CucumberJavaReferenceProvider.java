// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.steps.reference;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.manipulators.StringLiteralManipulator;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberUtil;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.intellij.codeInsight.AnnotationUtil.CHECK_TYPE;
import static org.jetbrains.plugins.cucumber.java.CucumberJavaExtension.CUCUMBER_JAVA_STEP_DEFINITION_ANNOTATION_CLASSES;

public class CucumberJavaReferenceProvider extends PsiReferenceProvider {
  @Override
  public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    if (!(element instanceof PsiLiteralExpression)) {
      return PsiReference.EMPTY_ARRAY;
    }
    PsiLiteralExpression literalExpression = (PsiLiteralExpression)element;
    Object value = literalExpression.getValue();
    if (!(value instanceof String)) {
      return PsiReference.EMPTY_ARRAY;
    }

    PsiAnnotation annotation = PsiTreeUtil.getParentOfType(element, PsiAnnotation.class);
    if (annotation == null) {
      return PsiReference.EMPTY_ARRAY;
    }
    PsiJavaCodeReferenceElement annotationRefElement = annotation.getNameReferenceElement();
    if (annotationRefElement == null) {
      return PsiReference.EMPTY_ARRAY;
    }
    PsiReference annotationClassReference = annotationRefElement.getReference();
    if (annotationClassReference == null) {
      return PsiReference.EMPTY_ARRAY;
    }
    PsiElement annotationClass = annotationClassReference.resolve();
    if (!(annotationClass instanceof PsiModifierListOwner)
            || !AnnotationUtil.isAnnotated((PsiModifierListOwner)annotationClass, Arrays.asList(CUCUMBER_JAVA_STEP_DEFINITION_ANNOTATION_CLASSES), CHECK_TYPE)) {
      return PsiReference.EMPTY_ARRAY;
    }

    PsiMethod method = PsiTreeUtil.getParentOfType(annotation, PsiMethod.class);
    if (method == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    PsiAnnotation stepAnnotation = CucumberJavaUtil.getCucumberStepAnnotation(method);
    if (stepAnnotation == null || !PsiTreeUtil.isAncestor(stepAnnotation, literalExpression, true)) {
      return PsiReference.EMPTY_ARRAY;
    }
    String cucumberExpression = CucumberJavaUtil.getAnnotationValue(stepAnnotation);
    if (cucumberExpression == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    List<CucumberJavaParameterTypeReference> result = new ArrayList<>();
    CucumberUtil.processParameterTypesInCucumberExpression(literalExpression.getValue().toString(), range -> {
      // Skip " in the begin of the String Literal
      range = range.shiftRight(StringLiteralManipulator.getValueRange(literalExpression).getStartOffset());
      result.add(new CucumberJavaParameterTypeReference(element, range));
      return true;
    });
    return result.toArray(new CucumberJavaParameterTypeReference[0]);
  }
}
