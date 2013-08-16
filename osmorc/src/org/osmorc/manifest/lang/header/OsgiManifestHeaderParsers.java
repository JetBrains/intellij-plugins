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
package org.osmorc.manifest.lang.header;

import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.lang.manifest.header.HeaderParser;
import org.jetbrains.lang.manifest.header.HeaderParserProvider;
import org.osmorc.manifest.ManifestConstants;

import java.util.Map;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class OsgiManifestHeaderParsers implements HeaderParserProvider {
  private final Map<String,HeaderParser> myParsers;

  public OsgiManifestHeaderParsers() {
    myParsers = ContainerUtil.newHashMap();
    myParsers.put(ManifestConstants.Headers.IMPORT_SERVICE, OsgiHeaderParser.INSTANCE);
    myParsers.put(ManifestConstants.Headers.EXPORT_SERVICE, OsgiHeaderParser.INSTANCE);
    myParsers.put(ManifestConstants.Headers.DYNAMIC_IMPORT_PACKAGE, OsgiHeaderParser.INSTANCE);
    myParsers.put(ManifestConstants.Headers.BUNDLE_VENDOR, OsgiHeaderParser.INSTANCE);
    myParsers.put(ManifestConstants.Headers.BUNDLE_UPDATE_LOCATION, OsgiHeaderParser.INSTANCE);
    myParsers.put(ManifestConstants.Headers.BUNDLE_NATIVE_CODE, OsgiHeaderParser.INSTANCE);
    myParsers.put(ManifestConstants.Headers.BUNDLE_LOCALIZATION, OsgiHeaderParser.INSTANCE);
    myParsers.put(ManifestConstants.Headers.BUNDLE_DOC_URL, OsgiHeaderParser.INSTANCE);
    myParsers.put(ManifestConstants.Headers.BUNDLE_DESCRIPTION, OsgiHeaderParser.INSTANCE);
    myParsers.put(ManifestConstants.Headers.BUNDLE_COPYRIGHT, OsgiHeaderParser.INSTANCE);
    myParsers.put(ManifestConstants.Headers.BUNDLE_CONTACT_ADDRESS, OsgiHeaderParser.INSTANCE);
    myParsers.put(ManifestConstants.Headers.BUNDLE_CLASS_PATH, OsgiHeaderParser.INSTANCE);
    myParsers.put(ManifestConstants.Headers.BUNDLE_CATEGORY, OsgiHeaderParser.INSTANCE);
    myParsers.put(ManifestConstants.Headers.BUNDLE_ACTIVATION_POLICY, OsgiHeaderParser.INSTANCE);
    myParsers.put(ManifestConstants.Headers.FRAGMENT_HOST, OsgiHeaderParser.INSTANCE);
    myParsers.put(ManifestConstants.Headers.BUNDLE_REQUIRED_EXECUTION_ENV, OsgiHeaderParser.INSTANCE);
    myParsers.put(ManifestConstants.Headers.BUNDLE_SYMBOLIC_NAME, OsgiHeaderParser.INSTANCE);
    myParsers.put(ManifestConstants.Headers.BUNDLE_NAME, OsgiHeaderParser.INSTANCE);
    myParsers.put(ManifestConstants.Headers.BUNDLE_MANIFEST_VERSION, OsgiHeaderParser.INSTANCE);

    myParsers.put(ManifestConstants.Headers.BUNDLE_ACTIVATOR, BundleActivatorParser.INSTANCE);
    myParsers.put(ManifestConstants.Headers.REQUIRE_BUNDLE, RequireBundleParser.INSTANCE);
    myParsers.put(ManifestConstants.Headers.PRIVATE_PACKAGE, BasePackageParser.INSTANCE);
    myParsers.put(ManifestConstants.Headers.IGNORE_PACKAGE, BasePackageParser.INSTANCE);
    myParsers.put(ManifestConstants.Headers.IMPORT_PACKAGE, BasePackageParser.INSTANCE);
    myParsers.put(ManifestConstants.Headers.EXPORT_PACKAGE, ExportPackageParser.INSTANCE);
    myParsers.put(ManifestConstants.Headers.BUNDLE_VERSION, BundleVersionParser.INSTANCE);
  }

  @NotNull
  @Override
  public Map<String, HeaderParser> getHeaderParsers() {
    return myParsers;
  }
}
