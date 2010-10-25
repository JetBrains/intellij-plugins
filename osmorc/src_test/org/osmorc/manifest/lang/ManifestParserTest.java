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
import com.intellij.lang.impl.PsiBuilderImpl;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.TokenType;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.JavaTestFixtureFactory;
import static org.hamcrest.Matchers.*;
import org.junit.After;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmorc.SwingRunner;
import org.osmorc.manifest.lang.headerparser.HeaderParserRepository;
import org.osmorc.manifest.lang.psi.ManifestStubElementTypes;


/**
 */
@RunWith(SwingRunner.class)
public class ManifestParserTest {
    private ManifestParser testObject;
    private IdeaProjectTestFixture fixture;
    private ManifestLexer lexer;

    @Before
    public void setUp() throws Exception {
        fixture = JavaTestFixtureFactory.createFixtureBuilder().getFixture();
        fixture.setUp();
        lexer = new ManifestLexer();
        testObject = new ManifestParser(ServiceManager.getService(HeaderParserRepository.class));
    }

    @After
    public void tearDown() throws Exception {
        fixture.tearDown();
    }

    @Test
    public void testSimple() {
        PsiBuilder builder = new PsiBuilderImpl(lexer, TokenSet.EMPTY, TokenSet.EMPTY,
                "Manifest-Version: 2\n");
        ASTNode node = testObject.parse(ManifestStubElementTypes.FILE, builder);

        assertThat(node.getElementType(), sameInstance((IElementType) ManifestStubElementTypes.FILE));
        checkContainsNodes(node, ManifestElementTypes.SECTION);
        node = node.getChildren(null)[0];
        checkContainsNodes(node, ManifestElementTypes.HEADER);
        node = node.getChildren(null)[0];
        checkContainsNodes(node,
                ManifestTokenType.HEADER_NAME,
                ManifestTokenType.COLON,
                ManifestTokenType.SIGNIFICANT_SPACE,
                ManifestElementTypes.CLAUSE);
        node = node.getChildren(null)[3];
        checkContainsNodes(node,
                ManifestElementTypes.HEADER_VALUE_PART);
    }

    @Test
    public void testSimpleWithSpaceInHeaderAssignment() {
        PsiBuilder builder = new PsiBuilderImpl(lexer, TokenSet.EMPTY, TokenSet.EMPTY,
                "Manifest-Version : 2\n");
        ASTNode node = testObject.parse(ManifestStubElementTypes.FILE, builder);

        assertThat(node.getElementType(), sameInstance((IElementType) ManifestStubElementTypes.FILE));
        checkContainsNodes(node, ManifestElementTypes.SECTION);
        node = node.getChildren(null)[0];
        checkContainsNodes(node, ManifestElementTypes.HEADER);
        node = node.getChildren(null)[0];
        checkContainsNodes(node,
                ManifestTokenType.HEADER_NAME,
                TokenType.BAD_CHARACTER,
                ManifestTokenType.COLON,
                ManifestTokenType.SIGNIFICANT_SPACE,
                ManifestElementTypes.CLAUSE);
        node = node.getChildren(null)[4];
        checkContainsNodes(node,
                ManifestElementTypes.HEADER_VALUE_PART);
    }

    @Test
    public void testSimpleMissingSpaceAfterHeaderAssignment1() {
        PsiBuilder builder = new PsiBuilderImpl(lexer, TokenSet.EMPTY, TokenSet.EMPTY,
                "Manifest-Version:2\n");
        ASTNode node = testObject.parse(ManifestStubElementTypes.FILE, builder);

        assertThat(node.getElementType(), sameInstance((IElementType) ManifestStubElementTypes.FILE));
        checkContainsNodes(node, ManifestElementTypes.SECTION);
        node = node.getChildren(null)[0];
        checkContainsNodes(node, ManifestElementTypes.HEADER);
        node = node.getChildren(null)[0];
        checkContainsNodes(node,
                ManifestTokenType.HEADER_NAME,
                ManifestTokenType.COLON,
                TokenType.BAD_CHARACTER,
                ManifestTokenType.NEWLINE);
    }

    @Test
    public void testSimpleMissingSpaceAfterHeaderAssignment2() {
        PsiBuilder builder = new PsiBuilderImpl(lexer, TokenSet.EMPTY, TokenSet.EMPTY,
                "Bundle-Name:name\n");
        ASTNode node = testObject.parse(ManifestStubElementTypes.FILE, builder);

        assertThat(node.getElementType(), sameInstance((IElementType) ManifestStubElementTypes.FILE));
        checkContainsNodes(node, ManifestElementTypes.SECTION);
        node = node.getChildren(null)[0];
        checkContainsNodes(node, ManifestElementTypes.HEADER);
        node = node.getChildren(null)[0];
        checkContainsNodes(node,
                ManifestTokenType.HEADER_NAME,
                ManifestTokenType.COLON,
                TokenType.BAD_CHARACTER,
                ManifestElementTypes.CLAUSE);
        node = node.getChildren(null)[3];
        checkContainsNodes(node,
                ManifestElementTypes.HEADER_VALUE_PART);
    }

    @Test
    public void testSimpleWithContinuation() {
        PsiBuilder builder = new PsiBuilderImpl(lexer, TokenSet.EMPTY, TokenSet.EMPTY,
                "Bundle-Vendor: Acme\n Company\n");
        ASTNode node = testObject.parse(ManifestStubElementTypes.FILE, builder);

        assertThat(node.getElementType(), sameInstance((IElementType) ManifestStubElementTypes.FILE));
        checkContainsNodes(node, ManifestElementTypes.SECTION);
        node = node.getChildren(null)[0];
        checkContainsNodes(node, ManifestElementTypes.HEADER);
        node = node.getChildren(null)[0];
        checkContainsNodes(node,
                ManifestTokenType.HEADER_NAME,
                ManifestTokenType.COLON,
                ManifestTokenType.SIGNIFICANT_SPACE,
                ManifestElementTypes.CLAUSE);
        node = node.getChildren(null)[3];
        checkContainsNodes(node,
                ManifestElementTypes.HEADER_VALUE_PART);
    }

    @Test
    public void testAttribute() {
        PsiBuilder builder = new PsiBuilderImpl(lexer, TokenSet.EMPTY, TokenSet.EMPTY,
                "Import-Package: com.acme;version=\"1.0.0\"\n");
        ASTNode node = testObject.parse(ManifestStubElementTypes.FILE, builder);

        assertThat(node.getElementType(), sameInstance((IElementType) ManifestStubElementTypes.FILE));
        checkContainsNodes(node, ManifestElementTypes.SECTION);
        node = node.getChildren(null)[0];
        checkContainsNodes(node, ManifestElementTypes.HEADER);
        node = node.getChildren(null)[0];
        checkContainsNodes(node,
                ManifestTokenType.HEADER_NAME,
                ManifestTokenType.COLON,
                ManifestTokenType.SIGNIFICANT_SPACE,
                ManifestElementTypes.CLAUSE);
        node = node.getChildren(null)[3];
        checkContainsNodes(node,
                ManifestElementTypes.HEADER_VALUE_PART,
                ManifestTokenType.SEMICOLON,
                ManifestElementTypes.ATTRIBUTE);
        node = node.getChildren(null)[2];
        checkContainsNodes(node,
                ManifestElementTypes.HEADER_VALUE_PART,
                ManifestTokenType.EQUALS,
                ManifestElementTypes.HEADER_VALUE_PART);
    }

    @Test
    public void testAttributeWithoutName() {
        PsiBuilder builder = new PsiBuilderImpl(lexer, TokenSet.EMPTY, TokenSet.EMPTY,
                "Import-Package: com.acme;=\"1.0.0\"\n");
        ASTNode node = testObject.parse(ManifestStubElementTypes.FILE, builder);

        assertThat(node.getElementType(), sameInstance((IElementType) ManifestStubElementTypes.FILE));
        checkContainsNodes(node, ManifestElementTypes.SECTION);
        node = node.getChildren(null)[0];
        checkContainsNodes(node, ManifestElementTypes.HEADER);
        node = node.getChildren(null)[0];
        checkContainsNodes(node,
                ManifestTokenType.HEADER_NAME,
                ManifestTokenType.COLON,
                ManifestTokenType.SIGNIFICANT_SPACE,
                ManifestElementTypes.CLAUSE);
        node = node.getChildren(null)[3];
        checkContainsNodes(node,
                ManifestElementTypes.HEADER_VALUE_PART,
                ManifestTokenType.SEMICOLON,
                ManifestElementTypes.ATTRIBUTE);
        node = node.getChildren(null)[2];
        checkContainsNodes(node,
                ManifestElementTypes.HEADER_VALUE_PART,
                ManifestTokenType.EQUALS,
                ManifestElementTypes.HEADER_VALUE_PART);
    }

    @Test
    public void testAttributeWithoutNameOutsideParameter() {
        PsiBuilder builder = new PsiBuilderImpl(lexer, TokenSet.EMPTY, TokenSet.EMPTY,
                "Import-Package: =\"1.0.0\"\n");
        ASTNode node = testObject.parse(ManifestStubElementTypes.FILE, builder);

        assertThat(node.getElementType(), sameInstance((IElementType) ManifestStubElementTypes.FILE));
        checkContainsNodes(node, ManifestElementTypes.SECTION);
        node = node.getChildren(null)[0];
        checkContainsNodes(node, ManifestElementTypes.HEADER);
        node = node.getChildren(null)[0];
        checkContainsNodes(node,
                ManifestTokenType.HEADER_NAME,
                ManifestTokenType.COLON,
                ManifestTokenType.SIGNIFICANT_SPACE,
                ManifestElementTypes.CLAUSE);
        node = node.getChildren(null)[3];
        checkContainsNodes(node,
                ManifestElementTypes.ATTRIBUTE);
        node = node.getChildren(null)[0];
        checkContainsNodes(node,
                ManifestElementTypes.HEADER_VALUE_PART,
                ManifestTokenType.EQUALS,
                ManifestElementTypes.HEADER_VALUE_PART);
    }

    @Test
    public void testParameterWithoutParametrized() {
        PsiBuilder builder = new PsiBuilderImpl(lexer, TokenSet.EMPTY, TokenSet.EMPTY,
                "Import-Package: ;version=\"1.0.0\"\n");
        ASTNode node = testObject.parse(ManifestStubElementTypes.FILE, builder);

        assertThat(node.getElementType(), sameInstance((IElementType) ManifestStubElementTypes.FILE));
        checkContainsNodes(node, ManifestElementTypes.SECTION);
        node = node.getChildren(null)[0];
        checkContainsNodes(node, ManifestElementTypes.HEADER);
        node = node.getChildren(null)[0];
        checkContainsNodes(node,
                ManifestTokenType.HEADER_NAME,
                ManifestTokenType.COLON,
                ManifestTokenType.SIGNIFICANT_SPACE,
                ManifestElementTypes.CLAUSE);
        node = node.getChildren(null)[3];
        checkContainsNodes(node,
                ManifestElementTypes.HEADER_VALUE_PART,
                ManifestTokenType.SEMICOLON,
                ManifestElementTypes.ATTRIBUTE);
        node = node.getChildren(null)[2];
        checkContainsNodes(node,
                ManifestElementTypes.HEADER_VALUE_PART,
                ManifestTokenType.EQUALS,
                ManifestElementTypes.HEADER_VALUE_PART);
    }

    @Test
    public void testDirective() {
        PsiBuilder builder = new PsiBuilderImpl(lexer, TokenSet.EMPTY, TokenSet.EMPTY,
                "Import-Package: com.acme;resolution:=optional\n");
        ASTNode node = testObject.parse(ManifestStubElementTypes.FILE, builder);

        assertThat(node.getElementType(), sameInstance((IElementType) ManifestStubElementTypes.FILE));
        checkContainsNodes(node, ManifestElementTypes.SECTION);
        node = node.getChildren(null)[0];
        checkContainsNodes(node, ManifestElementTypes.HEADER);
        node = node.getChildren(null)[0];
        checkContainsNodes(node,
                ManifestTokenType.HEADER_NAME,
                ManifestTokenType.COLON,
                ManifestTokenType.SIGNIFICANT_SPACE,
                ManifestElementTypes.CLAUSE);
        node = node.getChildren(null)[3];
        checkContainsNodes(node,
                ManifestElementTypes.HEADER_VALUE_PART,
                ManifestTokenType.SEMICOLON,
                ManifestElementTypes.DIRECTIVE);
        node = node.getChildren(null)[2];
        checkContainsNodes(node,
                ManifestElementTypes.HEADER_VALUE_PART,
                ManifestTokenType.COLON,
                ManifestTokenType.EQUALS,
                ManifestElementTypes.HEADER_VALUE_PART);
    }

    @Test
    public void testDirectiveWithoutName() {
        PsiBuilder builder = new PsiBuilderImpl(lexer, TokenSet.EMPTY, TokenSet.EMPTY,
                "Import-Package: com.acme;:=optional\n");
        ASTNode node = testObject.parse(ManifestStubElementTypes.FILE, builder);

        assertThat(node.getElementType(), sameInstance((IElementType) ManifestStubElementTypes.FILE));
        checkContainsNodes(node, ManifestElementTypes.SECTION);
        node = node.getChildren(null)[0];
        checkContainsNodes(node, ManifestElementTypes.HEADER);
        node = node.getChildren(null)[0];
        checkContainsNodes(node,
                ManifestTokenType.HEADER_NAME,
                ManifestTokenType.COLON,
                ManifestTokenType.SIGNIFICANT_SPACE,
                ManifestElementTypes.CLAUSE);
        node = node.getChildren(null)[3];
        checkContainsNodes(node,
                ManifestElementTypes.HEADER_VALUE_PART,
                ManifestTokenType.SEMICOLON,
                ManifestElementTypes.DIRECTIVE);
        node = node.getChildren(null)[2];
        checkContainsNodes(node,
                ManifestElementTypes.HEADER_VALUE_PART,
                ManifestTokenType.COLON,
                ManifestTokenType.EQUALS,
                ManifestElementTypes.HEADER_VALUE_PART);
    }

    @Test
    public void testDirectiveWithoutNameOutsideParameter() {
        PsiBuilder builder = new PsiBuilderImpl(lexer, TokenSet.EMPTY, TokenSet.EMPTY,
                "Import-Package: :=optional\n");
        ASTNode node = testObject.parse(ManifestStubElementTypes.FILE, builder);

        assertThat(node.getElementType(), sameInstance((IElementType) ManifestStubElementTypes.FILE));
        checkContainsNodes(node, ManifestElementTypes.SECTION);
        node = node.getChildren(null)[0];
        checkContainsNodes(node, ManifestElementTypes.HEADER);
        node = node.getChildren(null)[0];
        checkContainsNodes(node,
                ManifestTokenType.HEADER_NAME,
                ManifestTokenType.COLON,
                ManifestTokenType.SIGNIFICANT_SPACE,
                ManifestElementTypes.CLAUSE);
        node = node.getChildren(null)[3];
        checkContainsNodes(node,
                ManifestElementTypes.DIRECTIVE);
        node = node.getChildren(null)[0];
        checkContainsNodes(node,
                ManifestElementTypes.HEADER_VALUE_PART,
                ManifestTokenType.COLON,
                ManifestTokenType.EQUALS,
                ManifestElementTypes.HEADER_VALUE_PART);
    }

    @Test
    public void testDirectiveInvalidAssignmentTokens() {
        PsiBuilder builder = new PsiBuilderImpl(lexer, TokenSet.EMPTY, TokenSet.EMPTY,
                "Import-Package: com.acme;resolution: =optional\n");
        ASTNode node = testObject.parse(ManifestStubElementTypes.FILE, builder);

        assertThat(node.getElementType(), sameInstance((IElementType) ManifestStubElementTypes.FILE));
        checkContainsNodes(node, ManifestElementTypes.SECTION);
        node = node.getChildren(null)[0];
        checkContainsNodes(node, ManifestElementTypes.HEADER);
        node = node.getChildren(null)[0];
        checkContainsNodes(node,
                ManifestTokenType.HEADER_NAME,
                ManifestTokenType.COLON,
                ManifestTokenType.SIGNIFICANT_SPACE,
                ManifestElementTypes.CLAUSE);
        node = node.getChildren(null)[3];
        checkContainsNodes(node,
                ManifestElementTypes.HEADER_VALUE_PART,
                ManifestTokenType.SEMICOLON,
                ManifestElementTypes.DIRECTIVE);
        node = node.getChildren(null)[2];
        checkContainsNodes(node,
                ManifestElementTypes.HEADER_VALUE_PART,
                ManifestTokenType.COLON,
                ManifestElementTypes.HEADER_VALUE_PART);
    }

    @Test
    public void testSplitDirectiveAtColon() {
        PsiBuilder builder = new PsiBuilderImpl(lexer, TokenSet.EMPTY, TokenSet.EMPTY,
                "Import-Package: com.acme;resolution:\n =optional\n");
        ASTNode node = testObject.parse(ManifestStubElementTypes.FILE, builder);

        assertThat(node.getElementType(), sameInstance((IElementType) ManifestStubElementTypes.FILE));
        checkContainsNodes(node, ManifestElementTypes.SECTION);
        node = node.getChildren(null)[0];
        checkContainsNodes(node, ManifestElementTypes.HEADER);
        node = node.getChildren(null)[0];
        checkContainsNodes(node,
                ManifestTokenType.HEADER_NAME,
                ManifestTokenType.COLON,
                ManifestTokenType.SIGNIFICANT_SPACE,
                ManifestElementTypes.CLAUSE);
        node = node.getChildren(null)[3];
        checkContainsNodes(node,
                ManifestElementTypes.HEADER_VALUE_PART,
                ManifestTokenType.SEMICOLON,
                ManifestElementTypes.DIRECTIVE);
        node = node.getChildren(null)[2];
        checkContainsNodes(node,
                ManifestElementTypes.HEADER_VALUE_PART,
                ManifestTokenType.COLON,
                ManifestTokenType.NEWLINE,
                ManifestTokenType.SIGNIFICANT_SPACE,
                ManifestTokenType.EQUALS,
                ManifestElementTypes.HEADER_VALUE_PART);
    }

    @Test
    public void testAttributeAndDirective() {
        PsiBuilder builder = new PsiBuilderImpl(lexer, TokenSet.EMPTY, TokenSet.EMPTY,
                "Import-Package: com.acme;version=\"1.0.0\";resolution:=optional\n");
        ASTNode node = testObject.parse(ManifestStubElementTypes.FILE, builder);

        assertThat(node.getElementType(), sameInstance((IElementType) ManifestStubElementTypes.FILE));
        checkContainsNodes(node, ManifestElementTypes.SECTION);
        node = node.getChildren(null)[0];
        checkContainsNodes(node, ManifestElementTypes.HEADER);
        node = node.getChildren(null)[0];
        checkContainsNodes(node,
                ManifestTokenType.HEADER_NAME,
                ManifestTokenType.COLON,
                ManifestTokenType.SIGNIFICANT_SPACE,
                ManifestElementTypes.CLAUSE);
        node = node.getChildren(null)[3];
        checkContainsNodes(node,
                ManifestElementTypes.HEADER_VALUE_PART,
                ManifestTokenType.SEMICOLON,
                ManifestElementTypes.ATTRIBUTE,
                ManifestTokenType.SEMICOLON,
                ManifestElementTypes.DIRECTIVE);
        checkContainsNodes(node.getChildren(null)[2],
                ManifestElementTypes.HEADER_VALUE_PART,
                ManifestTokenType.EQUALS,
                ManifestElementTypes.HEADER_VALUE_PART);
        checkContainsNodes(node.getChildren(null)[4],
                ManifestElementTypes.HEADER_VALUE_PART,
                ManifestTokenType.COLON,
                ManifestTokenType.EQUALS,
                ManifestElementTypes.HEADER_VALUE_PART);
    }

    @Test
    public void testAttributeAndDirectiveWithContinuationBeforeSemicolon() {
        PsiBuilder builder = new PsiBuilderImpl(lexer, TokenSet.EMPTY, TokenSet.EMPTY,
                "Import-Package: com.acme;version=\"1.0.0\"\n ;resolution:=optional\n");
        ASTNode node = testObject.parse(ManifestStubElementTypes.FILE, builder);

        assertThat(node.getElementType(), sameInstance((IElementType) ManifestStubElementTypes.FILE));
        checkContainsNodes(node, ManifestElementTypes.SECTION);
        node = node.getChildren(null)[0];
        checkContainsNodes(node, ManifestElementTypes.HEADER);
        node = node.getChildren(null)[0];
        checkContainsNodes(node,
                ManifestTokenType.HEADER_NAME,
                ManifestTokenType.COLON,
                ManifestTokenType.SIGNIFICANT_SPACE,
                ManifestElementTypes.CLAUSE);
        node = node.getChildren(null)[3];
        checkContainsNodes(node,
                ManifestElementTypes.HEADER_VALUE_PART,
                ManifestTokenType.SEMICOLON,
                ManifestElementTypes.ATTRIBUTE,
                ManifestTokenType.SEMICOLON,
                ManifestElementTypes.DIRECTIVE);
        checkContainsNodes(node.getChildren(null)[2],
                ManifestElementTypes.HEADER_VALUE_PART,
                ManifestTokenType.EQUALS,
                ManifestElementTypes.HEADER_VALUE_PART);
        checkContainsNodes(node.getChildren(null)[4],
                ManifestElementTypes.HEADER_VALUE_PART,
                ManifestTokenType.COLON,
                ManifestTokenType.EQUALS,
                ManifestElementTypes.HEADER_VALUE_PART);
    }

    @Test
    public void testAttributeAndDirectiveWithContinuationAfterSemicolon() {
        PsiBuilder builder = new PsiBuilderImpl(lexer, TokenSet.EMPTY, TokenSet.EMPTY,
                "Import-Package: com.acme;version=\"1.0.0\";\n resolution:=optional\n");
        ASTNode node = testObject.parse(ManifestStubElementTypes.FILE, builder);

        assertThat(node.getElementType(), sameInstance((IElementType) ManifestStubElementTypes.FILE));
        checkContainsNodes(node, ManifestElementTypes.SECTION);
        node = node.getChildren(null)[0];
        checkContainsNodes(node, ManifestElementTypes.HEADER);
        node = node.getChildren(null)[0];
        checkContainsNodes(node,
                ManifestTokenType.HEADER_NAME,
                ManifestTokenType.COLON,
                ManifestTokenType.SIGNIFICANT_SPACE,
                ManifestElementTypes.CLAUSE);
        node = node.getChildren(null)[3];
        checkContainsNodes(node,
                ManifestElementTypes.HEADER_VALUE_PART,
                ManifestTokenType.SEMICOLON,
                ManifestElementTypes.ATTRIBUTE,
                ManifestTokenType.SEMICOLON,
                ManifestElementTypes.DIRECTIVE);
        checkContainsNodes(node.getChildren(null)[2],
                ManifestElementTypes.HEADER_VALUE_PART,
                ManifestTokenType.EQUALS,
                ManifestElementTypes.HEADER_VALUE_PART);
        checkContainsNodes(node.getChildren(null)[4],
                ManifestElementTypes.HEADER_VALUE_PART,
                ManifestTokenType.COLON,
                ManifestTokenType.EQUALS,
                ManifestElementTypes.HEADER_VALUE_PART);
    }

    @Test
    public void testAttributeAndDirectiveWithBadContinuationAfterAttributeName() {
        PsiBuilder builder = new PsiBuilderImpl(lexer, TokenSet.EMPTY, TokenSet.EMPTY,
                "Import-Package: com.acme;att\n=attvlue;dir:=dirvalue\n");
        ASTNode node = testObject.parse(ManifestStubElementTypes.FILE, builder);

        assertThat(node.getElementType(), sameInstance((IElementType) ManifestStubElementTypes.FILE));
        checkContainsNodes(node, ManifestElementTypes.SECTION);
        node = node.getChildren(null)[0];
        checkContainsNodes(node, ManifestElementTypes.HEADER, ManifestElementTypes.HEADER);
        ASTNode section = node;
        node = section.getChildren(null)[0];
        checkContainsNodes(node,
                ManifestTokenType.HEADER_NAME,
                ManifestTokenType.COLON,
                ManifestTokenType.SIGNIFICANT_SPACE,
                ManifestElementTypes.CLAUSE);
        node = node.getChildren(null)[3];
        checkContainsNodes(node,
                ManifestElementTypes.HEADER_VALUE_PART,
                ManifestTokenType.SEMICOLON,
                ManifestElementTypes.HEADER_VALUE_PART);
        node = section.getChildren(null)[1];
        checkContainsNodes(node,
                TokenType.BAD_CHARACTER,
                TokenType.BAD_CHARACTER,
                TokenType.BAD_CHARACTER,
                TokenType.BAD_CHARACTER,
                TokenType.BAD_CHARACTER,
                TokenType.BAD_CHARACTER,
                TokenType.BAD_CHARACTER,
                TokenType.BAD_CHARACTER,
                TokenType.BAD_CHARACTER,
                TokenType.BAD_CHARACTER,
                TokenType.BAD_CHARACTER,
                TokenType.BAD_CHARACTER,
                ManifestTokenType.COLON,
                TokenType.BAD_CHARACTER,
                ManifestElementTypes.CLAUSE);

        node = node.getChildren(null)[14];
        checkContainsNodes(node, ManifestElementTypes.HEADER_VALUE_PART);
    }

    @Test
    public void testTwoHeadersAndASecondSection() {
        PsiBuilder builder = new PsiBuilderImpl(lexer, TokenSet.EMPTY, TokenSet.EMPTY,
                "Bundle-SymbolicName: com.acme\n" +
                        "Bundle-Activator: com.acme.Activator\n" +
                        "\n" +
                        "Name: otherSection\n");
        ASTNode node = testObject.parse(ManifestStubElementTypes.FILE, builder);

        assertThat(node.getElementType(), sameInstance((IElementType) ManifestStubElementTypes.FILE));
        checkContainsNodes(node, ManifestElementTypes.SECTION, ManifestTokenType.SECTION_END, ManifestElementTypes.SECTION);
        ASTNode fileNode = node;
        node = fileNode.getChildren(null)[0];
        checkContainsNodes(node, ManifestElementTypes.HEADER, ManifestElementTypes.HEADER);
        ASTNode sectionNode = node;

        node = sectionNode.getChildren(null)[0];
        checkContainsNodes(node,
                ManifestTokenType.HEADER_NAME,
                ManifestTokenType.COLON,
                ManifestTokenType.SIGNIFICANT_SPACE,
                ManifestElementTypes.CLAUSE);
        node = node.getChildren(null)[3];
        checkContainsNodes(node, ManifestElementTypes.HEADER_VALUE_PART);
        node = sectionNode.getChildren(null)[1];
        checkContainsNodes(node,
                ManifestTokenType.HEADER_NAME,
                ManifestTokenType.COLON,
                ManifestTokenType.SIGNIFICANT_SPACE,
                ManifestElementTypes.CLAUSE);
        node = node.getChildren(null)[3];
        checkContainsNodes(node, ManifestElementTypes.HEADER_VALUE_PART);

        node = fileNode.getChildren(null)[2];
        checkContainsNodes(node, ManifestElementTypes.HEADER);
        sectionNode = node;
        node = sectionNode.getChildren(null)[0];
        checkContainsNodes(node,
                ManifestTokenType.HEADER_NAME,
                ManifestTokenType.COLON,
                ManifestTokenType.SIGNIFICANT_SPACE,
                ManifestElementTypes.CLAUSE);
        node = node.getChildren(null)[3];
        checkContainsNodes(node, ManifestElementTypes.HEADER_VALUE_PART);
    }

    @Test
    public void testSimpleHeaderValueStartsWithQuote() {
        PsiBuilder builder = new PsiBuilderImpl(lexer, TokenSet.EMPTY, TokenSet.EMPTY,
                        "Implementation-Vendor: \"Apache Software Foundation\"\n");
        ASTNode node = testObject.parse(ManifestStubElementTypes.FILE, builder);

        assertThat(node.getElementType(), sameInstance((IElementType) ManifestStubElementTypes.FILE));
        checkContainsNodes(node, ManifestElementTypes.SECTION);
        ASTNode fileNode = node;
        node = fileNode.getChildren(null)[0];
        checkContainsNodes(node, ManifestElementTypes.HEADER);

        node = node.getChildren(null)[0];
        checkContainsNodes(node,
                ManifestTokenType.HEADER_NAME,
                ManifestTokenType.COLON,
                ManifestTokenType.SIGNIFICANT_SPACE,
                ManifestElementTypes.CLAUSE);
        node = node.getChildren(null)[3];
        checkContainsNodes(node, ManifestElementTypes.HEADER_VALUE_PART);
    }

    @Test
    public void testSimpleHeaderValueStartsWithColon() {
        PsiBuilder builder = new PsiBuilderImpl(lexer, TokenSet.EMPTY, TokenSet.EMPTY,
                        "simpleHeader: :value\n");
        ASTNode node = testObject.parse(ManifestStubElementTypes.FILE, builder);

        assertThat(node.getElementType(), sameInstance((IElementType) ManifestStubElementTypes.FILE));
        checkContainsNodes(node, ManifestElementTypes.SECTION);
        ASTNode fileNode = node;
        node = fileNode.getChildren(null)[0];
        checkContainsNodes(node, ManifestElementTypes.HEADER);

        node = node.getChildren(null)[0];
        checkContainsNodes(node,
                ManifestTokenType.HEADER_NAME,
                ManifestTokenType.COLON,
                ManifestTokenType.SIGNIFICANT_SPACE,
                ManifestElementTypes.CLAUSE);
        node = node.getChildren(null)[3];
        checkContainsNodes(node, ManifestElementTypes.HEADER_VALUE_PART);
    }

    @Test
    public void testSimpleHeaderValueStartsWithEquals() {
        PsiBuilder builder = new PsiBuilderImpl(lexer, TokenSet.EMPTY, TokenSet.EMPTY,
                        "simpleHeader: =value\n");
        ASTNode node = testObject.parse(ManifestStubElementTypes.FILE, builder);

        assertThat(node.getElementType(), sameInstance((IElementType) ManifestStubElementTypes.FILE));
        checkContainsNodes(node, ManifestElementTypes.SECTION);
        ASTNode fileNode = node;
        node = fileNode.getChildren(null)[0];
        checkContainsNodes(node, ManifestElementTypes.HEADER);

        node = node.getChildren(null)[0];
        checkContainsNodes(node,
                ManifestTokenType.HEADER_NAME,
                ManifestTokenType.COLON,
                ManifestTokenType.SIGNIFICANT_SPACE,
                ManifestElementTypes.CLAUSE);
        node = node.getChildren(null)[3];
        checkContainsNodes(node, ManifestElementTypes.HEADER_VALUE_PART);
    }

    @Test
    public void testSimpleHeaderValueStartsWithSemicolon() {
        PsiBuilder builder = new PsiBuilderImpl(lexer, TokenSet.EMPTY, TokenSet.EMPTY,
                        "simpleHeader: ;value\n");
        ASTNode node = testObject.parse(ManifestStubElementTypes.FILE, builder);

        assertThat(node.getElementType(), sameInstance((IElementType) ManifestStubElementTypes.FILE));
        checkContainsNodes(node, ManifestElementTypes.SECTION);
        ASTNode fileNode = node;
        node = fileNode.getChildren(null)[0];
        checkContainsNodes(node, ManifestElementTypes.HEADER);

        node = node.getChildren(null)[0];
        checkContainsNodes(node,
                ManifestTokenType.HEADER_NAME,
                ManifestTokenType.COLON,
                ManifestTokenType.SIGNIFICANT_SPACE,
                ManifestElementTypes.CLAUSE);
        node = node.getChildren(null)[3];
        checkContainsNodes(node, ManifestElementTypes.HEADER_VALUE_PART);
    }

    @Test
    public void testEmptyClause() {
        PsiBuilder builder = new PsiBuilderImpl(lexer, TokenSet.EMPTY, TokenSet.EMPTY,
                "Import-Package: ,com.acme\n");
        ASTNode node = testObject.parse(ManifestStubElementTypes.FILE, builder);

        assertThat(node.getElementType(), sameInstance((IElementType) ManifestStubElementTypes.FILE));
        checkContainsNodes(node, ManifestElementTypes.SECTION);
        node = node.getChildren(null)[0];
        checkContainsNodes(node, ManifestElementTypes.HEADER);
        node = node.getChildren(null)[0];
        checkContainsNodes(node,
                ManifestTokenType.HEADER_NAME,
                ManifestTokenType.COLON,
                ManifestTokenType.SIGNIFICANT_SPACE,
                ManifestElementTypes.CLAUSE,
                ManifestTokenType.COMMA,
                ManifestElementTypes.CLAUSE
        );

        checkContainsNodes(node.getChildren(null)[3],
                ManifestElementTypes.HEADER_VALUE_PART);
        checkContainsNodes(node.getChildren(null)[5],
                ManifestElementTypes.HEADER_VALUE_PART);
    }

    @Test
    public void testTwoClauses() {
        PsiBuilder builder = new PsiBuilderImpl(lexer, TokenSet.EMPTY, TokenSet.EMPTY,
                "Import-Package: com.acme.p;a=b,com.acme\n");
        ASTNode node = testObject.parse(ManifestStubElementTypes.FILE, builder);

        assertThat(node.getElementType(), sameInstance((IElementType) ManifestStubElementTypes.FILE));
        checkContainsNodes(node, ManifestElementTypes.SECTION);
        node = node.getChildren(null)[0];
        checkContainsNodes(node, ManifestElementTypes.HEADER);
        node = node.getChildren(null)[0];
        checkContainsNodes(node,
                ManifestTokenType.HEADER_NAME,
                ManifestTokenType.COLON,
                ManifestTokenType.SIGNIFICANT_SPACE,
                ManifestElementTypes.CLAUSE,
                ManifestTokenType.COMMA,
                ManifestElementTypes.CLAUSE
        );

        checkContainsNodes(node.getChildren(null)[3],
                ManifestElementTypes.HEADER_VALUE_PART,
                ManifestTokenType.SEMICOLON,
                ManifestElementTypes.ATTRIBUTE);
        checkContainsNodes(node.getChildren(null)[5],
                ManifestElementTypes.HEADER_VALUE_PART);
    }



    private void checkContainsNodes(ASTNode node, IElementType... types) {
        ASTNode[] astNodes = node.getChildren(null);
        assertThat(astNodes, notNullValue());
        assertThat(astNodes.length, equalTo(types.length));
        for (int i = 0; i < types.length; i++) {
            assertThat(astNodes[i].getElementType(), equalTo(types[i]));
        }
    }
}
