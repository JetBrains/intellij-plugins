// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.osgi.project;

import aQute.bnd.header.Parameters;
import aQute.bnd.osgi.Constants;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNameHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents a bundle manifest.
 * Note that it may be approximate (e.g. for module - see {@link BundleManifestCache#getManifest(Module)} for details).
 */
public class BundleManifest {
  private final Map<String, String> myMap;
  private final PsiFile mySource;

  public BundleManifest(@NotNull Map<String, String> map) {
    this(map, null);
  }

  public BundleManifest(@NotNull Map<String, String> map, @Nullable PsiFile source) {
    mySource = source;
    myMap = new HashMap<>(map);
  }

  public @Nullable PsiFile getSource() {
    return mySource;
  }

  public @Nullable String get(@NotNull String attr) {
    return myMap.get(attr);
  }

  public @Nullable String getBundleSymbolicName() {
    return get(Constants.BUNDLE_SYMBOLICNAME);
  }

  public @Nullable String getBundleActivator() {
    return get(Constants.BUNDLE_ACTIVATOR);
  }

  public @Nullable String getExportedPackage(@NotNull String packageName) {
    for (String exported : getValues(Constants.EXPORT_PACKAGE)) {
      exported = StringUtil.trimEnd(exported, ".*");
      if (PsiNameHelper.isSubpackageOf(packageName, exported)) {
        return exported;
      }
    }

    return null;
  }

  public boolean isPackageImported(@NotNull String packageName) {
    for (String imported : getValues(Constants.IMPORT_PACKAGE)) {
      if (PsiNameHelper.isSubpackageOf(packageName, imported)) {
        return true;
      }
    }

    return false;
  }

  public boolean isBundleRequired(@NotNull String bsn) {
    for (String bundleName : getValues(Constants.REQUIRE_BUNDLE)) {
      if (bsn.equals(bundleName)) {
        return true;
      }
    }

    return false;
  }

  public boolean isPrivatePackage(@NotNull String packageName) {
    for (String privatePkg : getValues(Constants.PRIVATE_PACKAGE)) {
      if (PsiNameHelper.isSubpackageOf(packageName, privatePkg)) {
        return true;
      }
    }

    return false;
  }

  private Set<String> getValues(String header) {
    String value = get(header);
    return StringUtil.isEmptyOrSpaces(value) ? Collections.emptySet() : new Parameters(value).keySet();
  }
}