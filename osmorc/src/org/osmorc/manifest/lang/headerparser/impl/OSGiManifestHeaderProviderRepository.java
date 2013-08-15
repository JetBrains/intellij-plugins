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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class OSGiManifestHeaderProviderRepository implements HeaderParserProviderRepository {
  private final List<HeaderParserProvider> myProviders = Arrays.<HeaderParserProvider>asList(
    new HeaderParserProviderImpl(ManifestConstants.Headers.BUNDLE_MANIFEST_VERSION, AbstractHeaderParser.SIMPLE),
    new HeaderParserProviderImpl(ManifestConstants.Headers.BUNDLE_NAME, AbstractHeaderParser.SIMPLE),
    new HeaderParserProviderImpl(ManifestConstants.Headers.BUNDLE_SYMBOLIC_NAME, AbstractHeaderParser.COMPLEX),
    new HeaderParserProviderImpl(ManifestConstants.Headers.BUNDLE_VERSION, BundleVersionParser.INSTANCE),
    new HeaderParserProviderImpl(ManifestConstants.Headers.EXPORT_PACKAGE, ExportPackageParser.INSTANCE),
    new HeaderParserProviderImpl(ManifestConstants.Headers.IMPORT_PACKAGE, BasePackageParser.INSTANCE),
    new HeaderParserProviderImpl(ManifestConstants.Headers.IGNORE_PACKAGE, BasePackageParser.INSTANCE),
    new HeaderParserProviderImpl(ManifestConstants.Headers.PRIVATE_PACKAGE, BasePackageParser.INSTANCE),
    new HeaderParserProviderImpl(ManifestConstants.Headers.REQUIRE_BUNDLE, RequireBundleParser.INSTANCE),
    new HeaderParserProviderImpl(ManifestConstants.Headers.BUNDLE_REQUIRED_EXECUTION_ENV, AbstractHeaderParser.COMPLEX),
    new HeaderParserProviderImpl(ManifestConstants.Headers.FRAGMENT_HOST, AbstractHeaderParser.SIMPLE),
    new HeaderParserProviderImpl(ManifestConstants.Headers.BUNDLE_ACTIVATION_POLICY, AbstractHeaderParser.SIMPLE),
    new HeaderParserProviderImpl(ManifestConstants.Headers.BUNDLE_ACTIVATOR, BundleActivatorParser.INSTANCE),
    new HeaderParserProviderImpl(ManifestConstants.Headers.BUNDLE_CATEGORY, AbstractHeaderParser.SIMPLE),
    new HeaderParserProviderImpl(ManifestConstants.Headers.BUNDLE_CLASS_PATH, AbstractHeaderParser.SIMPLE),
    new HeaderParserProviderImpl(ManifestConstants.Headers.BUNDLE_CONTACT_ADDRESS, AbstractHeaderParser.SIMPLE),
    new HeaderParserProviderImpl(ManifestConstants.Headers.BUNDLE_COPYRIGHT, AbstractHeaderParser.SIMPLE),
    new HeaderParserProviderImpl(ManifestConstants.Headers.BUNDLE_DESCRIPTION, AbstractHeaderParser.SIMPLE),
    new HeaderParserProviderImpl(ManifestConstants.Headers.BUNDLE_DOC_URL, AbstractHeaderParser.SIMPLE),
    new HeaderParserProviderImpl(ManifestConstants.Headers.BUNDLE_LOCALIZATION, AbstractHeaderParser.SIMPLE),
    new HeaderParserProviderImpl(ManifestConstants.Headers.BUNDLE_NATIVE_CODE, AbstractHeaderParser.SIMPLE),
    new HeaderParserProviderImpl(ManifestConstants.Headers.BUNDLE_UPDATE_LOCATION, AbstractHeaderParser.SIMPLE),
    new HeaderParserProviderImpl(ManifestConstants.Headers.BUNDLE_VENDOR, AbstractHeaderParser.SIMPLE),
    new HeaderParserProviderImpl(ManifestConstants.Headers.DYNAMIC_IMPORT_PACKAGE, AbstractHeaderParser.SIMPLE),
    new HeaderParserProviderImpl(ManifestConstants.Headers.EXPORT_SERVICE, AbstractHeaderParser.SIMPLE),
    new HeaderParserProviderImpl(ManifestConstants.Headers.IMPORT_SERVICE, AbstractHeaderParser.SIMPLE),
    new HeaderParserProviderImpl(ManifestConstants.Headers.SERVICE_COMPONENT, AbstractHeaderParser.SIMPLE));

  @NotNull
  @Override
  public Collection<HeaderParserProvider> getHeaderParserProviders() {
    return myProviders;
  }
}
