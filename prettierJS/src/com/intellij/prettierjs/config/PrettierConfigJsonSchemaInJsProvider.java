// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs.config;

import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.lang.javascript.json.JSJsonSchemaProviderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.prettierjs.PrettierUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class PrettierConfigJsonSchemaInJsProvider extends JSJsonSchemaProviderBase {
  @Override
  public boolean isAvailable(@NotNull PsiElement element) {
    PsiFile file = element.getContainingFile();
    VirtualFile virtualFile = file == null ? null : CompletionUtil.getOriginalOrSelf(file).getVirtualFile();
    if (!PrettierUtil.isJSConfigFile(virtualFile)) return false;
    return isInTopLevelObject(element) &&
           isInSupportedArea(element, JSJsonSchemaProviderBase::computeDefaultModuleExportsAreas);
  }

  @Override
  public VirtualFile getSchemaFile() {
    return loadFile(PrettierConfigJsonSchemaInJsProvider.class, "/" + PrettierConfigJsonSchemaProviderFactory.SCHEMA_FILE_NAME);
  }
}
