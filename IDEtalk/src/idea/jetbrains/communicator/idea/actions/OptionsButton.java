/*
 * Copyright 2000-2006 JetBrains s.r.o.
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
package jetbrains.communicator.idea.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import jetbrains.communicator.core.Pico;
import org.apache.log4j.Logger;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * @author Kir
 */
public class OptionsButton extends DropDownButton {
  private static final Logger LOG = Logger.getLogger(OptionsButton.class);

  private AnAction myResetSettingsAction;

  public OptionsButton() {
    super(getOptionsActionGroup(), AllIcons.General.Settings);
  }

  public void addNotify() {
    super.addNotify();

    if (Pico.isLocalTesting()) {
      addResetPreferencesActionTo(getOptionsActionGroup());
    }
  }

  public void removeNotify() {
    if (Pico.isLocalTesting()) {
      getOptionsActionGroup().remove(myResetSettingsAction);
    }
    
    super.removeNotify();
  }

  private void addResetPreferencesActionTo(DefaultActionGroup actionGroup) {
    myResetSettingsAction = new AnAction("Reset to default settings") {
      public void actionPerformed(AnActionEvent e) {
        try {
          Preferences preferences = Preferences.userRoot().node("jetbrains.communicator");
          preferences.removeNode();
          preferences.flush();
        } catch (BackingStoreException e1) {
          LOG.error(e1.getMessage(), e1);
        }
      }
    };
    actionGroup.add(myResetSettingsAction);
  }

  private static DefaultActionGroup getOptionsActionGroup() {
    return (DefaultActionGroup) ActionManager.getInstance().getAction("IDEtalk.OptionsGroup");
  }
}
