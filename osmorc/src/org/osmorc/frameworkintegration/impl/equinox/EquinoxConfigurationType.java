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

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.run.OsgiConfigurationType;
import org.osmorc.run.OsgiRunConfiguration;

import javax.swing.*;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class EquinoxConfigurationType implements ConfigurationType {
    private ConfigurationFactory myFactory;
    private OsgiConfigurationType osgiConfigurationType;
    private LegacyEquinoxOsgiRunConfigurationLoader legacyEquinoxOsgiRunConfigurationLoader;

    public EquinoxConfigurationType(OsgiConfigurationType osgiConfigurationType) {
        this.osgiConfigurationType = osgiConfigurationType;
        legacyEquinoxOsgiRunConfigurationLoader = new LegacyEquinoxOsgiRunConfigurationLoader();
        myFactory = new ConfigurationFactory(this) {
            public RunConfiguration createTemplateConfiguration(Project project) {
                return EquinoxConfigurationType.this.osgiConfigurationType.getConfigurationFactories()[0].createTemplateConfiguration(project);
            }

            public RunConfiguration createConfiguration(String name, RunConfiguration template) {
                OsgiRunConfiguration runConfiguration = (OsgiRunConfiguration) EquinoxConfigurationType.this.osgiConfigurationType.getConfigurationFactories()[0].createConfiguration(name, template);
                runConfiguration.setLegacyOsgiRunConfigurationLoader(legacyEquinoxOsgiRunConfigurationLoader);
                return runConfiguration;
            }
        };
    }

    public String getDisplayName() {
        return "Eclipse Equinox";
    }

    public String getConfigurationTypeDescription() {
        return "Run Eclipse Equinox";
    }

    public Icon getIcon() {
        return OsmorcBundle.getSmallIcon();
    }

    @NotNull
    public String getId() {
        return "#org.osmorc.EquinoxConfigurationType";
    }

    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{myFactory};
    }
}
