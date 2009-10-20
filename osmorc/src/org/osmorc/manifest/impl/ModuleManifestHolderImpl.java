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

import com.intellij.openapi.application.Application;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.Nullable;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.manifest.BundleManifest;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class ModuleManifestHolderImpl extends AbstractManifestHolderImpl {
    public ModuleManifestHolderImpl(Module module,
                                    Application application) {
        this.module = module;
        this.application = application;
    }

    @Nullable
    public BundleManifest getBundleManifest() {
        final VirtualFile cachedFile = bundleManifest != null ? bundleManifest.getManifestFile().getVirtualFile() : null;
        final VirtualFile fileFromSettings = getManifestFile();
        if (bundleManifest != null && (cachedFile == null || fileFromSettings == null || !cachedFile.getPath().equals(fileFromSettings.getPath()))) {
            bundleManifest = null; // manifest file changed, we need to start from scratch.
        }

        // only try to load the manifest if we have an osmorc facet for that module
        if (bundleManifest == null && OsmorcFacet.hasOsmorcFacet(module)) {
            OsmorcFacet facet = OsmorcFacet.getInstance(module);
            // and only if this manifest is manually edited
            if (facet.getConfiguration().isManifestManuallyEdited()) {
                bundleManifest = loadManifest();
            }
        }
        return bundleManifest;
    }

    private BundleManifest loadManifest() {
        final VirtualFile manifestFile = getManifestFile();
        if (manifestFile != null) {
            return application.runReadAction(new Computable<BundleManifest>() {
                public BundleManifest compute() {
                    PsiFile psiFile = PsiManager.getInstance(module.getProject()).findFile(manifestFile);
                    if (psiFile == null) {
                        // IDEADEV-40349 removed all messageboxes
                        return null;
                    }
                    return new BundleManifestImpl(psiFile);
                }
            });
        }
        return null;
    }


    private
    @Nullable
    VirtualFile getManifestFile() {
        String path = getManifestPath();

        // if it is not set, just return null
        if (StringUtil.isEmpty(path)) {
            return null;
        }

        VirtualFile[] contentRoots = getContentRoots();
        for (VirtualFile contentRoot : contentRoots) {
            VirtualFile manifestFile = contentRoot.findFileByRelativePath(path);
            if (manifestFile != null) {
                return manifestFile;
            }
        }

        return null;
    }

    private String getManifestPath() {
        // get relative path from the configuration
        OsmorcFacet facet = OsmorcFacet.getInstance(module);
        String path = facet.getManifestLocation();
        path = path.replace('\\', '/');
        //IDEADEV-40357 allow any file name

//        if (!path.endsWith("/")) {
//            path = path + "/";
//        }
//        path = path + "MANIFEST.MF";
        return path;
    }

    private VirtualFile[] getContentRoots() {
        ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
        return moduleRootManager.getContentRoots();
    }


    private final Module module;
    private final Application application;
    private BundleManifest bundleManifest;
}
