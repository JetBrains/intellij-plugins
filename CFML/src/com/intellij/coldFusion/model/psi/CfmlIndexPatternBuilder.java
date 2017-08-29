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
package com.intellij.coldFusion.model.psi;


import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.coldFusion.model.lexer.CfmlLexer;
import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.coldFusion.model.parsers.CfmlElementTypes;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lexer.HtmlHighlightingLexer;
import com.intellij.lexer.LayeredLexer;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.search.IndexPatternBuilder;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.sql.dialects.SqlDialectMappings;
import com.intellij.sql.dialects.SqlLanguageDialect;
import org.jetbrains.annotations.NotNull;


/**
 */
public class CfmlIndexPatternBuilder implements IndexPatternBuilder {
  public Lexer getIndexingLexer(@NotNull final PsiFile file) {
    if (file instanceof CfmlFile) {
      Project project = file.getProject();
      SqlLanguageDialect dialect = SqlDialectMappings.getMapping(project, file.getVirtualFile());
      Lexer sqlLexer = LanguageParserDefinitions.INSTANCE.forLanguage(dialect).createLexer(project);

      LayeredLexer cfmlLayeredLexer = new LayeredLexer(new CfmlLexer(true, project));

      cfmlLayeredLexer.registerLayer(new HtmlHighlightingLexer(), CfmlElementTypes.TEMPLATE_TEXT);
      cfmlLayeredLexer.registerLayer(sqlLexer, CfmlElementTypes.SQL);

      return cfmlLayeredLexer;
    }
    return null;
  }

  private static final TokenSet tsCOMMENTS = TokenSet.create(CfmlTokenTypes.COMMENT, CfscriptTokenTypes.COMMENT);
  
  public TokenSet getCommentTokenSet(@NotNull final PsiFile file) {
    if (file instanceof CfmlFile) {
      return tsCOMMENTS;
    }
    return null;
  }

  public int getCommentStartDelta(final IElementType tokenType) {
    return 0;
  }

  public int getCommentEndDelta(final IElementType tokenType) {
    return 0;
  }
}