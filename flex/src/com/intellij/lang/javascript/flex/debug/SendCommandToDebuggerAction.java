// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.debug;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Maxim.Mossienko
 */
public class SendCommandToDebuggerAction extends AnAction {
  @Override
  public void update(@NotNull final AnActionEvent e) {
    Project project = e.getData(CommonDataKeys.PROJECT);
    if (project == null) return;

    final boolean internal = ApplicationManager.getApplication().isInternal();
    e.getPresentation().setVisible(internal);
    e.getPresentation().setEnabled(internal && XDebuggerManager.getInstance(project).getCurrentSession() != null);
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  @Override
  public void actionPerformed(@NotNull final AnActionEvent e) {
    Project project = e.getData(CommonDataKeys.PROJECT);
    if (project == null) return;

    final XDebugSession xDebugSession = XDebuggerManager.getInstance(project).getCurrentSession();
    new MyDialogWrapper(project, xDebugSession).show();
  }

  static class MyDialogWrapper extends DialogWrapper {
    private final XDebugSession session;
    private JPanel myPanel;
    private JTextArea myResultArea;
    private JTextArea myCommandArea;

    protected MyDialogWrapper(Project project, final XDebugSession xDebugSession) {
      super(project, false);
      session = xDebugSession;
      setOKButtonText("Send");
      setTitle("Send Commands to Flex Debugger");
      setModal(false);
      init();
    }

    @Override
    protected String getDimensionServiceKey() {
      return "flex.debug.send.commands.to.flex.debugger.service.key";
    }

    @Override
    protected void doOKAction() {
      getOKAction().setEnabled(false);
      ((FlexDebugProcess)session.getDebugProcess()).sendCommand(
          new DebuggerCommand(myCommandArea.getText(), CommandOutputProcessingType.SPECIAL_PROCESSING) {
            @Override
            CommandOutputProcessingMode onTextAvailable(@NonNls final String s) {
              ApplicationManager.getApplication().invokeLater(() -> {
                myResultArea.setText(s);
                getOKAction().setEnabled(true);
              }, ModalityState.defaultModalityState());

              return CommandOutputProcessingMode.DONE;
            }
          }
      );
    }

    @Override
    protected JComponent createCenterPanel() {
      return myPanel;
    }
  }
}