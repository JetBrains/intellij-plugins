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

package org.osmorc.frameworkintegration.impl.equinox;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.*;
import org.osmorc.make.BundleCompiler;
import org.osmorc.run.ui.SelectedBundle;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
@SuppressWarnings({"ComponentNotRegistered"})
class AdaptToRunWithoutUpdateConfigurator  extends BundleSelectionAction {
    private static final String ORG_ECLIPSE_UPDATE_CONFIGURATOR_URL = "org.eclipse.update.configurator_";
    private static final String ORG_ECLIPSE_OSGI_URL = "org.eclipse.osgi_";

    public AdaptToRunWithoutUpdateConfigurator() {
        super("Adapt to Run Without Update Configurator");
    }

    public void actionPerformed(AnActionEvent e) {

        Collection<SelectedBundle> currentlySelectedBundles = new ArrayList<SelectedBundle>(getContext().getCurrentlySelectedBundles()); 
        for (SelectedBundle selectedBundle : currentlySelectedBundles) {
            if (selectedBundle.getBundleType() == SelectedBundle.BundleType.FrameworkBundle) {
                String url = selectedBundle.getBundleUrl();
                if (url != null) {
                    if (url.contains(ORG_ECLIPSE_UPDATE_CONFIGURATOR_URL) || url.contains(ORG_ECLIPSE_OSGI_URL)) {
                        getContext().removeBundle(selectedBundle);
                    } else {
                        adaptBundle(selectedBundle);
                    }
                }
            }
        }

        SelectedBundle prototypeBundle = null;
        FrameworkInstanceDefinition instance = getContext().getUsedFrameworkInstance();
        FrameworkIntegratorRegistry registry = ServiceManager.getService(FrameworkIntegratorRegistry.class);
        assert instance != null;
        FrameworkIntegrator frameworkIntegrator = registry.findIntegratorByInstanceDefinition(instance);
        List<Library> libraries = frameworkIntegrator.getFrameworkInstanceManager().getLibraries(instance);
        for (Library library : libraries) {
            String[] urls = library.getUrls(OrderRootType.CLASSES);
            for (String url : urls) {
                if (!url.contains(ORG_ECLIPSE_UPDATE_CONFIGURATOR_URL) && !url.contains(ORG_ECLIPSE_OSGI_URL)) {
                    prototypeBundle = createSelectedFrameworkBundle(prototypeBundle, url);
                    if (prototypeBundle != null && !currentlySelectedBundles.contains(prototypeBundle)) {
                        adaptBundle(prototypeBundle);
                        getContext().addBundle(prototypeBundle);
                        prototypeBundle = null;
                    }
                }
            }
        }
    }

    private SelectedBundle createSelectedFrameworkBundle(final SelectedBundle prototypeBundle, final String url) {
        String bundleUrl = BundleCompiler.convertJarUrlToFileUrl(url);
        bundleUrl = BundleCompiler.fixFileURL(bundleUrl);
        String bundleName = CachingBundleInfoProvider.getBundleSymbolicName(bundleUrl);
        SelectedBundle bundle = null;
        
        if (bundleName != null) {
            bundle = prototypeBundle;
            String bundleVersion = CachingBundleInfoProvider.getBundleVersions(bundleUrl);
            String displayName = bundleName + " - " + bundleVersion;
            if (bundle != null) {
                bundle.setName(displayName);
                bundle.setBundleUrl(bundleUrl);
            } else {
                bundle = new SelectedBundle(displayName, bundleUrl, SelectedBundle.BundleType.FrameworkBundle);
            }
        }
        return bundle;
    }


    private void adaptBundle(@NotNull SelectedBundle bundle) {
        String url = bundle.getBundleUrl();
        assert url != null;
        if (url.contains("org.eclipse.core.runtime_")) {
            bundle.setStartLevel(4);
            bundle.setStartAfterInstallation(true);
        } else {
            bundle.setStartLevel(4);
            bundle.setStartAfterInstallation(false);
        }
    }
}
