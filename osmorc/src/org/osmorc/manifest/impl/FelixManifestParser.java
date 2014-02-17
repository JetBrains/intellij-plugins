/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.osmorc.manifest.impl;

import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.apache.felix.framework.Logger;
import org.apache.felix.framework.util.manifestparser.ParsedHeaderClause;
import org.jetbrains.annotations.Nullable;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleRevision;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * A wrapper around {@link org.apache.felix.framework.util.manifestparser.ManifestParser}.
 */
class FelixManifestParser {
  private static final Logger NULL_LOGGER = new Logger() {
    @Override
    protected void doLog(Bundle bundle, ServiceReference sr, int level, String msg, Throwable throwable) { }
  };

  private static final Method parseStandardHeader;
  private static final Method parseBundleSymbolicName;
  private static final Method normalizeExportClauses;
  private static final Method normalizeDynamicImportClauses;
  private static final Method normalizeRequireClauses;
  private static final Method convertExports;
  private static final Method convertImports;
  private static final Method convertRequires;

  static {
    try {
      //noinspection SpellCheckingInspection
      Class<?> aClass = Class.forName("org.apache.felix.framework.util.manifestparser.ManifestParser");

      parseStandardHeader = aClass.getDeclaredMethod("parseStandardHeader", String.class);
      parseBundleSymbolicName = aClass.getDeclaredMethod("parseBundleSymbolicName", BundleRevision.class, Map.class);
      normalizeExportClauses =
        aClass.getDeclaredMethod("normalizeExportClauses", Logger.class, List.class, String.class, String.class, Version.class);
      normalizeDynamicImportClauses = aClass.getDeclaredMethod("normalizeDynamicImportClauses", Logger.class, List.class, String.class);
      normalizeRequireClauses = aClass.getDeclaredMethod("normalizeRequireClauses", Logger.class, List.class, String.class);
      convertExports = aClass.getDeclaredMethod("convertExports", List.class, BundleRevision.class);
      convertImports = aClass.getDeclaredMethod("convertImports", List.class, BundleRevision.class);
      convertRequires = aClass.getDeclaredMethod("convertRequires", List.class, BundleRevision.class);

      parseStandardHeader.setAccessible(true);
      parseBundleSymbolicName.setAccessible(true);
      normalizeExportClauses.setAccessible(true);
      normalizeDynamicImportClauses.setAccessible(true);
      normalizeRequireClauses.setAccessible(true);
      convertExports.setAccessible(true);
      convertImports.setAccessible(true);
      convertRequires.setAccessible(true);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  @Nullable
  public static List<BundleCapability> parseExportHeader(String header, String bsn, String bv) {
    try {
      List<ParsedHeaderClause> exportClauses = (List)parseStandardHeader.invoke(null, header);
      exportClauses = (List)normalizeExportClauses.invoke(null, NULL_LOGGER, exportClauses, "2", bsn, new Version(bv));
      return (List)convertExports.invoke(null, exportClauses, null);
    }
    catch (Exception e) {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  @Nullable
  public static List<BundleRequirement> parseImportHeader(String header) {
    try {
      List<ParsedHeaderClause> importClauses = (List)parseStandardHeader.invoke(null, header);
      importClauses = (List)normalizeDynamicImportClauses.invoke(null, NULL_LOGGER, importClauses, "2");
      return (List)convertImports.invoke(null, importClauses, null);
    }
    catch (Exception e) {
      return null;
    }
  }

  @Nullable
  public static BundleCapability constructBundleCapability(String symbolicName, String version) {
    try {
      Map<String, String> headers = ContainerUtil.newHashMap();
      headers.put(Constants.BUNDLE_SYMBOLICNAME, symbolicName);
      headers.put(Constants.BUNDLE_VERSION, version);
      return (BundleCapability)parseBundleSymbolicName.invoke(null, null, headers);
    }
    catch (Exception e) {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  @Nullable
  public static List<BundleRequirement> parseRequireBundleHeader(String header) {
    try {
      List<ParsedHeaderClause> rbClauses = (List)parseStandardHeader.invoke(null, header);
      rbClauses = (List)normalizeRequireClauses.invoke(null, NULL_LOGGER, rbClauses, "2");
      return ContainerUtil.map((List)convertRequires.invoke(null, rbClauses, null), Function.ID);
    }
    catch (Exception e) {
      return null;
    }
  }
}