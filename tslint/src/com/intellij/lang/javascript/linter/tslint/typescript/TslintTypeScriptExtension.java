// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.linter.tslint.typescript;

import com.intellij.lang.javascript.linter.tslint.TslintUtil;
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceAnnotationResult;
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServiceExtension;
import com.intellij.lang.typescript.tsconfig.TypeScriptConfig;
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

public class TslintTypeScriptExtension implements TypeScriptServiceExtension {
  @NotNull
  @Override
  public Set<VirtualFile> getConfigFilesToWatch(@NotNull PsiFile fileToHighlight) {
    if (!hasTslint(fileToHighlight)) {
      return Collections.emptySet();
    }
    VirtualFile virtualFile = fileToHighlight.getVirtualFile();
    if (virtualFile != null) {
      VirtualFile tslintJson = TslintUtil.lookupConfig(fileToHighlight.getProject(), virtualFile);
      if (tslintJson != null) {
        return ContainerUtil.newHashSet(tslintJson);
      }
    }
    return Collections.emptySet();
  }

  @Override
  public boolean shouldReformatAfterFix(@NotNull TypeScriptLanguageServiceAnnotationResult result) {
    return !StringUtil.equals(result.getSource(), "tslint");
  }

  private static boolean hasTslint(@NotNull Project project, @NotNull VirtualFile virtualFile) {
    PsiFile psi = PsiManager.getInstance(project).findFile(virtualFile);
    return psi != null && hasTslint(psi);
  }

  private static boolean hasTslint(@NotNull PsiFile fileToHighlight) {
    TypeScriptConfig config = TypeScriptConfigUtil.getConfigForPsiFile(fileToHighlight);
    return config != null &&
           (config.getPlugins().contains(TslintUtil.TYPESCRIPT_PLUGIN_PACKAGE_NAME)
            || config.getPlugins().contains(TslintUtil.TYPESCRIPT_PLUGIN_OLD_PACKAGE_NAME));
  }
}
