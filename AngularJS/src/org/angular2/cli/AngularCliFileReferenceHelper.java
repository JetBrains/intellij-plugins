// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli;

import com.intellij.lang.html.HtmlCompatibleFile;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceHelper;
import org.angular2.cli.config.AngularConfigProvider;
import org.angular2.cli.config.AngularProject;
import org.angular2.lang.Angular2LangUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class AngularCliFileReferenceHelper extends FileReferenceHelper {

  @Override
  public boolean isMine(@NotNull Project project, @NotNull VirtualFile file) {
    return getPsiFileSystemItem(project, file) instanceof HtmlCompatibleFile
           && Angular2LangUtil.isAngular2Context(project, file)
           && AngularConfigProvider.getAngularProject(project, file) != null;
  }

  @Override
  public @NotNull Collection<PsiFileSystemItem> getContexts(@NotNull Project project, @NotNull VirtualFile file) {
    return Optional.ofNullable(AngularConfigProvider.getAngularProject(project, file))
      .map(AngularProject::getSourceDir)
      .map(sourceDir -> (PsiFileSystemItem)PsiManager.getInstance(project).findDirectory(sourceDir))
      .map(Collections::singletonList)
      .orElseGet(Collections::emptyList);
  }

  @Override
  public @NotNull Collection<PsiFileSystemItem> getRoots(@NotNull Module module, @NotNull VirtualFile file) {
    return getContexts(module.getProject(), file);
  }
}
