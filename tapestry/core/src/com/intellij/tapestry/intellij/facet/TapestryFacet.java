package com.intellij.tapestry.intellij.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetType;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;

/**
 * The Tapestry support facet.
 */
public class TapestryFacet extends Facet<TapestryFacetConfiguration> {

  public TapestryFacet(@NotNull final FacetType facetType,
                       @NotNull final Module module,
                       final String name,
                       @NotNull final TapestryFacetConfiguration configuration,
                       final Facet underlyingFacet) {
    super(facetType, module, name, configuration, underlyingFacet);
  }

  //public WebFacet getWebFacet() {
  //  return (WebFacet)getUnderlyingFacet();
  //}

  public static TapestryFacetConfiguration findFacetConfiguration(Module module) {
    final FacetManager facetManager = FacetManager.getInstance(module);
    TapestryFacet tapestryFacet = facetManager.getFacetByType(TapestryFacetType.ID);
    return tapestryFacet == null ? null : tapestryFacet.getConfiguration();
  }
}
