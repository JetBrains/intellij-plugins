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

import org.apache.felix.framework.util.manifestparser.Capability;
import org.apache.felix.framework.util.manifestparser.ManifestParser;
import org.apache.felix.framework.util.manifestparser.R4Attribute;
import org.apache.felix.framework.util.manifestparser.R4Directive;
import org.apache.felix.moduleloader.ICapability;
import org.apache.felix.moduleloader.IRequirement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.manifest.BundleManifest;
import org.osmorc.manifest.lang.psi.Clause;
import org.osmorc.manifest.lang.psi.Directive;
import org.osmorc.manifest.lang.psi.Header;
import org.osmorc.manifest.lang.psi.ManifestFile;
import org.osmorc.valueobject.Version;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.osgi.framework.Constants.*;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 * @author Jan Thom&auml; (janthomae@janthomae.de)
 */
public class BundleManifestImpl implements BundleManifest {
  @NotNull
  private final ManifestFile myManifestFile;

  /**
   * Ctor.
   *
   * @param manifestFile the manifest file to work on.
   */
  public BundleManifestImpl(@NotNull ManifestFile manifestFile) {
    myManifestFile = manifestFile;
  }

  @NotNull
  public ManifestFile getManifestFile() {
    return myManifestFile;
  }

  @NotNull
  public Version getBundleVersion() {
    Version headerValue = (Version)getHeaderValue(BUNDLE_VERSION);
    if (headerValue == null) {
      headerValue = new Version(0, 0, 0, null);
    }
    return headerValue;
  }

  @Nullable
  public String getBundleSymbolicName() {
    return (String)getHeaderValue(BUNDLE_SYMBOLICNAME);
  }

  @Nullable
  public String getBundleActivator() {
    return (String)getHeaderValue(BUNDLE_ACTIVATOR);
  }

  public boolean exportsPackage(@NotNull String packageSpec) {
    Header header = myManifestFile.getHeaderByName(EXPORT_PACKAGE);
    if (header == null) {
      return false;
    }

    List<ICapability> capabilities = new ArrayList<ICapability>();
    Clause[] clauses = header.getClauses();
    for (Clause clause : clauses) {
      try {
        capabilities.addAll(Arrays.asList(ManifestParser.parseExportHeader(clause.getClauseText())));
      }
      catch (Exception e) {
        // unparseable header
        return false;
      }
    }

    IRequirement[] requirements;
    try {
      requirements = ManifestParser.parseImportHeader(packageSpec);
    }
    catch (Exception e) {
      // unparseable header
      return false;
    }

    for (IRequirement requirement : requirements) {
      boolean satisfied = false;
      for (ICapability capability : capabilities) {
        if (requirement.isSatisfied(capability)) {
          satisfied = true;
          break;
        }
      }
      if (!satisfied) {
        // at least one requirement is not satisfied by any of the capabilities in this bundle
        return false;
      }
    }

    // all requiremets are satisfied
    return true;
  }

  @NotNull
  @Override
  public List<String> getImports() {
    Header header = myManifestFile.getHeaderByName(IMPORT_PACKAGE);
    if (header == null) {
      return Collections.emptyList();
    }
    Clause[] clauses = header.getClauses();
    List<String> result = new ArrayList<String>(clauses.length);
    for (Clause clause : clauses) {
      result.add(clause.getClauseText());
    }
    return result;
  }

  @Override
  @NotNull
  public List<String> getRequiredBundles() {
    Header header = myManifestFile.getHeaderByName(REQUIRE_BUNDLE);
    if (header == null) {
      return Collections.emptyList();
    }
    Clause[] clauses = header.getClauses();
    List<String> result = new ArrayList<String>(clauses.length);
    for (Clause clause : clauses) {
      result.add(clause.getClauseText());
    }
    return result;
  }


  @Override
  public boolean isRequiredBundle(@NotNull String bundleSpec) {

    IRequirement[] requirements;
    try {
      requirements = ManifestParser.parseRequireBundleHeader(bundleSpec);
    }
    catch (Exception e) {
      // invalid require spec
      return false;
    }

    // build a capability for this

    String symbolicName = getBundleSymbolicName();
    if (symbolicName == null) {
      return false;
    }
    Version version = getBundleVersion();

    ICapability moduleCapability = new Capability(ICapability.MODULE_NAMESPACE,
                                                  new R4Directive[]{new R4Directive(BUNDLE_SYMBOLICNAME, symbolicName)}, new R4Attribute[]{
      new R4Attribute(BUNDLE_SYMBOLICNAME_ATTRIBUTE, symbolicName, false),
      new R4Attribute(BUNDLE_VERSION_ATTRIBUTE,
                      new org.osgi.framework.Version(version.getMajor(), version.getMinor(), version.getMicro(), version.getQualifier()),
                      false)
    });


    for (IRequirement requirement : requirements) {
      if (!requirement.isSatisfied(moduleCapability)) {
        return false;
      }
    }
    // all requirements are satisfied
    return true;
  }

  @Override
  public boolean reExportsBundle(@NotNull BundleManifest otherBundle) {
    Header header = myManifestFile.getHeaderByName(REQUIRE_BUNDLE);
    if (header == null) {
      return false;
    }
    Clause[] clauses = header.getClauses();
    for (Clause clause : clauses) {
      String requireSpec = clause.getClauseText();
      // first check if the clause is set to re-export, if not, we can skip the more expensive checks
      Directive directiveByName = clause.getDirectiveByName(VISIBILITY_DIRECTIVE);
      if (directiveByName == null) {
        continue; // skip to the next require
      }
      if (VISIBILITY_REEXPORT.equals(directiveByName.getValue())) {
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
    Header header = myManifestFile.getHeaderByName(FRAGMENT_HOST);
    return header != null;
  }


  @Override
  public boolean isFragmentHostFor(@NotNull BundleManifest fragmentBundle) {
    Header header = fragmentBundle.getManifestFile().getHeaderByName(FRAGMENT_HOST);
    if ( header == null ) {
      return false;
    }

    Clause[] clauses = header.getClauses();
    if ( clauses.length != 1 ) { // bundle should have exactly one clause
      return false;
    }
    Clause clause = clauses[0];
    String hostSpec = clause.getClauseText();
    // they follow the same semantics so i think it is safe to reuse this method here. We do not handle extension bundles at all.
    return isRequiredBundle(hostSpec);
  }

  @Nullable
  private Object getHeaderValue(@NotNull String headerName) {
    Header header = myManifestFile.getHeaderByName(headerName);
    if (header != null) {
      return header.getSimpleConvertedValue();
    }
    return null;
  }
}
