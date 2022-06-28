// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.pubServer;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.jetbrains.lang.dart.DartBundle;
import org.jetbrains.annotations.NotNull;

public class StopDartWebdevServerAction extends DumbAwareAction {

  public StopDartWebdevServerAction() {
    //noinspection DialogTitleCapitalization
    super(DartBundle.messagePointer("stop.dart.webdev.server"),
          DartBundle.messagePointer("stop.dart.webdev.server"),
          AllIcons.Actions.Suspend);
  }

  @Override
  public void update(@NotNull final AnActionEvent e) {
    e.getPresentation().setEnabled(e.getProject() != null && PubServerManager.getInstance(e.getProject()).hasAlivePubServerProcesses());
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  @Override
  public void actionPerformed(@NotNull final AnActionEvent e) {
    if (e.getProject() != null) {
      PubServerManager.getInstance(e.getProject()).stopAllPubServerProcesses();
    }
  }
}
