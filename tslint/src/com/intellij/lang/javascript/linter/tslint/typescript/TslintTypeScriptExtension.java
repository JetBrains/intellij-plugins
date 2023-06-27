// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.linter.tslint.typescript;

import com.intellij.lang.javascript.linter.tslint.TslintUtil;
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceAnnotationResult;
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServiceExtension;
import com.intellij.lang.typescript.tsconfig.TypeScriptConfig;
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TslintTypeScriptExtension implements TypeScriptServiceExtension {

  @Override
  public boolean isConfigFile(@NotNull PsiFile configCandidate, @NotNull PsiElement context) {
    VirtualFile file = configCandidate.getVirtualFile();
    return file != null && TslintUtil.isConfigFile(file) && hasTslint(context.getContainingFile());
  }

  @Override
  public boolean shouldReformatAfterFix(@NotNull TypeScriptLanguageServiceAnnotationResult result) {
    return !StringUtil.equals(result.getSource(), "tslint");
  }

  private static boolean hasTslint(@Nullable PsiFile fileToHighlight) {
    TypeScriptConfig config = TypeScriptConfigUtil.getConfigForPsiFile(fileToHighlight);
    return config != null &&
           (config.getPlugins().contains(TslintUtil.TYPESCRIPT_PLUGIN_PACKAGE_NAME)
            || config.getPlugins().contains(TslintUtil.TYPESCRIPT_PLUGIN_OLD_PACKAGE_NAME));
  }
}
