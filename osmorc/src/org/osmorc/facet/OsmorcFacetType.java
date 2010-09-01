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
import com.intellij.facet.FacetModel;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.facet.autodetecting.FacetDetector;
import com.intellij.facet.autodetecting.FacetDetectorRegistry;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.framework.Constants;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.manifest.ManifestFileTypeFactory;

import javax.swing.*;
import java.io.*;
import java.util.*;

/**
 * The facet type of Osmorc.
 *
 * @author Robert F. Beeger (robert@beeger.net)
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 */
public class OsmorcFacetType extends FacetType<OsmorcFacet, OsmorcFacetConfiguration> {
    public static final FacetTypeId<OsmorcFacet> ID = new FacetTypeId<OsmorcFacet>("Osmorc");
    public static final OsmorcFacetType INSTANCE = new OsmorcFacetType();
    private final Logger logger = Logger.getInstance("#org.osmorc.facet.OsmorcFacetType");


    protected OsmorcFacetType() {
        super(ID, "Osmorc", "OSGi");
    }

    public OsmorcFacetConfiguration createDefaultConfiguration() {
        return new OsmorcFacetConfiguration();
    }

    public OsmorcFacet createFacet(
            @NotNull Module module, String name,
            @NotNull OsmorcFacetConfiguration configuration, @Nullable Facet underlyingFacet) {
        completeDefaultConfiguration(configuration, module);
        return new OsmorcFacet(this, module, configuration, underlyingFacet, name);
    }

    public boolean isSuitableModuleType(ModuleType moduleType) {
        return moduleType instanceof JavaModuleType;
    }

    private void completeDefaultConfiguration(final OsmorcFacetConfiguration configuration, final Module module) {
        if (configuration.getJarFileLocation().length() == 0) {
            final String outputPathUrl = CompilerModuleExtension.getInstance(module).getCompilerOutputUrl();
            StartupManager.getInstance(module.getProject()).runWhenProjectIsInitialized(new Runnable() {
                public void run() {
                    try {
                        // Must be run in event dispatch thread therefore... we put all there..
                        VfsUtil.createDirectories(VfsUtil.urlToPath(outputPathUrl));
                    }
                    catch (IOException e) {
                        return;
                    }
                    VirtualFile moduleCompilerOutputPath = CompilerModuleExtension.getInstance(module).getCompilerOutputPath();
                    if (moduleCompilerOutputPath == null) {
                        return;
                    }

                    String jarFileName = module.getName();
                    jarFileName = jarFileName.replaceAll("[\\s]", "_");
                    String jarFilePath = new File(moduleCompilerOutputPath.getParent().getPath(), jarFileName + ".jar")
                            .getAbsolutePath().replace('\\', '/');
                    configuration.setJarFileLocation(jarFilePath);
                }
            });

        }
    }


    public Icon getIcon() {
        return OsmorcBundle.getSmallIcon();
    }

    public void registerDetectors(
            FacetDetectorRegistry<OsmorcFacetConfiguration> osmorcFacetConfigurationFacetDetectorRegistry) {

        VirtualFileFilter virtualFileFilter = new VirtualFileFilter() {
            public boolean accept(VirtualFile file) {
                List<String> headersToDetect = new ArrayList<String>(Arrays.asList(DETECTION_HEADERS));

                if (file != null && file.exists() && !file.isDirectory()) {
                    BufferedReader bufferedReader = null;
                    try {
                        InputStream inputStream = file.getInputStream();
                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                        bufferedReader = new BufferedReader(inputStreamReader);

                        while (bufferedReader.ready() && headersToDetect.size() > 0) {
                            String line = bufferedReader.readLine();
                            if ( line == null ) {
                              break; // fixes: EA-20522
                            }
                            for (Iterator<String> headersToDetectIterator = headersToDetect.iterator();
                                 headersToDetectIterator.hasNext();) {
                                String headertoDeteect = headersToDetectIterator.next();
                                if (line.startsWith(headertoDeteect)) {
                                    headersToDetectIterator.remove();
                                    break;
                                }
                            }
                        }
                    }
                    catch (IOException e) {
                        // this should fix   IDEA-17977 (EA reported exception).
                        logger.warn("There was an unexpected exception when accessing " + file.getName() + " (" + e.getMessage() + ")");
                        return false;
                    }
                    finally {
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            }
                            catch (IOException e) {
                                logger.warn("There was an unexpected exception when closing stream to " + file.getName() + " (" + e.getMessage() + ")");
                                return false;
                            }
                        }
                    }
                }

                return headersToDetect.size() == 0;
            }
        };
        FacetDetector<VirtualFile, OsmorcFacetConfiguration> detector =
                new FacetDetector<VirtualFile, OsmorcFacetConfiguration>("Osmorc") {
                    public OsmorcFacetConfiguration detectFacet(VirtualFile source,
                                                                Collection<OsmorcFacetConfiguration> existentFacetConfigurations) {
                        if (!existentFacetConfigurations.isEmpty()) {
                            return existentFacetConfigurations.iterator().next();
                        }
                        OsmorcFacetConfiguration osmorcFacetConfiguration = createDefaultConfiguration();
                        osmorcFacetConfiguration.setOsmorcControlsManifest(false);
                        osmorcFacetConfiguration.setManifestLocation(source.getPath());
                        osmorcFacetConfiguration.setUseProjectDefaultManifestFileLocation(false);
                        return osmorcFacetConfiguration;
                    }

                    @Override
                    public void beforeFacetAdded(@NotNull Facet facet, FacetModel facetModel, @NotNull ModifiableRootModel modifiableRootModel) {
                        super.beforeFacetAdded(facet, facetModel, modifiableRootModel);
                        VirtualFile[] contentRoots = modifiableRootModel.getContentRoots();
                        OsmorcFacet osmorcFacet = (OsmorcFacet) facet;
                        OsmorcFacetConfiguration osmorcFacetConfiguration = osmorcFacet.getConfiguration();
                        VirtualFile manifestFile = LocalFileSystem.getInstance().findFileByPath(osmorcFacetConfiguration.getManifestLocation());
                        if (manifestFile != null) {
                            for (VirtualFile contentRoot : contentRoots) {
                                if (VfsUtil.isAncestor(contentRoot, manifestFile, false)) {
                                    // IDEADEV-40357
                                    osmorcFacetConfiguration.setManifestLocation(VfsUtil.getRelativePath(manifestFile , contentRoot, '/'));
                                    break;
                                }
                            }
                        }
                        else {
                            osmorcFacetConfiguration.setManifestLocation("");
                            osmorcFacetConfiguration.setUseProjectDefaultManifestFileLocation(true);
                        }
                        String manifestFileName = osmorcFacetConfiguration.getManifestLocation();
                        if ( manifestFileName.endsWith("template.mf") ) { // this is a bundlor manifest template, so make the facet do bundlor
                            osmorcFacetConfiguration.setManifestLocation("");
                            osmorcFacetConfiguration.setBundlorFileLocation(manifestFileName);
                            osmorcFacetConfiguration.setUseBundlorFile(true);
                        }
                    }
                };


        osmorcFacetConfigurationFacetDetectorRegistry.registerUniversalDetector(ManifestFileTypeFactory.MANIFEST,
                virtualFileFilter, detector);
    }

    private final String[] DETECTION_HEADERS = {Constants.BUNDLE_SYMBOLICNAME};
}
