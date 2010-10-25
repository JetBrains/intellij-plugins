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
package org.osmorc.manifest.lang.headerparser.impl;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.util.TextRange;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import static org.easymock.EasyMock.*;
import static org.hamcrest.Matchers.equalTo;
import org.junit.After;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmorc.SwingRunner;
import org.osmorc.manifest.lang.psi.HeaderValuePart;
import org.osmorc.valueobject.Version;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
@RunWith(SwingRunner.class)
public class BundleVersionParserTest {
    public BundleVersionParserTest() {
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
    public void testGetValue() {
        HeaderValuePart headerValueMock = createMock(HeaderValuePart.class);
        expect(headerValueMock.getUnwrappedText()).andReturn("1.2.3.b300");

        replay(headerValueMock);

        BundleVersionParser testObject = new BundleVersionParser();

        assertThat(testObject.getValue(headerValueMock), equalTo((Object) new Version(1, 2, 3, "b300")));

        verify(headerValueMock);
    }

    @Test
    public void testAnnotate() {
        AnnotationHolder annotationHolderMock = createMock(AnnotationHolder.class);
        HeaderValuePart headerValueMock = createMock(HeaderValuePart.class);
        expect(headerValueMock.getUnwrappedText()).andReturn("1.0,u");
        expect(headerValueMock.getTextRange()).andReturn(new TextRange(10, 15));
        expect(annotationHolderMock.createErrorAnnotation(new TextRange(12, 15),
                "The minor component of the defined version is not a valid number")).andReturn(null);

        replay(headerValueMock, annotationHolderMock);

        BundleVersionParser testObject = new BundleVersionParser();

        testObject.annotate(headerValueMock, annotationHolderMock);

        verify(headerValueMock, annotationHolderMock);
    }

    private final IdeaProjectTestFixture fixture;
}
