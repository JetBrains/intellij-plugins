// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class PrettierPostFormatProcessor implements PostFormatProcessor {
  @Override
  public @NotNull PsiElement processElement(@NotNull PsiElement source, @NotNull CodeStyleSettings settings) {
    return source;
  }

  @Override
  public @NotNull TextRange processText(@NotNull PsiFile psiFile, @NotNull TextRange rangeToReformat, @NotNull CodeStyleSettings settings) {
    if (isApplicable(psiFile)) {
      return ReformatWithPrettierAction.processFileAsPostFormatProcessor(psiFile, rangeToReformat);
    }
    return rangeToReformat;
  }

  private static boolean isApplicable(@NotNull PsiFile psiFile) {
    Project project = psiFile.getProject();
    PrettierConfiguration configuration = PrettierConfiguration.getInstance(project);
    if (!configuration.isRunOnReformat()) return false;
    VirtualFile file = psiFile.getVirtualFile();
    String pattern = configuration.getFilesPattern();
    return file != null &&
           PrettierSaveAction.getFilesMatchingGlobPattern(project, pattern, Collections.singletonList(file)).size() == 1;
  }
}
