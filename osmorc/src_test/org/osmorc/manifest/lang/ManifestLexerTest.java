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

import com.intellij.psi.tree.IElementType;
import static com.intellij.psi.TokenType.BAD_CHARACTER;
import com.intellij.openapi.util.Pair;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import static org.osmorc.manifest.lang.ManifestTokenType.*;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class ManifestLexerTest {
    @Test
    public void testValid() {
        checkTokens("Name: Value",
                token(HEADER_NAME, 4),
                token(COLON, 1),
                token(SIGNIFICANT_SPACE, 1),
                token(HEADER_VALUE_PART, 5));
    }

    @Test
    public void testInvalidSpaceBeforeColon() {
        checkTokens("Name : Value",
                token(HEADER_NAME, 4),
                token(BAD_CHARACTER, 1),
                token(COLON, 1),
                token(SIGNIFICANT_SPACE, 1),
                token(HEADER_VALUE_PART, 5));
    }

    @Test
    public void testMissingSpaceAfterColon() {
        checkTokens("Name:Value",
                token(HEADER_NAME, 4),
                token(COLON, 1),
                token(BAD_CHARACTER, 1),
                token(HEADER_VALUE_PART, 4));
    }

    @Test
    public void testTwoLines() {
        checkTokens("Name: Value\nName2: Value2",
                token(HEADER_NAME, 4),
                token(COLON, 1),
                token(SIGNIFICANT_SPACE, 1),
                token(HEADER_VALUE_PART, 5),
                token(NEWLINE, 1),
                token(HEADER_NAME, 5),
                token(COLON, 1),
                token(SIGNIFICANT_SPACE, 1),
                token(HEADER_VALUE_PART, 6));
    }

    @Test
    public void testContinuation() {
        checkTokens("Name: Value\n Value2",
                token(HEADER_NAME, 4),
                token(COLON, 1),
                token(SIGNIFICANT_SPACE, 1),
                token(HEADER_VALUE_PART, 5),
                token(NEWLINE, 1),
                token(SIGNIFICANT_SPACE, 1),
                token(HEADER_VALUE_PART, 6));
    }

    @Test
    public void testSection() {
        checkTokens("Name: Value\n\nName2: Value2",
                token(HEADER_NAME, 4),
                token(COLON, 1),
                token(SIGNIFICANT_SPACE, 1),
                token(HEADER_VALUE_PART, 5),
                token(NEWLINE, 1),
                token(SECTION_END, 1),
                token(HEADER_NAME, 5),
                token(COLON, 1),
                token(SIGNIFICANT_SPACE, 1),
                token(HEADER_VALUE_PART, 6));
    }

    @Test
    public void testNoIgnoredSpaces() {
        checkTokens("Name: Value \n   Value2",
                token(HEADER_NAME, 4),
                token(COLON, 1),
                token(SIGNIFICANT_SPACE, 1),
                token(HEADER_VALUE_PART, 6),
                token(NEWLINE, 1),
                token(SIGNIFICANT_SPACE, 1),
                token(HEADER_VALUE_PART, 8));
    }

    @Test
    public void testSpecialCharacters() {
        checkTokens("Name: ;:=,\"",
                token(HEADER_NAME, 4),
                token(COLON, 1),
                token(SIGNIFICANT_SPACE, 1),
                token(SEMICOLON, 1),
                token(COLON, 1),
                token(EQUALS, 1),
                token(COMMA, 1),
                token(QUOTE, 1));
    }

    @Test
    public void testErrorEndsAtNewline() {
        checkTokens("Name \n value",
                token(HEADER_NAME, 4),
                token(BAD_CHARACTER, 1),
                token(NEWLINE, 1),
                token(SIGNIFICANT_SPACE, 1),
                token(HEADER_VALUE_PART, 5));
    }

    @Test
    public void testNewlineBetweenSpecialChars() {
        checkTokens("Name: ab;dir:\n =value\n",
                token(HEADER_NAME, 4),
                token(COLON, 1),
                token(SIGNIFICANT_SPACE, 1),
                token(HEADER_VALUE_PART, 2),
                token(SEMICOLON, 1),
                token(HEADER_VALUE_PART, 3),
                token(COLON, 1),
                token(NEWLINE, 1),
                token(SIGNIFICANT_SPACE, 1),
                token(EQUALS, 1),
                token(HEADER_VALUE_PART, 5),
                token(NEWLINE, 1)
        );
    }

    @Test
    public void testBadHeaderStart() {
        checkTokens("Name: ab;dir:\n=value;a:=b\n",
                token(HEADER_NAME, 4),
                token(COLON, 1),
                token(SIGNIFICANT_SPACE, 1),
                token(HEADER_VALUE_PART, 2),
                token(SEMICOLON, 1),
                token(HEADER_VALUE_PART, 3),
                token(COLON, 1),
                token(NEWLINE, 1),
                token(HEADER_NAME, 0),
                token(BAD_CHARACTER, 1),
                token(BAD_CHARACTER, 1),
                token(BAD_CHARACTER, 1),
                token(BAD_CHARACTER, 1),
                token(BAD_CHARACTER, 1),
                token(BAD_CHARACTER, 1),
                token(BAD_CHARACTER, 1),
                token(BAD_CHARACTER, 1),
                token(COLON, 1),
                token(BAD_CHARACTER, 1),
                token(HEADER_VALUE_PART, 1),
                token(NEWLINE, 1)
        );
    }

    private void checkTokens(String manifest, Pair<IElementType,Integer>... tokens) {
        ManifestLexer lexer = new ManifestLexer();
        lexer.start(manifest);
        int lastTokenEnd = 0;
        for (Pair<IElementType,Integer> token : tokens) {
            IElementType tokenType = token.getFirst();
            int tokenStart = lastTokenEnd;
            int tokenEnd = tokenStart + token.getSecond();

            assertThat(lexer.getTokenType(), sameInstance(tokenType));
            assertThat(lexer.getTokenStart(), equalTo(tokenStart));
            assertThat(lexer.getTokenEnd(), equalTo(tokenEnd));
            lastTokenEnd = lexer.getTokenEnd();
            lexer.advance();
        }
        assertThat(lexer.getTokenType(), nullValue());
        assertThat(lexer.getTokenStart(), equalTo(lastTokenEnd));
        assertThat(lexer.getTokenEnd(), equalTo(lastTokenEnd));
    }

    private Pair<IElementType,Integer> token(IElementType type, int length) {
        return new Pair<IElementType, Integer>(type, length);
    }
}
