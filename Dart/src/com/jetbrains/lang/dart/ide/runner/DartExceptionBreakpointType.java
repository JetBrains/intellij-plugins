// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.jetbrains.lang.dart.ide.runner;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointType;
import com.intellij.xdebugger.breakpoints.ui.XBreakpointCustomPropertiesPanel;
import com.intellij.xdebugger.impl.breakpoints.XBreakpointBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class DartExceptionBreakpointType
  extends XBreakpointType<XBreakpoint<DartExceptionBreakpointProperties>, DartExceptionBreakpointProperties> {

  public DartExceptionBreakpointType() {
    super("dart-exception", "Dart Exception Breakpoint");
  }

  @NotNull
  @Override
  public Icon getEnabledIcon() {
    return AllIcons.Debugger.Db_exception_breakpoint;
  }

  @NotNull
  @Override
  public Icon getDisabledIcon() {
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
    return "Break on exceptions";
  }

  @Override
  public XBreakpoint<DartExceptionBreakpointProperties> createDefaultBreakpoint(@NotNull XBreakpointCreator<DartExceptionBreakpointProperties> creator) {
    final XBreakpoint<DartExceptionBreakpointProperties> breakpoint = creator.createBreakpoint(new DartExceptionBreakpointProperties());
    breakpoint.setEnabled(true);
    return breakpoint;
  }

  @Nullable
  @Override
  public XBreakpointCustomPropertiesPanel<XBreakpoint<DartExceptionBreakpointProperties>> createCustomPropertiesPanel(@NotNull final Project project) {
    return new DartExceptionBreakpointPropertiesPanel();
  }

  private static class DartExceptionBreakpointPropertiesPanel
    extends XBreakpointCustomPropertiesPanel<XBreakpoint<DartExceptionBreakpointProperties>> {

    private JBRadioButton myBreakOnUncaughtExceptions;
    private JBRadioButton myBreakOnAllExceptions;

    @NotNull
    @Override
    public JComponent getComponent() {
      myBreakOnUncaughtExceptions = new JBRadioButton("Break on uncaught exceptions");
      myBreakOnAllExceptions = new JBRadioButton("Break on all exceptions");

      final ButtonGroup group = new ButtonGroup();
      group.add(myBreakOnUncaughtExceptions);
      group.add(myBreakOnAllExceptions);

      final JPanel panel = new JPanel(new BorderLayout());
      panel.add(myBreakOnUncaughtExceptions, BorderLayout.NORTH);
      panel.add(myBreakOnAllExceptions, BorderLayout.SOUTH);
      panel.setBorder(IdeBorderFactory.createTitledBorder("Breaking policy"));

      return panel;
    }

    @Override
    public void saveTo(@NotNull final XBreakpoint<DartExceptionBreakpointProperties> breakpoint) {
      final boolean oldValue = breakpoint.getProperties().isBreakOnAllExceptions();
      final boolean newValue = myBreakOnAllExceptions.isSelected();
      if (oldValue != newValue) {
        breakpoint.getProperties().setBreakOnAllExceptions(newValue);
        ((XBreakpointBase)breakpoint).fireBreakpointChanged();
      }
    }

    @Override
    public void loadFrom(@NotNull final XBreakpoint<DartExceptionBreakpointProperties> breakpoint) {
      if (breakpoint.getProperties().isBreakOnAllExceptions()) {
        myBreakOnAllExceptions.setSelected(true);
      }
      else {
        myBreakOnUncaughtExceptions.setSelected(true);
      }
    }
  }
}
