// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.refactoring;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartBundle;
import org.dartlang.analysis.server.protocol.InlineLocalVariableFeedback;
import org.dartlang.analysis.server.protocol.RefactoringFeedback;
import org.dartlang.analysis.server.protocol.RefactoringKind;
import org.dartlang.analysis.server.protocol.RefactoringOptions;
import org.jetbrains.annotations.NotNull;

/**
 * LTK wrapper around Analysis Server 'Inline Local' refactoring.
 */
public class ServerInlineLocalRefactoring extends ServerRefactoring {
  private String variableName;
  private int occurrences;

  public ServerInlineLocalRefactoring(final @NotNull Project project, final @NotNull VirtualFile file, final int offset, final int length) {
    super(project, DartBundle.message("progress.title.inline.local.variable"), RefactoringKind.INLINE_LOCAL_VARIABLE, file, offset, length);
  }

  public int getOccurrences() {
    return occurrences;
  }

  public String getVariableName() {
    return variableName;
  }

  @Override
  protected RefactoringOptions getOptions() {
    return null;
  }

  @Override
  protected void setFeedback(@NotNull RefactoringFeedback _feedback) {
    InlineLocalVariableFeedback feedback = (InlineLocalVariableFeedback)_feedback;
    variableName = feedback.getName();
    occurrences = feedback.getOccurrences();
  }
}
