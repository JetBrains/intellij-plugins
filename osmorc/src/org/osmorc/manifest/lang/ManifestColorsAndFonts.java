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

import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.SyntaxHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.colors.TextAttributesKeyDefaults;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class ManifestColorsAndFonts {
  static final TextAttributesKey HEADER_NAME_KEY =
    TextAttributesKeyDefaults.createTextAttributesKey("osmorc.headerName",
                                                      TextAttributesKeyDefaults.getDefaultAttributes(SyntaxHighlighterColors.KEYWORD));
  static final TextAttributesKey HEADER_VALUE_KEY =
    TextAttributesKeyDefaults.createTextAttributesKey("osmorc.headerValue",
                                                      TextAttributesKeyDefaults.getDefaultAttributes(HighlighterColors.TEXT));
  static final TextAttributesKey DIRECTIVE_NAME_KEY =
    TextAttributesKeyDefaults.createTextAttributesKey("osmorc.directiveName",
                                                      TextAttributesKeyDefaults
                                                        .getDefaultAttributes(
                                                          TextAttributesKey.createTextAttributesKey("INSTANCE_FIELD_ATTRIBUTES")));
  static final TextAttributesKey ATTRIBUTE_NAME_KEY =
    TextAttributesKeyDefaults.createTextAttributesKey("osmorc.attributeName",
                                                      TextAttributesKeyDefaults
                                                        .getDefaultAttributes(
                                                          TextAttributesKey.createTextAttributesKey("INSTANCE_FIELD_ATTRIBUTES")));
  static final TextAttributesKey HEADER_ASSIGNMENT_KEY =
    TextAttributesKeyDefaults.createTextAttributesKey("osmorc.headerAssignment",
                                                      TextAttributesKeyDefaults.getDefaultAttributes(SyntaxHighlighterColors.COMMA));
  static final TextAttributesKey ATTRIBUTE_ASSIGNMENT_KEY =
    TextAttributesKeyDefaults.createTextAttributesKey("osmorc.attributeAssignment",
                                                      TextAttributesKeyDefaults.getDefaultAttributes(SyntaxHighlighterColors.COMMA));
  static final TextAttributesKey DIRECTIVE_ASSIGNMENT_KEY =
    TextAttributesKeyDefaults.createTextAttributesKey("osmorc.directiveAssignment",
                                                      TextAttributesKeyDefaults.getDefaultAttributes(SyntaxHighlighterColors.COMMA));
  static final TextAttributesKey CLAUSE_SEPARATOR_KEY =
    TextAttributesKeyDefaults.createTextAttributesKey("osmorc.clauseSeparator",
                                                      TextAttributesKeyDefaults.getDefaultAttributes(SyntaxHighlighterColors.COMMA));
  static final TextAttributesKey PARAMETER_SEPARATOR_KEY =
    TextAttributesKeyDefaults.createTextAttributesKey("osmorc.parameterSeparator",
                                                      TextAttributesKeyDefaults.getDefaultAttributes(SyntaxHighlighterColors.COMMA));

  private ManifestColorsAndFonts() {
  }
}
