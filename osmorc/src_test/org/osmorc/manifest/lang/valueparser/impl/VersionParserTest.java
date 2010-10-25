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
package org.osmorc.manifest.lang.valueparser.impl;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.util.TextRange;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmorc.SwingRunner;
import org.osmorc.manifest.lang.psi.HeaderValuePart;
import org.osmorc.valueobject.Version;

import static org.easymock.EasyMock.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
@RunWith(SwingRunner.class)
public class VersionParserTest {
    public VersionParserTest() {
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

    @Test
    public void testParseValueWOAnnotationHolder() {
        HeaderValuePart headerValueMock = createMock(HeaderValuePart.class);
        expect(headerValueMock.getUnwrappedText()).andReturn("1.2.3.b300");
        expect(headerValueMock.getUnwrappedText()).andReturn("1.2.3");
        expect(headerValueMock.getUnwrappedText()).andReturn("1.2.");
        expect(headerValueMock.getUnwrappedText()).andReturn("1");
        expect(headerValueMock.getUnwrappedText()).andReturn("1.x");
        expect(headerValueMock.getUnwrappedText()).andReturn("1-");
        expect(headerValueMock.getUnwrappedText()).andReturn("1.0,3");

        replay(headerValueMock);

        VersionParser testObject = new VersionParser();


        assertThat(testObject.parseValue(headerValueMock, null), equalTo(new Version(1, 2, 3, "b300")));
        assertThat(testObject.parseValue(headerValueMock, null), equalTo(new Version(1, 2, 3, "")));
        assertThat(testObject.parseValue(headerValueMock, null), equalTo(new Version(1, 2, 0, "")));
        assertThat(testObject.parseValue(headerValueMock, null), equalTo(new Version(1, 0, 0, "")));
        assertThat(testObject.parseValue(headerValueMock, null), equalTo(new Version(1, 0, 0, "")));
        assertThat(testObject.parseValue(headerValueMock, null), equalTo(new Version(0, 0, 0, "")));
        assertThat(testObject.parseValue(headerValueMock, null), equalTo(new Version(1, 0, 0, "")));

        verify(headerValueMock);
    }

    @Test
    public void testParseValueWAnnotationHolder() {
        AnnotationHolder annotationHolderMock = createMock(AnnotationHolder.class);
        HeaderValuePart headerValueMock = createMock(HeaderValuePart.class);
        expect(headerValueMock.getUnwrappedText()).andReturn("1.2.3.b300");
        expect(headerValueMock.getUnwrappedText()).andReturn("1-");
        expect(headerValueMock.getTextRange()).andReturn(new TextRange(10, 12));
        expect(annotationHolderMock.createErrorAnnotation(new TextRange(10, 12),
                "The major component of the defined version is not a valid number")).andReturn(null);
        expect(headerValueMock.getUnwrappedText()).andReturn("1.x");
        expect(headerValueMock.getTextRange()).andReturn(new TextRange(10, 13));
        expect(annotationHolderMock.createErrorAnnotation(new TextRange(12, 13),
                "The minor component of the defined version is not a valid number")).andReturn(null);
        expect(headerValueMock.getUnwrappedText()).andReturn("1.0.u");
        expect(headerValueMock.getTextRange()).andReturn(new TextRange(10, 15));
        expect(annotationHolderMock.createErrorAnnotation(new TextRange(14, 15),
                "The micro component of the defined version is not a valid number")).andReturn(null);
        expect(headerValueMock.getUnwrappedText()).andReturn("1.0,u");
        expect(headerValueMock.getTextRange()).andReturn(new TextRange(10, 15));
        expect(annotationHolderMock.createErrorAnnotation(new TextRange(12, 15),
                "The minor component of the defined version is not a valid number")).andReturn(null);
        expect(headerValueMock.getUnwrappedText()).andReturn("1.0.0.b2+3");
        expect(headerValueMock.getTextRange()).andReturn(new TextRange(10, 20));
        expect(annotationHolderMock.createErrorAnnotation(new TextRange(16, 20),
                "The qualifier component of the defined version is invalid. It may only contain alphanumeric characters, '-' and '_'"))
                .andReturn(null);
        expect(headerValueMock.getUnwrappedText()).andReturn("1.0.0.b2_3");
        expect(headerValueMock.getUnwrappedText()).andReturn("1.0.0.b2-3");

        replay(headerValueMock, annotationHolderMock);

        VersionParser testObject = new VersionParser();

        assertThat(testObject.parseValue(headerValueMock, annotationHolderMock), equalTo(new Version(1, 2, 3, "b300")));
        assertThat(testObject.parseValue(headerValueMock, annotationHolderMock), equalTo(new Version(0, 0, 0, "")));
        assertThat(testObject.parseValue(headerValueMock, annotationHolderMock), equalTo(new Version(1, 0, 0, "")));
        assertThat(testObject.parseValue(headerValueMock, annotationHolderMock), equalTo(new Version(1, 0, 0, "")));
        assertThat(testObject.parseValue(headerValueMock, annotationHolderMock), equalTo(new Version(1, 0, 0, "")));
        assertThat(testObject.parseValue(headerValueMock, annotationHolderMock), equalTo(new Version(1, 0, 0, "b2+3")));
        assertThat(testObject.parseValue(headerValueMock, annotationHolderMock), equalTo(new Version(1, 0, 0, "b2_3")));
        assertThat(testObject.parseValue(headerValueMock, annotationHolderMock), equalTo(new Version(1, 0, 0, "b2-3")));

        verify(headerValueMock, annotationHolderMock);
    }

    @Test
    public void testParseValueWAnnotationHolderAndStart() {
        AnnotationHolder annotationHolderMock = createMock(AnnotationHolder.class);
        HeaderValuePart headerValueMock = createMock(HeaderValuePart.class);
        expect(headerValueMock.getUnwrappedText()).andReturn("1.0.0,1-");
        expect(headerValueMock.getTextRange()).andReturn(new TextRange(10, 12));
        expect(annotationHolderMock.createErrorAnnotation(new TextRange(16, 18),
                "The major component of the defined version is not a valid number")).andReturn(null);

        replay(headerValueMock, annotationHolderMock);

        VersionParser testObject = new VersionParser();

        assertThat(testObject.parseValue(headerValueMock, 6, 8, annotationHolderMock), equalTo(new Version(0, 0, 0, "")));

        verify(headerValueMock, annotationHolderMock);
    }

    @Test
    public void testParseValueText() {
        VersionParser testObject = new VersionParser();

        assertThat(testObject.parseValue("1.2.3.b300", 0, 10), equalTo(new Version(1, 2, 3, "b300")));
        assertThat(testObject.parseValue("a.b.1.2.3", 4, 9), equalTo(new Version(1, 2, 3, "")));
    }

    private final IdeaProjectTestFixture fixture;
}
