// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.formatter;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.ide.actions.DartStyleAction;
import com.jetbrains.lang.dart.psi.DartFile;
import com.jetbrains.lang.dart.sdk.DartSdkLibUtil;
import org.jetbrains.annotations.NotNull;

final class DartPostFormatProcessor implements PostFormatProcessor {
  @Override
  public @NotNull PsiElement processElement(final @NotNull PsiElement source, final @NotNull CodeStyleSettings settings) {
    return source;
  }

  @Override
  public @NotNull TextRange processText(final @NotNull PsiFile psiFile,
                                        final @NotNull TextRange rangeToReformat,
                                        final @NotNull CodeStyleSettings settings) {
    if (!isApplicable(psiFile)) return rangeToReformat;

    return DartStyleAction.reformatRangeAsPostFormatProcessor(psiFile, rangeToReformat);
  }

  private static boolean isApplicable(final @NotNull PsiFile psiFile) {
    if (!(psiFile instanceof DartFile)) return false;
    final Project project = psiFile.getProject();
    final VirtualFile vFile = psiFile.getVirtualFile();
    if (!DartAnalysisServerService.isLocalAnalyzableFile(vFile)) return false;
    final Module module = ModuleUtilCore.findModuleForPsiElement(psiFile);
    if (module == null || !DartSdkLibUtil.isDartSdkEnabled(module)) return false;
    if (!ProjectFileIndex.getInstance(project).isInContent(vFile)) return false;
    if (!DartAnalysisServerService.getInstance(project).serverReadyForRequest()) return false;

    return true;
  }
}
