/*
 * Copyright 2000-2016 JetBrains s.r.o.
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
package org.jetbrains.osgi.project;

import aQute.bnd.header.Parameters;
import aQute.bnd.osgi.Constants;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNameHelper;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
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
    myMap = ContainerUtil.newHashMap(map);
  }

  @Nullable
  public PsiFile getSource() {
    return mySource;
  }

  @Nullable
  public String get(@NotNull String attr) {
    return myMap.get(attr);
  }

  @Nullable
  public String getBundleSymbolicName() {
    return get(Constants.BUNDLE_SYMBOLICNAME);
  }

  @Nullable
  public String getBundleActivator() {
    return get(Constants.BUNDLE_ACTIVATOR);
  }

  @Nullable
  public String getExportedPackage(@NotNull String packageName) {
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