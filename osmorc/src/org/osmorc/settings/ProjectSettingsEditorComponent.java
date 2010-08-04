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

import com.intellij.ui.UserActivityListener;
import com.intellij.ui.UserActivityWatcher;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;

import javax.swing.*;
import java.util.List;

/**
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 */
public class ProjectSettingsEditorComponent implements ApplicationSettings.ApplicationSettingsListener {
  private boolean myModified;
  @SuppressWarnings({"FieldAccessedSynchronizedAndUnsynchronized"}) private ProjectSettings mySettings;
  private UserActivityWatcher myWatcher;
  private JPanel mainPanel;
  @SuppressWarnings({"FieldAccessedSynchronizedAndUnsynchronized"}) private JComboBox frameworkInstance;
  private JCheckBox createFrameworkInstanceModule;
  private JComboBox defaultManifestFileLocation;

  public ProjectSettingsEditorComponent() {
    frameworkInstance.setRenderer(new FrameworkInstanceCellRenderer() {
      @Override
      protected boolean isInstanceDefined(FrameworkInstanceDefinition instance) {
        List<FrameworkInstanceDefinition> instanceDefinitions = ApplicationSettings.getInstance().getFrameworkInstanceDefinitions();
        for (FrameworkInstanceDefinition instanceDefinition : instanceDefinitions) {
          if (instance.equals(instanceDefinition)) {
            return true;
          }
        }
        return false;
      }
    });
    myWatcher = new UserActivityWatcher();
    myWatcher.register(mainPanel);
    myWatcher.addUserActivityListener(new UserActivityListener() {
      public void stateChanged() {
        myModified = true;
      }
    });

    defaultManifestFileLocation.setEditable(true);
    defaultManifestFileLocation.addItem("META-INF");

    ApplicationSettings.getInstance().addApplicationSettingsListener(this);
  }

  public void applyTo(ProjectSettings settings) {
    settings.setCreateFrameworkInstanceModule(createFrameworkInstanceModule.isSelected());
    final String fileLocation = (String)defaultManifestFileLocation.getSelectedItem();
    if (fileLocation != null) {
      settings.setDefaultManifestFileLocation(fileLocation);
    }
    final FrameworkInstanceDefinition instanceDefinition = (FrameworkInstanceDefinition)this.frameworkInstance.getSelectedItem();
    if (instanceDefinition != null) {
      settings.setFrameworkInstanceName(instanceDefinition.getName());
    }
  }

  public void dispose() {
    myWatcher = null;
    ApplicationSettings.getInstance().removeApplicationSettingsListener(this);
  }

  public JPanel getMainPanel() {
    return mainPanel;
  }

  public void resetTo(ProjectSettings settings) {
    mySettings = settings;
    refreshFrameworkInstanceCombobox();
    defaultManifestFileLocation.setSelectedItem(mySettings.getDefaultManifestFileLocation());
    createFrameworkInstanceModule.setSelected(mySettings.isCreateFrameworkInstanceModule());
    myModified = false;
  }

  private synchronized void refreshFrameworkInstanceCombobox() {
    if (mySettings == null) return;

    frameworkInstance.removeAllItems();
    List<FrameworkInstanceDefinition> instanceDefinitions = ApplicationSettings.getInstance().getFrameworkInstanceDefinitions();
    final String frameworkInstanceName = mySettings.getFrameworkInstanceName();

    FrameworkInstanceDefinition projectFrameworkInstance = null;
    for (FrameworkInstanceDefinition instanceDefinition : instanceDefinitions) {
      frameworkInstance.addItem(instanceDefinition);
      if (instanceDefinition.getName().equals(frameworkInstanceName)) {
        projectFrameworkInstance = instanceDefinition;
      }
    }

    // add it, but it will be marked red.
    if (projectFrameworkInstance == null && frameworkInstanceName != null) {
      projectFrameworkInstance = new FrameworkInstanceDefinition();
      projectFrameworkInstance.setName(frameworkInstanceName);
      projectFrameworkInstance.setDefined(false);
      frameworkInstance.addItem(projectFrameworkInstance);
    }
    frameworkInstance.setSelectedItem(projectFrameworkInstance);
  }

  public boolean isModified() {
    return myModified;
  }

  public void frameworkInstancesChanged() {
    boolean modified = myModified;
    refreshFrameworkInstanceCombobox();
    myModified = modified;
  }
}
