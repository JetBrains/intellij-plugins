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
import com.intellij.psi.impl.source.tree.LeafPsiElement;
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
public class ParsingTest
{
  public ParsingTest()
  {
    TestFixtureBuilder<IdeaProjectTestFixture> fixtureBuilder =
        IdeaTestFixtureFactory.getFixtureFactory().createLightFixtureBuilder();
    _fixture = fixtureBuilder.getFixture();
  }

  @Before
  public void setUp() throws Exception
  {
    _fixture.setUp();
  }

  @After
  public void tearDown() throws Exception
  {
    _fixture.tearDown();
  }

  @SuppressWarnings({"ConstantConditions"})
  @Test
  public void testSimple()
  {
    PsiFile fromText = PsiFileFactory.getInstance(_fixture.getProject()).createFileFromText("DUMMY.MF", "test0: testvalue\n");
    PsiElement currentElement = fromText.getFirstChild();
    assertThat(currentElement, is(ManifestHeader.class));
    ManifestHeader header = (ManifestHeader) currentElement;
    assertThat(header.getName(), equalTo("test0"));

    currentElement = currentElement.getFirstChild();
    assertThat(currentElement, is(ManifestHeaderName.class));
    assertThat(((ManifestHeaderName) currentElement).getName(), equalTo("test0"));

    currentElement = currentElement.getNextSibling();
    assertThat(currentElement, is(LeafPsiElement.class));
    assertThat(((LeafPsiElement) currentElement).getElementType(), is(ManifestTokenTypes.HEADER_ASSIGNMENT));

    currentElement = currentElement.getNextSibling();
    assertThat(currentElement, is(PsiWhiteSpace.class));

    currentElement = currentElement.getNextSibling();
    assertThat(currentElement, is(ManifestClause.class));

    currentElement = currentElement.getFirstChild();
    assertThat(currentElement, is(ManifestHeaderValue.class));
    ManifestHeaderValue headerValue = (ManifestHeaderValue) currentElement;
    assertThat(headerValue.getText(), equalTo("testvalue"));
    assertThat(headerValue.getValueText(), equalTo("testvalue"));
    assertThat(headerValue.getValue(), equalTo((Object) "testvalue"));

  }

  @SuppressWarnings({"ConstantConditions"})
  @Test
  public void testTwoLineHeader()
  {
    PsiFile fromText = PsiFileFactory.getInstance(_fixture.getProject()).createFileFromText("DUMMY.MF",
        "test0 : test\n" +
            " value\n");
    PsiElement currentElement = fromText.getFirstChild();
    assertThat(currentElement, is(ManifestHeader.class));

    currentElement = currentElement.getFirstChild();
    assertThat(currentElement, is(ManifestHeaderName.class));

    currentElement = currentElement.getNextSibling();
    assertThat(currentElement, is(PsiWhiteSpace.class));

    currentElement = currentElement.getNextSibling();
    assertThat(currentElement, is(LeafPsiElement.class));
    assertThat(((LeafPsiElement) currentElement).getElementType(), is(ManifestTokenTypes.HEADER_ASSIGNMENT));

    currentElement = currentElement.getNextSibling();
    assertThat(currentElement, is(PsiWhiteSpace.class));

    currentElement = currentElement.getNextSibling();
    assertThat(currentElement, is(ManifestClause.class));

    currentElement = currentElement.getFirstChild();
    assertThat(currentElement, is(ManifestHeaderValue.class));
    ManifestHeaderValue headerValue = (ManifestHeaderValue) currentElement;
    assertThat(headerValue.getText(), equalTo("test\n value"));
    assertThat(headerValue.getValueText(), equalTo("testvalue"));
    assertThat(headerValue.getValue(), equalTo((Object) "testvalue"));
  }

  @SuppressWarnings({"ConstantConditions"})
  @Test
  public void testTwoHeaders()
  {
    PsiFile fromText = PsiFileFactory.getInstance(_fixture.getProject()).createFileFromText("DUMMY.MF",
        "test0: test\n" +
            " value\n" +
            "test1: testvalue2");
    PsiElement currentElement = fromText.getFirstChild();
    assertThat(currentElement, is(ManifestHeader.class));
    assertThat(((ManifestHeader) currentElement).getName(), equalTo("test0"));

    currentElement = currentElement.getNextSibling();
    assertThat(currentElement, is(PsiWhiteSpace.class));

    currentElement = currentElement.getNextSibling();
    assertThat(currentElement, is(ManifestHeader.class));
    assertThat(((ManifestHeader) currentElement).getName(), equalTo("test1"));
  }

  @SuppressWarnings({"ConstantConditions"})
  @Test
  public void testClauses()
  {
    PsiFile fromText = PsiFileFactory.getInstance(_fixture.getProject()).createFileFromText("DUMMY.MF",
        "Require-Bundle:t1,t2");
    PsiElement currentElement = fromText.getFirstChild();
    assertThat(currentElement, is(ManifestHeader.class));

    currentElement = currentElement.getFirstChild();
    assertThat(currentElement, is(ManifestHeaderName.class));

    currentElement = currentElement.getNextSibling();
    assertThat(currentElement, is(LeafPsiElement.class));

    currentElement = currentElement.getNextSibling();
    assertThat(currentElement, is(ManifestClause.class));

    currentElement = currentElement.getFirstChild();
    assertThat(currentElement, is(ManifestHeaderValue.class));
    assertThat(currentElement.getText(), equalTo("t1"));

    currentElement = currentElement.getParent().getNextSibling();
    assertThat(currentElement, is(LeafPsiElement.class));
    assertThat(((LeafPsiElement) currentElement).getElementType(), equalTo(ManifestTokenTypes.CLAUSE_SEPARATOR));

    currentElement = currentElement.getNextSibling();
    assertThat(currentElement, is(ManifestClause.class));

    currentElement = currentElement.getFirstChild();
    assertThat(currentElement, is(ManifestHeaderValue.class));
    assertThat(currentElement.getText(), equalTo("t2"));
  }

  @SuppressWarnings({"ConstantConditions"})
  @Test
  public void testDirectivesAndAttributes()
  {
    PsiFile fromText = PsiFileFactory.getInstance(_fixture.getProject()).createFileFromText("DUMMY.MF",
        "Require-Bundle:t1;d1:=p1;a1=p2; d2 := p3 ;  a2 = p4");
    PsiElement currentElement = fromText.getFirstChild();
    assertThat(currentElement, is(ManifestHeader.class));

    currentElement = currentElement.getFirstChild();
    assertThat(currentElement, is(ManifestHeaderName.class));

    currentElement = currentElement.getNextSibling();
    assertThat(currentElement, is(LeafPsiElement.class));

    currentElement = currentElement.getNextSibling();
    assertThat(currentElement, is(ManifestClause.class));

    currentElement = currentElement.getFirstChild();
    assertThat(currentElement, is(ManifestHeaderValue.class));
    ManifestHeaderValue headerValue = (ManifestHeaderValue) currentElement;
    assertThat(headerValue.getValueText(), equalTo("t1"));

    currentElement = currentElement.getNextSibling();
    assertThat(currentElement, is(LeafPsiElement.class));
    assertThat(((LeafPsiElement) currentElement).getElementType(), equalTo(ManifestTokenTypes.PARAMETER_SEPARATOR));

    currentElement = currentElement.getNextSibling();
    assertThat(currentElement, is(ManifestDirective.class));
    assertThat(((ManifestDirective) currentElement).getName(), equalTo("d1"));
    assertThat(((ManifestDirective) currentElement).getValue().getValueText(), equalTo("p1"));

    currentElement = currentElement.getNextSibling();
    assertThat(currentElement, is(LeafPsiElement.class));
    assertThat(((LeafPsiElement) currentElement).getElementType(), equalTo(ManifestTokenTypes.PARAMETER_SEPARATOR));

    currentElement = currentElement.getNextSibling();
    assertThat(currentElement, is(ManifestAttribute.class));
    assertThat(((ManifestAttribute) currentElement).getName(), equalTo("a1"));
    assertThat(((ManifestAttribute) currentElement).getValue().getValueText(), equalTo("p2"));

    currentElement = currentElement.getNextSibling();
    assertThat(currentElement, is(LeafPsiElement.class));
    assertThat(((LeafPsiElement) currentElement).getElementType(), equalTo(ManifestTokenTypes.PARAMETER_SEPARATOR));

    currentElement = currentElement.getNextSibling();
    assertThat(currentElement, is(PsiWhiteSpace.class));

    currentElement = currentElement.getNextSibling();
    assertThat(currentElement, is(ManifestDirective.class));
    assertThat(((ManifestDirective) currentElement).getName(), equalTo("d2"));
    assertThat(((ManifestDirective) currentElement).getValue().getValueText(), equalTo("p3"));

    currentElement = currentElement.getNextSibling();
    assertThat(currentElement, is(LeafPsiElement.class));
    assertThat(((LeafPsiElement) currentElement).getElementType(), equalTo(ManifestTokenTypes.PARAMETER_SEPARATOR));

    currentElement = currentElement.getNextSibling();
    assertThat(currentElement, is(PsiWhiteSpace.class));

    currentElement = currentElement.getNextSibling();
    assertThat(currentElement, is(PsiWhiteSpace.class));

    currentElement = currentElement.getNextSibling();
    assertThat(currentElement, is(ManifestAttribute.class));
    assertThat(((ManifestAttribute) currentElement).getName(), equalTo("a2"));
    assertThat(((ManifestAttribute) currentElement).getValue().getValueText(), equalTo("p4"));

  }

  private IdeaProjectTestFixture _fixture;
}
