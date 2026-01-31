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

import com.intellij.openapi.util.io.NioFiles;
import org.jetbrains.osgi.jps.build.CachingBundleInfoProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osmorc.OsgiTestUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
public class CachingBundleInfoProviderTest {
  private Path myTempDir;
  private String myDirBundle;
  private String myInvalidDirBundle;
  private String myJarBundle;

  @Before
  @SuppressWarnings("SpellCheckingInspection")
  public void setUp() throws Exception {
    myTempDir = Files.createTempDirectory("osgi.test.");
    OsgiTestUtil.extractProject("CachingBundleInfoProviderTest", myTempDir);
    myDirBundle = myTempDir.resolve("t0/dirbundle").toString();
    myInvalidDirBundle = myTempDir.resolve("t0/invaliddirbundle").toString();
    myJarBundle = myTempDir.resolve("t0/jarbundle.jar").toString();
  }

  @After
  public void tearDown() throws IOException {
    NioFiles.deleteRecursively(myTempDir);
  }

  @Test
  public void testIsBundle() {
    assertTrue(CachingBundleInfoProvider.isBundle(myDirBundle));
    assertTrue(CachingBundleInfoProvider.isBundle(myJarBundle));
    assertFalse(CachingBundleInfoProvider.isBundle(myInvalidDirBundle));
  }

  @Test
  @SuppressWarnings("SpellCheckingInspection")
  public void testGetBundleSymbolicName() {
    assertEquals("dirbundle", CachingBundleInfoProvider.getBundleSymbolicName(myDirBundle));
    assertEquals("jarbundle", CachingBundleInfoProvider.getBundleSymbolicName(myJarBundle));
    assertNull(CachingBundleInfoProvider.getBundleSymbolicName(myInvalidDirBundle));
  }

  @Test
  public void testGetBundleVersions() {
    assertEquals("1.0.0", CachingBundleInfoProvider.getBundleVersion(myDirBundle));
    assertEquals("1.0.0", CachingBundleInfoProvider.getBundleVersion(myJarBundle));
    assertNull(CachingBundleInfoProvider.getBundleVersion(myInvalidDirBundle));
  }

  @Test
  public void testIsFragmentBundle() {
    assertTrue(CachingBundleInfoProvider.isFragmentBundle(myDirBundle));
    assertFalse(CachingBundleInfoProvider.isFragmentBundle(myJarBundle));
    assertFalse(CachingBundleInfoProvider.isFragmentBundle(myInvalidDirBundle));
  }
}
