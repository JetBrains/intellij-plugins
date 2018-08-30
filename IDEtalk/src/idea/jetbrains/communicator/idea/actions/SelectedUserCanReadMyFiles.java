// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import jetbrains.communicator.commands.ToggleFileAccessCommand;
import jetbrains.communicator.core.Pico;
import org.jetbrains.annotations.NotNull;

/**
 * @author Kir
 */
public class SelectedUserCanReadMyFiles extends ToggleAction {

  public SelectedUserCanReadMyFiles() {
  }

  @Override
  public boolean isSelected(@NotNull AnActionEvent anActionEvent) {
    ToggleFileAccessCommand command = getCommand(anActionEvent);
    return command != null && command.isSelected();
  }

  @Override
  public void setSelected(@NotNull AnActionEvent anActionEvent, boolean b) {
    getCommand(anActionEvent).execute();
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    super.update(e);
    if (e.getProject() == null) {
      e.getPresentation().setEnabled(false);
      return;
    }
    ToggleFileAccessCommand command = getCommand(e);
    boolean enabled = command.isEnabled();
    e.getPresentation().setEnabled(enabled);
    e.getPresentation().setText(command.getText());
  }

  private static ToggleFileAccessCommand getCommand(AnActionEvent e) {
    return Pico.getCommandManager().getCommand(ToggleFileAccessCommand.class, BaseAction.getContainer(e));
  }
}
