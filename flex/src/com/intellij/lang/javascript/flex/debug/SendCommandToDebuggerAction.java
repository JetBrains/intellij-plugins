package com.intellij.lang.javascript.flex.debug;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;

/**
 * @author Maxim.Mossienko
 * Date: Jan 22, 2008
 * Time: 4:37:21 PM
 */
public class SendCommandToDebuggerAction extends AnAction {
  public void update(final AnActionEvent e) {
    Project project = e.getData(PlatformDataKeys.PROJECT);
    if (project == null) return;

    final boolean internal = ApplicationManagerEx.getApplicationEx().isInternal();
    e.getPresentation().setVisible(internal);
    e.getPresentation().setEnabled(internal && XDebuggerManager.getInstance(project).getCurrentSession() != null);
  }

  public void actionPerformed(final AnActionEvent e) {
    Project project = e.getData(PlatformDataKeys.PROJECT);
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
              ApplicationManager.getApplication().invokeLater(new Runnable() {
                public void run() {
                  myResultArea.setText(s);
                  getOKAction().setEnabled(true);
                }
              }, ModalityState.defaultModalityState());

              return CommandOutputProcessingMode.DONE;
            }
          }
      );
    }

    protected JComponent createCenterPanel() {
      return myPanel;
    }
  }
}