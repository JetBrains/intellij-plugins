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

import com.intellij.openapi.application.Application;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleServiceManager;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.Library;
import org.osmorc.facet.OsmorcFacetUtil;
import org.osmorc.frameworkintegration.LibraryHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class ModuleDependencySynchronizer {
    public static ModuleDependencySynchronizer getInstance(Module module) {
        return ModuleServiceManager.getService(module, ModuleDependencySynchronizer.class);
    }

    public ModuleDependencySynchronizer(BundleManager bundleManager, ModuleRootManager moduleRootManager,
                                        Application application, LibraryHandler libraryHandler,
                                        OsmorcFacetUtil osmorcFacetUtil) {
        this.bundleManager = bundleManager;
        this.moduleRootManager = moduleRootManager;
        this.application = application;
        this.libraryHandler = libraryHandler;
        this.osmorcFacetUtil = osmorcFacetUtil;
        module = this.moduleRootManager.getModule();
    }

    public void syncDependenciesFromManifest() {
        if (osmorcFacetUtil.hasOsmorcFacet(module) &&
                !osmorcFacetUtil.getOsmorcFacet(module).getConfiguration().isOsmorcControlsManifest()) {
            application.runWriteAction(new Runnable() {
                public void run() {
                    ModifiableRootModel model = moduleRootManager.getModifiableModel();

                    Collection<Object> newBundles = bundleManager.determineBundleDependencies(module);

                    List<OrderEntry> oldOrderEntries = determineOldModuleDependencies(model);
                    List<OrderEntry> obsoleteOrderEntries = determineObsoleteModuleDependencies(oldOrderEntries, newBundles);

                    boolean commitNeeded = false;

                    //noinspection ConstantConditions
                    commitNeeded |= removeObsoleteModuleDependencies(model, obsoleteOrderEntries);
                    commitNeeded |= addNewModuleDependencies(model, newBundles);
                    commitNeeded |= checkAndSetReexport(model, newBundles);

                    if (commitNeeded) {
                        model.commit();
                    } else {
                        model.dispose();
                    }
                }
            });
        }
    }

    protected List<OrderEntry> determineOldModuleDependencies(ModifiableRootModel model) {
        List<OrderEntry> oldOrderEntries = new ArrayList<OrderEntry>();
        for (OrderEntry oldOrderEntry : model.getOrderEntries()) {
            if (oldOrderEntry instanceof ModuleOrderEntry) {
                Module module = ((ModuleOrderEntry) oldOrderEntry).getModule();
                if (module != null && osmorcFacetUtil.hasOsmorcFacet(module) &&
                        osmorcFacetUtil.getOsmorcFacet(module).getConfiguration().isManifestManuallyEdited()) {
                    oldOrderEntries.add(oldOrderEntry);
                }
            } else if (oldOrderEntry instanceof LibraryOrderEntry && libraryHandler.isFrameworkInstanceLibrary(
                    (LibraryOrderEntry) oldOrderEntry)) {
                oldOrderEntries.add(oldOrderEntry);
            }
        }
        return oldOrderEntries;
    }

    @SuppressWarnings({"MethodMayBeStatic"})
    protected List<OrderEntry> determineObsoleteModuleDependencies(List<OrderEntry> oldOrderEntries,
                                                                   Collection<Object> newBundles) {
        List<OrderEntry> result = new ArrayList<OrderEntry>();

        for (OrderEntry oldOrderEntry : oldOrderEntries) {
            boolean found = false;

            if (oldOrderEntry instanceof ModuleOrderEntry) {
                found = newBundles.remove(((ModuleOrderEntry) oldOrderEntry).getModule());
            } else if (oldOrderEntry instanceof LibraryOrderEntry) {
                found = newBundles.remove(((LibraryOrderEntry) oldOrderEntry).getLibrary());
            }

            if (!found) {
                result.add(oldOrderEntry);
            }
        }

        return result;
    }

    @SuppressWarnings({"MethodMayBeStatic"})
    protected boolean removeObsoleteModuleDependencies(ModifiableRootModel model, List<OrderEntry> oldOrderEntries) {
        boolean commitNeeded = false;

        for (OrderEntry orderEntry : oldOrderEntries) {
            model.removeOrderEntry(orderEntry);
            commitNeeded = true;
        }
        return commitNeeded;
    }

    protected boolean addNewModuleDependencies(ModifiableRootModel model, Collection<Object> newBundles) {
        boolean commitNeeded = false;
        for (Object newBundle : newBundles) {
            if (newBundle instanceof Module && newBundle != module) {
                model.addModuleOrderEntry((Module) newBundle);
                commitNeeded = true;
            } else if (newBundle instanceof Library && !(newBundle instanceof LibraryEx && ((LibraryEx) newBundle).isDisposed())) {
                model.addLibraryEntry((Library) newBundle);
                commitNeeded = true;
            }
        }
        return commitNeeded;
    }

    protected boolean checkAndSetReexport(ModifiableRootModel model, Collection<Object> newBundles) {
        boolean commitNeeded = false;

        for (OrderEntry orderEntry : model.getOrderEntries()) {
            if (orderEntry instanceof ModuleOrderEntry) {
                ModuleOrderEntry moduleOrderEntry = (ModuleOrderEntry) orderEntry;
                Module module = moduleOrderEntry.getModule();
                if (module != null && osmorcFacetUtil.hasOsmorcFacet(module)) {
                    boolean export = bundleManager.isReexported(moduleOrderEntry.getModule(), this.module);
                    if (export != moduleOrderEntry.isExported()) {
                        moduleOrderEntry.setExported(export);
                        commitNeeded = true;
                    }
                }
            } else if (orderEntry instanceof LibraryOrderEntry &&
                    libraryHandler.isFrameworkInstanceLibrary(((LibraryOrderEntry) orderEntry))) {
                LibraryOrderEntry libraryOrderEntry = (LibraryOrderEntry) orderEntry;
                Library library = libraryOrderEntry.getLibrary();
                if (library == null) {
                    for (Object newBundle : newBundles) {
                        if (newBundle instanceof Library &&
                                ((Library) newBundle).getName().equals(libraryOrderEntry.getLibraryName())) {
                            library = (Library) newBundle;
                            break;
                        }
                    }
                }
                if (library != null) {
                    boolean export = bundleManager.isReexported(library, module);
                    if (export != libraryOrderEntry.isExported()) {
                        libraryOrderEntry.setExported(export);
                        commitNeeded = true;
                    }
                }
            }
        }
        return commitNeeded;
    }

    private final ModuleRootManager moduleRootManager;
    private final Application application;
    private final LibraryHandler libraryHandler;
    private final OsmorcFacetUtil osmorcFacetUtil;
    private final BundleManager bundleManager;
    private final Module module;
}
