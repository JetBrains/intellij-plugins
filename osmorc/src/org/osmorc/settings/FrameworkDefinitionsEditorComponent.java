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

import org.jetbrains.annotations.Nullable;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.FrameworkInstanceManager;
import org.osmorc.frameworkintegration.FrameworkIntegrator;
import org.osmorc.frameworkintegration.FrameworkIntegratorRegistry;

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
public class FrameworkDefinitionsEditorComponent {
  private JPanel mainPanel;
  private JList frameworkInstances;
  private JButton addFramework;
  private JButton removeFramework;
  private JLabel frameworkIntegrator;
  private JLabel baseFolder;
  private JLabel frameworkInstanceName;
  private JButton editFramework;
  private FrameworkIntegratorRegistry frameworkIntegratorRegistry;
  private FrameworkInstanceDefinition selectedFrameworkInstance;
  private boolean myModified;
  private DefaultListModel myModel;

  public FrameworkDefinitionsEditorComponent(FrameworkIntegratorRegistry frameworkIntegratorRegistry) {
    this.frameworkIntegratorRegistry = frameworkIntegratorRegistry;
    this.myModel = new DefaultListModel();
    frameworkInstances.setModel(this.myModel);

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
        selectedFrameworkInstance = (FrameworkInstanceDefinition)frameworkInstances.getSelectedValue();

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
        if (selectedFrameworkInstance != null && e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
          // edit on doubleclick
          editFrameworkInstance();
        }
      }
    });
  }

  private void addFrameworkInstance() {
    //String selectedFrameworkInstanceForProject = getProjectSettingsWorkingCopy().getFrameworkInstanceName();
    //FrameworkInstanceDefinition frameworkInstanceDefinition =
    //        getApplicationSettingsWorkingCopy().getFrameworkInstance(selectedFrameworkInstanceForProject);
    //
    String frameworkInstanceNameForCreation = null;
    //if (frameworkInstanceDefinition == null) {
    //    frameworkInstanceNameForCreation = selectedFrameworkInstanceForProject;
    //}

    CreateFrameworkInstanceDialog dialog = new CreateFrameworkInstanceDialog(frameworkIntegratorRegistry, frameworkInstanceNameForCreation);
    dialog.pack();
    dialog.show();

    if (dialog.isOK()) {
      FrameworkInstanceDefinition instanceDefinition = new FrameworkInstanceDefinition();
      instanceDefinition.setName(dialog.getName());
      instanceDefinition.setFrameworkIntegratorName(dialog.getIntegratorName());
      instanceDefinition.setBaseFolder(dialog.getBaseFolder());

      myModel.addElement(instanceDefinition);
      myModified = true;
      frameworkInstances.setSelectedValue(instanceDefinition, true);
    }
  }


  private void removeFrameworkInstance() {
    FrameworkInstanceDefinition selectedFrameworkInstance = this.selectedFrameworkInstance;
    if (selectedFrameworkInstance != null) {
      myModified = true;
      myModel.removeElement(selectedFrameworkInstance);
      frameworkInstances.setSelectedIndex(0);
    }
  }

  private void editFrameworkInstance() {
    FrameworkInstanceDefinition frameworkInstanceDefinition = selectedFrameworkInstance;
    if (frameworkInstanceDefinition == null) {
      return; // usually should not happen, but you never know.
    }

    CreateFrameworkInstanceDialog dialog =
      new CreateFrameworkInstanceDialog(frameworkIntegratorRegistry, frameworkInstanceDefinition.getName());
    dialog.setIntegratorName(frameworkInstanceDefinition.getFrameworkIntegratorName());
    dialog.setBaseFolder(frameworkInstanceDefinition.getBaseFolder());
    dialog.pack();
    dialog.show();

    if (dialog.isOK()) {
      int index = myModel.indexOf(selectedFrameworkInstance);
      myModel.removeElement(selectedFrameworkInstance);

      frameworkInstanceDefinition = new FrameworkInstanceDefinition();
      // set new properties
      frameworkInstanceDefinition.setName(dialog.getName());
      frameworkInstanceDefinition.setFrameworkIntegratorName(dialog.getIntegratorName());
      frameworkInstanceDefinition.setBaseFolder(dialog.getBaseFolder());
      myModel.add(index, frameworkInstanceDefinition);
      // fire settings change.
      myModified = true;
      frameworkInstances.setSelectedValue(frameworkInstanceDefinition, true);
    }
  }

  @Nullable
  private FrameworkInstanceManager getFrameworkInstanceManager(FrameworkInstanceDefinition instanceDefinition) {
    FrameworkIntegrator frameworkIntegrator = frameworkIntegratorRegistry.findIntegratorByInstanceDefinition(instanceDefinition);
    if (frameworkIntegrator != null) {
      return frameworkIntegrator.getFrameworkInstanceManager();
    }
    else {
      return null;
    }
  }


  public void resetTo(ApplicationSettings settings) {
    myModel.clear();
    for (FrameworkInstanceDefinition frameworkInstanceDefinition : settings.getFrameworkInstanceDefinitions()) {
      myModel.addElement(frameworkInstanceDefinition);
    }
    myModified = false;
  }

  public void applyTo(ApplicationSettings settings) {
    int instances = myModel.getSize();
    ArrayList<FrameworkInstanceDefinition> definitions = new ArrayList<FrameworkInstanceDefinition>(instances);
    for (int i = 0; i < instances; i++) {
      definitions.add((FrameworkInstanceDefinition)frameworkInstances.getModel().getElementAt(i));
    }

    updateLibraries(settings.getFrameworkInstanceDefinitions(), definitions);

    settings.setFrameworkInstanceDefinitions(definitions);
    myModified = false;
  }

  private void updateLibraries(List<FrameworkInstanceDefinition> oldDefinitions, ArrayList<FrameworkInstanceDefinition> newDefinitions) {
    for (FrameworkInstanceDefinition oldDefinition : oldDefinitions) {
      if (!newDefinitions.contains(oldDefinition)) {
        // it was removed
        final FrameworkInstanceManager frameworkInstanceManager = getFrameworkInstanceManager(oldDefinition);
        if (frameworkInstanceManager != null) {
          frameworkInstanceManager.removeLibraries(oldDefinition);
        }
      }
    }

    for (FrameworkInstanceDefinition newDefinition : newDefinitions) {
      if (!oldDefinitions.contains(newDefinition)) {
        // it was added
        final FrameworkInstanceManager frameworkInstanceManager = getFrameworkInstanceManager(newDefinition);
        if (frameworkInstanceManager != null) {
          frameworkInstanceManager.createLibraries(newDefinition);
        }
      }
    }

  }

  public JPanel getMainPanel() {
    return mainPanel;
  }

  public boolean isModified() {
    return myModified;
  }

  public void dispose() {
    
  }
}
