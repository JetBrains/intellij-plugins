// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea.config;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import jetbrains.communicator.util.CommunicatorStrings;
import org.jetbrains.annotations.NotNull;

/**
 * @author Kir
 */
public class EditIDEtalkOptions extends AnAction {

  @Override
  public void update(@NotNull AnActionEvent e) {
    e.getPresentation().setText(CommunicatorStrings.getMsg("more.options"));
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    if (project != null) {
      new IDEtalkConfiguration(project).edit();
    }
  }
}
