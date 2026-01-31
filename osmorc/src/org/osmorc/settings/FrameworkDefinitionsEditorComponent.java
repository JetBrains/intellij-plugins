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
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Pair;
import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.LayeredIcon;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.util.ObjectUtils;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.FrameworkIntegrator;
import org.osmorc.frameworkintegration.FrameworkIntegratorRegistry;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.util.FrameworkInstanceRenderer;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
public class FrameworkDefinitionsEditorComponent {
  private JPanel myMainPanel;
  private JPanel myFrameworkInstancesPanel;
  private JLabel myFrameworkIntegrator;
  private JLabel myHomeDir;
  private JLabel myFrameworkInstanceName;
  private JLabel myVersion;
  private JPanel myContentPanel;

  private final DefaultListModel<FrameworkInstanceDefinition> myModel;
  private final JBList<FrameworkInstanceDefinition> myFrameworkInstances;
  private final MessageBus myBus;
  private final List<Pair<FrameworkInstanceDefinition, FrameworkInstanceDefinition>> myModified;

  public FrameworkDefinitionsEditorComponent() {
    myModel = new DefaultListModel<>();
    myBus = ApplicationManager.getApplication().getMessageBus();
    myModified = new ArrayList<>();

    myContentPanel.setBorder(IdeBorderFactory.createTitledBorder(OsmorcBundle.message("frameworks.title"), false, JBUI.insetsTop(8)).setShowLine(false));

    myFrameworkInstances = new JBList<>(myModel);
    myFrameworkInstances.getEmptyText().setText(OsmorcBundle.message("frameworks.empty"));
    myFrameworkInstances.setCellRenderer(new FrameworkInstanceRenderer());

    List<AddAction> addActions = FrameworkIntegratorRegistry.getInstance().getFrameworkIntegrators().stream()
      .map(AddAction::new)
      .sorted()
      .collect(Collectors.toList());

    myFrameworkInstancesPanel.add(
      ToolbarDecorator.createDecorator(myFrameworkInstances)
        .setAddAction((b) -> JBPopupFactory.getInstance().createActionGroupPopup(
          OsmorcBundle.message("frameworks.add.title"),
          new DefaultActionGroup(addActions),
          DataManager.getInstance().getDataContext(b.getContextComponent()),
          false, false, false, null, -1, null
        ).show(Objects.requireNonNull(b.getPreferredPopupPoint()))
        )
        .setAddIcon(LayeredIcon.ADD_WITH_DROPDOWN)
        .setRemoveAction((b) -> removeFrameworkInstance())
        .setEditAction((b) -> editFrameworkInstance())
        .createPanel(), BorderLayout.CENTER
    );

    myFrameworkInstances.addListSelectionListener((ListSelectionEvent e) -> {
      int index = myFrameworkInstances.getSelectedIndex();
      if (index != -1) {
        FrameworkInstanceDefinition instance = myModel.get(index);
        myFrameworkIntegrator.setText(instance.getFrameworkIntegratorName());
        myHomeDir.setText(instance.getBaseFolder());
        myVersion.setText(ObjectUtils.notNull(instance.getVersion(), ""));
        myFrameworkInstanceName.setText(instance.getName());
      }
    });

    new DoubleClickListener() {
      @Override
      protected boolean onDoubleClick(@NotNull MouseEvent e) {
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
    if (dialog.showAndGet()) {
      instance = dialog.createDefinition();
      myModel.addElement(instance);
      myFrameworkInstances.setSelectedIndex(myModel.getSize() - 1);
      myModified.add(Pair.create(null, instance));
    }
  }

  private void removeFrameworkInstance() {
    int index = myFrameworkInstances.getSelectedIndex();
    if (index != -1) {
      FrameworkInstanceDefinition instance = myModel.get(index);
      myModel.remove(index);
      myFrameworkInstances.setSelectedIndex(0);
      myModified.add(Pair.create(instance, null));
    }
  }

  private void editFrameworkInstance() {
    int index = myFrameworkInstances.getSelectedIndex();
    if (index != -1) {
      FrameworkInstanceDefinition instance = myModel.get(index);
      CreateFrameworkInstanceDialog dialog = new CreateFrameworkInstanceDialog(instance, myModel);
      dialog.pack();
      if (dialog.showAndGet()) {
        FrameworkInstanceDefinition newInstance = dialog.createDefinition();
        myModel.set(index, newInstance);
        myModified.add(Pair.create(instance, newInstance));
      }
    }
  }

  public JPanel getMainPanel() {
    return myMainPanel;
  }

  public void resetTo(@NotNull ApplicationSettings settings) {
    myModel.clear();
    for (FrameworkInstanceDefinition instance : settings.getActiveFrameworkInstanceDefinitions()) {
      myModel.addElement(instance);
    }
    myModified.clear();
  }

  public void applyTo(@NotNull ApplicationSettings settings) {
    int instances = myModel.getSize();
    List<FrameworkInstanceDefinition> definitions = new ArrayList<>(instances);
    for (int i = 0; i < instances; i++) {
      definitions.add(myModel.get(i));
    }
    settings.setFrameworkInstanceDefinitions(definitions);

    myBus.syncPublisher(FrameworkDefinitionListener.TOPIC).definitionsChanged(myModified);
    myModified.clear();
  }

  public boolean isModified() {
    return !myModified.isEmpty();
  }


  private final class AddAction extends AnAction implements DumbAware, Comparable<AddAction> {
    private final FrameworkIntegrator myIntegrator;

    AddAction(FrameworkIntegrator integrator) {
      super(integrator.getDisplayName());
      myIntegrator = integrator;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
      addFrameworkInstance(myIntegrator);
    }

    @Override
    public int compareTo(@NotNull AddAction o) {
      return myIntegrator.getDisplayName().compareTo(o.myIntegrator.getDisplayName());
    }
  }
}
