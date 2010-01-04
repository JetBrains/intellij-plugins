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
package org.osmorc.frameworkintegration.impl;

import com.intellij.execution.configurations.ParametersList;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.FrameworkInstanceManager;
import org.osmorc.frameworkintegration.FrameworkIntegrator;
import org.osmorc.frameworkintegration.FrameworkIntegratorRegistry;
import org.osmorc.frameworkintegration.util.PropertiesWrapper;
import org.osmorc.run.ui.SelectedBundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This class works a kind of glue code between the old simple FramworkRunnr interface and the new interface that
 * gives a greater access to the whole framework running process. FrameworkRunners which do not need the added
 * functionality can subclass this abstract class to use the simpler interface.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @author Robert F. Beeger (robert@beeger.net)
 */
public abstract class AbstractSimpleFrameworkRunner<P extends PropertiesWrapper> extends AbstractFrameworkRunner<P> {
    protected AbstractSimpleFrameworkRunner() {
    }

    @NotNull
    public List<VirtualFile> getFrameworkStarterLibraries() {
        List<VirtualFile> result = new ArrayList<VirtualFile>();

        FrameworkInstanceDefinition definition = getRunConfiguration().getInstanceToUse();
        FrameworkIntegratorRegistry registry = ServiceManager.getService(getProject(), FrameworkIntegratorRegistry.class);
        FrameworkIntegrator integrator = registry.findIntegratorByInstanceDefinition(definition);
        FrameworkInstanceManager frameworkInstanceManager = integrator.getFrameworkInstanceManager();

        List<Library> libs = frameworkInstanceManager.getLibraries(definition);

        for (Library lib : libs) {
            for (VirtualFile virtualFile : lib.getFiles(OrderRootType.CLASSES_AND_OUTPUT)) {
                if (getFrameworkStarterClasspathPattern().matcher(virtualFile.getName()).matches()) {
                    result.add(virtualFile);
                }
            }
        }
        return result;
    }

    /**
     * A pattern tested against all framework bundle jars to collect all jars that need to be put into the classpath in order
     * to start a framework.
     *
     * @return The pattern matching all needed jars for running of a framework instance.
     */
    protected abstract Pattern getFrameworkStarterClasspathPattern();

    @NotNull
    public String[] getCommandlineParameters(@NotNull SelectedBundle[] bundlesToInstall) {
        return getCommandlineParameters(bundlesToInstall, getAdditionalProperties());
    }

    /**
     * Returns an array of command line parameters that can be used to install and run the specified bundles.
     *
     * @param bundlesToInstall     an array containing the URLs of the bundles to be installed. The bundles must be sorted
     *                             in ascending order by their start level.
     * @param additionalProperties additional runner properties
     * @return a list of command line parameters
     */
    @NotNull
    protected abstract String[] getCommandlineParameters(@NotNull SelectedBundle[] bundlesToInstall,
                                                         @NotNull P additionalProperties);

    public void fillCommandLineParameters(ParametersList commandLineParameters,
                                          @NotNull SelectedBundle[] bundlesToInstall) {
        commandLineParameters.addAll(getCommandlineParameters(bundlesToInstall));
    }

    @NotNull
    public Map<String, String> getSystemProperties(@NotNull SelectedBundle[] bundlesToInstall) {
        return getSystemProperties(bundlesToInstall, getAdditionalProperties());
    }

    /**
     * Returns a map of system properties to be set in order to install and run the specified bundles.
     *
     * @param bundlesToInstall     an array containing the URLs of the bundles to be installed. The bundles must be sorted
     *                             in ascending order by their start level.
     * @param additionalProperties additonal runner properties
     * @return a map of system properties
     */
    @NotNull
    protected abstract Map<String, String> getSystemProperties(@NotNull SelectedBundle[] bundlesToInstall,
                                                               @NotNull P additionalProperties);

    public final void runCustomInstallationSteps(@NotNull SelectedBundle[] bundlesToInstall) {
        runCustomInstallationSteps(bundlesToInstall, getAdditionalProperties());
    }

    /**
     * Instructs the FrameworkRunnner to run any custom installation steps that are required for installing the given
     * bundles.
     *
     * @param bundlesToInstall     an array containing the URLs of the bundles to be installed. The bundles must be sorted
     *                             in ascending order by their start level.
     * @param additionalProperties additional runner properties
     */
    protected abstract void runCustomInstallationSteps(@NotNull SelectedBundle[] bundlesToInstall,
                                                       @NotNull P additionalProperties);


}
