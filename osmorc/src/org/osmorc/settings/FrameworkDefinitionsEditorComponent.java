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

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.*;
import com.intellij.ui.components.JBList;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.FrameworkIntegrator;
import org.osmorc.frameworkintegration.FrameworkIntegratorRegistry;
import org.osmorc.i18n.OsmorcBundle;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class FrameworkDefinitionsEditorComponent {
  private JPanel myMainPanel;
  private JBList myFrameworkInstances;
  private JLabel myFrameworkIntegrator;
  private JLabel myHomeDir;
  private JLabel myFrameworkInstanceName;
  private JLabel myVersion;
  private JPanel myFrameworkInstancesPanel;
  private boolean myModified;
  private DefaultListModel myModel;

  public FrameworkDefinitionsEditorComponent() {
    myModel = new DefaultListModel();
    myFrameworkInstances = new JBList(myModel);
    myFrameworkInstances.getEmptyText().setText(OsmorcBundle.message("frameworks.empty"));
    myFrameworkInstances.setCellRenderer(new ColoredListCellRenderer() {
      @Override
      protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
        FrameworkInstanceDefinition instance = (FrameworkInstanceDefinition)value;
        append(instance.getName());
        String version = instance.getVersion();
        if (StringUtil.isEmptyOrSpaces(version)) version = "(unknown)";
        append(" [" + instance.getFrameworkIntegratorName() + ", " + version + "]", SimpleTextAttributes.GRAY_ATTRIBUTES);
      }
    });

    final List<AddAction> addActions = ContainerUtil.newArrayList();
    for (FrameworkIntegrator integrator : FrameworkIntegratorRegistry.getInstance().getFrameworkIntegrators()) {
      addActions.add(new AddAction(integrator));
    }
    Collections.sort(addActions);

    myFrameworkInstancesPanel.add(
      ToolbarDecorator.createDecorator(myFrameworkInstances)
        .setAddAction(new AnActionButtonRunnable() {
          @Override
          public void run(AnActionButton button) {
            JBPopupFactory.getInstance().createActionGroupPopup(
              OsmorcBundle.message("frameworks.add.title"),
              new DefaultActionGroup(addActions),
              DataManager.getInstance().getDataContext(button.getContextComponent()),
              false, false, false, null, -1, null
            ).show(button.getPreferredPopupPoint());
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
        .createPanel(), BorderLayout.CENTER
    );

    myFrameworkInstances.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        int index = myFrameworkInstances.getSelectedIndex();
        if (index != -1) {
          FrameworkInstanceDefinition instance = (FrameworkInstanceDefinition)myModel.get(index);
          myFrameworkIntegrator.setText(instance.getFrameworkIntegratorName());
          myHomeDir.setText(instance.getBaseFolder());
          myVersion.setText(ObjectUtils.notNull(instance.getVersion(), ""));
          myFrameworkInstanceName.setText(instance.getName());
        }
      }
    });

    new DoubleClickListener() {
      @Override
      protected boolean onDoubleClick(MouseEvent e) {
        if (myFrameworkInstances.getSelectedIndex() != -1) {
          editFrameworkInstance();
          return true;
        }
        return false;
      }
    }.installOn(myFrameworkInstances);
  }

  private void addFrameworkInstance(FrameworkIntegrator integrator) {
    FrameworkInstanceDefinition instance = new FrameworkInstanceDefinition();
    instance.setFrameworkIntegratorName(integrator.getDisplayName());
    CreateFrameworkInstanceDialog dialog = new CreateFrameworkInstanceDialog(instance, myModel);
    dialog.pack();
    dialog.show();
    if (dialog.isOK()) {
      instance = dialog.createDefinition();
      //noinspection unchecked
      myModel.addElement(instance);
      myFrameworkInstances.setSelectedIndex(myModel.getSize() - 1);
      myModified = true;
    }
  }

  private void removeFrameworkInstance() {
    int index = myFrameworkInstances.getSelectedIndex();
    if (index != -1) {
      myModel.remove(index);
      myFrameworkInstances.setSelectedIndex(0);
      myModified = true;
    }
  }

  private void editFrameworkInstance() {
    int index = myFrameworkInstances.getSelectedIndex();
    if (index != -1) {
      FrameworkInstanceDefinition instance = (FrameworkInstanceDefinition)myModel.get(index);
      CreateFrameworkInstanceDialog dialog = new CreateFrameworkInstanceDialog(instance, myModel);
      dialog.pack();
      dialog.show();
      if (dialog.isOK()) {
        instance = dialog.createDefinition();
        //noinspection unchecked
        myModel.set(index, instance);
        myModified = true;
      }
    }
  }

  public JPanel getMainPanel() {
    return myMainPanel;
  }

  public void resetTo(@NotNull ApplicationSettings settings) {
    myModel.clear();
    for (FrameworkInstanceDefinition instance : settings.getFrameworkInstanceDefinitions()) {
      //noinspection unchecked
      myModel.addElement(instance);
    }
    myModified = false;
  }

  public void applyTo(@NotNull ApplicationSettings settings) {
    int instances = myModel.getSize();
    List<FrameworkInstanceDefinition> definitions = new ArrayList<FrameworkInstanceDefinition>(instances);
    for (int i = 0; i < instances; i++) {
      definitions.add((FrameworkInstanceDefinition)myModel.get(i));
    }
    settings.setFrameworkInstanceDefinitions(definitions);
    myModified = false;
  }

  public boolean isModified() {
    return myModified;
  }


  private class AddAction extends AnAction implements DumbAware, Comparable<AddAction> {
    private final FrameworkIntegrator myIntegrator;

    private AddAction(FrameworkIntegrator integrator) {
      super(integrator.getDisplayName());
      myIntegrator = integrator;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
      addFrameworkInstance(myIntegrator);
    }

    @Override
    public int compareTo(@NotNull AddAction o) {
      return myIntegrator.getDisplayName().compareTo(o.myIntegrator.getDisplayName());
    }
  }
}
