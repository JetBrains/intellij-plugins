package org.jetbrains.plugins.cucumber.java;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.jetbrains.plugins.cucumber.psi.GherkinRecursiveElementVisitor;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.cucumber.steps.AbstractCucumberExtension;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;

import java.util.*;

public abstract class AbstractCucumberJavaExtension extends AbstractCucumberExtension {
  @Override
  public boolean isStepLikeFile(@NotNull final PsiElement child, @NotNull final PsiElement parent) {
    if (child instanceof PsiClassOwner) {
      return true;
    }
    return false;
  }

  @Override
  public boolean isWritableStepLikeFile(@NotNull PsiElement child, @NotNull PsiElement parent) {
    if (child instanceof PsiClassOwner) {
      final PsiFile file = child.getContainingFile();
      if (file != null) {
        final VirtualFile vFile = file.getVirtualFile();
        if (vFile != null) {
          final VirtualFile rootForFile = ProjectRootManager.getInstance(child.getProject()).getFileIndex().getSourceRootForFile(vFile);
          return rootForFile != null;
        }
      }
    }
    return false;
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
        final String glue = CucumberJavaUtil.getPackageOfStep(step);
        if (glue != null) {
          CucumberJavaUtil.addGlue(glue, glues);
        }
      }
    });

    return glues;
  }

  @Override
  public Collection<? extends PsiFile> getStepDefinitionContainers(@NotNull GherkinFile featureFile) {
    final Module module = ModuleUtilCore.findModuleForPsiElement(featureFile);
    if (module == null) {
      return Collections.emptySet();
    }

    List<AbstractStepDefinition> stepDefs = loadStepsFor(featureFile, module);

    Set<PsiFile> result = new HashSet<>();
    for (AbstractStepDefinition stepDef : stepDefs) {
      PsiElement stepDefElement = stepDef.getElement();
      if (stepDefElement != null) {
        final PsiFile psiFile = stepDefElement.getContainingFile();

        if (isWritableStepLikeFile(psiFile, psiFile.getParent())) {
          result.add(psiFile);
        }
      }
    }
    return result;
  }
}
