// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.refactoring;

import com.google.common.collect.ImmutableList;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtilRt;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import org.dartlang.analysis.server.protocol.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * LTK wrapper around Analysis Server 'Extract Method' refactoring.
 */
public class ServerExtractMethodRefactoring extends ServerRefactoring {
  private final ExtractMethodOptions options =
    new ExtractMethodOptions("returnType", false, "name", ImmutableList.of(), false);
  private ExtractMethodFeedback feedback;

  public ServerExtractMethodRefactoring(final @NotNull Project project,
                                        final @NotNull VirtualFile file,
                                        final int offset,
                                        final int length) {
    super(project, DartBundle.message("progress.title.extract.method"), RefactoringKind.EXTRACT_METHOD, file, offset, length);
  }

  public boolean canExtractGetter() {
    return feedback.canCreateGetter();
  }

  public String @NotNull [] getNames() {
    return ArrayUtilRt.toStringArray(feedback.getNames());
  }

  public int getOccurrencesCount() {
    return feedback.getOffsets().length;
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

  public @NotNull List<RefactoringMethodParameter> getParameters() {
    return options.getParameters();
  }

  public @NotNull @NlsSafe String getSignature() {
    // TODO(scheglov) consider moving to server
    StringBuilder sb = new StringBuilder();
    sb.append(options.getReturnType());
    sb.append(" ");
    boolean createGetter = options.createGetter();
    if (createGetter) {
      sb.append("get ");
    }
    sb.append(options.getName());
    if (!createGetter) {
      sb.append("(");
      boolean firstParameter = true;
      for (RefactoringMethodParameter parameter : options.getParameters()) {
        if (!firstParameter) {
          sb.append(", ");
        }
        firstParameter = false;
        sb.append(parameter.getType());
        sb.append(" ");
        sb.append(parameter.getName());
      }
      sb.append(")");
    }
    return sb.toString();
  }

  public void setCreateGetter(boolean value) {
    options.setCreateGetter(value);
  }

  public void setExtractAll(boolean extractAll) {
    options.setExtractAll(extractAll);
  }

  @Override
  protected void setFeedback(@NotNull RefactoringFeedback _feedback) {
    boolean firstFeedback = feedback == null;
    feedback = (ExtractMethodFeedback)_feedback;
    if (firstFeedback) {
      options.setExtractAll(true);
      options.setReturnType(feedback.getReturnType());
      options.setCreateGetter(feedback.canCreateGetter());
      options.setParameters(feedback.getParameters());
    }
  }

  public void setName(@NotNull String name) {
    options.setName(name);
    setOptions(true, null);
  }
}
