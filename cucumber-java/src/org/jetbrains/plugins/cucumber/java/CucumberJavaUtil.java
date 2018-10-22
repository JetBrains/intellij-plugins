package org.jetbrains.plugins.cucumber.java;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.execution.PsiLocation;
import com.intellij.execution.junit2.info.LocationUtil;
import com.intellij.find.findUsages.JavaFindUsagesHelper;
import com.intellij.find.findUsages.JavaMethodFindUsagesOptions;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.ClassUtil;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.CommonProcessors;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.MapParameterTypeManager;
import org.jetbrains.plugins.cucumber.java.config.CucumberConfigUtil;
import org.jetbrains.plugins.cucumber.java.steps.reference.CucumberJavaAnnotationProvider;
import org.jetbrains.plugins.cucumber.psi.*;

import java.util.*;

import static com.intellij.psi.util.PsiTreeUtil.getChildOfType;
import static com.intellij.psi.util.PsiTreeUtil.getChildrenOfTypeAsList;
import static org.jetbrains.plugins.cucumber.CucumberUtil.STANDARD_PARAMETER_TYPES;
import static org.jetbrains.plugins.cucumber.MapParameterTypeManager.DEFAULT;

public class CucumberJavaUtil {
  public static final String CUCUMBER_STEP_ANNOTATION_PREFIX_1_0 = "cucumber.annotation.";
  public static final String CUCUMBER_STEP_ANNOTATION_PREFIX_1_1 = "cucumber.api.java.";

  public static final String PARAMETER_TYPE_CLASS = "io.cucumber.cucumberexpressions.ParameterType";

  private static final Map<String, String> JAVA_PARAMETER_TYPES;
  public static final String CUCUMBER_EXPRESSIONS_CLASS_MARKER = "io.cucumber.cucumberexpressions.CucumberExpressionGenerator";

  static {
    Map<String, String> javaParameterTypes = new HashMap<>();
    javaParameterTypes.put("short", STANDARD_PARAMETER_TYPES.get("int"));
    javaParameterTypes.put("biginteger", STANDARD_PARAMETER_TYPES.get("int"));
    javaParameterTypes.put("bigdecimal", "-?\\d*[.,]\\d+");
    javaParameterTypes.put("byte", STANDARD_PARAMETER_TYPES.get("int"));
    javaParameterTypes.put("double", STANDARD_PARAMETER_TYPES.get("float"));
    javaParameterTypes.put("long", STANDARD_PARAMETER_TYPES.get("int"));

    JAVA_PARAMETER_TYPES = Collections.unmodifiableMap(javaParameterTypes);
  }

  private static String getCucumberAnnotationSuffix(@NotNull String name) {
    if (name.startsWith(CUCUMBER_STEP_ANNOTATION_PREFIX_1_0)) {
      return name.substring(CUCUMBER_STEP_ANNOTATION_PREFIX_1_0.length());
    }
    else if (name.startsWith(CUCUMBER_STEP_ANNOTATION_PREFIX_1_1)) {
      return name.substring(CUCUMBER_STEP_ANNOTATION_PREFIX_1_1.length());
    } else {
      return "";
    }
  }

  public static String getCucumberPendingExceptionFqn(@NotNull final PsiElement context) {
    final String version = CucumberConfigUtil.getCucumberCoreVersion(context);
    if (version == null || version.compareTo(CucumberConfigUtil.CUCUMBER_VERSION_1_1) >= 0) {
      return "cucumber.api.PendingException";
    }
    return "cucumber.runtime.PendingException";
  }

  @Nullable
  private static String getAnnotationName(@NotNull final PsiAnnotation annotation) {
    final Ref<String> qualifiedAnnotationName = new Ref<>();
    ApplicationManager.getApplication().runReadAction(() -> {
      String qualifiedName = annotation.getQualifiedName();
      qualifiedAnnotationName.set(qualifiedName);
    }
    );
    return qualifiedAnnotationName.get();
  }

  public static boolean isCucumberStepAnnotation(@NotNull final PsiAnnotation annotation) {
    final String annotationName = getAnnotationName(annotation);
    if (annotationName == null) return false;

    final String annotationSuffix = getCucumberAnnotationSuffix(annotationName);
    if (annotationSuffix.contains(".")) {
      return true;
    }
    return CucumberJavaAnnotationProvider.STEP_MARKERS.contains(annotationName);
  }

  public static boolean isCucumberHookAnnotation(@NotNull final PsiAnnotation annotation) {
    final String annotationName = getAnnotationName(annotation);
    if (annotationName == null) return false;

    final String annotationSuffix = getCucumberAnnotationSuffix(annotationName);
    return CucumberJavaAnnotationProvider.HOOK_MARKERS.contains(annotationSuffix);
  }

  public static boolean isStepDefinition(@NotNull final PsiMethod method) {
    final PsiAnnotation stepAnnotation = getCucumberStepAnnotation(method);
    return stepAnnotation != null && getAnnotationValue(stepAnnotation) != null;
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

  public static PsiAnnotation getCucumberStepAnnotation(@NotNull PsiMethod method) {
    return getCucumberStepAnnotation(method, null);
  }

  @Nullable
  public static PsiAnnotation getCucumberStepAnnotation(@NotNull PsiMethod method, @Nullable String annotationClassName) {
    if (!method.hasModifierProperty(PsiModifier.PUBLIC)) {
      return null;
    }

    final PsiAnnotation[] annotations = method.getModifierList().getAnnotations();

    for (PsiAnnotation annotation : annotations) {
      if (annotation != null &&
          (annotationClassName == null || annotationClassName.equals(annotation.getQualifiedName())) &&
          isCucumberStepAnnotation(annotation)) {
        return annotation;
      }
    }
    return null;
  }

  /**
   * Computes value of Step Definition Annotation. If {@code annotationClassName provided} value of the annotation with corresponding class
   * will be returned. Operations with string constants handled.
   */
  @Nullable
  public static String getStepAnnotationValue(@NotNull PsiMethod method, @Nullable String annotationClassName) {
    final PsiAnnotation stepAnnotation = getCucumberStepAnnotation(method, annotationClassName);
    if (stepAnnotation == null) {
      return null;
    }

    return getAnnotationValue(stepAnnotation);
  }

  @Nullable
  public static String getAnnotationValue(@NotNull PsiAnnotation stepAnnotation) {
    return AnnotationUtil.getDeclaredStringAttributeValue(stepAnnotation, "value");
  }

  @Nullable
  public static PsiAnnotation getCucumberHookAnnotation(PsiMethod method) {
    if (!method.hasModifierProperty(PsiModifier.PUBLIC)) {
      return null;
    }

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
    return AnnotationUtil.getStringAttributeValue(stepAnnotation, null);
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
      if (refElement instanceof PsiMethod || refElement instanceof PsiMethodCallExpression) {
        PsiClassOwner file = (PsiClassOwner)refElement.getContainingFile();
        final String packageName = file.getPackageName();
        if (StringUtil.isNotEmpty(packageName)) {
          return packageName;
        }
      }
    }
    return null;
  }

  public static void addGlue(String glue, Set<String> glues) {
    boolean covered = false;
    final Set<String> toRemove = ContainerUtil.newHashSet();
    for (String existedGlue : glues) {
      if (glue.startsWith(existedGlue + ".")) {
        covered = true;
        break;
      }
      else if (existedGlue.startsWith(glue + ".")) {
        toRemove.add(existedGlue);
      }
    }

    for (String removing : toRemove) {
      glues.remove(removing);
    }

    if (!covered) {
      glues.add(glue);
    }
  }

  public static MapParameterTypeManager getAllParameterTypes(@NotNull Module module) {
    Project project = module.getProject();
    PsiManager manager = PsiManager.getInstance(project);

    VirtualFile projectDir = ProjectUtil.guessProjectDir(project);
    PsiDirectory psiDirectory = projectDir != null ? manager.findDirectory(projectDir) : null;
    if (psiDirectory != null) {
      return CachedValuesManager.getCachedValue(psiDirectory, () ->
        CachedValueProvider.Result.create(doGetAllParameterTypes(module), PsiModificationTracker.MODIFICATION_COUNT));
    }

    return DEFAULT;
  }

  @NotNull
  private static MapParameterTypeManager doGetAllParameterTypes(@NotNull Module module) {
    final GlobalSearchScope dependenciesScope = module.getModuleWithDependenciesAndLibrariesScope(true);
    CommonProcessors.CollectProcessor<UsageInfo> processor = new CommonProcessors.CollectProcessor<>();
    JavaMethodFindUsagesOptions options = new JavaMethodFindUsagesOptions(dependenciesScope);

    PsiClass parameterTypeClass = ClassUtil.findPsiClass(PsiManager.getInstance(module.getProject()), PARAMETER_TYPE_CLASS);
    if (parameterTypeClass != null) {
      for (PsiMethod constructor: parameterTypeClass.getConstructors()) {
        JavaFindUsagesHelper.processElementUsages(constructor, options, processor);
      }
    }

    SmartPointerManager smartPointerManager = SmartPointerManager.getInstance(module.getProject());
    Map<String, String> values = new HashMap<>();
    Map<String, SmartPsiElementPointer<PsiElement>> declarations = new HashMap<>();
    for (UsageInfo ui: processor.getResults()) {
      PsiElement element = ui.getElement();
      if (element != null && element.getParent() instanceof PsiNewExpression) {
        PsiNewExpression newExpression = (PsiNewExpression)element.getParent();
        PsiExpressionList arguments = newExpression.getArgumentList();
        if (arguments != null) {
          PsiExpression[] expressions = arguments.getExpressions();
          if (expressions.length > 1) {
            PsiConstantEvaluationHelper evaluationHelper = JavaPsiFacade.getInstance(module.getProject()).getConstantEvaluationHelper();

            Object constantValue = evaluationHelper.computeConstantExpression(expressions[0], false);
            if (constantValue == null) {
              continue;
            }
            String name = constantValue.toString();

            constantValue = evaluationHelper.computeConstantExpression(expressions[1], false);
            if (constantValue == null) {
              continue;
            }
            String value = constantValue.toString();
            values.put(name, value);

            SmartPsiElementPointer<PsiElement> smartPointer = smartPointerManager.createSmartPsiElementPointer(expressions[0]);
            declarations.put(name, smartPointer);
          }
        }
      }
    }

    values.putAll(STANDARD_PARAMETER_TYPES);
    values.putAll(JAVA_PARAMETER_TYPES);
    return new MapParameterTypeManager(values, declarations);
  }

  /**
   * Checks if library with CucumberExpressions library attached to the project.
   * @return true if step definitions should be written in Cucumber Expressions (since Cucumber v 3.0),
   * false in case of old-style Regexp step definitions.
   */
  public static boolean isCucumberExpressionsAvailable(@NotNull PsiElement context) {
    PsiLocation<PsiElement> location = new PsiLocation<>(context);
    return LocationUtil.isJarAttached(location, PsiDirectory.EMPTY_ARRAY, CUCUMBER_EXPRESSIONS_CLASS_MARKER);
  }
}
