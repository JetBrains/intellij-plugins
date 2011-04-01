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
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.osmorc.manifest.lang.headerparser.HeaderParser;
import org.osmorc.manifest.lang.headerparser.HeaderParserRepository;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
class ManifestParser implements PsiParser {

    ManifestParser(@NotNull final HeaderParserRepository headerParserRepository) {
        this.headerParserRepository = headerParserRepository;
    }

    @NotNull
    public ASTNode parse(IElementType root, PsiBuilder builder) {
        builder.setDebugMode(DEBUG_MODE /*|| ApplicationManager.getApplication().isUnitTestMode()*/);
        final PsiBuilder.Marker rootMarker = builder.mark();

        while (!builder.eof()) {
            parse(builder);
        }
        closeAll();

        rootMarker.done(root);
        return builder.getTreeBuilt();
    }

    private void closeAll() {
        closeHeaderValuePart();
        closeAssignmentMarker();
        closeClause();
        closeHeader();
        closeSection();
    }

    protected void parse(PsiBuilder builder) {
        if (sectionMarker == null) {
            sectionMarker = builder.mark();
        }

        final IElementType tokenType = builder.getTokenType();
        if (tokenType == ManifestTokenType.HEADER_NAME) {
            parseHeaderName(builder);
        } else if (tokenType == ManifestTokenType.SECTION_END) {
            closeAll();
            builder.advanceLexer();
        } else if (tokenType == ManifestTokenType.SIGNIFICANT_SPACE ||
                   tokenType == TokenType.BAD_CHARACTER ||
                   tokenType == ManifestTokenType.NEWLINE) {
            builder.advanceLexer();
        } else if (currentHeaderIsSimpleHeader) {
            parseSimpleHeaderValue(builder);
        } else {
            parseComplexHeaderValue(builder);
        }
    }

    private void parseSimpleHeaderValue(PsiBuilder builder) {
        clauseMarker = builder.mark();
        headerValuePartMarker = builder.mark();
        while (!builder.eof() &&
                builder.getTokenType() != ManifestTokenType.SECTION_END &&
                builder.getTokenType() != ManifestTokenType.HEADER_NAME) {
            builder.advanceLexer();
        }
        closeHeaderValuePart();
        closeClause();
        closeHeader();
    }

    private void parseComplexHeaderValue(PsiBuilder builder) {
        while (!builder.eof() &&
                builder.getTokenType() != ManifestTokenType.SECTION_END &&
                builder.getTokenType() != ManifestTokenType.HEADER_NAME) {
            if (clauseMarker == null) {
                clauseMarker = builder.mark();
            }
            if (headerValuePartMarker == null) {
                headerValuePartMarker = builder.mark();
            }
            boolean lexerAdvanced = parseQuotedString(builder) ||
                            parseClause(builder) ||
                            parseParameter(builder) ||
                            parseDirective(builder) ||
                            parseAttribute(builder);
            if (!lexerAdvanced) {
                builder.advanceLexer();
            }
        }

        closeHeaderValuePart();
        closeAssignmentMarker();
        closeClause();
        closeHeader();
    }

    private boolean parseQuotedString(PsiBuilder builder) {
        if (builder.getTokenType() == ManifestTokenType.QUOTE) {
            do {
                builder.advanceLexer();
            } while (!builder.eof() &&
                    builder.getTokenType() != ManifestTokenType.QUOTE &&
                    builder.getTokenType() != ManifestTokenType.SECTION_END &&
                    builder.getTokenType() != ManifestTokenType.HEADER_NAME);
            if (builder.getTokenType() == ManifestTokenType.QUOTE) {
                builder.advanceLexer();
            }
            return true;
        }
        return false;
    }

    private boolean parseClause(PsiBuilder builder) {
        if (builder.getTokenType() == ManifestTokenType.COMMA) {
            closeHeaderValuePart();
            closeAssignmentMarker();
            closeClause();
            builder.advanceLexer();
            return true;
        }
        return false;
    }

    private boolean parseParameter(PsiBuilder builder) {
        if (builder.getTokenType() == ManifestTokenType.SEMICOLON) {
            closeHeaderValuePart();
            closeAssignmentMarker();
            builder.advanceLexer();
            return true;
        }
        return false;
    }

    private boolean parseDirective(PsiBuilder builder) {
        if (builder.getTokenType() == ManifestTokenType.COLON && assignmentMarkerType == null) {
            assignmentMarker = headerValuePartMarker.precede();
            assignmentMarkerType = ManifestElementTypes.DIRECTIVE;
            closeHeaderValuePart();
            builder.advanceLexer();
            if (builder.getTokenType() == ManifestTokenType.NEWLINE) {
                builder.advanceLexer();
                if (builder.getTokenType() == ManifestTokenType.SIGNIFICANT_SPACE) {
                    builder.advanceLexer();
                }
            }
            if (builder.getTokenType() == ManifestTokenType.EQUALS) {
                builder.advanceLexer();
            }
            return true;
        }
        return false;
    }

    private boolean parseAttribute(PsiBuilder builder) {
        if (builder.getTokenType() == ManifestTokenType.EQUALS && assignmentMarkerType == null) {
            assignmentMarker = headerValuePartMarker.precede();
            assignmentMarkerType = ManifestElementTypes.ATTRIBUTE;
            closeHeaderValuePart();
            builder.advanceLexer();
            return true;
        }
        return false;
    }

    private void parseHeaderName(PsiBuilder builder) {
        closeHeader();
        headerMarker = builder.mark();
        currentHeaderName = builder.getTokenText();

        HeaderParser headerParser = headerParserRepository.getHeaderParser(currentHeaderName);
        currentHeaderIsSimpleHeader = headerParser == null || headerParser.isSimpleHeader();

        builder.advanceLexer();

        while (!builder.eof() && builder.getTokenType() != ManifestTokenType.COLON && builder.getTokenType() != ManifestTokenType.NEWLINE) {
            builder.advanceLexer();
        }

        builder.advanceLexer();
    }

    private void closeHeader() {
        if (headerMarker != null) {
            headerMarker.done(ManifestElementTypes.HEADER);
            headerMarker = null;
        }
    }

    private void closeClause() {
        if (clauseMarker != null) {
            clauseMarker.done(ManifestElementTypes.CLAUSE);
            clauseMarker = null;
        }
    }

    private void closeSection() {
        if (sectionMarker != null) {
            sectionMarker.done(ManifestElementTypes.SECTION);
            sectionMarker = null;
        }
    }

    private void closeAssignmentMarker() {
        if (assignmentMarker != null) {
            assignmentMarker.done(assignmentMarkerType);
            assignmentMarker = null;
            assignmentMarkerType = null;
        }
    }

    private void closeHeaderValuePart() {
        if (headerValuePartMarker != null) {
            headerValuePartMarker.done(ManifestElementTypes.HEADER_VALUE_PART);
            headerValuePartMarker = null;
        }
    }

    private String currentHeaderName;
    private boolean currentHeaderIsSimpleHeader;
    private final HeaderParserRepository headerParserRepository;
    private PsiBuilder.Marker headerValuePartMarker;
    private PsiBuilder.Marker sectionMarker;
    private PsiBuilder.Marker headerMarker;
    private PsiBuilder.Marker clauseMarker;
    private PsiBuilder.Marker assignmentMarker;
    private IElementType assignmentMarkerType;

    private static final boolean DEBUG_MODE = Boolean.getBoolean("Osmorc.debug");
}
