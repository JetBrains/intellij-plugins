
// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.runner.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbAware;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.impl.ui.DebuggerUIUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.server.vmService.frame.DartVmServiceStackFrame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartPopFrameAction extends AnAction implements DumbAware {
  public DartPopFrameAction() {
    Presentation presentation = getTemplatePresentation();
    presentation.setText(DartBundle.messagePointer("dart.pop.frame.action.text"));
    presentation.setDescription(DartBundle.messagePointer("dart.pop.frame.action.description"));
    presentation.setIcon(AllIcons.Actions.PopFrame);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    DartVmServiceStackFrame frame = getStackFrame(e);
    if (frame != null) {
      frame.dropFrame();
    }
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    DartVmServiceStackFrame frame = getStackFrame(e);
    boolean enabled = frame != null && frame.canDrop();

    if (ActionPlaces.isMainMenuOrActionSearch(e.getPlace()) || ActionPlaces.DEBUGGER_TOOLBAR.equals(e.getPlace())) {
      e.getPresentation().setEnabled(enabled);
    }
    else {
      e.getPresentation().setVisible(enabled);
    }
  }

  private static @Nullable DartVmServiceStackFrame getStackFrame(final @NotNull AnActionEvent e) {
    XDebugSession session = DebuggerUIUtil.getSession(e);
    if (session != null) {
      XStackFrame frame = session.getCurrentStackFrame();
      if (frame instanceof DartVmServiceStackFrame) {
        return ((DartVmServiceStackFrame)frame);
      }
    }

    return null;
  }
}
