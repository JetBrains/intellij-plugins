// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.linter.tslint.typescript;

import com.intellij.lang.javascript.linter.tslint.TslintUtil;
import com.intellij.lang.typescript.compiler.languageService.TypescriptServiceExtension;
import com.intellij.lang.typescript.tsconfig.TypeScriptConfig;
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TslintTypescriptExtension implements TypescriptServiceExtension {
  @NotNull
  @Override
  public List<VirtualFile> getConfigFilesToWatch(@NotNull PsiFile fileToHighlight) {
    TypeScriptConfig config = TypeScriptConfigUtil.getConfigForPsiFile(fileToHighlight);
    if (config != null && config.getPlugins().contains(TslintUtil.TYPESCRIPT_PLUGIN_PACKAGE_NAME)) {
      VirtualFile virtualFile = fileToHighlight.getVirtualFile();
      if (virtualFile != null) {
        return ContainerUtil.createMaybeSingletonList(TslintUtil.lookupConfig(virtualFile));  
      }
    }
    return ContainerUtil.emptyList();
  }
}
