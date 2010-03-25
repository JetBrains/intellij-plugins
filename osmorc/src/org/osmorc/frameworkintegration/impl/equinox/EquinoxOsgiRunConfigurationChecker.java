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

import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.configurations.RuntimeConfigurationWarning;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.Version;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.run.OsgiRunConfiguration;
import org.osmorc.run.OsgiRunConfigurationChecker;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class EquinoxOsgiRunConfigurationChecker implements OsgiRunConfigurationChecker {
    private static Version ECLIPSE_3_1_0 = Version.parseVersion("3.1.0");
    private static Version ECLIPSE_3_3_0 = Version.parseVersion("3.3.0");
    private EquinoxFrameworkInstanceManager instanceManager;


    public EquinoxOsgiRunConfigurationChecker(@NotNull EquinoxFrameworkInstanceManager instanceManager) {
        this.instanceManager = instanceManager;
    }

    public void checkConfiguration(OsgiRunConfiguration runConfiguration) throws RuntimeConfigurationException {
        FrameworkInstanceDefinition frameworkInstanceDefinition = runConfiguration.getInstanceToUse();
        assert frameworkInstanceDefinition != null;

        Version eclipseVersion = instanceManager.getEclipseVersion(frameworkInstanceDefinition);

        if (eclipseVersion == null) {
            throw new RuntimeConfigurationError(
                    OsmorcBundle.getTranslation("runconfiguration.equinox.instanceVersionNotFound"));
        }

        if (ECLIPSE_3_1_0.compareTo(eclipseVersion) > 0) {
            throw new RuntimeConfigurationError(
                    OsmorcBundle.getTranslation("runconfiguration.equinox.unsupportedInstanceVersion", eclipseVersion));
        }

        if (ECLIPSE_3_3_0.compareTo(eclipseVersion) > 0) {
            VirtualFile frameworkInstallDir =
                    LocalFileSystem.getInstance().findFileByPath(frameworkInstanceDefinition.getBaseFolder());
            if (frameworkInstallDir == null || !frameworkInstallDir.exists() || !frameworkInstallDir.isDirectory()) {
                throw new RuntimeConfigurationError(
                        OsmorcBundle.getTranslation("runconfiguration.equinox.instanceInstallFolderDoesNotExist",
                                frameworkInstanceDefinition.getBaseFolder()));
            }
            VirtualFile startup = frameworkInstallDir.findChild("startup.jar");
            if (startup == null || !startup.exists() || startup.isDirectory()) {
                throw new RuntimeConfigurationError(
                        OsmorcBundle.getTranslation("runconfiguration.equinox.startupJARDoesNotExist",
                                frameworkInstanceDefinition.getBaseFolder()));
            }
        }

        EquinoxRunProperties runProperties = new EquinoxRunProperties(runConfiguration.getAdditionalProperties());

        if (runProperties.getEquinoxApplication() != null && runProperties.getEquinoxApplication().length() > 0 ||
                runProperties.getEquinoxProduct() != null && runProperties.getEquinoxProduct().length() > 0) {
            if (SystemInfo.isMac && !runConfiguration.getVmParameters().contains("-XstartOnFirstThread")) {
                throw new RuntimeConfigurationWarning(
                        "Using the JVM option -XstartOnFirstThread for running SWT apps on Mac OS X is highly recommended.");
            }

            if (runProperties.isStartConsole()) {
                throw new RuntimeConfigurationWarning(OsmorcBundle.getTranslation("runconfiguration.equinox.runningWithConsole"));
            }
        }
    }
}
