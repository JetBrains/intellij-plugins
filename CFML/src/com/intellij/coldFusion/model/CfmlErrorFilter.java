// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.model;


import com.intellij.codeInsight.highlighting.HighlightErrorFilter;
import com.intellij.coldFusion.model.files.CfmlFileType;
import com.intellij.lang.Language;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiErrorElement;
import org.jetbrains.annotations.NotNull;


public class CfmlErrorFilter extends HighlightErrorFilter {

  @Override
  public boolean shouldHighlightErrorElement(@NotNull final PsiErrorElement element) {
    VirtualFile vFile = element.getContainingFile() != null ? element.getContainingFile().getVirtualFile() : null;
    if (!(vFile != null && vFile.getFileType() instanceof CfmlFileType)) {
      return true;
    }
    final Language language = element.getParent().getLanguage();
    if (language == CfmlLanguage.INSTANCE || language == HTMLLanguage.INSTANCE || language == CfmlUtil.getSqlLanguage()) return true;
    return false;
  }
}
