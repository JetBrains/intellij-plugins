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

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.Version;
import org.osmorc.frameworkintegration.CachingBundleInfoProvider;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.FrameworkIntegrator;
import org.osmorc.frameworkintegration.FrameworkIntegratorRegistry;
import org.osmorc.frameworkintegration.impl.AbstractFrameworkRunner;
import org.osmorc.run.ui.SelectedBundle;

import java.io.*;
import java.util.*;
import java.net.MalformedURLException;

/**
 * Framework runner for the Equinox OSGi framework.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @author Robert F. Beeger (robert@beeger.net)
 * @version $Id$
 */
public class EquinoxFrameworkRunner extends AbstractFrameworkRunner<EquinoxRunProperties> {

    public List<VirtualFile> getFrameworkStarterLibraries() {
        List<VirtualFile> result = new ArrayList<VirtualFile>();

        if (isEclipseVersion33OrGreater()) {
            List<Library> libraries = getFrameworkLibraries();
            for (Library library : libraries) {
                VirtualFile[] files = library.getFiles(OrderRootType.CLASSES);
                for (VirtualFile file : files) {
                    if (file.getName().contains("org.eclipse.equinox.launcher_")) {
                        result.add(file);
                    }
                }
            }
        } else {
            VirtualFile frameworkInstallDir =
                    LocalFileSystem.getInstance().findFileByPath(getFrameworkInstance().getBaseFolder());

            assert frameworkInstallDir != null;
            VirtualFile startup = frameworkInstallDir.findChild("startup.jar");
            result.add(startup);
        }
        return result;
    }

    private List<Library> getFrameworkLibraries() {
        return getFrameworkInstanceManager().getLibraries(getFrameworkInstance());
    }

    private boolean isEclipseVersion33OrGreater() {
        Version eclipseVersion = getFrameworkInstanceManager().getEclipseVersion(getFrameworkInstance());

        return eclipseVersion != null && ECLIPSE_3_3_0.compareTo(eclipseVersion) <= 0;
    }

    private EquinoxFrameworkInstanceManager getFrameworkInstanceManager() {
        if (frameworkInstanceManager == null) {
            FrameworkInstanceDefinition frameworkInstance = getFrameworkInstance();
            assert frameworkInstance != null;
            FrameworkIntegratorRegistry registry = ServiceManager.getService(FrameworkIntegratorRegistry.class);
            FrameworkIntegrator frameworkIntegrator = registry.findIntegratorByInstanceDefinition(frameworkInstance);
            frameworkInstanceManager = (EquinoxFrameworkInstanceManager) frameworkIntegrator.getFrameworkInstanceManager();
        }

        return frameworkInstanceManager;
    }

    private FrameworkInstanceDefinition getFrameworkInstance() {
        return getRunConfiguration().getInstanceToUse();
    }


    public void fillCommandLineParameters(ParametersList commandLineParameters,
                                          @NotNull SelectedBundle[] bundlesToInstall) {
        String configDirPath = getFrameworkDirCanonicalPath();
        commandLineParameters.add("-configuration", configDirPath);
        commandLineParameters.add("-data", configDirPath);

        if (getAdditionalProperties().isDebugMode()) {
            commandLineParameters.add("-debug");
            commandLineParameters.add("-consoleLog");
        }

        if (getAdditionalProperties().isStartEquinoxOSGIConsole()) {
            commandLineParameters.add("-console");
        }

        if (!getRunConfiguration().isRuntimeDirsOsmorcControlled() && getAdditionalProperties().isCleanEquinoxCache()) {
            commandLineParameters.add("-clean");
        }
    }

    @NotNull
    public Map<String, String> getSystemProperties(@NotNull SelectedBundle[] bundlesToInstall) {
        return new HashMap<String, String>();
    }

    public void runCustomInstallationSteps(@NotNull final SelectedBundle[] bundlesToInstall) throws ExecutionException {
        if (getRunConfiguration().isRuntimeDirsOsmorcControlled() || getAdditionalProperties().isRecreateEquinoxConfigIni()) {
            writeConfigFile(bundlesToInstall);
        }
    }

    private void writeConfigFile(@NotNull final SelectedBundle[] bundlesToInstall) throws ExecutionException {

        Properties properties = new Properties();

        fillOsgiFrameworkProperty(properties);
        fillBundlesProperties(properties, bundlesToInstall);
        fillProductOrApplicationProperty(properties);
        fillBootDelegationAndSystemPackagesProperties(properties);
        fillMiscProperties(properties);

        try {
            properties.store(new FileOutputStream(new File(getFrameworkDir(), "config.ini")), "");
        }
        catch (IOException e) {
            throw new ExecutionException("Error on writing a file", e);
        }
    }

    private void fillMiscProperties(Properties properties) {
        FrameworkInstanceDefinition frameworkInstance = getFrameworkInstance();
        String osgiInstallArea;
        try {
            osgiInstallArea = createFileURL(new File(frameworkInstance.getBaseFolder()).toURL().toString());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        properties.put("osgi.install.area", osgiInstallArea);
        properties.put("osgi.configuration.cascaded", "false");
    }

    private void fillBootDelegationAndSystemPackagesProperties(Properties properties) {
        String bootDelegation = getAdditionalProperties().getBootDelegation();
        if (bootDelegation != null && !(bootDelegation.trim().length() == 0)) {
            properties.put("org.osgi.framework.bootdelegation", bootDelegation);
        }
        String systemPackages = getAdditionalProperties().getSystemPackages();
        if (systemPackages != null && !(systemPackages.trim().length() == 0)) {
            properties.put("org.osgi.framework.system.packages", systemPackages);
        }
    }

    private void fillProductOrApplicationProperty(Properties properties) {
        String product = getAdditionalProperties().getEquinoxProduct();
        if (product != null && product.length() > 0) {
            properties.put("eclipse.product", product);
        } else {
            String application = getAdditionalProperties().getEquinoxApplication();
            if (application != null && application.length() > 0) {
                properties.put("eclipse.application", application);
            } else {
                properties.put("eclipse.ignoreApp", "true");
            }
        }
    }

    private void fillBundlesProperties(Properties properties, SelectedBundle[] bundlesToInstall) {
        StringBuilder osgiBundlesBuilder = new StringBuilder();
        int level = 0;
        for (SelectedBundle selectedBundle : bundlesToInstall) {
            String bundleUrl = selectedBundle.getBundleUrl();
            osgiBundlesBuilder.append("reference:");
            osgiBundlesBuilder.append(bundleUrl);
            if (selectedBundle.isStartAfterInstallation()) {
                osgiBundlesBuilder.append("@").append(selectedBundle.getStartLevel());
                if (!CachingBundleInfoProvider.isFragmentBundle(selectedBundle.getBundleUrl())) {
                    osgiBundlesBuilder.append(":start");
                }
            }
            osgiBundlesBuilder.append(",");
            level = Math.max(level, selectedBundle.getStartLevel());
        }
        if (osgiBundlesBuilder.length() > 0) {
            osgiBundlesBuilder.delete(osgiBundlesBuilder.length() - 1, osgiBundlesBuilder.length());
        }

        String osgiBundles = osgiBundlesBuilder.toString();

        properties.put("osgi.bundles", osgiBundles);
        properties.put("osgi.bundles.defaultStartLevel", "4");
        properties.put("osgi.framework.beginningstartlevel", String.valueOf(level));
        properties.put("osgi.startLevel", String.valueOf(level));
    }

    private void fillOsgiFrameworkProperty(Properties properties) {
        String osgiFramework = null;
        List<Library> libraries = getFrameworkLibraries();
        for (Library library : libraries) {
            String[] urls = library.getUrls(OrderRootType.CLASSES);
            for (String url : urls) {
                if (url.contains("org.eclipse.osgi_")) {
                    osgiFramework = createFileURL(url);
                }
            }
        }
        if (osgiFramework != null) {
            properties.put("osgi.framework", osgiFramework);
        }
    }

    private String createFileURL(final String path) {
        String result = path;
        result = result.replace("jar://", "file://");
        result = result.replaceAll("!.*", "");

        return result.replaceFirst("file:/*", "file:///");
    }

    @NotNull
    public String getMainClass() {
        if (isEclipseVersion33OrGreater()) {
            return ECLIPSE_330_LAUNCHER_CLASS;
        } else {
            return ECLIPSE_PRE330_LAUNCHER_CLASS;
        }
    }

    @NotNull
    public EquinoxRunProperties convertProperties(Map<String, String> properties) {
        return new EquinoxRunProperties(properties);
    }

    private EquinoxFrameworkInstanceManager frameworkInstanceManager;

    private static final Version ECLIPSE_3_3_0 = Version.parseVersion("3.3.0");

    private static final String ECLIPSE_330_LAUNCHER_CLASS = "org.eclipse.equinox.launcher.Main";
    private static final String ECLIPSE_PRE330_LAUNCHER_CLASS = "org.eclipse.core.launcher.Main";
}
