// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.css.refs;

import com.intellij.lang.javascript.frameworks.webpack.WebpackCssFileReferenceHelper;
import com.intellij.lang.typescript.tsconfig.TypeScriptConfig;
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigService;
import com.intellij.model.ModelBranch;
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

public class Angular2CssFileReferenceHelper extends WebpackCssFileReferenceHelper {
  @Override
  public @NotNull Collection<PsiFileSystemItem> getContexts(final @NotNull Project project, final @NotNull VirtualFile file) {
    final Collection<PsiFileSystemItem> result = new SmartList<>(new AngularCliAwareCssFileReferenceResolver(project, file));
    StreamEx.ofNullable(AngularConfigProvider.getAngularProject(project, file))
      .flatCollection(AngularProject::getStylePreprocessorIncludeDirs)
      .map(dir -> PsiManager.getInstance(project).findDirectory(dir))
      .nonNull()
      .into(result);
    return result;
  }

  private static class AngularCliAwareCssFileReferenceResolver extends WebpackTildeFileReferenceResolver {
    AngularCliAwareCssFileReferenceResolver(final @NotNull Project project, final @NotNull VirtualFile contextFile) {
      super(project, contextFile);
    }

    @Override
    public @NotNull AngularCliAwareCssFileReferenceResolver obtainBranchCopy(@NotNull ModelBranch branch) {
      VirtualFile fileCopy = branch.findFileCopy(getVirtualFile());
      return new AngularCliAwareCssFileReferenceResolver(getProject(), fileCopy);
    }

    @Override
    protected Collection<VirtualFile> findRootDirectories(final @NotNull VirtualFile context, final @NotNull Project project) {
      AngularProject ngProject = AngularConfigProvider.getAngularProject(project, context);
      if (ngProject != null) {
        TypeScriptConfig tsConfig = TypeScriptConfigService.Provider.parseConfigFile(project, ngProject.getTsConfigFile());
        if (tsConfig != null) {
          VirtualFile baseUrl = tsConfig.getBaseUrl();
          if (baseUrl != null) {
            return Collections.singletonList(baseUrl);
          }
        }
        VirtualFile cssResolveDir = ngProject.getCssResolveRootDir();
        if (cssResolveDir != null) {
          return Collections.singletonList(cssResolveDir);
        }
      }
      return Collections.emptyList();
    }
  }
}
