/*
 * Copyright 2000-2005 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PerforceIntegratePanel implements PerforcePanel{
  private JPanel myParticularConnectionsPanel;
  private JPanel myPanel;

  private JCheckBox myRunResolve;
  private JCheckBox myRevertUnchanged;

  private final List<ParticularConnectionPerforceIntegratePanel> myConnectionPanels = new ArrayList<>();

  public PerforceIntegratePanel(Project project, List<? extends P4Connection> connections) {
    myParticularConnectionsPanel.setLayout(new GridLayout(0, 1));
    for (P4Connection p4Connection : connections) {
      final ParticularConnectionPerforceIntegratePanel panel = new ParticularConnectionPerforceIntegratePanel(project, p4Connection);
      myConnectionPanels.add(panel);
      myParticularConnectionsPanel.add(panel.getPanel());
    }

  }

  @Override
  public void updateFrom(PerforceSettings settings) {
    myRevertUnchanged.setSelected(settings.INTEGRATE_REVERT_UNCHANGED);
    myRunResolve.setSelected(settings.INTEGRATE_RUN_RESOLVE);
    for (ParticularConnectionPerforceIntegratePanel panel : myConnectionPanels) {
      panel.updateFrom(settings);
    }
  }

  @Override
  public void applyTo(PerforceSettings settings) throws ConfigurationException {
    settings.INTEGRATE_REVERT_UNCHANGED = myRevertUnchanged.isSelected();
    settings.INTEGRATE_RUN_RESOLVE = myRunResolve.isSelected();
    for (ParticularConnectionPerforceIntegratePanel panel : myConnectionPanels) {
      panel.applyTo(settings);
    }
  }

  @Override
  public void cancel(PerforceSettings settings) {
    if (!isModified(settings))
      return;

    settings.INTEGRATE_REVERT_UNCHANGED = myRevertUnchanged.isSelected();
    settings.INTEGRATE_RUN_RESOLVE = myRunResolve.isSelected();
    for (ParticularConnectionPerforceIntegratePanel panel : myConnectionPanels) {
      panel.cancel(settings);
    }
  }

  @Override
  public boolean isModified(PerforceSettings settings) {
    for (ParticularConnectionPerforceIntegratePanel panel : myConnectionPanels) {
      if (panel.isModified(settings)) {
        return true;
      }
    }
    return
      settings.INTEGRATE_REVERT_UNCHANGED != myRevertUnchanged.isSelected() ||
      settings.INTEGRATE_RUN_RESOLVE != myRunResolve.isSelected();
  }

  @Override
  public JPanel getPanel() {
    return myPanel;
  }
}
