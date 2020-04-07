// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli;

import com.intellij.psi.FileContextProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.impl.source.html.HtmlLikeFile;
import org.angular2.cli.config.AngularConfigProvider;
import org.angular2.cli.config.AngularProject;
import org.angular2.lang.Angular2LangUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class AngularCliContextProvider extends FileContextProvider {

  @Override
  protected boolean isAvailable(PsiFile file) {
    return file instanceof HtmlLikeFile
           && Angular2LangUtil.isAngular2Context(file)
           && AngularConfigProvider.getAngularProject(file) != null;
  }

  @Override
  public @NotNull Collection<PsiFileSystemItem> getContextFolders(PsiFile file) {
    return Optional.ofNullable(AngularConfigProvider.getAngularProject(file))
      .map(AngularProject::getSourceDir)
      .map(sourceDir -> ((PsiFileSystemItem)file.getManager().findDirectory(sourceDir)))
      .map(Collections::singletonList)
      .orElseGet(Collections::emptyList);
  }

  @Override
  public @Nullable PsiFile getContextFile(PsiFile file) {
    return null;
  }
}
