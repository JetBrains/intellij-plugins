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

import org.jetbrains.annotations.NotNull;
import org.osmorc.manifest.ManifestConstants;
import org.osmorc.manifest.lang.headerparser.HeaderParserProvider;
import org.osmorc.manifest.lang.headerparser.HeaderParserProviderRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class OSGiManifestHeaderProviderRepository implements HeaderParserProviderRepository {
  private final List<HeaderParserProvider> headerProviders;

  public OSGiManifestHeaderProviderRepository(GenericComplexHeaderParser genericComplexHeaderParser,
                                              BundleSymbolicNameParser bundleSymbolicNameParser,
                                              BundleVersionParser bundleVersionParser,
                                              ExportPackageParser exportPackageParser,
                                              ImportPackageParser importPackageParser,
                                              PrivatePackageParser privatePackageParser,
                                              RequireBundleParser requireBundleParser,
                                              BundleActivatorParser bundleActivatorParser) {
    AbstractHeaderParserImpl simpleHeaderParser = AbstractHeaderParserImpl.SIMPLE;
    headerProviders = new ArrayList<HeaderParserProvider>();

    headerProviders.add(new HeaderParserProviderImpl(ManifestConstants.Headers.BUNDLE_MANIFEST_VERSION, simpleHeaderParser));
    headerProviders.add(new HeaderParserProviderImpl(ManifestConstants.Headers.BUNDLE_NAME, simpleHeaderParser));
    headerProviders.add(new HeaderParserProviderImpl(ManifestConstants.Headers.BUNDLE_SYMBOLIC_NAME, bundleSymbolicNameParser));
    headerProviders.add(new HeaderParserProviderImpl(ManifestConstants.Headers.BUNDLE_VERSION, bundleVersionParser));
    headerProviders.add(new HeaderParserProviderImpl(ManifestConstants.Headers.EXPORT_PACKAGE, exportPackageParser));
    headerProviders.add(new HeaderParserProviderImpl(ManifestConstants.Headers.IMPORT_PACKAGE, importPackageParser));
    headerProviders.add(new HeaderParserProviderImpl(ManifestConstants.Headers.PRIVATE_PACKAGE, privatePackageParser));
    headerProviders.add(new HeaderParserProviderImpl(ManifestConstants.Headers.REQUIRE_BUNDLE, requireBundleParser));
    headerProviders.add(new HeaderParserProviderImpl(ManifestConstants.Headers.BUNDLE_REQUIRED_EXECUTION_ENV, genericComplexHeaderParser));
    headerProviders.add(new HeaderParserProviderImpl(ManifestConstants.Headers.FRAGMENT_HOST, simpleHeaderParser));
    headerProviders.add(new HeaderParserProviderImpl(ManifestConstants.Headers.BUNDLE_ACTIVATION_POLICY, simpleHeaderParser));
    headerProviders.add(new HeaderParserProviderImpl(ManifestConstants.Headers.BUNDLE_ACTIVATOR, bundleActivatorParser));
    headerProviders.add(new HeaderParserProviderImpl(ManifestConstants.Headers.BUNDLE_CATEGORY, simpleHeaderParser));
    headerProviders.add(new HeaderParserProviderImpl(ManifestConstants.Headers.BUNDLE_CLASS_PATH, simpleHeaderParser));
    headerProviders.add(new HeaderParserProviderImpl(ManifestConstants.Headers.BUNDLE_CONTACT_ADDRESS, simpleHeaderParser));
    headerProviders.add(new HeaderParserProviderImpl(ManifestConstants.Headers.BUNDLE_COPYRIGHT, simpleHeaderParser));
    headerProviders.add(new HeaderParserProviderImpl(ManifestConstants.Headers.BUNDLE_DESCRIPTION, simpleHeaderParser));
    headerProviders.add(new HeaderParserProviderImpl(ManifestConstants.Headers.BUNDLE_DOC_URL, simpleHeaderParser));
    headerProviders.add(new HeaderParserProviderImpl(ManifestConstants.Headers.BUNDLE_LOCALIZATION, simpleHeaderParser));
    headerProviders.add(new HeaderParserProviderImpl(ManifestConstants.Headers.BUNDLE_NATIVE_CODE, simpleHeaderParser));
    headerProviders.add(new HeaderParserProviderImpl(ManifestConstants.Headers.BUNDLE_UPDATE_LOCATION, simpleHeaderParser));
    headerProviders.add(new HeaderParserProviderImpl(ManifestConstants.Headers.BUNDLE_VENDOR, simpleHeaderParser));
    headerProviders.add(new HeaderParserProviderImpl(ManifestConstants.Headers.DYNAMIC_IMPORT_PACKAGE, simpleHeaderParser));
    headerProviders.add(new HeaderParserProviderImpl(ManifestConstants.Headers.EXPORT_SERVICE, simpleHeaderParser));
    headerProviders.add(new HeaderParserProviderImpl(ManifestConstants.Headers.IMPORT_SERVICE, simpleHeaderParser));
    headerProviders.add(new HeaderParserProviderImpl(ManifestConstants.Headers.SERVICE_COMPONENT, simpleHeaderParser));
  }

  @NotNull
  public Collection<HeaderParserProvider> getHeaderParserProviders() {
    return Collections.unmodifiableList(headerProviders);
  }
}
