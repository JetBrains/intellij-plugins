package org.jetbrains.plugins.cucumber.java;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaFullClassNameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.intellij.util.Query;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.StepDefinitionCreator;
import org.jetbrains.plugins.cucumber.java.steps.JavaStepDefinition;
import org.jetbrains.plugins.cucumber.java.steps.JavaStepDefinitionCreator;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.jetbrains.plugins.cucumber.psi.GherkinRecursiveElementVisitor;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.cucumber.steps.AbstractCucumberExtension;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;

import java.util.*;

/**
 * User: Andrey.Vokin
 * Date: 7/16/12
 */
public class CucumberJavaExtension extends AbstractCucumberExtension {
  public static final String CUCUMBER_RUNTIME_JAVA_STEP_DEF_ANNOTATION = "cucumber.runtime.java.StepDefAnnotation";

  @Override
  public boolean isStepLikeFile(@NotNull final PsiElement child, @NotNull final PsiElement parent) {
    if (child instanceof PsiJavaFile) {
      return true;
    }
    return false;
  }

  @Override
  public boolean isWritableStepLikeFile(@NotNull PsiElement child, @NotNull PsiElement parent) {
    return isStepLikeFile(child, parent);
  }

  @NotNull
  @Override
  public FileType getStepFileType() {
    return JavaFileType.INSTANCE;
  }

  @NotNull
  @Override
  public StepDefinitionCreator getStepDefinitionCreator() {
    return new JavaStepDefinitionCreator();
  }

  @NotNull
  @Override
  public Collection<String> getGlues(@NotNull GherkinFile file, Set<String> gluesFromOtherFiles) {
    if (gluesFromOtherFiles == null) {
      gluesFromOtherFiles = ContainerUtil.newHashSet();
    }
    final Set<String> glues = gluesFromOtherFiles;

    file.accept(new GherkinRecursiveElementVisitor() {
      @Override
      public void visitStep(GherkinStep step) {
        boolean covered = false;
        final String glue = CucumberJavaUtil.getPackageOfStep(step);
        if (glue != null) {
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
      }
    });

    return glues;
  }

  @Override
  public List<AbstractStepDefinition> loadStepsFor(@Nullable PsiFile featureFile, @NotNull Module module) {
    final GlobalSearchScope dependenciesScope = module.getModuleWithDependenciesAndLibrariesScope(true);

    Collection<PsiClass> stepDefAnnotationCandidates = JavaFullClassNameIndex.getInstance().get(
      CUCUMBER_RUNTIME_JAVA_STEP_DEF_ANNOTATION.hashCode(), module.getProject(), dependenciesScope);

    PsiClass stepDefAnnotationClass = null;
    for (PsiClass candidate : stepDefAnnotationCandidates) {
      if (CUCUMBER_RUNTIME_JAVA_STEP_DEF_ANNOTATION.equals(candidate.getQualifiedName())) {
        stepDefAnnotationClass = candidate;
        break;
      }
    }
    if (stepDefAnnotationClass == null) {
      return Collections.emptyList();
    }

    final List<AbstractStepDefinition> result = new ArrayList<AbstractStepDefinition>();
    final Query<PsiClass> stepDefAnnotations = AnnotatedElementsSearch.searchPsiClasses(stepDefAnnotationClass, dependenciesScope);
    for (PsiClass annotationClass : stepDefAnnotations) {
      final Query<PsiMethod> javaStepDefinitions = AnnotatedElementsSearch.searchPsiMethods(annotationClass, dependenciesScope);
      for (PsiMethod stepDefMethod : javaStepDefinitions) {
        result.add(new JavaStepDefinition(stepDefMethod));
      }
    }
    return result;
  }

  @Override
  public Collection<? extends PsiFile> getStepDefinitionContainers(@NotNull GherkinFile featureFile) {
    final Module module = ModuleUtilCore.findModuleForPsiElement(featureFile);
    if (module == null) {
      return Collections.emptySet();
    }

    List<AbstractStepDefinition> stepDefs = loadStepsFor(featureFile, module);

    Set<PsiFile> result = new HashSet<PsiFile>();
    for (AbstractStepDefinition stepDef : stepDefs) {
      result.add(stepDef.getElement().getContainingFile());
    }
    return result;
  }
}
