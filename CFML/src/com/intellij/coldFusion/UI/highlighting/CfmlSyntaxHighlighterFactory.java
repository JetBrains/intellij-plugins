// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.UI.highlighting;

import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.sql.dialects.SqlDialectMappings;
import com.intellij.sql.highlighting.SqlSyntaxHighlighter;
import org.jetbrains.annotations.NotNull;

public class CfmlSyntaxHighlighterFactory extends SyntaxHighlighterFactory {

  @Override
  @NotNull
  public SyntaxHighlighter getSyntaxHighlighter(final Project project, final VirtualFile virtualFile) {
    if (project != null && virtualFile != null) {
      return new CfmlHighlighter.CfmlFileHighlighter(project);
    }
    return new SqlSyntaxHighlighter(SqlDialectMappings.getDefaultSqlDialect(), project);
  }
}
