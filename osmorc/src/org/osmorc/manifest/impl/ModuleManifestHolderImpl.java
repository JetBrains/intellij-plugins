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
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.Nullable;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.manifest.BundleManifest;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.WeakHashMap;
import java.util.jar.Attributes;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class ModuleManifestHolderImpl extends AbstractManifestHolderImpl {
    public ModuleManifestHolderImpl(Module module,
                                    Application application) {
        this.module = module;
        this.application = application;
        askedQuestions = new WeakHashMap<String, Boolean>();
    }

    @Nullable
    public BundleManifest getBundleManifest() {
        // only try to load the manifest if we have an osmorc facet for that module
        if (bundleManifest == null && OsmorcFacet.hasOsmorcFacet(module)) {
            OsmorcFacet facet = OsmorcFacet.getInstance(module);
            // and only if this manifest is manually edited
            if (facet.getConfiguration().isManifestManuallyEdited()) {
                tryCreateBundleManifest();
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
                        if (application.isDispatchThread() || application.isUnitTestMode()) {
                            Messages.showMessageDialog(module.getProject(), String.format("The manifest file %s could not be loaded. " +
                                    "Please make sure that it is not located in an excluded directory", manifestFile.getPath()), "Error while loading manifest", Messages.getInformationIcon());
                        }
                        return null;
                    }
                    return new BundleManifestImpl(psiFile);
                }
            });
        }
        return null;
    }


    private void tryCreateBundleManifest() {
        // check if the file is already there, in this case leave it alone
        VirtualFile file = getManifestFile();
        if (file != null && file.exists()) {
            return;
        }

        // check if a manifest path has been set up
        final String manifestPath = getManifestPath();
        if (StringUtil.isEmpty(manifestPath)) {
            return;
        }

        if (getContentRoots().length > 0) {
            // okay now, we have set up a manifest path, but it doesn't exist. Ask the user
            // if  we should create it.
            // don't ask the user more than once for the same path (unless the GC grabs our map)
            if (!askedQuestions.containsKey(manifestPath)) {
                int result = DialogWrapper.CANCEL_EXIT_CODE;
                // TODO: Need to refactor this out of here. Don't want to check for dispatcher thread and unit test mode everywhere a message dialog is opened.
                if (application.isDispatchThread() || application.isUnitTestMode()) {
                    result = Messages
                            .showYesNoDialog(module.getProject(),
                                    OsmorcBundle.getTranslation("manifestholder.createmanifest.question", manifestPath),
                                    OsmorcBundle.getTranslation("manifestholder.createmanifest.title"), Messages.getQuestionIcon());
                }

                if (result == DialogWrapper.CANCEL_EXIT_CODE) {
                    askedQuestions.put(manifestPath, Boolean.FALSE);
                    return; // don't mess with it if the user doesn't want it
                } else {
                    // save decision
                    askedQuestions.put(manifestPath, Boolean.TRUE);
                }
            } else {
                if (askedQuestions.get(manifestPath) == Boolean.FALSE) {
                    return;
                }
            }

            application.runWriteAction(new Runnable() {
                public void run() {
                    try {

                        VirtualFile[] contentRoots = getContentRoots();
                        VirtualFile contentRoot = contentRoots[0];
                        String completePath = contentRoot.getPath() + File.separator + manifestPath;

                        // unify file separators
                        completePath = completePath.replace('\\', '/');

                        // strip off the last part (its the filename)
                        int lastPathSep = completePath.lastIndexOf('/');
                        String path = completePath.substring(0, lastPathSep);
                        String filename = completePath.substring(lastPathSep + 1);

                        // make sure the folders exist
                        VfsUtil.createDirectories(path);

                        // and get the virtual file for it
                        VirtualFile parentFolder = LocalFileSystem.getInstance().refreshAndFindFileByPath(path);

                        // some heuristics for bundle name and version
                        String bundleName = module.getName();
                        Version bundleVersion = null;
                        int nextDotPos = bundleName.indexOf('.');
                        while (bundleVersion == null && nextDotPos >= 0) {
                            try {
                                bundleVersion = new Version(bundleName.substring(nextDotPos + 1));
                                bundleName = bundleName.substring(0, nextDotPos);
                            }
                            catch (IllegalArgumentException e) {
                                // Retry after next dot.
                            }
                            nextDotPos = bundleName.indexOf('.', nextDotPos + 1);
                        }


                        VirtualFile manifest = parentFolder.createChildData(this, filename);
                        OutputStream outputStream = manifest.getOutputStream(this);
                        PrintWriter writer = new PrintWriter(outputStream);
                        writer.write(Attributes.Name.MANIFEST_VERSION + ": 1.0.0\n" +
                                Constants.BUNDLE_MANIFESTVERSION + ": 2\n" +
                                Constants.BUNDLE_NAME + ": " + bundleName + "\n" +
                                Constants.BUNDLE_SYMBOLICNAME + ": " + bundleName + "\n" +
                                Constants.BUNDLE_VERSION + ": " + (bundleVersion != null ? bundleVersion.toString() : "1.0.0") +
                                "\n");
                        writer.flush();
                        writer.close();
                    }
                    catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            VirtualFileManager.getInstance().refresh(false);
        }

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
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        path = path + "MANIFEST.MF";
        return path;
    }

    private VirtualFile[] getContentRoots() {

        ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
        VirtualFile[] contentRoots = moduleRootManager.getContentRoots();

        return contentRoots != null ? contentRoots : new VirtualFile[0];
    }

    private final Module module;
    private final Application application;
    private BundleManifest bundleManifest;
    private WeakHashMap<String, Boolean> askedQuestions;
}
