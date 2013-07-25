/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
