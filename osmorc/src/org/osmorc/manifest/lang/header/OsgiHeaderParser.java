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
package org.osmorc.manifest.lang.header;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.lang.manifest.header.HeaderParser;
import org.jetbrains.lang.manifest.header.impl.StandardHeaderParser;
import org.jetbrains.lang.manifest.parser.ManifestParser;
import org.jetbrains.lang.manifest.psi.ManifestElementType;
import org.jetbrains.lang.manifest.psi.ManifestTokenType;
import org.osmorc.manifest.lang.psi.OsgiManifestElementType;

import static com.intellij.lang.PsiBuilderUtil.expect;

/**
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
public class OsgiHeaderParser extends StandardHeaderParser {
  public static final HeaderParser INSTANCE = new OsgiHeaderParser();

  private static final TokenSet CLAUSE_END_TOKENS = TokenSet.orSet(
    ManifestParser.HEADER_END_TOKENS, TokenSet.create(ManifestTokenType.COMMA));
  private static final TokenSet SUB_CLAUSE_END_TOKENS = TokenSet.orSet(
    CLAUSE_END_TOKENS, TokenSet.create(ManifestTokenType.SEMICOLON));

  @Override
  public void parse(@NotNull PsiBuilder builder) {
    while (!builder.eof()) {
      if (!parseClause(builder)) {
        break;
      }

      IElementType tokenType = builder.getTokenType();
      if (ManifestParser.HEADER_END_TOKENS.contains(tokenType)) {
        break;
      }
      else if (tokenType == ManifestTokenType.COMMA) {
        builder.advanceLexer();
      }
    }
  }

  private static boolean parseClause(PsiBuilder builder) {
    PsiBuilder.Marker clause = builder.mark();
    boolean result = true;

    while (!builder.eof()) {
      if (!parseSubClause(builder, false)) {
        result = false;
        break;
      }

      IElementType tokenType = builder.getTokenType();
      if (CLAUSE_END_TOKENS.contains(tokenType)) {
        break;
      }
      else if (tokenType == ManifestTokenType.SEMICOLON) {
        builder.advanceLexer();
      }
    }

    clause.done(OsgiManifestElementType.CLAUSE);
    return result;
  }

  private static boolean parseSubClause(PsiBuilder builder, boolean assignment) {
    PsiBuilder.Marker marker = builder.mark();
    boolean result = true;

    while (!builder.eof()) {
      IElementType tokenType = builder.getTokenType();
      if (SUB_CLAUSE_END_TOKENS.contains(tokenType)) {
        break;
      }
      else if (tokenType == ManifestTokenType.QUOTE) {
        parseQuotedString(builder);
      }
      else if (!assignment && tokenType == ManifestTokenType.EQUALS) {
        marker.done(ManifestElementType.HEADER_VALUE_PART);
        return parseAttribute(builder, marker.precede());
      }
      else if (!assignment && tokenType == ManifestTokenType.COLON) {
        marker.done(ManifestElementType.HEADER_VALUE_PART);
        return parseDirective(builder, marker.precede());
      }
      else {
        IElementType lastToken = builder.getTokenType();
        builder.advanceLexer();
        if (lastToken == ManifestTokenType.NEWLINE && builder.getTokenType() != ManifestTokenType.SIGNIFICANT_SPACE) {
          result = false;
          break;
        }
      }
    }

    marker.done(ManifestElementType.HEADER_VALUE_PART);
    return result;
  }

  private static void parseQuotedString(PsiBuilder builder) {
    do {
      builder.advanceLexer();
    }
    while (!builder.eof() &&
           !ManifestParser.HEADER_END_TOKENS.contains(builder.getTokenType()) &&
           !expect(builder, ManifestTokenType.QUOTE));
  }

  private static boolean parseAttribute(PsiBuilder builder, PsiBuilder.Marker marker) {
    builder.advanceLexer();
    boolean result = parseSubClause(builder, true);
    marker.done(OsgiManifestElementType.ATTRIBUTE);
    return result;
  }

  private static boolean parseDirective(PsiBuilder builder, PsiBuilder.Marker marker) {
    builder.advanceLexer();

    if (expect(builder, ManifestTokenType.NEWLINE)) {
      expect(builder, ManifestTokenType.SIGNIFICANT_SPACE);
    }
    expect(builder, ManifestTokenType.EQUALS);

    boolean result = parseSubClause(builder, true);

    marker.done(OsgiManifestElementType.DIRECTIVE);
    return result;
  }
}
