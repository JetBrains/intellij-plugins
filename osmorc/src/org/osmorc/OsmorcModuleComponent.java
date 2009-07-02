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

package org.osmorc;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetManagerAdapter;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.facet.OsmorcFacetType;
import org.osmorc.frameworkintegration.FrameworkInstanceModuleManager;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class OsmorcModuleComponent implements ModuleComponent {
    private MessageBusConnection connection;

    public OsmorcModuleComponent(Module module, ModuleDependencySynchronizer moduleDependencySynchronizer,
                                 FrameworkInstanceModuleManager frameworkInstanceModuleManager,
                                 AdditionalJARContentsWatcherManager additionalJARContentsWatcherManager,
                                 Application application) {
        this.module = module;
        this.moduleDependencySynchronizer = moduleDependencySynchronizer;
        this.frameworkInstanceModuleManager = frameworkInstanceModuleManager;
        this.additionalJARContentsWatcherManager = additionalJARContentsWatcherManager;
        this.application = application;
        disposed = false;
    }

    public void projectOpened() {
        application.invokeLater(new Runnable() {
            public void run() {
                if (!disposed && OsmorcFacet.hasOsmorcFacet(module)) {
                    moduleDependencySynchronizer.syncDependenciesFromManifest();
                }
            }
        });
    }

    public void projectClosed() {
        additionalJARContentsWatcherManager.dispose();
    }

    public void moduleAdded() {
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return "OsmorcModuleComponent";
    }

    public void initComponent() {
        disposed = false;
        connection = module.getMessageBus().connect();
        connection.subscribe(FacetManager.FACETS_TOPIC, new FacetManagerAdapter() {
            public void facetAdded(@NotNull Facet facet) {
                handleFacetChange(facet);
            }

            @Override
            public void facetRemoved(@NotNull Facet facet) {
                frameworkInstanceModuleManager.updateFrameworkInstanceModule();
            }

            public void facetConfigurationChanged(@NotNull Facet facet) {
                handleFacetChange(facet);
            }
        });
    }

    private void handleFacetChange(Facet facet) {
        if (!disposed && facet.getTypeId() == OsmorcFacetType.ID) {
            if (facet.getModule().getProject().isInitialized()) {
                moduleDependencySynchronizer.syncDependenciesFromManifest();
                syncAllModuleDependencies();
                frameworkInstanceModuleManager.updateFrameworkInstanceModule();
            }
            additionalJARContentsWatcherManager.updateWatcherSetup();
        }
    }

    public void disposeComponent() {
        if (connection != null) {
            connection.disconnect();
        }
        disposed = true;
    }

    private void syncAllModuleDependencies() {
        Module[] modules = ModuleManager.getInstance(module.getProject()).getModules();
        for (Module module : modules) {
            if (OsmorcFacet.hasOsmorcFacet(module)) {
                ModuleDependencySynchronizer.getInstance(module).syncDependenciesFromManifest();
            }
        }
    }


    private final Module module;
    private final ModuleDependencySynchronizer moduleDependencySynchronizer;
    private FrameworkInstanceModuleManager frameworkInstanceModuleManager;
    private final AdditionalJARContentsWatcherManager additionalJARContentsWatcherManager;
    private final Application application;
    private boolean disposed;
}
