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
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.testFramework.LightVirtualFileBase;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import static com.intellij.lang.javascript.library.JSLibraryUtil.NODE_MODULES;
import static org.angular2.lang.html.psi.impl.Angular2HtmlReferenceVariableImpl.ANGULAR_CORE_PACKAGE;

public class Angular2LangUtil {

  @NonNls private static final Key<CachedValue<Boolean>> ANGULAR2_CONTEXT_KEY = new Key<>("angular2.isContext");
  @NonNls private static final String NODE_MODULE_ANGULAR_CORE_PATH = "/" + NODE_MODULES + "/" + ANGULAR_CORE_PACKAGE + "/";

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
    return CachedValuesManager.getCachedValue(psiDir, ANGULAR2_CONTEXT_KEY, () -> new CachedValueProvider.Result<>(
      isAngular2ContextDir(psiDir),
      PackageJsonFileManager.getInstance(project).getModificationTracker()));
  }

  /**
   * @deprecated kept for compatibility with NativeScript
   */
  @Deprecated
  public static boolean isAngular2Context(@NotNull Project project) {
    if (project.getBaseDir() != null) {
      return isAngular2Context(project, project.getBaseDir());
    }
    return false;
  }

  private static boolean isAngular2ContextDir(@NotNull PsiDirectory psiDir) {
    VirtualFile dir = psiDir.getVirtualFile();
    PackageJsonFileManager manager = PackageJsonFileManager.getInstance(psiDir.getProject());
    String dirPath = ObjectUtils.notNull(dir.getCanonicalPath(), dir::getPath) + "/";
    for (VirtualFile config : manager.getValidPackageJsonFiles()) {
      String configDirPath = ObjectUtils.notNull(config.getParent().getCanonicalPath(), config.getParent()::getPath) + "/";
      if (configDirPath.endsWith(NODE_MODULE_ANGULAR_CORE_PATH)) {
        if (dirPath.startsWith(configDirPath.substring(0, configDirPath.length() - NODE_MODULE_ANGULAR_CORE_PATH.length()) + "/")) {
          return true;
        }
      }
      else if (dirPath.startsWith(configDirPath)) {
        PackageJsonData data = PackageJsonUtil.getOrCreateData(config);
        if (data.isDependencyOfAnyType(ANGULAR_CORE_PACKAGE)) {
          return true;
        }
      }
    }
    return false;
  }
}
