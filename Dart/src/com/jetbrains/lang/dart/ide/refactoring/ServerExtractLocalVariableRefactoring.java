// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.refactoring;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtilRt;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import org.dartlang.analysis.server.protocol.*;
import org.jetbrains.annotations.NotNull;

/**
 * LTK wrapper around Analysis Server 'Extract Local Variable' refactoring.
 */
public class ServerExtractLocalVariableRefactoring extends ServerRefactoring {
  private final ExtractLocalVariableOptions options = new ExtractLocalVariableOptions("name", true);
  private ExtractLocalVariableFeedback feedback;

  public ServerExtractLocalVariableRefactoring(final @NotNull Project project,
                                               final @NotNull VirtualFile file,
                                               final int offset,
                                               final int length) {
    super(project, DartBundle.message("progress.title.extract.local.variable"), RefactoringKind.EXTRACT_LOCAL_VARIABLE, file, offset, length);
  }

  public int @NotNull [] getCoveringExpressionOffsets() {
    return DartAnalysisServerService.getInstance(getProject()).getConvertedOffsets(getFile(), feedback.getCoveringExpressionOffsets());
  }

  public int @NotNull [] getCoveringExpressionLengths() {
    return DartAnalysisServerService.getInstance(getProject())
      .getConvertedLengths(getFile(), feedback.getCoveringExpressionOffsets(), feedback.getCoveringExpressionLengths());
  }

  public String @NotNull [] getNames() {
    return ArrayUtilRt.toStringArray(feedback.getNames());
  }

  public int @NotNull [] getOccurrencesOffsets() {
    return DartAnalysisServerService.getInstance(getProject()).getConvertedOffsets(getFile(), feedback.getOffsets());
  }

  public int @NotNull [] getOccurrencesLengths() {
    return DartAnalysisServerService.getInstance(getProject()).getConvertedLengths(getFile(), feedback.getOffsets(), feedback.getLengths());
  }

  @Override
  protected RefactoringOptions getOptions() {
    return options;
  }

  public void setExtractAll(boolean extractAll) {
    options.setExtractAll(extractAll);
  }

  @Override
  protected void setFeedback(@NotNull RefactoringFeedback _feedback) {
    feedback = (ExtractLocalVariableFeedback)_feedback;
  }

  public void setName(@NotNull String name) {
    options.setName(name);
    setOptions(true, null);
  }
}
