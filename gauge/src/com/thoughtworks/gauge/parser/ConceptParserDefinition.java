/*
 * Copyright (C) 2020 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.thoughtworks.gauge.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import com.thoughtworks.gauge.language.Concept;
import com.thoughtworks.gauge.language.ConceptFile;
import com.thoughtworks.gauge.language.token.ConceptTokenTypes;
import com.thoughtworks.gauge.lexer.ConceptLexer;
import org.jetbrains.annotations.NotNull;

public final class ConceptParserDefinition implements ParserDefinition {

  public static final TokenSet WHITE_SPACES = TokenSet.WHITE_SPACE;
  public static final TokenSet COMMENTS = TokenSet.create(ConceptTokenTypes.CONCEPT_COMMENT);

  public static final IFileElementType FILE = new IFileElementType(Language.findInstance(Concept.class));

  @NotNull
  @Override
  public Lexer createLexer(Project project) {
    return new ConceptLexer();
  }

  @Override
  @NotNull
  public TokenSet getWhitespaceTokens() {
    return WHITE_SPACES;
  }

  @Override
  @NotNull
  public TokenSet getCommentTokens() {
    return COMMENTS;
  }

  @Override
  @NotNull
  public TokenSet getStringLiteralElements() {
    return TokenSet.EMPTY;
  }

  @Override
  @NotNull
  public PsiParser createParser(final Project project) {
    return new ConceptParser();
  }

  @Override
  public @NotNull IFileElementType getFileNodeType() {
    return FILE;
  }

  @Override
  public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
    return new ConceptFile(viewProvider);
  }

  @Override
  public @NotNull SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
    return SpaceRequirements.MAY;
  }

  @Override
  @NotNull
  public PsiElement createElement(ASTNode node) {
    return ConceptTokenTypes.Factory.createElement(node);
  }
}
