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
import com.intellij.util.ArrayUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class FrameworkDefinitionsEditor implements SearchableConfigurable, ApplicationSettingsAwareEditor,
        ProjectSettingsAwareEditor {

    public FrameworkDefinitionsEditor(FrameworkIntegratorRegistry frameworkIntegratorRegistry,
                                      FrameworkInstanceUpdateNotifier updateNotifier,
                                      ApplicationSettingsUpdateNotifier applicationSettingsUpdateNotifier) {
        this.frameworkIntegratorRegistry = frameworkIntegratorRegistry;
        this.updateNotifier = updateNotifier;
        this.applicationSettingsUpdateNotifier = applicationSettingsUpdateNotifier;

        addedFrameworkInstances = new ArrayList<FrameworkInstanceDefinition>();
        removedFrameworkInstances = new ArrayList<FrameworkInstanceDefinition>();
        reloadedFrameworkInstances = new ArrayList<FrameworkInstanceDefinition>();

        addFramework.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addFrameworkInstance();
            }
        });

        editFramework.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editFrameworkInstance();
            }
        });
        removeFramework.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeFrameworkInstance();
            }
        });
        editFramework.setEnabled(selectedFrameworkInstance != null);

        frameworkInstances.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                selectedFrameworkInstance = (FrameworkInstanceDefinition) frameworkInstances.getSelectedValue();

                if (selectedFrameworkInstance != null) {
                    frameworkIntegrator.setText(selectedFrameworkInstance.getFrameworkIntegratorName());
                    baseFolder.setText(selectedFrameworkInstance.getBaseFolder());
                    frameworkInstanceName.setText(selectedFrameworkInstance.getName());
                }
                editFramework.setEnabled(selectedFrameworkInstance != null);
            }
        });

        frameworkInstances.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if ( selectedFrameworkInstance != null && e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    // edit on doubleclick
                    editFrameworkInstance();
                }
            }
        });
    }

    private void addFrameworkInstance() {
        String selectedFrameworkInstanceForProject = getProjectSettingsWorkingCopy().getFrameworkInstanceName();
        FrameworkInstanceDefinition frameworkInstanceDefinition =
                getApplicationSettingsWorkingCopy().getFrameworkInstance(selectedFrameworkInstanceForProject);

        String frameworkInstanceNameForCreation = null;
        if (frameworkInstanceDefinition == null) {
            frameworkInstanceNameForCreation = selectedFrameworkInstanceForProject;
        }

        CreateFrameworkInstanceDialog dialog =
                new CreateFrameworkInstanceDialog(frameworkIntegratorRegistry,
                        frameworkInstanceNameForCreation);
        dialog.pack();
        dialog.show();

        if (dialog.isOK()) {
            FrameworkInstanceDefinition instanceDefinition = new FrameworkInstanceDefinition();
            instanceDefinition.setName(dialog.getName());
            instanceDefinition.setFrameworkIntegratorName(dialog.getIntegratorName());
            instanceDefinition.setBaseFolder(dialog.getBaseFolder());

            getFrameworkInistanceManager(instanceDefinition).createLibraries(instanceDefinition);

            getApplicationSettingsWorkingCopy().addFrameworkInstanceDefinition(instanceDefinition);
            applicationSettingsUpdateNotifier.fireApplicationSettingsChanged();
            changed = true;
            refreshFrameworkInstanceList();
            frameworkInstances.setSelectedValue(instanceDefinition, true);

            addedFrameworkInstances.add(instanceDefinition);
            removedFrameworkInstances.remove(instanceDefinition);
            assert !reloadedFrameworkInstances.contains(instanceDefinition);
        }
    }


    private void editFrameworkInstance() {
        final FrameworkInstanceDefinition frameworkInstanceDefinition = selectedFrameworkInstance;
        if ( frameworkInstanceDefinition == null ) {
            return; // usually should not happen, but you never know.
        }

        CreateFrameworkInstanceDialog dialog =
                new CreateFrameworkInstanceDialog(frameworkIntegratorRegistry,
                        frameworkInstanceDefinition.getName());
        dialog.setIntegratorName(frameworkInstanceDefinition.getFrameworkIntegratorName());
        dialog.setBaseFolder(frameworkInstanceDefinition.getBaseFolder());
        dialog.pack();
        dialog.show();

        if (dialog.isOK()) {
            // remove old libraries
            getFrameworkInistanceManager(frameworkInstanceDefinition).removeLibraries(frameworkInstanceDefinition);

            // set new properties
            frameworkInstanceDefinition.setName(dialog.getName());
            frameworkInstanceDefinition.setFrameworkIntegratorName(dialog.getIntegratorName());
            frameworkInstanceDefinition.setBaseFolder(dialog.getBaseFolder());

            // create new libraries
            getFrameworkInistanceManager(frameworkInstanceDefinition).createLibraries(frameworkInstanceDefinition);

            // fire settings change.
            applicationSettingsUpdateNotifier.fireApplicationSettingsChanged();
            changed = true;
            refreshFrameworkInstanceList();
            frameworkInstances.setSelectedValue(frameworkInstanceDefinition, true);
            reloadedFrameworkInstances.add(frameworkInstanceDefinition);
        }
    }

    private FrameworkInstanceManager getFrameworkInistanceManager(FrameworkInstanceDefinition instanceDefinition) {
        FrameworkIntegrator frameworkIntegrator =
                frameworkIntegratorRegistry.findIntegratorByInstanceDefinition(instanceDefinition);
        return frameworkIntegrator.getFrameworkInstanceManager();
    }

    private void removeFrameworkInstance() {
        FrameworkInstanceDefinition selectedFrameworkInstance = this.selectedFrameworkInstance;
        if (selectedFrameworkInstance != null) {
            getFrameworkInistanceManager(selectedFrameworkInstance).removeLibraries(selectedFrameworkInstance);
            getApplicationSettingsWorkingCopy().removeFrameworkInstanceDefinition(selectedFrameworkInstance);
            applicationSettingsUpdateNotifier.fireApplicationSettingsChanged();
            changed = true;
            refreshFrameworkInstanceList();
            frameworkInstances.setSelectedIndex(0);

            if (!addedFrameworkInstances.contains(selectedFrameworkInstance)) {
                removedFrameworkInstances.add(selectedFrameworkInstance);
            }
            addedFrameworkInstances.remove(selectedFrameworkInstance);
            reloadedFrameworkInstances.remove(selectedFrameworkInstance);
        }
    }

    private void refreshFrameworkInstanceList() {
        List<FrameworkInstanceDefinition> instanceDefinitions =
                getApplicationSettingsWorkingCopy().getFrameworkInstanceDefinitions();
        frameworkInstances.setListData(ArrayUtil.toObjectArray(instanceDefinitions));
    }

    private void copySettings(ApplicationSettings from, ApplicationSettings to) {
        List<FrameworkInstanceDefinition> copiedDefinitions = new ArrayList<FrameworkInstanceDefinition>();
        for (FrameworkInstanceDefinition definition : from.getFrameworkInstanceDefinitions()) {
            FrameworkInstanceDefinition copiedDefinition = new FrameworkInstanceDefinition();
            XmlSerializerUtil.copyBean(definition, copiedDefinition);
            copiedDefinitions.add(copiedDefinition);
        }
        to.setFrameworkInstanceDefinitions(copiedDefinitions);
    }


    @Nls
    public String getDisplayName() {
        return "Framework Definitions";
    }

    public Icon getIcon() {
        return null;
    }

    public String getHelpTopic() {
        return "reference.settings.project.osgi.framework.definitions";
    }

    public String getId() {
      return getHelpTopic();
    }

    public Runnable enableSearch(String option) {
      return null;
    }

    public JComponent createComponent() {
        return mainPanel;
    }

    public boolean isModified() {
        return changed;
    }

    public void apply() throws ConfigurationException {
        copySettings(getApplicationSettingsWorkingCopy(), getApplicationSettings());

        for (FrameworkInstanceDefinition addedFrameworkInstance : addedFrameworkInstances) {
            updateNotifier.fireUpdateFrameworkInstance(addedFrameworkInstance,
                    FrameworkInstanceUpdateNotifier.UpdateKind.ADDITION);
        }
        for (FrameworkInstanceDefinition removedFrameworkInstance : removedFrameworkInstances) {
            updateNotifier.fireUpdateFrameworkInstance(removedFrameworkInstance,
                    FrameworkInstanceUpdateNotifier.UpdateKind.REMOVAL);
        }
        for (FrameworkInstanceDefinition reloadedFrameworkInstance : reloadedFrameworkInstances) {
            updateNotifier.fireUpdateFrameworkInstance(reloadedFrameworkInstance,
                    FrameworkInstanceUpdateNotifier.UpdateKind.RELOAD);
        }
        changed = false;
    }

    public void reset() {
        if (getApplicationSettings() != null) {
            copySettings(getApplicationSettings(), getApplicationSettingsWorkingCopy());
            refreshFrameworkInstanceList();
            changed = false;
        }
    }

    public void disposeUIResources() {
        if (changed) {
            cleanUpFrameworkInstances(getApplicationSettings().getFrameworkInstanceDefinitions(),
                    getApplicationSettingsWorkingCopy().getFrameworkInstanceDefinitions());
        }
    }

    private void cleanUpFrameworkInstances(List<FrameworkInstanceDefinition> oldFrameworkInstances,
                                           List<FrameworkInstanceDefinition> newFrameworkInstances) {
        for (FrameworkInstanceDefinition newFrameworkInstance : newFrameworkInstances) {
            if (!oldFrameworkInstances.contains(newFrameworkInstance)) {
                getFrameworkInistanceManager(newFrameworkInstance).removeLibraries(newFrameworkInstance);
            }
        }
    }

    public void setApplicationSettingsProvider(
            @NotNull ApplicationSettingsProvider applicationSettingsProvider) {
        this.applicationSettingsProvider = applicationSettingsProvider;
        reset();
    }

    public void setProjectSettingsProvider(@NotNull ProjectSettingsProvider projectSettingsProvider) {
        this.projectSettingsProvider = projectSettingsProvider;
    }

    private ProjectSettings getProjectSettingsWorkingCopy() {
        return projectSettingsProvider != null ? projectSettingsProvider.getProjectSettingsWorkingCopy() : null;
    }

    private ApplicationSettings getApplicationSettings() {
        return applicationSettingsProvider != null ? applicationSettingsProvider.getApplicationSettings() : null;
    }

    private ApplicationSettings getApplicationSettingsWorkingCopy() {
        return applicationSettingsProvider != null ? applicationSettingsProvider.getApplicationSettingsWorkingCopy() : null;
    }

    private JPanel mainPanel;
    private JList frameworkInstances;
    private JButton addFramework;
    private JButton removeFramework;
    private JLabel frameworkIntegrator;
    private JLabel baseFolder;
    private JLabel frameworkInstanceName;
    private JButton editFramework;
    private FrameworkIntegratorRegistry frameworkIntegratorRegistry;
    private FrameworkInstanceUpdateNotifier updateNotifier;
    private ApplicationSettingsUpdateNotifier applicationSettingsUpdateNotifier;
    private List<FrameworkInstanceDefinition> addedFrameworkInstances;
    private List<FrameworkInstanceDefinition> removedFrameworkInstances;
    private List<FrameworkInstanceDefinition> reloadedFrameworkInstances;
    private FrameworkInstanceDefinition selectedFrameworkInstance;
    private boolean changed;
    private ApplicationSettingsProvider applicationSettingsProvider;
    private ProjectSettingsProvider projectSettingsProvider;
}
