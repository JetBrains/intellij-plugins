// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.angularjs.codeInsight;

import com.intellij.lang.javascript.frameworks.webpack.WebpackCssFileReferenceHelper;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiManager;
import com.intellij.util.SmartList;
import org.angularjs.cli.AngularCliConfigLoader;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class AngularCssFileReferenceHelper extends WebpackCssFileReferenceHelper {
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
    public AngularCliAwareCssFileReferenceResolver(@NotNull final Project project, @NotNull final VirtualFile contextFile) {
      super(project, contextFile);
    }

    @Override
    protected Collection<VirtualFile> findRootDirectories(@NotNull final VirtualFile context, @NotNull final Project project) {
      return AngularCliConfigLoader.load(project, context).getRootDirs();
    }
  }
}
