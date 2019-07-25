// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.testFramework.LightVirtualFileBase;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.hash.HashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class Angular2LangUtil {

  @NonNls public static final String ANGULAR_CORE_PACKAGE = "@angular/core";
  @NonNls public static final String ANGULAR_CLI_PACKAGE = "@angular/cli";

  @NonNls private static final Key<CachedValue<Boolean>> ANGULAR2_CONTEXT_KEY = new Key<>("angular2.isContext");

  public static boolean isAngular2Context(@NotNull PsiElement context) {
    if (!context.isValid()) {
      return false;
    }
    final PsiFile psiFile = InjectedLanguageManager.getInstance(context.getProject()).getTopLevelFile(context);
    if (psiFile == null) {
      return false;
    }
    final VirtualFile file = psiFile.getOriginalFile().getVirtualFile();
    if (file == null || !file.isInLocalFileSystem()) {
      return isAngular2Context(psiFile.getProject());
    }
    return isAngular2Context(psiFile.getProject(), file);
  }

  public static boolean isAngular2Context(@NotNull Project project, @NotNull VirtualFile context) {
    //noinspection HardCodedStringLiteral
    if (ApplicationManager.getApplication().isUnitTestMode()
        && "disabled".equals(System.getProperty("angular.js"))) {
      return false;
    }
    while (context instanceof LightVirtualFileBase) {
      context = ((LightVirtualFileBase)context).getOriginalFile();
    }
    PsiDirectory psiDir = ObjectUtils.doIfNotNull(
      context != null ? context.getParent() : null,
      dir -> dir.isValid() ? PsiManager.getInstance(project).findDirectory(dir) : null);
    if (psiDir == null) {
      return false;
    }
    return CachedValuesManager.getCachedValue(psiDir, ANGULAR2_CONTEXT_KEY, () -> {
      Set<Object> dependencies = new HashSet<>();
      for (Angular2ContextProvider provider : Angular2ContextProvider.ANGULAR_CONTEXT_PROVIDER_EP.getExtensionList()) {
        CachedValueProvider.Result<Boolean> result = provider.isAngular2Context(psiDir);
        if (result.getValue() == Boolean.TRUE) {
          return result;
        }
        ContainerUtil.addAll(dependencies, result.getDependencyItems());
      }
      return new CachedValueProvider.Result<>(false, dependencies.toArray());
    });
  }

  private static boolean isAngular2Context(@NotNull Project project) {
    if (project.getBaseDir() != null) {
      return isAngular2Context(project, project.getBaseDir());
    }
    return false;
  }
}
