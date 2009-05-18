/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.osmorc.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleServiceManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.osmorc.settings.ProjectSettings;

/**
 * The Osmorc facet.
 * <p/>
 *
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class OsmorcFacet extends Facet<OsmorcFacetConfiguration> {
    public OsmorcFacet(@NotNull Module module) {
        this(FacetTypeRegistry.getInstance().findFacetType(OsmorcFacetType.ID), module,
                new OsmorcFacetConfiguration(),
                null, "OSGi");
    }

    public OsmorcFacet(@NotNull FacetType facetType, @NotNull Module module, @NotNull OsmorcFacetConfiguration configuration, Facet underlyingFacet,
                       final String name) {
        super(facetType, module, name, configuration, underlyingFacet);
    }

    /**
     * Returns the Osmorc facet for the given module.
     *
     * @param module the module
     * @return the Osmorc facet of this module or null if the module doesn't have an Osmorc facet.
     */
    public static OsmorcFacet getInstance(Module module) {
        return FacetManager.getInstance(module).getFacetByType(OsmorcFacetType.ID);
    }

    /**
     * Determines the module to which the given element belongs and returns the Osmorc facet for this module.
     *
     * @param element the element
     * @return the Osmorc facet of the module to which the element belongs or null if this module doesn't have an Osmorc
     *         facet or if the belonging module could not be determined.
     */
    public static OsmorcFacet getInstance(PsiElement element) {
        Module module = ModuleUtil.findModuleForPsiElement(element);
        return getInstance(module);
    }

    /**
     * @param module the module to check
     * @return true if there is an Osmorc facet for the given module, false otherwise.
     */
    public static boolean hasOsmorcFacet(Module module) {
        return getInstance(module) != null;
    }

    /**
     * @param element the element to check
     * @return true if the module of the element could be determined and this module has an Osmorc facet, false
     *         otherwise.
     */
    public static boolean hasOsmorcFacet(PsiElement element) {
        Module module = ModuleUtil.findModuleForPsiElement(element);
        return module != null && hasOsmorcFacet(module);
    }

    public String getManifestLocation() {
        if (getConfiguration().isUseProjectDefaultManifestFileLocation()) {

            final ProjectSettings projectSettings = ModuleServiceManager.getService(getModule(), ProjectSettings.class);
            return projectSettings.getDefaultManifestFileLocation();
        } else {
            return getConfiguration().getManifestLocation();
        }
    }
}
