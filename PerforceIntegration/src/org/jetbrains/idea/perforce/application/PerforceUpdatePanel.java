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
import org.jetbrains.idea.perforce.perforce.PerforceSettings;

import javax.swing.*;

public class PerforceUpdatePanel implements PerforcePanel {
  private JCheckBox myForceSync;
  private JCheckBox myAutomaticallyResolve;
  private JPanel myPanel;
  private JCheckBox myRevertUnchanged;

  @Override
  public void updateFrom(PerforceSettings settings) {
    myForceSync.setSelected(settings.SYNC_FORCE);
    myAutomaticallyResolve.setSelected(settings.SYNC_RUN_RESOLVE);
    myRevertUnchanged.setSelected(settings.REVERT_UNCHANGED_FILES);
  }

  @Override
  public void applyTo(PerforceSettings settings) throws ConfigurationException {
    settings.SYNC_FORCE = myForceSync.isSelected();
    settings.SYNC_RUN_RESOLVE = myAutomaticallyResolve.isSelected();
    settings.REVERT_UNCHANGED_FILES = myRevertUnchanged.isSelected();
  }

  @Override
  public boolean isModified(PerforceSettings settings) {
    if (myAutomaticallyResolve.isSelected() != settings.SYNC_RUN_RESOLVE) {
      return true;
    }

    if (myRevertUnchanged.isSelected() != settings.REVERT_UNCHANGED_FILES){
      return true;
    }

    if (myForceSync.isSelected() != settings.SYNC_FORCE) {
      return true;
    }

    return false;
  }

  @Override
  public JPanel getPanel(){
    return myPanel;
  }
}
