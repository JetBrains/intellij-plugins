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

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import org.junit.After;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.osmorc.manifest.lang.psi.*;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class ParsingTest {
    public ParsingTest() {
        TestFixtureBuilder<IdeaProjectTestFixture> fixtureBuilder =
                IdeaTestFixtureFactory.getFixtureFactory().createLightFixtureBuilder();
        fixture = fixtureBuilder.getFixture();
    }

    @Before
    public void setUp() throws Exception {
        fixture.setUp();
    }

    @After
    public void tearDown() throws Exception {
        fixture.tearDown();
    }

    @SuppressWarnings({"ConstantConditions"})
    @Test
    public void testSimple() {
        PsiFile fromText = PsiFileFactory.getInstance(fixture.getProject()).createFileFromText("DUMMY.MF", "test0: testvalue\n");
        PsiElement currentElement = fromText.getFirstChild();
        assertThat(currentElement, is(Section.class));

       currentElement = currentElement.getFirstChild();
        assertThat(currentElement, is(Header.class));

        Header header = (Header) currentElement;
        assertThat(header.getName(), equalTo("test0"));

        currentElement = currentElement.getFirstChild();
        assertThat(currentElement, is(ManifestToken.class));
        assertThat(((ManifestToken) currentElement).getTokenType(), is(ManifestTokenType.HEADER_NAME));
        assertThat(currentElement.getText(), equalTo("test0"));

        currentElement = currentElement.getNextSibling();
        assertThat(currentElement, is(ManifestToken.class));
        assertThat(((ManifestToken) currentElement).getTokenType(), is(ManifestTokenType.COLON));

        currentElement = currentElement.getNextSibling();
        assertThat(currentElement, is(ManifestToken.class));
        assertThat(((ManifestToken) currentElement).getTokenType(), is(ManifestTokenType.SIGNIFICANT_SPACE));

        currentElement = currentElement.getNextSibling();
        assertThat(currentElement, is(Clause.class));

        currentElement = currentElement.getFirstChild();
        assertThat(currentElement, is(HeaderValuePart.class));
        HeaderValuePart headerValue = (HeaderValuePart) currentElement;
        assertThat(headerValue.getText(), equalTo("testvalue\n"));
        assertThat(headerValue.getUnwrappedText(), equalTo("testvalue"));
        assertThat(headerValue.getConvertedValue(), equalTo((Object) "testvalue"));

    }

    @SuppressWarnings({"ConstantConditions"})
    @Test
    public void testTwoLineHeader() {
        PsiFile fromText = PsiFileFactory.getInstance(fixture.getProject()).createFileFromText("DUMMY.MF",
                "test0: test\n" +
                        " value\n");
        PsiElement currentElement = fromText.getFirstChild();
        assertThat(currentElement, is(Section.class));

        currentElement = currentElement.getFirstChild();
        assertThat(currentElement, is(Header.class));

        currentElement = currentElement.getFirstChild();
        assertThat(currentElement, is(ManifestToken.class));


        currentElement = currentElement.getNextSibling();
        assertThat(currentElement, is(ManifestToken.class));
        assertThat(((ManifestToken) currentElement).getTokenType(), is(ManifestTokenType.COLON));

        currentElement = currentElement.getNextSibling();
        assertThat(currentElement, is(ManifestToken.class));
        assertThat(((ManifestToken) currentElement).getTokenType(), is(ManifestTokenType.SIGNIFICANT_SPACE));

        currentElement = currentElement.getNextSibling();
        assertThat(currentElement, is(Clause.class));

        currentElement = currentElement.getFirstChild();
        assertThat(currentElement, is(HeaderValuePart.class));
        HeaderValuePart headerValue = (HeaderValuePart) currentElement;
        assertThat(headerValue.getText(), equalTo("test\n value\n"));
        assertThat(headerValue.getUnwrappedText(), equalTo("testvalue"));
        assertThat(headerValue.getConvertedValue(), equalTo((Object) "testvalue"));
    }

    @SuppressWarnings({"ConstantConditions"})
    @Test
    public void testTwoHeaders() {
        PsiFile fromText = PsiFileFactory.getInstance(fixture.getProject()).createFileFromText("DUMMY.MF",
                "test0: test\n" +
                        " value\n" +
                        "test1: testvalue2");
        PsiElement currentElement = fromText.getFirstChild();
        assertThat(currentElement, is(Section.class));

        currentElement = currentElement.getFirstChild();
        assertThat(currentElement, is(Header.class));
        assertThat(((Header) currentElement).getName(), equalTo("test0"));

        currentElement = currentElement.getNextSibling();
        assertThat(currentElement, is(Header.class));
        assertThat(((Header) currentElement).getName(), equalTo("test1"));
    }

    @SuppressWarnings({"ConstantConditions"})
    @Test
    public void testClauses() {
        PsiFile fromText = PsiFileFactory.getInstance(fixture.getProject()).createFileFromText("DUMMY.MF",
                "Require-Bundle: t1,t2");
        PsiElement currentElement = fromText.getFirstChild();
        assertThat(currentElement, is(Section.class));

        currentElement = currentElement.getFirstChild();
        assertThat(currentElement, is(Header.class));

        currentElement = currentElement.getFirstChild();
        assertThat(currentElement, is(ManifestToken.class));

        currentElement = currentElement.getNextSibling();
        assertThat(currentElement, is(ManifestToken.class));

        currentElement = currentElement.getNextSibling();
        assertThat(currentElement, is(ManifestToken.class));

        currentElement = currentElement.getNextSibling();
        assertThat(currentElement, is(Clause.class));

        currentElement = currentElement.getFirstChild();
        assertThat(currentElement, is(HeaderValuePart.class));
        assertThat(currentElement.getText(), equalTo("t1"));

        currentElement = currentElement.getParent().getNextSibling();
        assertThat(currentElement, is(ManifestToken.class));
        assertThat(((ManifestToken) currentElement).getTokenType(), equalTo(ManifestTokenType.COMMA));

        currentElement = currentElement.getNextSibling();
        assertThat(currentElement, is(Clause.class));

        currentElement = currentElement.getFirstChild();
        assertThat(currentElement, is(HeaderValuePart.class));
        assertThat(currentElement.getText(), equalTo("t2"));
    }

    @SuppressWarnings({"ConstantConditions"})
    @Test
    public void testDirectivesAndAttributes() {
        PsiFile fromText = PsiFileFactory.getInstance(fixture.getProject()).createFileFromText("DUMMY.MF",
                "Require-Bundle: t1;d1:=p1;a1=p2; d2 := p3 ;  a2 = p4");
        PsiElement currentElement = fromText.getFirstChild();
        assertThat(currentElement, is(Section.class));

        currentElement = currentElement.getFirstChild();
        assertThat(currentElement, is(Header.class));

        currentElement = currentElement.getFirstChild();
        assertThat(currentElement, is(ManifestToken.class));

        currentElement = currentElement.getNextSibling();
        assertThat(currentElement, is(ManifestToken.class));

        currentElement = currentElement.getNextSibling();
        assertThat(currentElement, is(ManifestToken.class));

        currentElement = currentElement.getNextSibling();
        assertThat(currentElement, is(Clause.class));

        currentElement = currentElement.getFirstChild();
        assertThat(currentElement, is(HeaderValuePart.class));
        HeaderValuePart headerValue = (HeaderValuePart) currentElement;
        assertThat(headerValue.getUnwrappedText(), equalTo("t1"));

        currentElement = currentElement.getNextSibling();
        assertThat(currentElement, is(ManifestToken.class));
        assertThat(((ManifestToken) currentElement).getTokenType(), equalTo(ManifestTokenType.SEMICOLON));

        currentElement = currentElement.getNextSibling();
        assertThat(currentElement, is(Directive.class));
        assertThat(((Directive) currentElement).getName(), equalTo("d1"));
        assertThat(((Directive) currentElement).getValue(), equalTo("p1"));

        currentElement = currentElement.getNextSibling();
        assertThat(currentElement, is(ManifestToken.class));
        assertThat(((ManifestToken) currentElement).getTokenType(), equalTo(ManifestTokenType.SEMICOLON));

        currentElement = currentElement.getNextSibling();
        assertThat(currentElement, is(Attribute.class));
        assertThat(((Attribute) currentElement).getName(), equalTo("a1"));
        assertThat(((Attribute) currentElement).getValue(), equalTo("p2"));

        currentElement = currentElement.getNextSibling();
        assertThat(currentElement, is(ManifestToken.class));
        assertThat(((ManifestToken) currentElement).getTokenType(), equalTo(ManifestTokenType.SEMICOLON));

        currentElement = currentElement.getNextSibling();
        assertThat(currentElement, is(Directive.class));
        assertThat(((Directive) currentElement).getName(), equalTo("d2"));
        assertThat(((Directive) currentElement).getValue(), equalTo("p3"));

        currentElement = currentElement.getNextSibling();
        assertThat(currentElement, is(ManifestToken.class));
        assertThat(((ManifestToken) currentElement).getTokenType(), equalTo(ManifestTokenType.SEMICOLON));

        currentElement = currentElement.getNextSibling();
        assertThat(currentElement, is(Attribute.class));
        assertThat(((Attribute) currentElement).getName(), equalTo("a2"));
        assertThat(((Attribute) currentElement).getValue(), equalTo("p4"));

    }

    private final IdeaProjectTestFixture fixture;
}
