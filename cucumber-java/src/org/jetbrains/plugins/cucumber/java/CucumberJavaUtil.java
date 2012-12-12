package org.jetbrains.plugins.cucumber.java;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.java.steps.reference.CucumberJavaAnnotationProvider;
import org.jetbrains.plugins.cucumber.psi.*;

import java.util.List;

import static com.intellij.psi.util.PsiTreeUtil.getChildOfType;
import static com.intellij.psi.util.PsiTreeUtil.getChildrenOfTypeAsList;

/**
 * User: Andrey.Vokin
 * Date: 7/25/12
 */
public class CucumberJavaUtil {
  public static final String CUCUMBER_STEP_ANNOTATION_PREFIX_1_0 = "cucumber.annotation.";
  public static final String CUCUMBER_STEP_ANNOTATION_PREFIX_1_1 = "cucumber.api.java.";

  public static boolean isUnderTestSources(@NotNull final PsiElement element) {
    final ProjectRootManager rootManager = ProjectRootManager.getInstance(element.getProject());
    final VirtualFile file = element.getContainingFile().getVirtualFile();
    return file != null && rootManager.getFileIndex().isInTestSourceContent(file);
  }

  private static String getCucumberAnnotationSuffix(@NotNull String name) {
    if (name.startsWith(CUCUMBER_STEP_ANNOTATION_PREFIX_1_0)) {
      return name.substring(CUCUMBER_STEP_ANNOTATION_PREFIX_1_0.length());
    }
    else if (name.startsWith(CUCUMBER_STEP_ANNOTATION_PREFIX_1_1)) {
      return name.substring(CUCUMBER_STEP_ANNOTATION_PREFIX_1_1.length());
    } else {
      name = "";
    }

    return name;
  }

  private static String getAnnotationName(@NotNull final PsiAnnotation annotation) {
    final Ref<String> qualifiedAnnotationName = new Ref<String>();
    ApplicationManager.getApplication().runReadAction(new Runnable() {
      public void run() {
        String qualifiedName = annotation.getQualifiedName();
        qualifiedAnnotationName.set(qualifiedName);
      }
    }
    );
    return qualifiedAnnotationName.get();
  }

  public static boolean isCucumberStepAnnotation(@NotNull final PsiAnnotation annotation) {
    final String annotationName = getAnnotationName(annotation);

    final String annotationSuffix = getCucumberAnnotationSuffix(annotationName);
    if (annotationSuffix.contains(".")) {
      return true;
    }
    for (String providedAnnotations : CucumberJavaAnnotationProvider.getCucumberStepAnnotations()) {
      if (providedAnnotations.equals(annotationName)) {
        return true;
      }
    }
    return false;
  }

  public static boolean isCucumberHookAnnotation(@NotNull final PsiAnnotation annotation) {
    final String annotationName = getAnnotationName(annotation);
    final String annotationSuffix = getCucumberAnnotationSuffix(annotationName);
    for (String providedAnnotations : CucumberJavaAnnotationProvider.getCucumberHookAnnotations()) {
      if (providedAnnotations.equals(annotationSuffix)) {
        return true;
      }
    }
    return false;
  }

  public static boolean isStepDefinition(@NotNull final PsiMethod method) {
    return getCucumberStepAnnotation(method) != null;
  }

  public static boolean isHook(@NotNull final PsiMethod method) {
    return getCucumberHookAnnotation(method) != null;
  }

  public static boolean isStepDefinitionClass(@NotNull final PsiClass clazz) {
    PsiMethod[] methods = clazz.getAllMethods();
    for (PsiMethod method : methods) {
      if (getCucumberStepAnnotation(method) != null || getCucumberHookAnnotation(method) != null) return true;
    }
    return false;
  }

  @Nullable
  public static PsiAnnotation getCucumberStepAnnotation(PsiMethod method) {
    final PsiAnnotation[] annotations = method.getModifierList().getAnnotations();

    for (PsiAnnotation annotation : annotations) {
      if (annotation != null && isCucumberStepAnnotation(annotation)) {
        return annotation;
      }
    }
    return null;
  }

  @Nullable
  public static PsiAnnotation getCucumberHookAnnotation(PsiMethod method) {
    final PsiAnnotation[] annotations = method.getModifierList().getAnnotations();

    for (PsiAnnotation annotation : annotations) {
      if (annotation != null && isCucumberHookAnnotation(annotation)) {
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
          result = patternContainer.substring(1, patternContainer.length() - 1).replace("\\\\", "\\");
        }
      }
    }
    return result;
  }

  @Nullable
  private static String getPackageOfStepDef(GherkinStep[] steps) {
    for (GherkinStep step : steps) {
      final String pack = getPackageOfStep(step);
      if (pack != null) return pack;
    }
    return null;
  }

  @NotNull
  public static String getPackageOfStepDef(final PsiElement element) {
    PsiFile file = element.getContainingFile();
    if (file instanceof GherkinFile) {
      GherkinFeature feature = getChildOfType(file, GherkinFeature.class);
      if (feature != null) {
        List<GherkinScenario> scenarioList = getChildrenOfTypeAsList(feature, GherkinScenario.class);
        for (GherkinScenario scenario : scenarioList) {
          String result = getPackageOfStepDef(scenario.getSteps());
          if (result != null) {
            return result;
          }
        }

        List<GherkinScenarioOutline> scenarioOutlineList = getChildrenOfTypeAsList(feature, GherkinScenarioOutline.class);
        for (GherkinScenarioOutline scenario : scenarioOutlineList) {
          String result = getPackageOfStepDef(scenario.getSteps());
          if (result != null) {
            return result;
          }
        }
      }
    }
    return "";
  }

  public static String getPackageOfStep(GherkinStep step) {
    for (PsiReference ref : step.getReferences()) {
      PsiElement refElement = ref.resolve();
      if (refElement != null && refElement instanceof PsiMethod) {
        PsiJavaFile javaFile = (PsiJavaFile)refElement.getContainingFile();
        final String packageName = javaFile.getPackageName();
        if (StringUtil.isNotEmpty(packageName)) {
          return packageName;
        }
      }
    }
    return null;
  }
}
