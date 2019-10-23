// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;
import jetbrains.communicator.core.Pico;
import org.jetbrains.annotations.NotNull;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * @author Kir
 */
public class OptionsButton extends DropDownButton {
  private static final Logger LOG = Logger.getInstance(OptionsButton.class);

  private AnAction myResetSettingsAction;

  public OptionsButton() {
    super(getOptionsActionGroup(), AllIcons.General.Settings);
  }

  @Override
  public void addNotify() {
    super.addNotify();

    if (Pico.isLocalTesting()) {
      addResetPreferencesActionTo(getOptionsActionGroup());
    }
  }

  @Override
  public void removeNotify() {
    if (Pico.isLocalTesting()) {
      getOptionsActionGroup().remove(myResetSettingsAction);
    }

    super.removeNotify();
  }

  private void addResetPreferencesActionTo(DefaultActionGroup actionGroup) {
    myResetSettingsAction = new AnAction("Reset to default settings") {
      @Override
      public void actionPerformed(@NotNull AnActionEvent e) {
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
