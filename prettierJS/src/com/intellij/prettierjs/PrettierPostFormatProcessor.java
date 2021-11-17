// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.lang.javascript.linter.GlobPatternUtil;
import com.intellij.lang.javascript.psi.JSBlockStatement;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public final class PrettierPostFormatProcessor implements PostFormatProcessor {
  @Override
  public @NotNull PsiElement processElement(@NotNull PsiElement source, @NotNull CodeStyleSettings settings) {
    return source;
  }

  @Override
  public @NotNull TextRange processText(@NotNull PsiFile psiFile, @NotNull TextRange rangeToReformat, @NotNull CodeStyleSettings settings) {
    if (isApplicable(psiFile)) {
      TextRange extendedRange = extendRange(psiFile, rangeToReformat);
      return ReformatWithPrettierAction.processFileAsPostFormatProcessor(psiFile, extendedRange);
    }
    return rangeToReformat;
  }

  private static boolean isApplicable(@NotNull PsiFile psiFile) {
    Project project = psiFile.getProject();
    PrettierConfiguration configuration = PrettierConfiguration.getInstance(project);
    if (!configuration.isRunOnReformat()) return false;

    VirtualFile file = psiFile.getVirtualFile();
    if (file == null) return false;

    FileEditor fileEditor = FileEditorManager.getInstance(project).getSelectedEditor(file);
    if (fileEditor instanceof TextEditor) {
      Template template = TemplateManager.getInstance(psiFile.getProject()).getActiveTemplate(((TextEditor)fileEditor).getEditor());
      if (template != null) return false;
    }

    return GlobPatternUtil.isFileMatchingGlobPattern(project, configuration.getFilesPattern(), file);
  }

  private static @NotNull TextRange extendRange(@NotNull PsiFile file, @NotNull TextRange rangeToReformat) {
    if (rangeToReformat.isEmpty() ||
        rangeToReformat.getStartOffset() == 0 ||
        rangeToReformat.getEndOffset() == file.getTextRange().getEndOffset()) {
      return rangeToReformat;
    }

    PsiElement end = file.findElementAt(rangeToReformat.getEndOffset() - 1);
    PsiElement start = file.findElementAt(rangeToReformat.getStartOffset());
    if (start == null || end == null) {
      return rangeToReformat;
    }

    PsiElement commonParent = PsiTreeUtil.findCommonParent(start, end);
    if (!(commonParent instanceof JSBlockStatement)) {
      return rangeToReformat;
    }

    TextRange parentRange = commonParent.getTextRange();
    // If a format range ends exactly on a boundary between two nodes, a trailing node will be included in the formatting range.
    // So, for this selection `{  <selection>  abc</selection>}`
    // we get this formatting range `{<selection>    abc}</selection>`.
    // In such cases, prettier can go crazy and insert a comma somewhere in front of the block in the wrong place.
    if (!rangeToReformat.contains(parentRange) &&
        (rangeToReformat.getStartOffset() == parentRange.getStartOffset() ||
         rangeToReformat.getEndOffset() == parentRange.getEndOffset())) {
      return parentRange;
    }

    return rangeToReformat;
  }
}
