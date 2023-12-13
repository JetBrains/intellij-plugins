package com.jetbrains.lang.dart.highlight;

import com.intellij.codeInsight.highlighting.HighlightErrorFilter;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiErrorElement;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;

public final class DartHighlightingErrorFilter extends HighlightErrorFilter {
  @Override
  public boolean shouldHighlightErrorElement(@NotNull final PsiErrorElement element) {
    if (!(element.getLanguage() == DartLanguage.INSTANCE)) return true;

    final VirtualFile file = DartResolveUtil.getRealVirtualFile(element.getContainingFile());
    if (file != null && file.isInLocalFileSystem() && ProjectFileIndex.getInstance(element.getProject()).isInContent(file)) {
      return false;
    }

    return true;
  }
}
