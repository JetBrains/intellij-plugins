// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.uml;

import com.intellij.diagram.ChangeTracker;
import com.intellij.diagram.DiagramChangesProvider;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.lang.javascript.ActionScriptFileType;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

final class FlashUmlChangesProvider extends DiagramChangesProvider {
  @Override
  public boolean accept(FileType type, String filename) {
    if (type == ActionScriptFileType.INSTANCE) {
      return true;
    }
    else if (type instanceof XmlFileType) {
      return JavaScriptSupportLoader.isFlexMxmFile(filename);
    }
    return false;
  }

  @Override
  public ChangeTracker<?, ?, ?> createTracker(Project project, @Nullable PsiFile before, @Nullable PsiFile after) {
    return new FlashUmlChangeTracker(project, before, after);
  }
}
