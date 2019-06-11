// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea.config;

import com.intellij.openapi.actionSystem.AnActionEvent;
import jetbrains.communicator.OptionFlag;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.users.SettingsChanged;
import jetbrains.communicator.util.CommunicatorStrings;
import org.jetbrains.annotations.NotNull;

/**
 * @author Kir
 */
public class OptionHideOfflineUsers extends OptionExpandToolWindow1 {
  public OptionHideOfflineUsers() {
    super(OptionFlag.OPTION_HIDE_OFFLINE_USERS);
  }

  @Override
  public void update(@NotNull final AnActionEvent e) {
    super.update(e);
    e.getPresentation().setText(CommunicatorStrings.getMsg("hide.offline.users"));
    e.getPresentation().setDescription(CommunicatorStrings.getMsg("hide.offline.users.description"));
  }

  @Override
  public void setSelected(@NotNull AnActionEvent e, boolean state) {
    super.setSelected(e, state);
    Pico.getEventBroadcaster().fireEvent(new SettingsChanged());
  }
}
