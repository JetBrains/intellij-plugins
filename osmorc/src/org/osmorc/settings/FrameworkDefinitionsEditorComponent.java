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

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.*;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.Nullable;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.FrameworkInstanceManager;
import org.osmorc.frameworkintegration.FrameworkIntegrator;
import org.osmorc.frameworkintegration.FrameworkIntegratorRegistry;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class FrameworkDefinitionsEditorComponent {
  private JPanel mainPanel;
  private JBList myFrameworkInstances;
  private JLabel myFrameworkIntegrator;
  private JLabel myBaseFolder;
  private JLabel myFrameworkInstanceName;
  private JLabel myVersion;
  private JPanel myFrameworkInstancesPanel;
  private FrameworkIntegratorRegistry myFrameworkIntegratorRegistry;
  private FrameworkInstanceDefinition mySelectedFrameworkInstance;
  private boolean myModified;
  private DefaultListModel myModel;

  public FrameworkDefinitionsEditorComponent(FrameworkIntegratorRegistry frameworkIntegratorRegistry) {
    myFrameworkIntegratorRegistry = frameworkIntegratorRegistry;
    myModel = new DefaultListModel();
    myFrameworkInstances = new JBList(myModel);
    myFrameworkInstances.getEmptyText().setText("No frameworks configured");

    myFrameworkInstancesPanel.add(
      ToolbarDecorator.createDecorator(myFrameworkInstances)
      .setAddAction(new AnActionButtonRunnable() {
        @Override
        public void run(AnActionButton button) {
          addFrameworkInstance();
        }
      }).setRemoveAction(new AnActionButtonRunnable() {
        @Override
        public void run(AnActionButton button) {
          removeFrameworkInstance();
        }
      }).setEditAction(new AnActionButtonRunnable() {
        @Override
        public void run(AnActionButton button) {
          editFrameworkInstance();
        }
      }).disableUpDownActions().createPanel(), BorderLayout.CENTER);

    ToolbarDecorator.findEditButton(myFrameworkInstancesPanel).addCustomUpdater(new AnActionButtonUpdater() {
      @Override
      public boolean isEnabled(AnActionEvent e) {
        return mySelectedFrameworkInstance != null;
      }
    });

    myFrameworkInstances.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        mySelectedFrameworkInstance = (FrameworkInstanceDefinition)myFrameworkInstances.getSelectedValue();

        if (mySelectedFrameworkInstance != null) {
          myFrameworkIntegrator.setText(mySelectedFrameworkInstance.getFrameworkIntegratorName());
          myBaseFolder.setText(mySelectedFrameworkInstance.getBaseFolder());
          final String theVersion = mySelectedFrameworkInstance.getVersion();
          myVersion.setText(theVersion != null && theVersion.length() > 0 ? theVersion : "latest");
          myFrameworkInstanceName.setText(mySelectedFrameworkInstance.getName());
        }
      }
    });

    new DoubleClickListener() {
      @Override
      protected boolean onDoubleClick(MouseEvent e) {
        if (mySelectedFrameworkInstance != null) {
          editFrameworkInstance();
          return true;
        }
        return false;
      }
    }.installOn(myFrameworkInstances);
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

    CreateFrameworkInstanceDialog dialog = new CreateFrameworkInstanceDialog(myFrameworkIntegratorRegistry, frameworkInstanceNameForCreation);
    dialog.pack();
    dialog.show();

    if (dialog.isOK()) {
      FrameworkInstanceDefinition instanceDefinition = new FrameworkInstanceDefinition();
      instanceDefinition.setName(dialog.getName());
      instanceDefinition.setFrameworkIntegratorName(dialog.getIntegratorName());
      instanceDefinition.setBaseFolder(dialog.getBaseFolder());
      instanceDefinition.setVersion(dialog.getVersion());
      myModel.addElement(instanceDefinition);
      myModified = true;
      myFrameworkInstances.setSelectedValue(instanceDefinition, true);
    }
  }


  private void removeFrameworkInstance() {
    FrameworkInstanceDefinition selectedFrameworkInstance = this.mySelectedFrameworkInstance;
    if (selectedFrameworkInstance != null) {
      myModified = true;
      myModel.removeElement(selectedFrameworkInstance);
      myFrameworkInstances.setSelectedIndex(0);
    }
  }

  private void editFrameworkInstance() {
    FrameworkInstanceDefinition frameworkInstanceDefinition = mySelectedFrameworkInstance;
    if (frameworkInstanceDefinition == null) {
      return; // usually should not happen, but you never know.
    }

    CreateFrameworkInstanceDialog dialog =
      new CreateFrameworkInstanceDialog(myFrameworkIntegratorRegistry, frameworkInstanceDefinition.getName());
    dialog.setIntegratorName(frameworkInstanceDefinition.getFrameworkIntegratorName());
    dialog.setBaseFolder(frameworkInstanceDefinition.getBaseFolder());
    dialog.setVersion(frameworkInstanceDefinition.getVersion());
    dialog.pack();
    dialog.show();

    if (dialog.isOK()) {
      int index = myModel.indexOf(mySelectedFrameworkInstance);
      myModel.removeElement(mySelectedFrameworkInstance);

      frameworkInstanceDefinition = new FrameworkInstanceDefinition();
      // set new properties
      frameworkInstanceDefinition.setName(dialog.getName());
      frameworkInstanceDefinition.setFrameworkIntegratorName(dialog.getIntegratorName());
      frameworkInstanceDefinition.setBaseFolder(dialog.getBaseFolder());
      frameworkInstanceDefinition.setVersion(dialog.getVersion());
      myModel.add(index, frameworkInstanceDefinition);
      // fire settings change.
      myModified = true;
      myFrameworkInstances.setSelectedValue(frameworkInstanceDefinition, true);
    }
  }

  @Nullable
  private FrameworkInstanceManager getFrameworkInstanceManager(FrameworkInstanceDefinition instanceDefinition) {
    FrameworkIntegrator frameworkIntegrator = myFrameworkIntegratorRegistry.findIntegratorByInstanceDefinition(instanceDefinition);
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
      definitions.add((FrameworkInstanceDefinition)myFrameworkInstances.getModel().getElementAt(i));
    }

    settings.setFrameworkInstanceDefinitions(definitions);
    myModified = false;
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
