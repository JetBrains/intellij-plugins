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
  public static final String CUCUMBER_ANNOTATION_PREFIX = "cucumber.annotation.";

  public static boolean isUnderTestSources(@NotNull final PsiElement element) {
    final ProjectRootManager rootManager = ProjectRootManager.getInstance(element.getProject());
    final VirtualFile file = element.getContainingFile().getVirtualFile();
    return file != null && rootManager.getFileIndex().isInTestSourceContent(file);
  }

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

  public static boolean isStepDefinition(@NotNull final PsiMethod method) {
    return getCucumberAnnotation(method) != null;
  }

  public static boolean isStepDefinitionClass(@NotNull final PsiClass clazz) {
    PsiMethod[] methods = clazz.getAllMethods();
    for (PsiMethod method : methods) {
      if (getCucumberAnnotation(method) != null) return true;
    }
    return false;
  }

  @Nullable
  public static PsiAnnotation getCucumberAnnotation(PsiMethod method) {
    final PsiAnnotation[] annotations = method.getModifierList().getAnnotations();

    for (PsiAnnotation annotation : annotations) {
      if (annotation != null && isCucumberAnnotation(annotation)) {
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

  @Nullable
  private static String getPackageOfStepDef(GherkinStep[] steps) {
    for (GherkinStep step : steps) {
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
    }
    return null;
  }

  @Nullable
  public static String getPackageOfStepDef(final PsiElement element) {
    PsiFile file = element.getContainingFile();
    if (file instanceof GherkinFile) {
      GherkinFeature feature = getChildOfType(file, GherkinFeature.class);
      if (feature != null) {
        List<GherkinScenario> scenarioList = getChildrenOfTypeAsList(feature, GherkinScenario.class);
        for(GherkinScenario scenario : scenarioList) {
          String result = getPackageOfStepDef(scenario.getSteps());
          if (result != null) {
            return result;
          }
        }

        List<GherkinScenarioOutline> scenarioOutlineList = getChildrenOfTypeAsList(feature, GherkinScenarioOutline.class);
        for(GherkinScenarioOutline scenario : scenarioOutlineList) {
          String result = getPackageOfStepDef(scenario.getSteps());
          if (result != null) {
            return result;
          }
        }
      }
    }
    return null;
  }

  public static String getGlue(@NotNull final PsiElement step) {
    final String packageName = getPackageOfStepDef(step);
    if (packageName != null) {
      return " --glue " + packageName;
    } else {
      return "";
    }
  }
}
