package org.jetbrains.plugins.cucumber.psi;

import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.i18n.JsonGherkinKeywordProvider;

/**
 * @author yole
 */
public class GherkinSyntaxHighlighterFactory extends SyntaxHighlighterFactory {
  @NotNull
  public SyntaxHighlighter getSyntaxHighlighter(Project project, VirtualFile virtualFile) {
    return new GherkinSyntaxHighlighter(JsonGherkinKeywordProvider.getKeywordProvider());
  }
}
