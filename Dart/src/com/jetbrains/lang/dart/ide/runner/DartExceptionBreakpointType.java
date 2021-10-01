// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.runner;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointType;
import com.intellij.xdebugger.breakpoints.ui.XBreakpointCustomPropertiesPanel;
import com.intellij.xdebugger.impl.breakpoints.XBreakpointBase;
import com.jetbrains.lang.dart.DartBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class DartExceptionBreakpointType
  extends XBreakpointType<XBreakpoint<DartExceptionBreakpointProperties>, DartExceptionBreakpointProperties> {

  public DartExceptionBreakpointType() {
    super("dart-exception", DartBundle.message("breakpoint.type.title.dart.exception.breakpoint"));
  }

  @Override
  public @NotNull Icon getEnabledIcon() {
    return AllIcons.Debugger.Db_exception_breakpoint;
  }

  @Override
  public @NotNull Icon getDisabledIcon() {
    return AllIcons.Debugger.Db_disabled_exception_breakpoint;
  }

  @Override
  public DartExceptionBreakpointProperties createProperties() {
    return new DartExceptionBreakpointProperties();
  }

  @Override
  public String getBreakpointsDialogHelpTopic() {
    return "reference.dialogs.breakpoints";
  }

  @Override
  public String getDisplayText(XBreakpoint<DartExceptionBreakpointProperties> breakpoint) {
    return DartBundle.message("break.on.exceptions");
  }

  @Override
  public XBreakpoint<DartExceptionBreakpointProperties> createDefaultBreakpoint(@NotNull XBreakpointCreator<DartExceptionBreakpointProperties> creator) {
    final XBreakpoint<DartExceptionBreakpointProperties> breakpoint = creator.createBreakpoint(new DartExceptionBreakpointProperties());
    breakpoint.setEnabled(true);
    return breakpoint;
  }

  @Override
  public @Nullable XBreakpointCustomPropertiesPanel<XBreakpoint<DartExceptionBreakpointProperties>> createCustomPropertiesPanel(final @NotNull Project project) {
    return new DartExceptionBreakpointPropertiesPanel();
  }

  private static class DartExceptionBreakpointPropertiesPanel
    extends XBreakpointCustomPropertiesPanel<XBreakpoint<DartExceptionBreakpointProperties>> {

    private JBRadioButton myBreakOnUncaughtExceptions;
    private JBRadioButton myBreakOnAllExceptions;

    @Override
    public @NotNull JComponent getComponent() {
      myBreakOnUncaughtExceptions = new JBRadioButton(DartBundle.message("radio.text.break.on.uncaught.exceptions"));
      myBreakOnAllExceptions = new JBRadioButton(DartBundle.message("radio.text.break.on.all.exceptions"));

      final ButtonGroup group = new ButtonGroup();
      group.add(myBreakOnUncaughtExceptions);
      group.add(myBreakOnAllExceptions);

      final JPanel panel = new JPanel(new BorderLayout());
      panel.add(myBreakOnUncaughtExceptions, BorderLayout.NORTH);
      panel.add(myBreakOnAllExceptions, BorderLayout.SOUTH);
      panel.setBorder(IdeBorderFactory.createTitledBorder(DartBundle.message("border.breaking.policy")));

      return panel;
    }

    @Override
    public void saveTo(final @NotNull XBreakpoint<DartExceptionBreakpointProperties> breakpoint) {
      final boolean oldValue = breakpoint.getProperties().isBreakOnAllExceptions();
      final boolean newValue = myBreakOnAllExceptions.isSelected();
      if (oldValue != newValue) {
        breakpoint.getProperties().setBreakOnAllExceptions(newValue);
        ((XBreakpointBase<?, ?, ?>)breakpoint).fireBreakpointChanged();
      }
    }

    @Override
    public void loadFrom(final @NotNull XBreakpoint<DartExceptionBreakpointProperties> breakpoint) {
      if (breakpoint.getProperties().isBreakOnAllExceptions()) {
        myBreakOnAllExceptions.setSelected(true);
      }
      else {
        myBreakOnUncaughtExceptions.setSelected(true);
      }
    }
  }
}
