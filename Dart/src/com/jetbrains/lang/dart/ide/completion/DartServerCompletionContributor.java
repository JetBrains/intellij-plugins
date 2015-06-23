package com.jetbrains.lang.dart.ide.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ProcessingContext;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class DartServerCompletionContributor extends CompletionContributor {
  public DartServerCompletionContributor() {
    extend(CompletionType.BASIC, psiElement().withLanguage(DartLanguage.INSTANCE), new CompletionProvider<CompletionParameters>() {
      @Override
      protected void addCompletions(@NotNull final CompletionParameters parameters,
                                    @NotNull final ProcessingContext context,
                                    @NotNull final CompletionResultSet result) {
        final VirtualFile file = DartResolveUtil.getRealVirtualFile(parameters.getOriginalFile());
        if (file == null) return;

        DartAnalysisServerService.getInstance().updateFilesContent();

        final String filePath = FileUtil.toSystemDependentName(file.getPath());
        final String completionId = DartAnalysisServerService.getInstance().completion_getSuggestions(filePath, parameters.getOffset());
        if (completionId == null) return;

        DartAnalysisServerService.getInstance().addCompletions(parameters.getOriginalFile().getProject(), completionId, result);
      }
    });
  }
}
