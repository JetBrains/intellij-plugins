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

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.frameworkintegration.*;
import org.osmorc.make.BundleCompiler;
import org.osmorc.run.LegacyOsgiRunConfigurationLoader;
import org.osmorc.run.OsgiRunConfiguration;
import org.osmorc.run.ui.SelectedBundle;
import org.osmorc.settings.ApplicationSettings;
import org.osmorc.settings.ProjectSettings;

import java.util.List;

/**
 * Loads the legacy Eclipse Equinox Run Configurations as OSGi Run Configurations.
 * @author Robert F. Beeger (robert@beeger.net)
 */
@SuppressWarnings({"MethodMayBeStatic", "UnusedDeclaration"})
public class LegacyEquinoxOsgiRunConfigurationLoader implements LegacyOsgiRunConfigurationLoader {

    public void finishAfterModulesAreAvailable(OsgiRunConfiguration osgiRunConfiguration) {
        List<SelectedBundle> bundlesToDeploy = osgiRunConfiguration.getBundlesToDeploy();
        addModuleBundles(bundlesToDeploy, osgiRunConfiguration.getProject());

        FrameworkInstanceDefinition frameworkInstanceDefinition = getFrameworkInstance(osgiRunConfiguration.getProject());

        if (frameworkInstanceDefinition != null) {
            osgiRunConfiguration.setInstanceToUse(frameworkInstanceDefinition);

            addFrameworkBundle(bundlesToDeploy, frameworkInstanceDefinition);
        }

    }

    @Nullable
    private FrameworkInstanceDefinition getFrameworkInstance(Project project) {
        ApplicationSettings applicationSettings = ServiceManager.getService(ApplicationSettings.class);
        ProjectSettings projectSettings = ServiceManager.getService(project, ProjectSettings.class);
        return applicationSettings.getFrameworkInstance(projectSettings.getFrameworkInstanceName());
    }

    private void addModuleBundles(List<SelectedBundle> bundlesToDeploy, Project project) {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            if (OsmorcFacet.hasOsmorcFacet(module)) {
                SelectedBundle bundle = new SelectedBundle(module.getName(), null, SelectedBundle.BundleType.Module);
                bundle.setStartLevel(4);
                bundlesToDeploy.add(bundle);
            }
        }
    }

    private void addFrameworkBundle(List<SelectedBundle> bundlesToDeploy, @NotNull FrameworkInstanceDefinition frameworkInstanceDefinition) {

        List<Library> libraries = getFrameworkLibraries(frameworkInstanceDefinition);
        for (Library library : libraries) {
            String[] urls = library.getUrls(OrderRootType.CLASSES);
            for (String url : urls) {
                if (url.contains("org.eclipse.equinox.common_")) {
                    SelectedBundle bundle = createSelectedFrameworkBundle(url);
                    if (bundle != null) {
                        bundle.setStartLevel(2);
                        bundle.setStartAfterInstallation(true);
                        bundlesToDeploy.add(bundle);
                    }
                } else if (url.contains("org.eclipse.update.configurator_")) {
                    SelectedBundle bundle = createSelectedFrameworkBundle(url);
                    if (bundle != null) {
                        bundle.setStartLevel(3);
                        bundle.setStartAfterInstallation(true);
                        bundlesToDeploy.add(bundle);
                    }
                } else if (url.contains("org.eclipse.core.runtime_")) {
                    SelectedBundle bundle = createSelectedFrameworkBundle(url);
                    if (bundle != null) {
                        bundle.setStartLevel(4);
                        bundle.setStartAfterInstallation(true);
                        bundlesToDeploy.add(bundle);
                    }
                }
            }
        }
    }

    @Nullable
    private SelectedBundle createSelectedFrameworkBundle(String url) {
        url = BundleCompiler.convertJarUrlToFileUrl(url);
        url = BundleCompiler.fixFileURL(url);
        String bundleName = CachingBundleInfoProvider.getBundleSymbolicName(url);
        SelectedBundle bundle = null;
        if (bundleName != null) {
            String bundleVersion = CachingBundleInfoProvider.getBundleVersions(url);
            bundle = new SelectedBundle(bundleName + " - " + bundleVersion, url, SelectedBundle.BundleType.FrameworkBundle);
        }
        return bundle;
    }

    private List<Library> getFrameworkLibraries(@NotNull FrameworkInstanceDefinition frameworkInstance) {
        FrameworkIntegratorRegistry registry = ServiceManager.getService(FrameworkIntegratorRegistry.class);
        FrameworkIntegrator frameworkIntegrator = registry.findIntegratorByInstanceDefinition(frameworkInstance);
        FrameworkInstanceManager frameworkInstanceManager = frameworkIntegrator.getFrameworkInstanceManager();

        return frameworkInstanceManager.getLibraries(frameworkInstance);
    }


}
