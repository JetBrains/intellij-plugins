// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools;

import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.fileTypes.EditorHighlighterProvider;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.plugins.drools.lang.support.DroolsEditorHighlighter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DroolsEditorHighlighterProvider implements EditorHighlighterProvider {
  @Override
  public EditorHighlighter getEditorHighlighter(@Nullable Project project,
                                                @NotNull FileType fileType, @Nullable VirtualFile virtualFile,
                                                @NotNull EditorColorsScheme colors) {
    return new DroolsEditorHighlighter(project, virtualFile, colors);
  }
}
