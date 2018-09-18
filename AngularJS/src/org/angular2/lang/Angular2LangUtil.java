// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang;

import com.intellij.javascript.nodejs.PackageJsonData;
import com.intellij.javascript.nodejs.packageJson.PackageJsonFileManager;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.ParameterizedCachedValue;
import com.intellij.testFramework.LightVirtualFileBase;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;

public class Angular2LangUtil {

  private static final Key<ParameterizedCachedValue<Boolean, VirtualFile>> ANGULAR2_CONTEXT_KEY = new Key<>("angular2.isContext");

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
      //noinspection deprecation
      return isAngular2Context(psiFile.getProject());
    }
    return isAngular2Context(psiFile.getProject(), file);
  }

  public static boolean isAngular2Context(@NotNull Project project, @NotNull VirtualFile context) {
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
    return CachedValuesManager.getManager(project).getParameterizedCachedValue(project, ANGULAR2_CONTEXT_KEY, dir ->
      new CachedValueProvider.Result<>(isAngular2ContextDir(project, dir),
                                       PackageJsonFileManager.getInstance(project).getModificationTracker()), false, context.getParent());
  }

  /**
   * @deprecated kept for compatibility with NativeScript
   */
  public static boolean isAngular2Context(@NotNull Project project) {
    if (project.getBaseDir() != null) {
      return isAngular2Context(project, project.getBaseDir());
    }
    return false;
  }

  private static boolean isAngular2ContextDir(Project project, VirtualFile dir) {
    PackageJsonFileManager manager = PackageJsonFileManager.getInstance(project);
    String dirPath = ObjectUtils.notNull(dir.getCanonicalPath(), dir::getPath) + "/";
    for (VirtualFile config : manager.getValidPackageJsonFiles()) {
      if (dirPath.startsWith(ObjectUtils.notNull(config.getParent().getCanonicalPath(), dir::getPath) + "/")) {
        PackageJsonData data = PackageJsonUtil.getOrCreateData(config);
        if (data.isDependencyOfAnyType("@angular/core")) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean isDirective(@NotNull String decoratorName) {
    return "Directive".equals(decoratorName) || "Component".equals(decoratorName);
  }
}
