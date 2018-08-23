// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang;

import com.intellij.javascript.nodejs.PackageJsonData;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightVirtualFileBase;
import org.jetbrains.annotations.NotNull;

public class Angular2LangUtil {

  public static boolean isAngular2Context(@NotNull PsiElement context) {
    if (!context.isValid()) {
      return false;
    }
    final PsiFile psiFile = context.getContainingFile();
    if (psiFile == null) {
      return false;
    }
    final VirtualFile file = psiFile.getOriginalFile().getVirtualFile();
    if (file == null || !file.isInLocalFileSystem()) {
      return isAngular2Context(psiFile.getProject());
    }
    return isAngular2Context(file);
  }

  public static boolean isAngular2Context(@NotNull Project project) {
    if (project.getBaseDir() != null) {
      return isAngular2Context(project.getBaseDir());
    }
    return false;
  }

  public static boolean isAngular2Context(@NotNull VirtualFile context) {
    if (ApplicationManager.getApplication().isUnitTestMode()
        && "disabled".equals(System.getProperty("angular.js"))) {
      return false;
    }
    if (context instanceof LightVirtualFileBase) {
      while (context instanceof LightVirtualFileBase) {
        context = ((LightVirtualFileBase)context).getOriginalFile();
      }
      if (context == null) {
        return false;
      }
    }

    Ref<Boolean> isAngular2Context = Ref.create(false);
    PackageJsonUtil.processUpPackageJsonFilesInAllScope(context, file -> {
      PackageJsonData data = PackageJsonUtil.getOrCreateData(file);
      if (data.isDependencyOfAnyType("@angular/core")) {
        isAngular2Context.set(true);
      }
      return false;
    });
    return !isAngular2Context.isNull() && isAngular2Context.get();
  }

  public static boolean isDirective(@NotNull String decoratorName) {
    return "Directive".equals(decoratorName) || "Component".equals(decoratorName);
  }
}
