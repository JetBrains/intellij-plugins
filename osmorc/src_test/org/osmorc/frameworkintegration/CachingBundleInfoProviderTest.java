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

package org.osmorc.frameworkintegration;

import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TempDirTestFixture;
import static org.hamcrest.Matchers.equalTo;
import org.junit.After;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.osmorc.TestUtil;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class CachingBundleInfoProviderTest {
    private IdeaProjectTestFixture fixture;
    private TempDirTestFixture myTempDirFixture;
    private String dirbundleUrl;
    private String invaliddirbundleUrl;
    private String jarbundleUrl;

    @Before
    public void setUp() throws Exception {
        fixture = TestUtil.createTestFixture();
        myTempDirFixture = IdeaTestFixtureFactory.getFixtureFactory().createTempDirTestFixture();
        myTempDirFixture.setUp();
        fixture.setUp();
        String tempDirPath = myTempDirFixture.getTempDirPath();
        TestUtil.loadModules("CachingBundleInfoProviderTest", fixture.getProject(), tempDirPath);
        dirbundleUrl = myTempDirFixture.getFile("t0/dirbundle").getUrl();
        invaliddirbundleUrl = myTempDirFixture.getFile("t0/invaliddirbundle").getUrl();
        jarbundleUrl = myTempDirFixture.getFile("t0/jarbundle.jar").getUrl();
    }

    @After
    public void tearDown() throws Exception {
        fixture.tearDown();
        myTempDirFixture.tearDown();
    }

    @Test
    public void testIsBundle() throws Exception {
        assertThat(CachingBundleInfoProvider.isBundle(dirbundleUrl), equalTo(true));
        assertThat(CachingBundleInfoProvider.isBundle(jarbundleUrl), equalTo(true));
        assertThat(CachingBundleInfoProvider.isBundle(invaliddirbundleUrl), equalTo(false));
    }

    @Test
    public void testGetBundleSymbolicName() throws Exception {
        assertThat(CachingBundleInfoProvider.getBundleSymbolicName(dirbundleUrl), equalTo("dirbundle"));
        assertThat(CachingBundleInfoProvider.getBundleSymbolicName(jarbundleUrl), equalTo("jarbundle"));
        assertThat(CachingBundleInfoProvider.getBundleSymbolicName(invaliddirbundleUrl), equalTo(null));
    }

    @Test
    public void testGetBundleVersions() throws Exception {
        assertThat(CachingBundleInfoProvider.getBundleVersions(dirbundleUrl), equalTo("1.0.0"));
        assertThat(CachingBundleInfoProvider.getBundleVersions(jarbundleUrl), equalTo("1.0.0"));
        assertThat(CachingBundleInfoProvider.getBundleVersions(invaliddirbundleUrl), equalTo(null));
    }

    @Test
    public void testIsFragmentBundle() throws Exception {
        assertThat(CachingBundleInfoProvider.isFragmentBundle(dirbundleUrl), equalTo(true));
        assertThat(CachingBundleInfoProvider.isFragmentBundle(jarbundleUrl), equalTo(false));
        assertThat(CachingBundleInfoProvider.isFragmentBundle(invaliddirbundleUrl), equalTo(false));
    }
}
