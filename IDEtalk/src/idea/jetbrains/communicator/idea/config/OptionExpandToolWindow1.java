// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea.config;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import jetbrains.communicator.OptionFlag;
import org.jetbrains.annotations.NotNull;

/**
 * @author Kir
 */
public class OptionExpandToolWindow1 extends ToggleAction {
  private final OptionFlag myFlag;

  public OptionExpandToolWindow1(OptionFlag property) {
    myFlag = property;
  }

  @Override
  public boolean isSelected(@NotNull AnActionEvent e) {
    return myFlag.isSet();
  }

  @Override
  public void setSelected(@NotNull AnActionEvent e, boolean state) {
    myFlag.change(state);
  }
}
