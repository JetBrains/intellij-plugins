/*
 * Copyright 2013 The authors
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

package com.intellij.lang.ognl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.LanguageUtil;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lang.ognl.lexer.OgnlLexer;
import com.intellij.lang.ognl.parser.OgnlParser;
import com.intellij.lang.ognl.psi.OgnlTokenGroups;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

/**
 * @author Yann C&eacute;bron
 */
public class OgnlParserDefinition implements ParserDefinition {

  private static final IFileElementType OGNL_FILE = new IFileElementType(OgnlLanguage.INSTANCE);

  private static final TokenSet WHITE_SPACE_TOKENS = TokenSet.create(TokenType.WHITE_SPACE);

  @NotNull
  @Override
  public Lexer createLexer(final Project project) {
    return new OgnlLexer();
  }

  @Override
  public @NotNull PsiParser createParser(final Project project) {
    return new OgnlParser();
  }

  @Override
  public @NotNull IFileElementType getFileNodeType() {
    return OGNL_FILE;
  }

  @NotNull
  @Override
  public TokenSet getWhitespaceTokens() {
    return WHITE_SPACE_TOKENS;
  }

  @NotNull
  @Override
  public TokenSet getCommentTokens() {
    return TokenSet.EMPTY;
  }

  @NotNull
  @Override
  public TokenSet getStringLiteralElements() {
    return OgnlTokenGroups.TEXT;
  }

  @NotNull
  @Override
  public PsiElement createElement(final ASTNode node) {
    return OgnlTypes.Factory.createElement(node);
  }

  @Override
  public @NotNull PsiFile createFile(final @NotNull FileViewProvider fileViewProvider) {
    return new OgnlFile(fileViewProvider);
  }

  @Override
  public @NotNull SpaceRequirements spaceExistenceTypeBetweenTokens(final ASTNode left, final ASTNode right) {
    final Lexer lexer = createLexer(left.getPsi().getProject());
    return LanguageUtil.canStickTokensTogetherByLexer(left, right, lexer);
  }
}