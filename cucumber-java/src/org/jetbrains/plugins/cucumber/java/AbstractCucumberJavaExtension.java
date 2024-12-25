// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberUtil;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.jetbrains.plugins.cucumber.steps.AbstractCucumberExtension;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;

import java.util.*;

public abstract class AbstractCucumberJavaExtension extends AbstractCucumberExtension {
  @Override
  public boolean isStepLikeFile(final @NotNull PsiElement child, final @NotNull PsiElement parent) {
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

  @Override
  public Collection<? extends PsiFile> getStepDefinitionContainers(@NotNull GherkinFile featureFile) {
    final Module module = ModuleUtilCore.findModuleForPsiElement(featureFile);
    if (module == null) {
      return Collections.emptySet();
    }
    List<AbstractStepDefinition> stepDefs = CucumberUtil.loadFrameworkSteps(this, featureFile, module);

    Set<PsiFile> result = new HashSet<>();
    for (AbstractStepDefinition stepDef : stepDefs) {
      PsiElement stepDefElement = stepDef.getElement();
      if (stepDefElement != null) {
        final PsiFile psiFile = stepDefElement.getContainingFile();
        PsiDirectory psiDirectory = psiFile.getParent();
        if (psiDirectory != null && isWritableStepLikeFile(psiFile, psiDirectory)) {
          result.add(psiFile);
        }
      }
    }
    return result;
  }
}
