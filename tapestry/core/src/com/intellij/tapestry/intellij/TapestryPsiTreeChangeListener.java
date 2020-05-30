package com.intellij.tapestry.intellij;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.*;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.intellij.core.resource.IntellijResource;
import com.intellij.tapestry.intellij.util.IdeaUtils;
import com.intellij.tapestry.intellij.util.TapestryUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class TapestryPsiTreeChangeListener extends PsiTreeChangeAdapter {
  @Override
  public void childRemoved(@NotNull PsiTreeChangeEvent event) {
    Module module = getModuleFromEvent(event);
    if (module == null || module.isDisposed()) return;
    TapestryProject tapestryProject = TapestryModuleSupportLoader.getTapestryProject(module);

    if (tapestryProject == null || !TapestryUtils.isTapestryModule(module)) {
      return;
    }

    PsiElement child = event.getChild();
    if (child instanceof PsiClassOwner) {
      PsiClass classDeleted = IdeaUtils.findPublicClass((PsiClassOwner)child);
      if (classDeleted != null) {
        tapestryProject.getEventsManager().classDeleted(classDeleted.getQualifiedName());
      }
    }

    if (child instanceof PsiFile) {
      tapestryProject.getEventsManager().fileDeleted(((PsiFile)child).getVirtualFile().getPath());
    }

    if (child instanceof PsiDirectory) {
      tapestryProject.getEventsManager().fileDeleted(((PsiDirectory)child).getVirtualFile().getPath());
    }

    final PsiFile psiFile = event.getFile();
    if (psiFile != null) {
      tapestryProject.getEventsManager().fileContentsChanged(new IntellijResource(psiFile));
    }
  }

  @Override
  public void childAdded(@NotNull PsiTreeChangeEvent event) {
    Module module = getModuleFromEvent(event);
    if (module == null) {
      return;
    }

    TapestryProject tapestryProject = TapestryModuleSupportLoader.getTapestryProject(module);

    if (tapestryProject == null || !TapestryUtils.isTapestryModule(module)) {
      return;
    }

    final PsiFile eventFile = event.getFile();
    if (eventFile != null) {
      tapestryProject.getEventsManager().fileContentsChanged(new IntellijResource(eventFile));
    }

    if (!(event.getChild() instanceof PsiFile)) {
      return;
    }

    PsiFile psiFile = (PsiFile)event.getChild();

    if (psiFile instanceof PsiClassOwner) {
      tapestryProject.getEventsManager().classCreated(null);
      return;
    }

    tapestryProject.getEventsManager().fileCreated(psiFile.getVirtualFile().getPath());
  }

  @Nullable
  private static Module getModuleFromEvent(PsiTreeChangeEvent event) {
    PsiElement parent = event.getParent();
    return parent == null ? null : ModuleUtilCore.findModuleForPsiElement(parent);
  }
}
