// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.runner.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.server.vmService.DartVmServiceDebugProcess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartReloadSourcesAction extends AnAction implements DumbAware {

  public DartReloadSourcesAction() {
    Presentation presentation = getTemplatePresentation();
    presentation.setText(DartBundle.message("dart.reload.sources.action.text"));
    presentation.setDescription(DartBundle.message("dart.reload.sources.action.description"));
    presentation.setIcon(AllIcons.Actions.Lightning);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    DartVmServiceDebugProcess process = getProcess(e);
    if (process != null && process.getCurrentIsolateId() != null) {
      FileDocumentManager.getInstance().saveAllDocuments();
      process.reloadSources();
    }
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    DartVmServiceDebugProcess process = getProcess(e);
    String isolateId = process == null ? null : process.getCurrentIsolateId();
    e.getPresentation().setEnabled(isolateId != null);
  }

  @Nullable
  private static DartVmServiceDebugProcess getProcess(AnActionEvent e) {
    final Project project = e.getProject();
    if (project == null) return null;

    XDebugSession session = e.getData(XDebugSession.DATA_KEY);

    if (session == null) {
      session = XDebuggerManager.getInstance(project).getCurrentSession();
    }

    if (session != null) {
      XDebugProcess process = session.getDebugProcess();
      if (process instanceof DartVmServiceDebugProcess) {
        return ((DartVmServiceDebugProcess) process);
      }
    }

    return null;
  }
}
