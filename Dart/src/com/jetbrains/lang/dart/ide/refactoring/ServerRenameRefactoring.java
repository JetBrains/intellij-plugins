// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.refactoring;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartBundle;
import org.dartlang.analysis.server.protocol.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The LTK wrapper around the Analysis Server 'Rename' refactoring.
 */
public class ServerRenameRefactoring extends ServerRefactoring {
  private RenameOptions options;
  private String elementKindName;
  private String oldName;

  public ServerRenameRefactoring(@NotNull final Project project, @NotNull final VirtualFile file, final int offset, final int length) {
    super(project, DartBundle.message("progress.title.rename"), RefactoringKind.RENAME, file, offset, length);
  }

  @NotNull
  public String getElementKindName() {
    return elementKindName;
  }

  @NotNull
  public String getOldName() {
    return oldName;
  }

  @Override
  @Nullable
  protected RefactoringOptions getOptions() {
    return options;
  }

  @Override
  protected void setFeedback(@NotNull RefactoringFeedback _feedback) {
    RenameFeedback feedback = (RenameFeedback)_feedback;
    elementKindName = StringUtil.capitalize(feedback.getElementKindName());
    oldName = feedback.getOldName();
    if (options == null) {
      options = new RenameOptions(oldName);
    }
  }

  public void setNewName(@NotNull String newName) {
    options.setNewName(newName);
    setOptions(true, null);
  }
}
