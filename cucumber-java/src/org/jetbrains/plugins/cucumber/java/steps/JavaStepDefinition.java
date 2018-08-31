package org.jetbrains.plugins.cucumber.java.steps;

import com.intellij.find.findUsages.JavaFindUsagesHelper;
import com.intellij.find.findUsages.JavaMethodFindUsagesOptions;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.ClassUtil;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.CommonProcessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;

import java.util.HashMap;
import java.util.Map;

import static org.jetbrains.plugins.cucumber.CucumberUtil.STANDARD_PARAMETER_TYPES;

public class JavaStepDefinition extends AbstractJavaStepDefinition {
  public static final String PARAMETER_TYPE_CLASS = "io.cucumber.cucumberexpressions.ParameterType";
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
    final PsiAnnotation stepAnnotation = CucumberJavaUtil.getCucumberStepAnnotation((PsiMethod)element, myAnnotationClassName);
    if (stepAnnotation == null) {
      return null;
    }
    final PsiElement annotationValue = CucumberJavaUtil.getAnnotationValue(stepAnnotation);
    if (annotationValue == null) {
      return null;
    }
    Project project = element.getProject();
    final PsiConstantEvaluationHelper evaluationHelper = JavaPsiFacade.getInstance(project).getConstantEvaluationHelper();
    final Object constantValue = evaluationHelper.computeConstantExpression(annotationValue, false);
    if (constantValue != null) {
      String patternText = constantValue.toString();
      if (patternText.length() > 1) {
        final Module module = ModuleUtilCore.findModuleForPsiElement(element);
        if (module != null) {
          return processCucumberExpressions(module, patternText.replace("\\\\", "\\").replace("\\\"", "\""));
        }
      }
    }

    return null;
  }

  @Nullable
  private static String processCucumberExpressions(@NotNull Module module, String annotationValue) {
    Map<String, String> parameterTypes = getAllParameterTypes(module);
    
    StringBuilder result = new StringBuilder();
    int i = 0;
    while (i < annotationValue.length()) {
      char c = annotationValue.charAt(i);
      if (c == '{') {
        int j = i;
        while (j < annotationValue.length()) {
          char parameterTypeChar = annotationValue.charAt(j);
          if (parameterTypeChar == '}') {
            break;
          }
          if (parameterTypeChar == '\\') {
            j++;
          }
          j++;
        }
        if (j < annotationValue.length()) {
          String parameterTypeName = annotationValue.substring(i + 1, j);
          String parameterTypeValue = parameterTypes.get(parameterTypeName);
          if (parameterTypeValue == null) {
            return null;
          } else {
            result.append(parameterTypeValue);
          }
          i = j + 1;
          continue;
        } else {
          // unclosed parameter type
          return null;
        }
      }

      result.append(c);
      if (c == '\\') {
        if (i >= annotationValue.length() - 1) {
          // escape without following symbol;
          return null;
        }
        i++;
        result.append(annotationValue.charAt(i));
      }
      i++;
    }
    return result.toString();
  }

  private static Map<String, String> getAllParameterTypes(@NotNull Module module) {
    Project project = module.getProject();
    PsiManager manager = PsiManager.getInstance(project);

    VirtualFile projectDir = ProjectUtil.guessProjectDir(project);
    PsiDirectory psiDirectory = manager.findDirectory(projectDir);
    if (psiDirectory != null) {
      return CachedValuesManager.getCachedValue(psiDirectory, () ->
        CachedValueProvider.Result.create(doGetAllParameterTypes(module), PsiModificationTracker.MODIFICATION_COUNT));
    }

    return STANDARD_PARAMETER_TYPES;
  }

  @NotNull
  private static Map<String, String> doGetAllParameterTypes(@NotNull Module module) {
    final GlobalSearchScope dependenciesScope = module.getModuleWithDependenciesAndLibrariesScope(true);
    CommonProcessors.CollectProcessor<UsageInfo> processor = new CommonProcessors.CollectProcessor<>();
    JavaMethodFindUsagesOptions options = new JavaMethodFindUsagesOptions(dependenciesScope);

    PsiClass parameterTypeClass = ClassUtil.findPsiClass(PsiManager.getInstance(module.getProject()), PARAMETER_TYPE_CLASS);
    if (parameterTypeClass != null) {
      for (PsiMethod constructor: parameterTypeClass.getConstructors()) {
        JavaFindUsagesHelper.processElementUsages(constructor, options, processor);
      }
    }

    Map<String, String> result = new HashMap<>();
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
            
            result.put(name, value);
          }
        }
      }
    }

    result.putAll(STANDARD_PARAMETER_TYPES);
    return result;
  }
}
