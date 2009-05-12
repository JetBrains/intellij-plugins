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

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: Robert F. Beeger (robert@beeger.net)
 */
class ManifestParser implements PsiParser
{
  @NotNull
  public ASTNode parse(IElementType root, PsiBuilder builder)
  {
    builder.setDebugMode(true);
    final PsiBuilder.Marker rootMarker = builder.mark();

    while (!builder.eof())
    {
      parse(builder);
    }

    closeBinMarker(null);
    if (_clauseMarker != null)
    {
      _clauseMarker.done(ManifestElementTypes.CLAUSE);
      _clauseMarker = null;
    }
    if (_headerMarker != null)
    {
      _headerMarker.done(ManifestElementTypes.HEADER);
      _headerMarker = null;
    }
    rootMarker.done(root);
    return builder.getTreeBuilt();
  }

  protected void parse(PsiBuilder builder)
  {
    IElementType tokenType = builder.getTokenType();
    if (tokenType == ManifestTokenTypes.HEADER_NAME)
    {
      closeBinMarker(null);
      parseHeaderName(builder);
    }
    else if (isValueAttributeOrDirective(tokenType))
    {
      if (_clauseMarker == null)
      {
        _clauseMarker = builder.mark();
      }
      _headerValuePartMarker = builder.mark();
      builder.advanceLexer();
      tokenType = builder.getTokenType();
      if (isValueAttributeOrDirective(tokenType))
      {
        _headerValuePartMarker.error("Separator between header value parts missing");
      }
      else
      {
        _headerValuePartMarker.done(ManifestElementTypes.HEADER_VALUE);
      }
      closeBinMarker(null);
    }
    else if (tokenType == ManifestTokenTypes.DIRECTIVE_ASSIGNMENT ||
        tokenType == ManifestTokenTypes.ATTRIBUTE_ASSIGNMENT)
    {
      closeBinMarker("Cannot concatenate or cascade directives or attribute assignements.");
      if (_headerValuePartMarker != null)
      {
        _binMarker = _headerValuePartMarker.precede();
        _binMarkerType = tokenType == ManifestTokenTypes.DIRECTIVE_ASSIGNMENT ? ManifestElementTypes.DIRECTIVE :
            ManifestElementTypes.ATTRIBUTE;
      }
      else
      {
        builder.error("Missing left side value");
      }
      builder.advanceLexer();
    }
    else if (tokenType == ManifestTokenTypes.CLAUSE_SEPARATOR)
    {
      closeBinMarker(null);
      if (_clauseMarker != null)
      {
        _clauseMarker.done(ManifestElementTypes.CLAUSE);
        _clauseMarker = null;
      }
      builder.advanceLexer();
    }
    else if (tokenType == ManifestTokenTypes.PARAMETER_SEPARATOR)
    {
      closeBinMarker(null);
      builder.advanceLexer();
    }
    else if (tokenType == ManifestTokenTypes.HEADER_ASSIGNMENT)
    {
      builder.error("Header assignment not allowed here.");
      builder.advanceLexer();
    }
  }

  private boolean isValueAttributeOrDirective(IElementType tokenType)
  {
    return tokenType == ManifestTokenTypes.HEADER_VALUE || tokenType == ManifestTokenTypes.ATTRIBUTE_NAME ||
        tokenType == ManifestTokenTypes.DIRECTIVE_NAME;
  }

  private void closeBinMarker(@Nullable String errorMessage)
  {
    if (_binMarker != null)
    {
      if (errorMessage != null)
      {
        _binMarker.error(errorMessage);
      }
      else
      {
        _binMarker.done(_binMarkerType);
      }
      _binMarker = null;
      _binMarkerType = null;
    }
  }

  private void parseHeaderName(PsiBuilder builder)
  {
    IElementType tokenType;
    if (_clauseMarker != null)
    {
      _clauseMarker.done(ManifestElementTypes.CLAUSE);
      _clauseMarker = null;
    }
    if (_headerMarker != null)
    {
      _headerMarker.done(ManifestElementTypes.HEADER);
      _headerMarker = null;
    }
    _headerMarker = builder.mark();
    PsiBuilder.Marker headerNameMarker = builder.mark();
    builder.advanceLexer();
    tokenType = builder.getTokenType();
    if (tokenType == ManifestTokenTypes.HEADER_ASSIGNMENT)
    {
      headerNameMarker.done(ManifestElementTypes.HEADER_NAME);
    }
    else
    {
      headerNameMarker.error("':' expected");
    }
    builder.advanceLexer();
  }

  private PsiBuilder.Marker _headerMarker;
  private PsiBuilder.Marker _clauseMarker;
  private PsiBuilder.Marker _headerValuePartMarker;
  private PsiBuilder.Marker _binMarker;
  private IElementType _binMarkerType;
}
