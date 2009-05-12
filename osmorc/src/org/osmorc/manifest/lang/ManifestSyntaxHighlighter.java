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

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.SyntaxHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class ManifestSyntaxHighlighter extends SyntaxHighlighterBase
{
  @NotNull
  public Lexer getHighlightingLexer()
  {
    return new ManifestLexer();
  }

  @NotNull
  public TextAttributesKey[] getTokenHighlights(IElementType tokenType)
  {
    return pack(KEYS.get(tokenType));
  }

  private static final Map<IElementType, TextAttributesKey> KEYS;

  private static final String HEADER_NAME_ID = "osmorc.headerName";
  private static final String HEADER_VALUE_ID = "osmorc.headerValue";
  private static final String HEADER_ASSIGNMENT_ID = "osmorc.headerAssignment";
  private static final String ATTRIBUTE_ASSIGNMENT_ID = "osmorc.attributeAssignment";
  private static final String DIRECTIVE_ASSIGNMENT_ID = "osmorc.directiveAssignment";
  private static final String ATTRIBUTE_NAME_ID = "osmorc.attributeName";
  private static final String DIRECTIVE_NAME_ID = "osmorc.directiveName";
  private static final String CLAUSE_SEPARATOR_ID = "osmorc.clauseSeparator";
  private static final String PARAMETER_SEPARATOR_ID = "osmorc.parameterSeparator";

  static final TextAttributesKey HEADER_NAME_KEY =
      TextAttributesKey.createTextAttributesKey(HEADER_NAME_ID,
          SyntaxHighlighterColors.KEYWORD.getDefaultAttributes());
  static final TextAttributesKey HEADER_VALUE_KEY =
      TextAttributesKey.createTextAttributesKey(HEADER_VALUE_ID,
          HighlighterColors.TEXT.getDefaultAttributes());
  static final TextAttributesKey DIRECTIVE_NAME_KEY =
      TextAttributesKey.createTextAttributesKey(DIRECTIVE_NAME_ID,
          TextAttributesKey.createTextAttributesKey("INSTANCE_FIELD_ATTRIBUTES").getDefaultAttributes());
  static final TextAttributesKey ATTRIBUTE_NAME_KEY =
      TextAttributesKey.createTextAttributesKey(ATTRIBUTE_NAME_ID,
          TextAttributesKey.createTextAttributesKey("INSTANCE_FIELD_ATTRIBUTES").getDefaultAttributes());

  static final TextAttributesKey HEADER_ASSIGNMENT_KEY =
      TextAttributesKey.createTextAttributesKey(HEADER_ASSIGNMENT_ID,
          SyntaxHighlighterColors.COMMA.getDefaultAttributes());
  static final TextAttributesKey ATTRIBUTE_ASSIGNMENT_KEY =
      TextAttributesKey.createTextAttributesKey(ATTRIBUTE_ASSIGNMENT_ID,
          SyntaxHighlighterColors.COMMA.getDefaultAttributes());
  static final TextAttributesKey DIRECTIVE_ASSIGNMENT_KEY =
      TextAttributesKey.createTextAttributesKey(DIRECTIVE_ASSIGNMENT_ID,
          SyntaxHighlighterColors.COMMA.getDefaultAttributes());
  static final TextAttributesKey CLAUSE_SEPARATOR_KEY =
      TextAttributesKey.createTextAttributesKey(CLAUSE_SEPARATOR_ID,
          SyntaxHighlighterColors.COMMA.getDefaultAttributes());
  static final TextAttributesKey PARAMETER_SEPARATOR_KEY =
      TextAttributesKey.createTextAttributesKey(PARAMETER_SEPARATOR_ID,
          SyntaxHighlighterColors.COMMA.getDefaultAttributes());

  static
  {
    KEYS = new HashMap<IElementType, TextAttributesKey>();

    KEYS.put(ManifestTokenTypes.HEADER_NAME, HEADER_NAME_KEY);
    KEYS.put(ManifestTokenTypes.HEADER_VALUE, HEADER_VALUE_KEY);
    KEYS.put(ManifestTokenTypes.DIRECTIVE_NAME, DIRECTIVE_NAME_KEY);
    KEYS.put(ManifestTokenTypes.ATTRIBUTE_NAME, ATTRIBUTE_NAME_KEY);
    KEYS.put(ManifestTokenTypes.ATTRIBUTE_ASSIGNMENT, ATTRIBUTE_ASSIGNMENT_KEY);
    KEYS.put(ManifestTokenTypes.DIRECTIVE_ASSIGNMENT, DIRECTIVE_ASSIGNMENT_KEY);
    KEYS.put(ManifestTokenTypes.CLAUSE_SEPARATOR, CLAUSE_SEPARATOR_KEY);
    KEYS.put(ManifestTokenTypes.HEADER_ASSIGNMENT, HEADER_ASSIGNMENT_KEY);
    KEYS.put(ManifestTokenTypes.PARAMETER_SEPARATOR, PARAMETER_SEPARATOR_KEY);
  }

}
