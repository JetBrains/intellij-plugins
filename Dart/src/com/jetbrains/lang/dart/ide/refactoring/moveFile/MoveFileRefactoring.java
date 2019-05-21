// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.refactoring.moveFile;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.ide.refactoring.ServerRefactoring;
import org.dartlang.analysis.server.protocol.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MoveFileRefactoring extends ServerRefactoring {

  @NotNull private final String myOldFile;
  @NotNull private final MoveFileOptions options;

  public MoveFileRefactoring(@NotNull final Project project, @NotNull final VirtualFile file, @NotNull final String oldFile, @NotNull final String newFile) {
    super(project, "File Move", RefactoringKind.MOVE_FILE, file, 0, 0);
    this.myOldFile = oldFile;
    options = new MoveFileOptions(newFile);
  }

  @Nullable
  @Override
  protected RefactoringOptions getOptions() {
    return options;
  }

  @Override
  protected void setFeedback(@NotNull RefactoringFeedback _feedback) {
    // no-op, the MOVE_FILE refactoring, does not have feedback
  }

  @NotNull
  public String getOldFilePath() {
    return  myOldFile;
  }
}
