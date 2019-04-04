// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea.messagesWindow;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import jetbrains.communicator.idea.IDEtalkMessagesWindow;
import org.jetbrains.annotations.NotNull;

/**
 * @author Kir
 */
public class CloseAction extends AnAction {
  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getData(CommonDataKeys.PROJECT);
    doHideToolwindow(project);
  }

  public static void doHideToolwindow(Project project) {
    if (project != null) {
      project.getComponent(IDEtalkMessagesWindow.class).removeToolWindow();
    }
  }
}
