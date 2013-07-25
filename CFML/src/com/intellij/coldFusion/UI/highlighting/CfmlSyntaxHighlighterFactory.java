package com.intellij.coldFusion.UI.highlighting;

import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.sql.dialects.SqlDialectMappings;
import com.intellij.sql.highlighting.SqlSyntaxHighlighter;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Nadya.Zabrodina
 * Date: 9/5/11
 */
public class CfmlSyntaxHighlighterFactory extends SyntaxHighlighterFactory {

  @NotNull
  public SyntaxHighlighter getSyntaxHighlighter(final Project project, final VirtualFile virtualFile) {
    if (project != null && virtualFile != null) {
      return new CfmlHighlighter.CfmlFileHighlighter(project);
    }
    return new SqlSyntaxHighlighter(SqlDialectMappings.getDefaultSqlDialect(), project);
  }
}
