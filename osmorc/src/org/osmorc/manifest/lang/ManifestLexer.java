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

import com.intellij.lexer.LexerBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class ManifestLexer extends LexerBase {
    public ManifestLexer() {
    }

    public void start(CharSequence buffer, int startOffset, int endOffset, int initialState) {
        this.buffer = buffer;
        this.endOffset = endOffset;
        currentState = initialState;

        tokenStart = startOffset;
        parseNextToken();
    }

    public void advance() {
        tokenStart = tokenEnd;
        parseNextToken();
    }

    public int getState() {
        return currentState;
    }

    @Nullable
    public IElementType getTokenType() {
        return tokenType;
    }

    public int getTokenStart() {
        return tokenStart;
    }

    public int getTokenEnd() {
        return tokenEnd;
    }

    public int getBufferEnd() {
        return endOffset;
    }

    public CharSequence getBufferSequence() {
        return buffer;
    }

    private void parseNextToken() {
        if (tokenStart < endOffset) {
            if (isNewline(tokenStart)) {
                tokenType = isLineStart(tokenStart) ? ManifestTokenType.SECTION_END : ManifestTokenType.NEWLINE;
                tokenEnd = tokenStart + 1;
                currentState = INITIAL_STATE;
            } else if (currentState == WAITING_FOR_HEADER_ASSIGNMENT_STATE ||
                    currentState == WAITING_FOR_HEADER_ASSIGNMENT_AFTER_BAD_CHARACTER_STATE) {
                if (isColon(tokenStart)) {
                    tokenType = ManifestTokenType.COLON;
                    currentState = WAITING_FOR_SPACE_AFTER_HEADER_NAME_STATE;
                } else {
                    tokenType = TokenType.BAD_CHARACTER;
                    currentState = WAITING_FOR_HEADER_ASSIGNMENT_AFTER_BAD_CHARACTER_STATE;
                }
                tokenEnd = tokenStart + 1;
            } else if (currentState == WAITING_FOR_SPACE_AFTER_HEADER_NAME_STATE) {
                if (isSpace(tokenStart)) {
                    tokenType = ManifestTokenType.SIGNIFICANT_SPACE;
                } else {
                    tokenType = TokenType.BAD_CHARACTER;
                }
                currentState = INITIAL_STATE;
                tokenEnd = tokenStart + 1;
            } else if (isHeaderStart(tokenStart)) {
                if (isAlphaNum(tokenStart)) {
                    tokenEnd = tokenStart + 1;
                    while (tokenEnd < endOffset && isHeaderChar(tokenEnd)) {
                        tokenEnd++;
                    }
                }
                tokenType = ManifestTokenType.HEADER_NAME;
                currentState = WAITING_FOR_HEADER_ASSIGNMENT_STATE;
            } else if (isContinuationStart(tokenStart)) {
                tokenType = ManifestTokenType.SIGNIFICANT_SPACE;
                tokenEnd = tokenStart + 1;
                currentState = INITIAL_STATE;
            } else if (isSpecialCharacter(tokenStart)) {
                tokenType = getTokenTypeForSpecialCharacter(tokenStart);
                tokenEnd = tokenStart + 1;
                currentState = INITIAL_STATE;
            } else {
                tokenEnd = tokenStart;
                while (tokenEnd < endOffset && !isSpecialCharacter(tokenEnd) && !isNewline(tokenEnd)) {
                    tokenEnd++;
                }
                tokenType = ManifestTokenType.HEADER_VALUE_PART;
            }
        } else {
            tokenType = null;
            tokenEnd = tokenStart;
        }
    }

    private boolean isNewline(int position) {
        return '\n' == buffer.charAt(position);
    }

    private boolean isHeaderStart(int position) {
        return isLineStart(position) && !Character.isWhitespace(buffer.charAt(position));
    }

    private boolean isAlphaNum(int position) {
        return Character.isLetterOrDigit(buffer.charAt(position));
    }

    private boolean isHeaderChar(int position) {
        return isAlphaNum(position) || buffer.charAt(position) == '-' || buffer.charAt(position) == '_';
    }

    private boolean isContinuationStart(int position) {
        return isLineStart(position) && !isHeaderStart(position);
    }

    private boolean isLineStart(int position) {
        return (position == 0 || isNewline(position - 1));
    }

    private boolean isSpace(int position) {
        return buffer.charAt(position) == ' ';
    }

    private boolean isColon(int position) {
        return buffer.charAt(position) == ':';
    }

    private boolean isSpecialCharacter(int position) {
        return SPECIAL_CHARACTERS_TOKEN_MAPPING.get(buffer.charAt(position)) != null;
    }

    private IElementType getTokenTypeForSpecialCharacter(int position) {
        return SPECIAL_CHARACTERS_TOKEN_MAPPING.get(buffer.charAt(position));
    }

    private CharSequence buffer;
    private int endOffset;
    private int tokenStart;
    private int tokenEnd;
    private int currentState;
    private IElementType tokenType;

    private static final int INITIAL_STATE = 0;
    private static final int WAITING_FOR_HEADER_ASSIGNMENT_STATE = 1;
    private static final int WAITING_FOR_HEADER_ASSIGNMENT_AFTER_BAD_CHARACTER_STATE = 2;
    private static final int WAITING_FOR_SPACE_AFTER_HEADER_NAME_STATE = 3;

    private static final Map<Character, IElementType> SPECIAL_CHARACTERS_TOKEN_MAPPING;

    static {
        SPECIAL_CHARACTERS_TOKEN_MAPPING = new HashMap<Character, IElementType>();
        SPECIAL_CHARACTERS_TOKEN_MAPPING.put(':', ManifestTokenType.COLON);
        SPECIAL_CHARACTERS_TOKEN_MAPPING.put(';', ManifestTokenType.SEMICOLON);
        SPECIAL_CHARACTERS_TOKEN_MAPPING.put(',', ManifestTokenType.COMMA);
        SPECIAL_CHARACTERS_TOKEN_MAPPING.put('=', ManifestTokenType.EQUALS);
        SPECIAL_CHARACTERS_TOKEN_MAPPING.put('\"', ManifestTokenType.QUOTE);
}
}
