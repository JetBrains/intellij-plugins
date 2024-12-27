// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.build;

import com.intellij.flex.build.FlexCompilerConfigFileUtilBase;
import com.intellij.openapi.util.Pair;
import com.intellij.util.xml.NanoXmlUtil;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;

import static com.intellij.lang.javascript.flex.build.FlexCompilerConfigFileUtil.*;

public class NamespacesXmlBuilder extends NanoXmlUtil.BaseXmlBuilder {

  private static final String INCLUDE_NAMESPACES_URI_LOCATION = NanoXmlUtil.createLocation(FlexCompilerConfigFileUtilBase.FLEX_CONFIG, INCLUDE_NAMESPACES, URI);
  private static final String NAMESPACE_LOCATION = NanoXmlUtil.createLocation(FlexCompilerConfigFileUtilBase.FLEX_CONFIG, FlexCompilerConfigFileUtilBase.COMPILER, NAMESPACES, NAMESPACE);
  private static final String NAMESPACE_URI_LOCATION = NanoXmlUtil.createLocation(FlexCompilerConfigFileUtilBase.FLEX_CONFIG, FlexCompilerConfigFileUtilBase.COMPILER, NAMESPACES, NAMESPACE, URI);
  private static final String MANIFEST_LOCATION = NanoXmlUtil.createLocation(FlexCompilerConfigFileUtilBase.FLEX_CONFIG, FlexCompilerConfigFileUtilBase.COMPILER, NAMESPACES, NAMESPACE, MANIFEST);

  private final Collection<String> myIncludedNamespaces = new ArrayList<>();
  private final Collection<Pair<String, String>> myNamespacesAndManifests = new ArrayList<>();

  private final StringBuilder myNamespaceUri = new StringBuilder();
  private final StringBuilder myManifest = new StringBuilder();

  @Override
  public void startElement(final String name, final String nsPrefix, final String nsURI, final String systemID, final int lineNr)
    throws Exception {
    super.startElement(name, nsPrefix, nsURI, systemID, lineNr);

    final String location = getLocation();
    if (NAMESPACE_LOCATION.equals(location)) {
      myNamespaceUri.delete(0, myNamespaceUri.length());
      myManifest.delete(0, myManifest.length());
    }
    else if (NAMESPACE_URI_LOCATION.equals(location)) {
      myNamespaceUri.delete(0, myNamespaceUri.length());
    }
    else if (MANIFEST_LOCATION.equals(location)) {
      myManifest.delete(0, myManifest.length());
    }
  }

  @Override
  public void addPCData(final Reader reader, final String systemID, final int lineNr) throws Exception {
    final String location = getLocation();
    if (INCLUDE_NAMESPACES_URI_LOCATION.equals(location)) {
      myIncludedNamespaces.add(readText(reader));
    }
    else if (NAMESPACE_URI_LOCATION.equals(location)) {
      myNamespaceUri.append(readText(reader));
    }
    else if (MANIFEST_LOCATION.equals(location)) {
      myManifest.append(readText(reader));
    }
  }

  @Override
  public void endElement(final String name, final String nsPrefix, final String nsURI) throws Exception {
    if (NAMESPACE_LOCATION.equals(getLocation())) {
      final String uri = myNamespaceUri.toString().trim();
      final String manifest = myManifest.toString().trim();
      if (!uri.isEmpty() && !manifest.isEmpty()) {
        myNamespacesAndManifests.add(Pair.create(uri, manifest));
      }
    }

    super.endElement(name, nsPrefix, nsURI);
  }

  public Collection<String> getIncludedNamespaces() {
    return myIncludedNamespaces;
  }

  public Collection<Pair<String, String>> getNamespacesAndManifests() {
    return myNamespacesAndManifests;
  }
}
