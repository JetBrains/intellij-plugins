// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.model;


import com.intellij.codeInsight.highlighting.HighlightErrorFilter;
import com.intellij.coldFusion.model.files.CfmlFileType;
import com.intellij.lang.Language;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiErrorElement;
import org.jetbrains.annotations.NotNull;


public final class CfmlErrorFilter extends HighlightErrorFilter {

  @Override
  public boolean shouldHighlightErrorElement(final @NotNull PsiErrorElement element) {
    VirtualFile vFile = element.getContainingFile() != null ? element.getContainingFile().getVirtualFile() : null;
    if (!(vFile != null && vFile.getFileType() instanceof CfmlFileType)) {
      return true;
    }
    final Language language = element.getParent().getLanguage();
    if (language == CfmlLanguage.INSTANCE || language == HTMLLanguage.INSTANCE || language == CfmlUtil.getSqlLanguage()) return true;
    return false;
  }
}
