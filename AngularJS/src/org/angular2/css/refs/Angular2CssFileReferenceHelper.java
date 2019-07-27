// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.css.refs;

import com.intellij.lang.javascript.frameworks.webpack.WebpackCssFileReferenceHelper;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiManager;
import com.intellij.util.SmartList;
import one.util.streamex.StreamEx;
import org.angular2.cli.config.AngularConfigProvider;
import org.angular2.cli.config.AngularProject;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class Angular2CssFileReferenceHelper extends WebpackCssFileReferenceHelper {
  @NotNull
  @Override
  public Collection<PsiFileSystemItem> getContexts(@NotNull final Project project, @NotNull final VirtualFile file) {
    final Collection<PsiFileSystemItem> result = new SmartList<>(new AngularCliAwareCssFileReferenceResolver(project, file));
    StreamEx.ofNullable(AngularConfigProvider.getAngularProject(project, file))
      .flatCollection(AngularProject::getStylePreprocessorIncludeDirs)
      .map(dir -> PsiManager.getInstance(project).findDirectory(dir))
      .nonNull()
      .into(result);
    return result;
  }

  private static class AngularCliAwareCssFileReferenceResolver extends WebpackTildeFileReferenceResolver {
    AngularCliAwareCssFileReferenceResolver(@NotNull final Project project, @NotNull final VirtualFile contextFile) {
      super(project, contextFile);
    }

    @Override
    protected Collection<VirtualFile> findRootDirectories(@NotNull final VirtualFile context, @NotNull final Project project) {
      return Optional.ofNullable(AngularConfigProvider.getAngularProject(project, context))
        .map(AngularProject::getSourceDir)
        .map(Collections::singletonList)
        .orElseGet(Collections::emptyList);
    }
  }
}
