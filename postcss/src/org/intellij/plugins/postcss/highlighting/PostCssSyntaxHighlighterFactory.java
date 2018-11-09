package org.intellij.plugins.postcss.highlighting;

import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class PostCssSyntaxHighlighterFactory extends SyntaxHighlighterFactory {
  @Override
  @NotNull
  public SyntaxHighlighter getSyntaxHighlighter(final Project project, final VirtualFile virtualFile) {
    return new PostCssSyntaxHighlighter();
  }
}