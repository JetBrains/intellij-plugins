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
package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;

import javax.swing.*;

public class PerforceConfigurable implements Configurable {

  private ConfigPanel myPanel = null;
  private final Project myProject;


  public PerforceConfigurable(Project project) {
    myProject = project;
  }

  @Override
  public String getDisplayName() {
    return PerforceVcs.NAME;
  }

  @Override
  public String getHelpTopic() {
    return "project.propVCSSupport.VCSs.Perforce";
  }

  @Override
  public JComponent createComponent() {
    myPanel = new ConfigPanel(myProject);
    return myPanel.getPanel();
  }

  @Override
  public boolean isModified() {
    if (myPanel == null) {
      return false;
    }
    return !myPanel.equalsToSettings(getSettings());
  }

  @Override
  public void apply() {
    if (myPanel != null) {
      final PerforceSettings settings = getSettings();
      boolean wasEnabled = settings.ENABLED;
      myPanel.applyTo(settings);
      PerforceConnectionManager.getInstance(myProject).updateConnections();
      if (settings.ENABLED != wasEnabled) {
        VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
      }
    }
  }

  private PerforceSettings getSettings() {
    return PerforceSettings.getSettings(myProject);
  }

  @Override
  public void reset() {
    if (myPanel != null) {
      myPanel.resetFrom(getSettings());
    }
  }

  @Override
  public void disposeUIResources() {
    myPanel = null;
  }

}
