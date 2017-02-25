
package com.jetbrains.lang.dart.ide.runner.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbAware;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.server.vmService.frame.DartVmServiceStackFrame;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class DartPopFrameAction extends AnAction implements DumbAware {
  public DartPopFrameAction() {
    Presentation presentation = getTemplatePresentation();
    presentation.setText(DartBundle.message("dart.pop.frame.action.text"));
    presentation.setDescription(DartBundle.message("dart.pop.frame.action.description"));
    presentation.setIcon(AllIcons.Actions.PopFrame);
  }

  public void actionPerformed(@NotNull AnActionEvent e) {
    DartVmServiceStackFrame frame = getStackFrame(e);
    if (frame != null) {
      frame.dropFrame();
    }
  }

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

  static DartVmServiceStackFrame getStackFrame(AnActionEvent e) {
    Component component = PlatformDataKeys.CONTEXT_COMPONENT.getData(e.getDataContext());
    if (component instanceof JList) {
      JList frames = (JList)component;
      if (frames.getSelectedValue() instanceof DartVmServiceStackFrame) {
        return (DartVmServiceStackFrame)frames.getSelectedValue();
      }
    }
    return null;
  }
}
