// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.debug;

import com.intellij.lang.javascript.ActionScriptFileType;
import com.intellij.lang.javascript.flex.FlexSupportLoader;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.breakpoints.XLineBreakpointTypeBase;
import com.intellij.xdebugger.breakpoints.ui.XBreakpointGroupingRule;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Maxim.Mossienko
 */
public final class FlexBreakpointType extends XLineBreakpointTypeBase {
  private FlexBreakpointType() {
    super("flex", FlexBundle.message("flex.break.point.title"), new FlexDebuggerEditorsProvider());
  }

  @Override
  public boolean canPutAt(final @NotNull VirtualFile file, final int line, @NotNull Project project) {
    if(FileTypeRegistry.getInstance().isFileOfType(file, ActionScriptFileType.INSTANCE) ||
       FlexSupportLoader.isFlexMxmFile(file)
      ) {
      return true;
    }

    return false;
  }

  @Override
  public List<XBreakpointGroupingRule<XLineBreakpoint<XBreakpointProperties>, ?>> getGroupingRules() {
    return XDebuggerUtil.getInstance().getGroupingByFileRuleAsList();
  }

  @Override
  public String getBreakpointsDialogHelpTopic() {
    return "reference.dialogs.breakpoints";
  }
}
