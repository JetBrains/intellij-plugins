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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class ManifestLexerTest {
  private ManifestLexer myLexer;

  @Before
  public void setUp() throws Exception {
    myLexer = new ManifestLexer();
  }

  @After
  public void tearDown() throws Exception {
    myLexer = null;
  }

  @Test
  public void testValid() {
    doTest("Name: Value",

           "HEADER_NAME_TOKEN ('Name')\n" +
           "COLON_TOKEN (':')\n" +
           "SIGNIFICANT_SPACE_TOKEN (' ')\n" +
           "HEADER_VALUE_PART_TOKEN ('Value')");
  }

  @Test
  public void testInvalidSpaceBeforeColon() {
    doTest("Name : Value",

           "HEADER_NAME_TOKEN ('Name')\n" +
           "BAD_CHARACTER (' ')\n" +
           "COLON_TOKEN (':')\n" +
           "SIGNIFICANT_SPACE_TOKEN (' ')\n" +
           "HEADER_VALUE_PART_TOKEN ('Value')");
  }

  @Test
  public void testMissingSpaceAfterColon() {
    doTest("Name:Value",

           "HEADER_NAME_TOKEN ('Name')\n" +
           "COLON_TOKEN (':')\n" +
           "BAD_CHARACTER ('V')\n" +
           "HEADER_VALUE_PART_TOKEN ('alue')");
  }

  @Test
  public void testTwoHeaders() {
    doTest("Name: Value\nName2: Value2",

           "HEADER_NAME_TOKEN ('Name')\n" +
           "COLON_TOKEN (':')\n" +
           "SIGNIFICANT_SPACE_TOKEN (' ')\n" +
           "HEADER_VALUE_PART_TOKEN ('Value')\n" +
           "NEWLINE_TOKEN ('\n')\n" +
           "HEADER_NAME_TOKEN ('Name2')\n" +
           "COLON_TOKEN (':')\n" +
           "SIGNIFICANT_SPACE_TOKEN (' ')\n" +
           "HEADER_VALUE_PART_TOKEN ('Value2')");
  }

  @Test
  public void testContinuation() {
    doTest("Name: Value\n Value2",

           "HEADER_NAME_TOKEN ('Name')\n" +
           "COLON_TOKEN (':')\n" +
           "SIGNIFICANT_SPACE_TOKEN (' ')\n" +
           "HEADER_VALUE_PART_TOKEN ('Value')\n" +
           "NEWLINE_TOKEN ('\n')\n" +
           "SIGNIFICANT_SPACE_TOKEN (' ')\n" +
           "HEADER_VALUE_PART_TOKEN ('Value2')");
  }

  @Test
  public void testSection() {
    doTest("Name: Value\n\nName2: Value2",

           "HEADER_NAME_TOKEN ('Name')\n" +
           "COLON_TOKEN (':')\n" +
           "SIGNIFICANT_SPACE_TOKEN (' ')\n" +
           "HEADER_VALUE_PART_TOKEN ('Value')\n" +
           "NEWLINE_TOKEN ('\n')\n" +
           "SECTION_END_TOKEN ('\n')\n" +
           "HEADER_NAME_TOKEN ('Name2')\n" +
           "COLON_TOKEN (':')\n" +
           "SIGNIFICANT_SPACE_TOKEN (' ')\n" +
           "HEADER_VALUE_PART_TOKEN ('Value2')");
  }

  @Test
  public void testNoIgnoredSpaces() {
    doTest("Name: Value \n   Value2",

           "HEADER_NAME_TOKEN ('Name')\n" +
           "COLON_TOKEN (':')\n" +
           "SIGNIFICANT_SPACE_TOKEN (' ')\n" +
           "HEADER_VALUE_PART_TOKEN ('Value ')\n" +
           "NEWLINE_TOKEN ('\n')\n" +
           "SIGNIFICANT_SPACE_TOKEN (' ')\n" +
           "HEADER_VALUE_PART_TOKEN ('  Value2')");
  }

  @Test
  public void testSpecialCharacters() {
    doTest("Name: ;:=,\"",

           "HEADER_NAME_TOKEN ('Name')\n" +
           "COLON_TOKEN (':')\n" +
           "SIGNIFICANT_SPACE_TOKEN (' ')\n" +
           "SEMICOLON_TOKEN (';')\n" +
           "COLON_TOKEN (':')\n" +
           "EQUALS_TOKEN ('=')\n" +
           "COMMA_TOKEN (',')\n" +
           "QUOTE_TOKEN ('\"')");
  }

  @Test
  public void testErrorEndsAtNewline() {
    doTest("Name \n value",

           "HEADER_NAME_TOKEN ('Name')\n" +
           "BAD_CHARACTER (' ')\n" +
           "NEWLINE_TOKEN ('\n')\n" +
           "SIGNIFICANT_SPACE_TOKEN (' ')\n" +
           "HEADER_VALUE_PART_TOKEN ('value')");
  }

  @Test
  public void testNewlineBetweenSpecialChars() {
    doTest("Name: ab;dir:\n =value\n",

           "HEADER_NAME_TOKEN ('Name')\n" +
           "COLON_TOKEN (':')\n" +
           "SIGNIFICANT_SPACE_TOKEN (' ')\n" +
           "HEADER_VALUE_PART_TOKEN ('ab')\n" +
           "SEMICOLON_TOKEN (';')\n" +
           "HEADER_VALUE_PART_TOKEN ('dir')\n" +
           "COLON_TOKEN (':')\n" +
           "NEWLINE_TOKEN ('\n')\n" +
           "SIGNIFICANT_SPACE_TOKEN (' ')\n" +
           "EQUALS_TOKEN ('=')\n" +
           "HEADER_VALUE_PART_TOKEN ('value')\n" +
           "NEWLINE_TOKEN ('\n')");
  }

  @Test
  public void testBadHeaderStart() {
    doTest("Name: ab;dir:\n=value;a:=b\n",

           "HEADER_NAME_TOKEN ('Name')\n" +
           "COLON_TOKEN (':')\n" +
           "SIGNIFICANT_SPACE_TOKEN (' ')\n" +
           "HEADER_VALUE_PART_TOKEN ('ab')\n" +
           "SEMICOLON_TOKEN (';')\n" +
           "HEADER_VALUE_PART_TOKEN ('dir')\n" +
           "COLON_TOKEN (':')\n" +
           "NEWLINE_TOKEN ('\n')\n" +
           "HEADER_NAME_TOKEN ('')\n" +
           "BAD_CHARACTER ('=')\n" +
           "BAD_CHARACTER ('v')\n" +
           "BAD_CHARACTER ('a')\n" +
           "BAD_CHARACTER ('l')\n" +
           "BAD_CHARACTER ('u')\n" +
           "BAD_CHARACTER ('e')\n" +
           "BAD_CHARACTER (';')\n" +
           "BAD_CHARACTER ('a')\n" +
           "COLON_TOKEN (':')\n" +
           "BAD_CHARACTER ('=')\n" +
           "HEADER_VALUE_PART_TOKEN ('b')\n" +
           "NEWLINE_TOKEN ('\n')");
  }

  private void doTest(String text, String expected) {
    myLexer.start(text);

    StringBuilder actual = new StringBuilder();
    IElementType token;
    while ((token = myLexer.getTokenType()) != null) {
      actual.append(token).append(" ('").append(myLexer.getTokenText()).append("')\n");
      myLexer.advance();
    }

    assertEquals(expected.trim(), actual.toString().trim());
  }
}
