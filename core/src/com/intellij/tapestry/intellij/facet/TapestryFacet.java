package com.intellij.tapestry.intellij.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetType;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The Tapestry support facet.
 */
public class TapestryFacet extends Facet<TapestryFacetConfiguration> {

    public TapestryFacet(@NotNull final FacetType facetType, @NotNull final Module module, final String name, @NotNull final TapestryFacetConfiguration configuration, final Facet underlyingFacet) {
        super(facetType, module, name, configuration, underlyingFacet);
    }

    @Nullable
    public static TapestryFacet getInstance(@NotNull final WebFacet webFacet) {
        return FacetManager.getInstance(webFacet.getModule()).getFacetByType(webFacet, TapestryFacetType.ID);
    }

    public WebFacet getWebFacet() {
        return (WebFacet) getUnderlyingFacet();
    }
}
