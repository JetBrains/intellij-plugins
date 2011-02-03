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
package org.osmorc.manifest.impl;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.Library;
import org.jetbrains.annotations.NotNull;
import org.osmorc.manifest.ManifestHolder;
import org.osmorc.manifest.ManifestHolderRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public final class ManifestHolderRegistryImpl implements ManifestHolderRegistry {
    public ManifestHolderRegistryImpl(Project project) {
        this.project = project;
        libraryManifestHolders = new HashMap<Library, ManifestHolder>();
    }

    public ManifestHolder getManifestHolder(@NotNull Object bundle) {
        if (bundle instanceof Module) {
            return getModuleManifestHolder((Module) bundle);
        } else if (bundle instanceof Library) {
            return getLibraryManifestHolder((Library) bundle);
        }
        throw new RuntimeException(
                "The bundle is neither a Module nor a Library. It is of type " + bundle.getClass().getName());
    }

    private ManifestHolder getModuleManifestHolder(@NotNull Module module) {
        return ModuleServiceManager.getService(module, ManifestHolder.class);
    }

    private ManifestHolder getLibraryManifestHolder(@NotNull Library library) {
        ManifestHolder result = libraryManifestHolders.get(library);
        if (result == null) {
            result = new LibraryManifestHolderImpl(library, project);
            libraryManifestHolders.put(library, result);
        }

        return result;
    }

    public List<Long> getLibraryBundleIDs() {
        List<Long> ids = new ArrayList<Long>();

        for (ManifestHolder manifestHolder : libraryManifestHolders.values()) {
            ids.add(manifestHolder.getBundleID());
        }

        return ids;
    }

    public void clearLibraryManifestHolders() {
        libraryManifestHolders.clear();
    }

  @Override
  public boolean isEmpty() {
    return libraryManifestHolders.isEmpty();
  }


  private final Project project;
    private final Map<Library, ManifestHolder> libraryManifestHolders;
}
