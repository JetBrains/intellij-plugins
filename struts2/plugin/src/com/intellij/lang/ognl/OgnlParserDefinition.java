/*
 * Copyright 2011 The authors
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
import com.intellij.lang.ognl.parsing.OgnlElementType;
import com.intellij.lang.ognl.parsing.OgnlParser;
import com.intellij.lang.ognl.psi.OgnlTokenTypes;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

/**
 * @author Yann C&eacute;bron
 */
public class OgnlParserDefinition implements ParserDefinition {

  private static final IFileElementType OGNL_FILE = new IFileElementType(OgnlLanguage.INSTANCE);

  @NotNull
  @Override
  public Lexer createLexer(final Project project) {
    return new OgnlLexer();
  }

  @Override
  public PsiParser createParser(final Project project) {
    return new OgnlParser();
  }

  @Override
  public IFileElementType getFileNodeType() {
    return OGNL_FILE;
  }

  @NotNull
  @Override
  public TokenSet getWhitespaceTokens() {
    return TokenSet.create(TokenType.WHITE_SPACE);
  }

  @NotNull
  @Override
  public TokenSet getCommentTokens() {
    return TokenSet.EMPTY;
  }

  @NotNull
  @Override
  public TokenSet getStringLiteralElements() {
    return OgnlTokenTypes.TEXT;
  }

  @NotNull
  @Override
  public PsiElement createElement(final ASTNode node) {
    final IElementType type = node.getElementType();
    if (type instanceof OgnlElementType) {
      return ((OgnlElementType) type).createPsiElement(node);
    }

    throw new AssertionError("Unknown type: " + type);
  }

  @Override
  public PsiFile createFile(final FileViewProvider fileViewProvider) {
    return new OgnlFile(fileViewProvider);
  }

  @Override
  public SpaceRequirements spaceExistanceTypeBetweenTokens(final ASTNode left, final ASTNode right) {
    final Lexer lexer = createLexer(left.getPsi().getProject());
    return LanguageUtil.canStickTokensTogetherByLexer(left, right, lexer);
  }

}