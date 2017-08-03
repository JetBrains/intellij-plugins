package com.intellij.javascript.flex.compiled;

import com.intellij.openapi.fileTypes.SyntaxHighlighterProvider;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SwfSyntaxHighlighterProvider implements SyntaxHighlighterProvider {
  @Override
  public SyntaxHighlighter create(@NotNull FileType fileType, @Nullable Project project, @Nullable VirtualFile file) {
    return SyntaxHighlighterFactory.getSyntaxHighlighter(JavaScriptSupportLoader.ECMA_SCRIPT_L4, project, file);
  }
}
