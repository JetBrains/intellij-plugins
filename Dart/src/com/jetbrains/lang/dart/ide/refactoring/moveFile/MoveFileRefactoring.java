// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.refactoring.moveFile;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.ide.refactoring.ServerRefactoring;
import org.dartlang.analysis.server.protocol.MoveFileOptions;
import org.dartlang.analysis.server.protocol.RefactoringFeedback;
import org.dartlang.analysis.server.protocol.RefactoringKind;
import org.dartlang.analysis.server.protocol.RefactoringOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MoveFileRefactoring extends ServerRefactoring {

  private final @NotNull MoveFileOptions options;

  public MoveFileRefactoring(final @NotNull Project project, final @NotNull VirtualFile file, final @NotNull String newFilePath) {
    super(project, DartBundle.message("progress.title.move.file"), RefactoringKind.MOVE_FILE, file, 0, 0);
    options = new MoveFileOptions(DartAnalysisServerService.getInstance(project).getLocalFileUri(newFilePath));
  }

  @Override
  protected @Nullable RefactoringOptions getOptions() {
    return options;
  }

  @Override
  protected void setFeedback(@NotNull RefactoringFeedback _feedback) {
    // no-op, the MOVE_FILE refactoring, does not have feedback
  }
}
