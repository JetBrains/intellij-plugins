/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.osmorc.manifest.lang;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.LanguageUtil;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
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
import org.osmorc.manifest.lang.psi.*;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class ManifestParserDefinition implements ParserDefinition
{
  @NotNull
  public Lexer createLexer(Project project)
  {
    return new ManifestLexer();
  }

  public PsiParser createParser(Project project)
  {
    return new ManifestParser();
  }

  public IFileElementType getFileNodeType()
  {
    return ManifestElementTypes.FILE;
  }

  @NotNull
  public TokenSet getWhitespaceTokens()
  {
    return TokenSet.create(TokenType.WHITE_SPACE);
  }

  @NotNull
  public TokenSet getCommentTokens()
  {
    return TokenSet.EMPTY;
  }

  @NotNull
  public TokenSet getStringLiteralElements()
  {
    return new TokenSet(); // TODO: Check wether this should be implemented
  }

  @NotNull
  public PsiElement createElement(ASTNode node)
  {
    final IElementType type = node.getElementType();
    if (type == ManifestElementTypes.HEADER)
    {
      return new ManifestHeader(node);
    }
    else if (type == ManifestElementTypes.HEADER_NAME)
    {
      return new ManifestHeaderName(node);
    }
    else if (type == ManifestElementTypes.HEADER_VALUE)
    {
      return new ManifestHeaderValueImpl(node);
    }
    else if (type == ManifestElementTypes.CLAUSE)
    {
      return new ManifestClause(node);
    }
    else if (type == ManifestElementTypes.DIRECTIVE)
    {
      return new ManifestDirective(node);
    }
    else if (type == ManifestElementTypes.ATTRIBUTE)
    {
      return new ManifestAttribute(node);
    }
    return new ASTWrapperPsiElement(node);
  }

  public PsiFile createFile(FileViewProvider viewProvider)
  {
    return new ManifestFile(viewProvider);
  }

  @SuppressWarnings({"MethodNameWithMistakes"})
  public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right)
  {
    Lexer lexer = createLexer(left.getPsi().getProject());
    return LanguageUtil.canStickTokensTogetherByLexer(left, right, lexer, 0);
  }
}
