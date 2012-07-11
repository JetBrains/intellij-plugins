package org.jetbrains.plugins.cucumber.psi;

import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * @author yole
 */
public class GherkinSyntaxHighlighterFactory extends SyntaxHighlighterFactory {
  @NotNull
  public SyntaxHighlighter getSyntaxHighlighter(Project project, VirtualFile virtualFile) {
    final GherkinKeywordProvider keywordProvider = project != null
                                                   ? CucumberLanguageService.getInstance(project).getKeywordProvider()
                                                   : new PlainGherkinKeywordProvider(); 
    return new GherkinSyntaxHighlighter(keywordProvider);
  }
}
