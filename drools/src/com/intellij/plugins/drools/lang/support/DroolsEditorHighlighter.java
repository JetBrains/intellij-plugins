// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.support;

import com.intellij.ide.highlighter.JavaFileHighlighter;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.util.LayerDescriptor;
import com.intellij.openapi.editor.ex.util.LayeredLexerEditorHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.plugins.drools.lang.highlight.DroolsSyntaxHighlighter;
import com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DroolsEditorHighlighter extends LayeredLexerEditorHighlighter{

  @Nullable private final VirtualFile myVirtualFile;

  public DroolsEditorHighlighter(@Nullable final Project project,
                                 @Nullable final VirtualFile virtualFile,
                                 @NotNull final EditorColorsScheme colors) {
    super(new DroolsSyntaxHighlighter(), colors);
    myVirtualFile = virtualFile;
    registerLayer(DroolsTokenTypes.JAVA_STATEMENT, new LayerDescriptor(new JavaFileHighlighter(), ""));
  }
}
