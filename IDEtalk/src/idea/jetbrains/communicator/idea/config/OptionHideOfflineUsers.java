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
package jetbrains.communicator.idea.config;

import com.intellij.openapi.actionSystem.AnActionEvent;
import jetbrains.communicator.OptionFlag;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.users.SettingsChanged;
import jetbrains.communicator.util.StringUtil;

/**
 * @author Kir
 */
public class OptionHideOfflineUsers extends OptionExpandToolWindow1 {
  public OptionHideOfflineUsers() {
    super(OptionFlag.OPTION_HIDE_OFFLINE_USERS);
  }

  public void update(final AnActionEvent e) {
    super.update(e);
    e.getPresentation().setText(StringUtil.getMsg("hide.offline.users"));
    e.getPresentation().setDescription(StringUtil.getMsg("hide.offline.users.description"));
  }

  public void setSelected(AnActionEvent e, boolean state) {
    super.setSelected(e, state);
    Pico.getEventBroadcaster().fireEvent(new SettingsChanged());
  }
}
