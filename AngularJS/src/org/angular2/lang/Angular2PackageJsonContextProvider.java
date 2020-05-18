// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang;

import com.intellij.javascript.nodejs.PackageJsonData;
import com.intellij.javascript.nodejs.packageJson.PackageJsonFileManager;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.util.CachedValueProvider;
import org.jetbrains.annotations.NotNull;

import static org.angular2.lang.Angular2LangUtil.ANGULAR_CORE_PACKAGE;

public class Angular2PackageJsonContextProvider implements Angular2ContextProvider {
  @Override
  public @NotNull CachedValueProvider.Result<Boolean> isAngular2Context(@NotNull PsiDirectory psiDir) {
    PackageJsonFileManager manager = PackageJsonFileManager.getInstance(psiDir.getProject());
    boolean result = false;
    for (VirtualFile config : manager.getValidPackageJsonFiles()) {
      if (VfsUtilCore.isAncestor(config.getParent(), psiDir.getVirtualFile(), false)) {
        PackageJsonData data = PackageJsonUtil.getOrCreateData(config);
        if (data.isDependencyOfAnyType(ANGULAR_CORE_PACKAGE)) {
          result = true;
          break;
        }
      }
    }
    return CachedValueProvider.Result.create(result, manager.getModificationTracker());
  }
}
