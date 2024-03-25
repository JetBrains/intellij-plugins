// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.jetbrains.lang.dart.ide.toolingDaemon.DartToolingDaemonService;
import com.jetbrains.lang.dart.pubServer.PubServerManager;
import org.jetbrains.annotations.NotNull;

public class DtdUriCopyAction extends AnAction {
  @Override
  public void update(@NotNull final AnActionEvent e) {
    e.getPresentation().setEnabled(e.getProject() != null && DartToolingDaemonService.getInstance(e.getProject()).getWebSocketReady());
  }


  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    if (e.getProject() != null) {
      final String uri = DartToolingDaemonService.getInstance(e.getProject()).getUri();
      if (uri != null) {
        CopyPasteManager.copyTextToClipboard(uri);
      }
    }
  }
}
