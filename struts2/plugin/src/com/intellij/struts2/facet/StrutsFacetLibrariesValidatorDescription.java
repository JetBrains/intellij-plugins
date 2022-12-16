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

import com.intellij.facet.Facet;
import com.intellij.facet.ui.libraries.FacetLibrariesValidatorDescription;
import com.intellij.javaee.artifact.JavaeeArtifactUtil;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.roots.libraries.Library;
import org.jetbrains.annotations.NotNull;

/**
 * Validates required libraries and adjusts WebFacet deployment settings.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsFacetLibrariesValidatorDescription extends FacetLibrariesValidatorDescription {

  public StrutsFacetLibrariesValidatorDescription() {
    this("");
  }

  /**
   * CTOR with .
   *
   * @param version Version-ID to append to library name.
   */
  public StrutsFacetLibrariesValidatorDescription(final String version) {
    super("struts2-" + version);
  }

  @Override
  public void onLibraryAdded(final Facet facet, @NotNull final Library library) {
    super.onLibraryAdded(facet, library);
    final StrutsFacet strutsFacet = (StrutsFacet) facet;
    final WebFacet webFacet = strutsFacet.getWebFacet();
    if (null != webFacet) {
      JavaeeArtifactUtil.getInstance().addLibraryToAllArtifactsContainingFacet(library, webFacet);
    }
  }

}