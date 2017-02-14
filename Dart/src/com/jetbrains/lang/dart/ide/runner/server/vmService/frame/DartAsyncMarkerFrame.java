package com.jetbrains.lang.dart.ide.runner.server.vmService.frame;

import com.intellij.icons.AllIcons;
import com.intellij.ui.ColoredTextContainer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.xdebugger.frame.XStackFrame;
import org.jetbrains.annotations.NotNull;

/**
 * A XStackFrame used to render a separator between two sets of async stack frames.
 */
public class DartAsyncMarkerFrame extends XStackFrame {
  public DartAsyncMarkerFrame() {
  }

  public void customizePresentation(@NotNull ColoredTextContainer component) {
    component.append("<asynchronous gap>", SimpleTextAttributes.EXCLUDED_ATTRIBUTES);
    component.setIcon(AllIcons.General.SeparatorH);
  }
}
