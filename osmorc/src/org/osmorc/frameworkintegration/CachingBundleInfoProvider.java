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

import org.jetbrains.jps.osmorc.model.JpsCachingBundleInfoProvider;
import org.osmorc.util.OsgiFileUtil;

import java.io.File;

public class CachingBundleInfoProvider extends JpsCachingBundleInfoProvider {

  public static String getBundleSymbolicName(String bundleUrl) {
    return getBundleSymbolicName(urlToFile(bundleUrl));
  }

  public static String getBundleVersion(String bundleUrl) {
    return getBundleVersion(urlToFile(bundleUrl));
  }

  public static boolean canBeBundlified(String url) {
    return canBeBundlified(urlToFile(url));
  }

  public static boolean isBundle(String url) {
    return isBundle(urlToFile(url));
  }

  public static String getBundleAttribute(String bundleUrl, String attribute) {
    return getBundleAttribute(urlToFile(bundleUrl), attribute);
  }

  public static boolean isFragmentBundle(String bundleUrl) {
    return isFragmentBundle(urlToFile(bundleUrl));
  }

  private static File urlToFile(String bundleUrl) {
    return new File(OsgiFileUtil.urlToPath(normalize(bundleUrl)));
  }

  private static String normalize(String bundleUrl) {
    return bundleUrl.replaceAll("file:/([^/]+)", "file:///$1");
  }
}
