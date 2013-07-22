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
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.FrameworkIntegratorRegistry;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

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
        })
        .setRemoveAction(new AnActionButtonRunnable() {
          @Override
          public void run(AnActionButton button) {
            removeFrameworkInstance();
          }
        })
        .setEditAction(new AnActionButtonRunnable() {
          @Override
          public void run(AnActionButton button) {
            editFrameworkInstance();
          }
        })
        .disableUpDownActions().createPanel(), BorderLayout.CENTER);

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
    CreateFrameworkInstanceDialog dialog = new CreateFrameworkInstanceDialog(myFrameworkIntegratorRegistry, null);
    dialog.pack();
    dialog.show();

    if (dialog.isOK()) {
      FrameworkInstanceDefinition instanceDefinition = dialog.createDefinition();
      //noinspection unchecked
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
    FrameworkInstanceDefinition selectedFrameworkInstance = this.mySelectedFrameworkInstance;
    if (selectedFrameworkInstance != null) {
      CreateFrameworkInstanceDialog dialog = new CreateFrameworkInstanceDialog(myFrameworkIntegratorRegistry, selectedFrameworkInstance);
      dialog.pack();
      dialog.show();

      if (dialog.isOK()) {
        int index = myModel.indexOf(mySelectedFrameworkInstance);
        myModel.removeElement(mySelectedFrameworkInstance);
        selectedFrameworkInstance = dialog.createDefinition();
        //noinspection unchecked
        myModel.add(index, selectedFrameworkInstance);
        myModified = true;
        myFrameworkInstances.setSelectedValue(selectedFrameworkInstance, true);
      }
    }
  }

  public JPanel getMainPanel() {
    return mainPanel;
  }

  public void resetTo(@NotNull ApplicationSettings settings) {
    myModel.clear();
    for (FrameworkInstanceDefinition frameworkInstanceDefinition : settings.getFrameworkInstanceDefinitions()) {
      //noinspection unchecked
      myModel.addElement(frameworkInstanceDefinition);
    }
    myModified = false;
  }

  public void applyTo(@NotNull ApplicationSettings settings) {
    int instances = myModel.getSize();
    List<FrameworkInstanceDefinition> definitions = new ArrayList<FrameworkInstanceDefinition>(instances);
    for (int i = 0; i < instances; i++) {
      definitions.add((FrameworkInstanceDefinition)myModel.getElementAt(i));
    }
    settings.setFrameworkInstanceDefinitions(definitions);
    myModified = false;
  }

  public boolean isModified() {
    return myModified;
  }
}
