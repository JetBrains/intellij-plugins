// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.frameworks.webpack.WebpackCssFileReferenceHelper;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiManager;
import com.intellij.util.SmartList;
import org.angular2.cli.AngularCliConfigLoader;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class Angular2CssFileReferenceHelper extends WebpackCssFileReferenceHelper {
  @NotNull
  @Override
  public Collection<PsiFileSystemItem> getContexts(@NotNull final Project project, @NotNull final VirtualFile file) {
    final Collection<PsiFileSystemItem> result = new SmartList<>(new AngularCliAwareCssFileReferenceResolver(project, file));
    for (VirtualFile dir : AngularCliConfigLoader.load(project, file).getStylePreprocessorIncludeDirs()) {
      final PsiDirectory psiDir = PsiManager.getInstance(project).findDirectory(dir);
      if (psiDir != null) {
        result.add(psiDir);
      }
    }
    return result;
  }

  private static class AngularCliAwareCssFileReferenceResolver extends WebpackTildeFileReferenceResolver {
    AngularCliAwareCssFileReferenceResolver(@NotNull final Project project, @NotNull final VirtualFile contextFile) {
      super(project, contextFile);
    }

    @Override
    protected Collection<VirtualFile> findRootDirectories(@NotNull final VirtualFile context, @NotNull final Project project) {
      return AngularCliConfigLoader.load(project, context).getRootDirs();
    }
  }
}
