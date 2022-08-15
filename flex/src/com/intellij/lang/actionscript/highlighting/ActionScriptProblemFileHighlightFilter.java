// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.actionscript.highlighting;

import com.intellij.lang.javascript.ActionScriptFileType;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class ActionScriptProblemFileHighlightFilter implements Condition<VirtualFile> {

  private final Project myProject;

  public ActionScriptProblemFileHighlightFilter(@NotNull Project project) {
    myProject = project;
  }

  @Override
  public boolean value(VirtualFile file) {
    FileType fileType = file.getFileType();

    if (fileType == ActionScriptFileType.INSTANCE || JavaScriptSupportLoader.isMxmlOrFxgFile(file)) {
      return ProjectFileIndex.getInstance(myProject).isInSource(file);
    }

    return false;
  }
}
