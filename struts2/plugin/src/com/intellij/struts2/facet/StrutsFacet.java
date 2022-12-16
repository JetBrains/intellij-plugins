/*
 * Copyright 2013 The authors
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
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Struts2 facet.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsFacet extends Facet<StrutsFacetConfiguration> {

  public static final FacetTypeId<StrutsFacet> FACET_TYPE_ID = new FacetTypeId<>("struts2");

  public StrutsFacet(@NotNull final FacetType facetType,
                     @NotNull final Module module,
                     final String name,
                     @NotNull final StrutsFacetConfiguration configuration,
                     final Facet underlyingFacet) {
    super(facetType, module, name, configuration, underlyingFacet);
    Disposer.register(this, configuration);
  }

  /**
   * Gets the StrutsFacet for the given module.
   *
   * @param module Module to check.
   * @return Instance or {@code null} if none configured.
   */
  @Nullable
  public static StrutsFacet getInstance(@NotNull final Module module) {
    return FacetManager.getInstance(module).getFacetByType(FACET_TYPE_ID);
  }

  /**
   * Gets the StrutsFacet for the module containing the given PsiElement.
   *
   * @param element Element to check.
   * @return Instance or {@code null} if none configured.
   */
  @Nullable
  public static StrutsFacet getInstance(@NotNull final PsiElement element) {
    final Module module = ModuleUtilCore.findModuleForPsiElement(element);
    return module != null ? getInstance(module) : null;
  }

  /**
   * Returns the underlying WebFacet.
   *
   * @return WebFacet.
   */
  @Nullable
  public WebFacet getWebFacet() {
    return FacetManager.getInstance(getModule()).getFacetByType(WebFacet.ID);
  }

}