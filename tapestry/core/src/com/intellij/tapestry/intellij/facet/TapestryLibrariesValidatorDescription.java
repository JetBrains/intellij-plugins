package com.intellij.tapestry.intellij.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.ui.libraries.FacetLibrariesValidatorDescription;
import com.intellij.openapi.roots.libraries.Library;
import org.jetbrains.annotations.NotNull;

public class TapestryLibrariesValidatorDescription extends FacetLibrariesValidatorDescription {

    public TapestryLibrariesValidatorDescription() {
        super("tapestry");
    }

    @Override
    public void onLibraryAdded(final Facet facet, @NotNull final Library library) {
        //((TapestryFacet) facet).getWebFacet().getPackagingConfiguration().addLibraryLink(library);
    }
}
