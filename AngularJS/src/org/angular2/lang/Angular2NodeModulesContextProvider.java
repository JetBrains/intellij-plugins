// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang;

import com.intellij.javascript.nodejs.library.NodeModulesDirectoryManager;
import com.intellij.javascript.nodejs.library.NodeModulesLibraryDirectory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.util.CachedValueProvider;
import org.jetbrains.annotations.NotNull;

import static org.angular2.lang.Angular2LangUtil.ANGULAR_CORE_PACKAGE;

public class Angular2NodeModulesContextProvider implements Angular2ContextProvider {
  @NotNull
  @Override
  public CachedValueProvider.Result<Boolean> isAngular2Context(@NotNull PsiDirectory psiDir) {
    NodeModulesDirectoryManager manager = NodeModulesDirectoryManager.getInstance(psiDir.getProject());
    String dirPath = psiDir.getVirtualFile().getPath() + "/";
    boolean result = false;
    for (NodeModulesLibraryDirectory dir : manager.getNodeModulesDirectories()) {
      VirtualFile nodeModules = dir.getNodeModulesDir();
      if (dirPath.startsWith(nodeModules.getParent().getPath() + "/")) {
        VirtualFile child = dir.getNodeModulesDir().findFileByRelativePath(ANGULAR_CORE_PACKAGE);
        if (child != null && child.isValid() && child.isDirectory()) {
          result = true;
          break;
        }
      }
    }
    return CachedValueProvider.Result.create(result, manager.getNodeModulesDirChangeTracker());
  }
}
