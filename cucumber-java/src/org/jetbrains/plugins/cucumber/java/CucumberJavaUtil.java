package org.jetbrains.plugins.cucumber.java;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.java.steps.reference.CucumberJavaAnnotationProvider;

/**
 * User: Andrey.Vokin
 * Date: 7/25/12
 */
public class CucumberJavaUtil {
  public static final String CUCUMBER_ANNOTATION_PREFIX = "cucumber.annotation.";

  public static boolean isCucumberAnnotation(@NotNull final PsiAnnotation annotation) {
    final Ref<String> qualifiedAnnotationName = new Ref<String>();
    ApplicationManager.getApplication().runReadAction(new Runnable() {
      public void run() {
        String qualifiedName = annotation.getQualifiedName();
        qualifiedAnnotationName.set(qualifiedName);
      }
    }
    );

    if (qualifiedAnnotationName.get() == null) {
      return false;
    }
    String name = qualifiedAnnotationName.get();
    if (name.startsWith(CUCUMBER_ANNOTATION_PREFIX) && name.substring(CUCUMBER_ANNOTATION_PREFIX.length()).contains(".")) {
      return true;
    } else {
      for (String providedAnnotations : CucumberJavaAnnotationProvider.getCucumberAnnotations()) {
        if (providedAnnotations.equals(qualifiedAnnotationName.get())) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean isStepDefinition(@NotNull final PsiElement element) {
    if (!(element instanceof PsiMethod)) {
      return false;
    }

    PsiMethod method = (PsiMethod)element;
    if (getCucumberAnnotation(method) != null) return true;

    return false;
  }

  @Nullable
  public static PsiAnnotation getCucumberAnnotation(PsiMethod method) {
    final PsiAnnotation[] annotations = method.getModifierList().getAnnotations();

    for (PsiAnnotation annotation : annotations) {
      if (annotation != null && CucumberJavaUtil.isCucumberAnnotation(annotation)) {
        return annotation;
      }
    }
    return null;
  }

  @Nullable
  public static String getPatternFromStepDefinition(@NotNull final PsiAnnotation stepAnnotation) {
    String result = null;
    if (stepAnnotation.getParameterList().getAttributes().length > 0) {
      final PsiElement annotationValue = stepAnnotation.getParameterList().getAttributes()[0].getValue();
      if (annotationValue != null) {
        final PsiElement patternLiteral = annotationValue.getFirstChild();
        if (patternLiteral != null) {
          final String patternContainer = patternLiteral.getText();
          result =  patternContainer.substring(1, patternContainer.length() - 1).replace("\\\\", "\\");
        }
      }
    }
    return result;
  }
}
