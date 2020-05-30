
package com.jetbrains.lang.dart.ide.runner.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.frame.XStackFrame;
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

  @Nullable
  private static DartVmServiceStackFrame getStackFrame(@NotNull final AnActionEvent e) {
    final Project project = e.getProject();
    if (project == null) return null;

    XDebugSession session = e.getData(XDebugSession.DATA_KEY);

    if (session == null) {
      session = XDebuggerManager.getInstance(project).getCurrentSession();
    }

    if (session != null) {
      XStackFrame frame = session.getCurrentStackFrame();
      if (frame instanceof DartVmServiceStackFrame) {
        return ((DartVmServiceStackFrame)frame);
      }
    }

    return null;
  }
}
