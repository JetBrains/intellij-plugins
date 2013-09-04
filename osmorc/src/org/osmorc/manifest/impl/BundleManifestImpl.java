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
package org.osmorc.manifest.impl;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.util.Function;
import com.intellij.util.NullableFunction;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.lang.manifest.header.HeaderParserRepository;
import org.jetbrains.lang.manifest.psi.Header;
import org.jetbrains.lang.manifest.psi.HeaderValue;
import org.jetbrains.lang.manifest.psi.ManifestFile;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
import org.osmorc.manifest.BundleManifest;
import org.osmorc.manifest.lang.psi.Clause;
import org.osmorc.manifest.lang.psi.Directive;
import org.osmorc.valueobject.Version;

import java.util.Collections;
import java.util.List;

import static org.osgi.framework.Constants.*;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 * @author Jan Thom&auml; (janthomae@janthomae.de)
 */
public class BundleManifestImpl implements BundleManifest {
  private final HeaderParserRepository myRepository;
  private final ManifestFile myManifestFile;

  public BundleManifestImpl(@NotNull ManifestFile manifestFile) {
    myRepository = ServiceManager.getService(HeaderParserRepository.class);
    myManifestFile = manifestFile;
  }

  @NotNull
  @Override
  public ManifestFile getManifestFile() {
    return myManifestFile;
  }

  @NotNull
  @Override
  public Version getBundleVersion() {
    Version headerValue = (Version)getHeaderValue(BUNDLE_VERSION);
    return headerValue != null ? headerValue : new Version(0, 0, 0, null);
  }

  @Nullable
  @Override
  public String getBundleSymbolicName() {
    return (String)getHeaderValue(BUNDLE_SYMBOLICNAME);
  }

  @Nullable
  @Override
  public String getBundleActivator() {
    return (String)getHeaderValue(BUNDLE_ACTIVATOR);
  }

  @Override
  public boolean exportsPackage(@NotNull String packageSpec) {
    Header header = myManifestFile.getHeader(EXPORT_PACKAGE);
    if (header == null) {
      return false;
    }

    List<BundleCapability> capabilities = ContainerUtil.newArrayList();
    String bsn = getBundleSymbolicName();
    String bv = getBundleVersion().toString();
    for (HeaderValue headerValue : header.getHeaderValues()) {
      List<BundleCapability> caps = FelixManifestParser.parseExportHeader(headerValue.getUnwrappedText(), bsn, bv);
      if (caps == null) return false;  // parse error
      capabilities.addAll(caps);
    }

    List<BundleRequirement> requirements = FelixManifestParser.parseImportHeader(packageSpec);
    if (requirements == null) return false;  // parse error

    return satisfies(capabilities, requirements);
  }

  @NotNull
  @Override
  public List<String> getImports() {
    return getHeaderValues(IMPORT_PACKAGE);
  }

  @Override
  @NotNull
  public List<String> getRequiredBundles() {
    return getHeaderValues(REQUIRE_BUNDLE);
  }

  @NotNull
  @Override
  public List<String> getReExportedBundles() {
    Header header = myManifestFile.getHeader(REQUIRE_BUNDLE);
    return header == null ? ContainerUtil.<String>emptyList() : ContainerUtil.mapNotNull(header.getHeaderValues(), new NullableFunction<HeaderValue, String>() {
             @Override
             public String fun(HeaderValue value) {
               Directive directive = ((Clause)value).getDirective(VISIBILITY_DIRECTIVE);
               return directive != null && VISIBILITY_REEXPORT.equals(directive.getValue()) ? value.getUnwrappedText() : null;
             }
           });
  }

  @Override
  public boolean isRequiredBundle(@NotNull String bundleSpec) {
    BundleCapability capability = FelixManifestParser.constructBundleCapability(getBundleSymbolicName(), getBundleVersion().toString());
    if (capability == null) return false;  // parse error

    List<BundleRequirement> requirements = FelixManifestParser.parseRequireBundleHeader(bundleSpec);
    if (requirements == null) return false;  // parse error

    return satisfies(Collections.singletonList(capability), requirements);
  }

  @Override
  public boolean reExportsBundle(@NotNull BundleManifest otherBundle) {
    Header header = myManifestFile.getHeader(REQUIRE_BUNDLE);
    if (header == null) return false;

    for (HeaderValue value : header.getHeaderValues()) {
      String requireSpec = value.getUnwrappedText();
      // first check if the clause is set to re-export, if not, we can skip the more expensive checks
      Directive directive = ((Clause)value).getDirective(VISIBILITY_DIRECTIVE);
      if (directive == null) {
        continue; // skip to the next require
      }
      if (VISIBILITY_REEXPORT.equals(directive.getValue())) {
        // ok it's a re-export. Now check if the bundle would satisfy the dependency
        if (otherBundle.isRequiredBundle(requireSpec)) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  public boolean isFragmentBundle() {
    return myManifestFile.getHeader(FRAGMENT_HOST) != null;
  }

  @NotNull
  @Override
  public List<String> getBundleClassPathEntries() {
    return getHeaderValues(BUNDLE_CLASSPATH);
  }

  @Override
  public boolean isFragmentHostFor(@NotNull BundleManifest fragmentBundle) {
    Header header = fragmentBundle.getManifestFile().getHeader(FRAGMENT_HOST);
    if (header == null) return false;

    List<HeaderValue> clauses = header.getHeaderValues();
    // bundle should have exactly one clause
    // they follow the same semantics so i think it is safe to reuse this method here. We do not handle extension bundles at all.
    return clauses.size() == 1 && isRequiredBundle(clauses.get(0).getUnwrappedText());
  }

  private Object getHeaderValue(String headerName) {
    Header header = myManifestFile.getHeader(headerName);
    return header != null ? myRepository.getConvertedValue(header) : null;
  }

  private List<String> getHeaderValues(String headerName) {
    Header header = myManifestFile.getHeader(headerName);
    return header == null ? ContainerUtil.<String>emptyList() : ContainerUtil.map(header.getHeaderValues(), new Function<HeaderValue, String>() {
      @Override
      public String fun(HeaderValue value) {
        return value.getUnwrappedText();
      }
    });
  }

  private static boolean satisfies(List<BundleCapability> capabilities, List<BundleRequirement> requirements) {
    nextRequirement:
    for (BundleRequirement requirement : requirements) {
      for (BundleCapability capability : capabilities) {
        if (requirement.matches(capability)) {
          continue nextRequirement;
        }
      }
      return false;  // requirement is not satisfied by any of the capabilities
    }

    return true;
  }
}
