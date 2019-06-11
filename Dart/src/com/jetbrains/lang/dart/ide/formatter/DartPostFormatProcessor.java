// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.formatter;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.ide.actions.DartStyleAction;
import com.jetbrains.lang.dart.ide.application.options.DartCodeStyleSettings;
import com.jetbrains.lang.dart.psi.DartFile;
import com.jetbrains.lang.dart.sdk.DartSdkLibUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

final class DartPostFormatProcessor implements PostFormatProcessor {
  @NotNull
  @Override
  public PsiElement processElement(@NotNull final PsiElement source, @NotNull final CodeStyleSettings settings) {
    return source;
  }

  @NotNull
  @Override
  public TextRange processText(@NotNull final PsiFile psiFile,
                               @NotNull final TextRange rangeToReformat,
                               @NotNull final CodeStyleSettings settings) {
    if (!isApplicable(psiFile, rangeToReformat)) return rangeToReformat;

    DartStyleAction.runDartfmt(psiFile.getProject(), Collections.singletonList(psiFile.getVirtualFile()));

    final Document document = PsiDocumentManager.getInstance(psiFile.getProject()).getDocument(psiFile);
    if (document != null) {
      PsiDocumentManager.getInstance(psiFile.getProject()).commitDocument(document); // otherwise we can't return correct range
    }
    return psiFile.getTextRange();
  }

  private static boolean isApplicable(@NotNull final PsiFile psiFile, @NotNull final TextRange rangeToReformat) {
    if (!(psiFile instanceof DartFile)) return false;
    if (!psiFile.getTextRange().equals(rangeToReformat)) return false;
    final Project project = psiFile.getProject();
    if (!CodeStyleSettingsManager.getSettings(project).getCustomSettings(DartCodeStyleSettings.class).DELEGATE_TO_DARTFMT) return false;
    final VirtualFile vFile = psiFile.getVirtualFile();
    if (!DartAnalysisServerService.isLocalAnalyzableFile(vFile)) return false;
    final Module module = ModuleUtilCore.findModuleForPsiElement(psiFile);
    if (module == null || !DartSdkLibUtil.isDartSdkEnabled(module)) return false;
    if (!ProjectFileIndex.getInstance(project).isInContent(vFile)) return false;
    if (!DartAnalysisServerService.getInstance(project).serverReadyForRequest()) return false;

    return true;
  }
}
