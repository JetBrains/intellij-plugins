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

package org.osmorc.settings;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.FrameworkInstanceUpdateNotifier;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class ProjectSettingsEditor implements SearchableConfigurable, ProjectSettingsAwareEditor, ApplicationSettingsAwareEditor,
        ApplicationSettingsUpdateNotifier.Listener {

    public ProjectSettingsEditor(Project project, FrameworkInstanceUpdateNotifier updateNotifier,
                                 ApplicationSettingsUpdateNotifier applicationSettingsUpdateNotifier,
                                 ProjectSettingsUpdateNotifier projectSettingsUpdateNotifier) {
        this.project = project;
        this.updateNotifier = updateNotifier;
        this.projectSettingsUpdateNotifier = projectSettingsUpdateNotifier;

      
        frameworkInstance.setRenderer(new FrameworkInstanceCellRenderer() {
          @Override
          protected boolean isInstanceDefined(FrameworkInstanceDefinition instance) {
             List<FrameworkInstanceDefinition> instanceDefinitions =
                getApplicationSettingsWorkingCopy().getFrameworkInstanceDefinitions();
            for (FrameworkInstanceDefinition instanceDefinition : instanceDefinitions) {
              if ( instance.equals(instanceDefinition)) {
                return true;
              }
            }
            return false;
          }
        });

        frameworkInstance.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!updatingFrameworkInstanceComboBox && frameworkInstance.getSelectedItem() != null) {
                    getProjectSettingsWorkingCopy().setFrameworkInstanceName(
                            ((FrameworkInstanceDefinition) frameworkInstance.getSelectedItem()).getName());
                    ProjectSettingsEditor.this.projectSettingsUpdateNotifier.fireProjectSettingsChanged();
                    updateChangedFlag();
                    refreshFrameworkInstanceCombobox();
                }
            }
        });

        createFrameworkInstanceModule.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getProjectSettingsWorkingCopy().setCreateFrameworkInstanceModule(createFrameworkInstanceModule.isSelected());
                ProjectSettingsEditor.this.projectSettingsUpdateNotifier.fireProjectSettingsChanged();
                updateChangedFlag();
            }
        });

        defaultManifestFileLocation.setEditable(true);
        defaultManifestFileLocation.addItem("META-INF");
        defaultManifestFileLocation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (defaultManifestFileLocation.getSelectedItem() != null) {
                    getProjectSettingsWorkingCopy()
                            .setDefaultManifestFileLocation((String) defaultManifestFileLocation.getSelectedItem());
                    ProjectSettingsEditor.this.projectSettingsUpdateNotifier.fireProjectSettingsChanged();
                    updateChangedFlag();
                }
            }
        });

        this.applicationSettingsUpdateNotifier = applicationSettingsUpdateNotifier;
    }

    public void applicationSettingsChanged() {
        if (getApplicationSettingsWorkingCopy() != null) {
            refreshFrameworkInstanceCombobox();
        }
    }


    private void updateChangedFlag() {
        changed = !Comparing
                .strEqual(getProjectSettingsWorkingCopy().getFrameworkInstanceName(), getProjectSettings().getFrameworkInstanceName()) ||
                getProjectSettingsWorkingCopy().isCreateFrameworkInstanceModule() !=
                        getProjectSettings().isCreateFrameworkInstanceModule() ||
                !Comparing.strEqual(getProjectSettingsWorkingCopy().getDefaultManifestFileLocation(),
                        getProjectSettings().getDefaultManifestFileLocation());
    }


    @Nls
    public String getDisplayName() {
        return "Project Settings";
    }

    public Icon getIcon() {
        return null;
    }

    public String getHelpTopic() {
        return "reference.settings.project.osgi.project.settings";
    }

    public String getId() {
      return getHelpTopic();
    }

    public Runnable enableSearch(String option) {
      return null;
    }

    public JComponent createComponent() {
        applicationSettingsUpdateNotifier.addListener(this);
        return mainPanel;
    }

    public void disposeUIResources() {
        applicationSettingsUpdateNotifier.removeListener(this);
    }

    public boolean isModified() {
        return changed;
    }

    public void apply() throws ConfigurationException {
        String oldFrameworkInstanceName = getProjectSettings().getFrameworkInstanceName();
        final boolean oldCreateFrameworkInstanceModule = getProjectSettings().isCreateFrameworkInstanceModule();

        copySettings(getProjectSettingsWorkingCopy(), getProjectSettings());

        if (!Comparing.strEqual(getProjectSettings().getFrameworkInstanceName(), oldFrameworkInstanceName)) {
            updateNotifier.fireUpdateFrameworkInstanceSelection(project);
        }
        if (oldCreateFrameworkInstanceModule != getProjectSettings().isCreateFrameworkInstanceModule()) {
            updateNotifier.fireFrameworkInstanceModuleHandlingChanged(project);
        }
        changed = false;
    }

    public void reset() {
        if (getProjectSettingsWorkingCopy() != null && getApplicationSettingsWorkingCopy() != null) {
            copySettings(getProjectSettings(), getProjectSettingsWorkingCopy());

            refreshFrameworkInstanceCombobox();
            defaultManifestFileLocation.setSelectedItem(getProjectSettingsWorkingCopy().getDefaultManifestFileLocation());
            createFrameworkInstanceModule.setSelected(getProjectSettingsWorkingCopy().isCreateFrameworkInstanceModule());
            changed = false;
        }
    }

    private void refreshFrameworkInstanceCombobox() {
        updatingFrameworkInstanceComboBox = true;
        List<FrameworkInstanceDefinition> instanceDefinitions =
                getApplicationSettingsWorkingCopy().getFrameworkInstanceDefinitions();

        String projectFrameworkInstanceName = getProjectSettingsWorkingCopy().getFrameworkInstanceName();
        FrameworkInstanceDefinition projectFrameworkInstance = null;

        frameworkInstance.removeAllItems();
        for (FrameworkInstanceDefinition instanceDefinition : instanceDefinitions) {
            frameworkInstance.addItem(instanceDefinition);
            if (instanceDefinition.getName().equals(projectFrameworkInstanceName)) {
                projectFrameworkInstance = instanceDefinition;
            }
        }
        if (projectFrameworkInstance == null && projectFrameworkInstanceName != null) {
            projectFrameworkInstance = new FrameworkInstanceDefinition();
            projectFrameworkInstance.setName(projectFrameworkInstanceName);
            projectFrameworkInstance.setDefined(false);
            frameworkInstance.addItem(projectFrameworkInstance);
        }
        frameworkInstance.setSelectedItem(projectFrameworkInstance);

        updatingFrameworkInstanceComboBox = false;
    }

    private void copySettings(ProjectSettings from, ProjectSettings to) {
        to.setDefaultManifestFileLocation(from.getDefaultManifestFileLocation());
        to.setCreateFrameworkInstanceModule(from.isCreateFrameworkInstanceModule());
        to.setFrameworkInstanceName(from.getFrameworkInstanceName());

    }

    public void setProjectSettingsProvider(@NotNull ProjectSettingsProvider projectSettingsProvider) {
        this.projectSettingsProvider = projectSettingsProvider;
        reset();
    }

    public void setApplicationSettingsProvider(
            @NotNull ApplicationSettingsProvider applicationSettingsProvider) {
        this.applicationSettingsProvider = applicationSettingsProvider;
        reset();
    }

    private ProjectSettings getProjectSettings() {
        return projectSettingsProvider != null ? projectSettingsProvider.getProjectSettings() : null;
    }

    private ProjectSettings getProjectSettingsWorkingCopy() {
        return projectSettingsProvider != null ? projectSettingsProvider.getProjectSettingsWorkingCopy() : null;
    }

    private ApplicationSettings getApplicationSettingsWorkingCopy() {
        return applicationSettingsProvider != null ? applicationSettingsProvider.getApplicationSettingsWorkingCopy() : null;
    }

    private JPanel mainPanel;
    private JComboBox frameworkInstance;
    private JCheckBox createFrameworkInstanceModule;
    private JComboBox defaultManifestFileLocation;
    private final Project project;
    private final FrameworkInstanceUpdateNotifier updateNotifier;
    private final ProjectSettingsUpdateNotifier projectSettingsUpdateNotifier;
    private boolean changed;
    private final ApplicationSettingsUpdateNotifier applicationSettingsUpdateNotifier;
    private boolean updatingFrameworkInstanceComboBox = false;
    private ProjectSettingsProvider projectSettingsProvider;
    private ApplicationSettingsProvider applicationSettingsProvider;
}
