// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor;
import org.jetbrains.annotations.NotNull;

import static com.intellij.prettierjs.formatting.PrettierRangeKt.extendRange;

public final class PrettierPostFormatProcessor implements PostFormatProcessor {
  @Override
  public @NotNull PsiElement processElement(@NotNull PsiElement source, @NotNull CodeStyleSettings settings) {
    return source;
  }

  @Override
  public @NotNull TextRange processText(@NotNull PsiFile psiFile, @NotNull TextRange rangeToReformat, @NotNull CodeStyleSettings settings) {
    if (Registry.get("prettier.use.async.formatting.service").asBoolean()) {
      return rangeToReformat;
    }

    if (isApplicable(psiFile)) {
      TextRange extendedRange = extendRange(psiFile, rangeToReformat);
      return ReformatWithPrettierAction.processFileAsPostFormatProcessor(psiFile, extendedRange);
    }
    return rangeToReformat;
  }

  static boolean isApplicable(@NotNull PsiFile psiFile) {
    VirtualFile file = psiFile.getVirtualFile();
    if (file == null) return false;

    Project project = psiFile.getProject();
    PrettierConfiguration configuration = PrettierConfiguration.getInstance(project);
    if (!configuration.isRunOnReformat()) return false;

    FileEditor fileEditor = FileEditorManager.getInstance(project).getSelectedEditor(file);
    if (fileEditor instanceof TextEditor) {
      Template template = TemplateManager.getInstance(psiFile.getProject()).getActiveTemplate(((TextEditor)fileEditor).getEditor());
      if (template != null) return false;
    }

    return PrettierUtil.isFormattingAllowedForFile(project, file);
  }
}
