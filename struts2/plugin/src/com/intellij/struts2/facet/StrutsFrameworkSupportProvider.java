/*
 * Copyright 2008 The authors
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
package com.intellij.struts2.facet;

import com.intellij.facet.impl.ui.FacetTypeFrameworkSupportProvider;
import com.intellij.facet.ui.libraries.LibraryInfo;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.struts2.facet.ui.StrutsVersion;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * "Add Framework" support.
 *
 * @author Yann CŽbron
 */
public class StrutsFrameworkSupportProvider extends FacetTypeFrameworkSupportProvider<StrutsFacet> {

  protected StrutsFrameworkSupportProvider() {
    super(StrutsFacetType.INSTANCE);
  }

  @NotNull
  protected String getLibraryName(final String version) {
    return "struts2-" + version;
  }

  public String getTitle() {
    return UIUtil.replaceMnemonicAmpersand("&Struts 2");
  }

  @NotNull
  public String[] getVersions() {
    final List<String> versions = new ArrayList<String>();
    for (final StrutsVersion version : StrutsVersion.values()) {
      versions.add(version.toString());
    }
    return versions.toArray(new String[versions.size()]);
  }

  @NotNull
  private static StrutsVersion getVersion(final String versionName) {
    for (final StrutsVersion version : StrutsVersion.values()) {
      if (versionName.equals(version.toString())) {
        return version;
      }
    }

    throw new IllegalArgumentException("Invalid S2 version '" + versionName + "'");
  }

  @NotNull
  protected LibraryInfo[] getLibraries(final String selectedVersion) {
    final StrutsVersion version = getVersion(selectedVersion);
    return version.getLibraryInfos();
  }

  protected void onLibraryAdded(final StrutsFacet facet, final @NotNull Library library) {
    facet.getWebFacet().getPackagingConfiguration().addLibraryLink(library);
  }

  protected void setupConfiguration(final StrutsFacet strutsFacet,
                                    final ModifiableRootModel modifiableRootModel, final String version) {
  }

}