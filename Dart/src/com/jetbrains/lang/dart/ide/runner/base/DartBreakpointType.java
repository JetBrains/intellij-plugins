package com.jetbrains.lang.dart.ide.runner.base;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import com.intellij.xdebugger.breakpoints.ui.XBreakpointGroupingRule;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.ide.module.DartModuleTypeBase;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Fedor.Korotkov
 */
public class DartBreakpointType extends XLineBreakpointType<XBreakpointProperties> {
  private final XDebuggerEditorsProvider myEditorProvider = new DartDebuggerEditorsProvider();

  protected DartBreakpointType() {
    super("Dart", DartBundle.message("dart.break.point.title"));
  }

  public boolean canPutAt(@NotNull final VirtualFile file, final int line, @NotNull Project project) {
    if (file.getFileType() == DartFileType.INSTANCE) {
      Module module = ModuleUtilCore.findModuleForFile(file, project);
      if (module != null) {
        // only in dart module.
        return ModuleType.get(module) instanceof DartModuleTypeBase;
      }
    }
    return false;
  }

  public XBreakpointProperties createBreakpointProperties(@NotNull final VirtualFile file, final int line) {
    return null;
  }

  @Override
  public List<XBreakpointGroupingRule<XLineBreakpoint<XBreakpointProperties>, ?>> getGroupingRules() {
    XBreakpointGroupingRule<XLineBreakpoint<XBreakpointProperties>, ?> byFile = XDebuggerUtil.getInstance().getGroupingByFileRule();
    List<XBreakpointGroupingRule<XLineBreakpoint<XBreakpointProperties>, ?>> rules =
      new ArrayList<XBreakpointGroupingRule<XLineBreakpoint<XBreakpointProperties>, ?>>();
    rules.add(byFile);
    return rules;
  }

  @Override
  public XDebuggerEditorsProvider getEditorsProvider(@NotNull XLineBreakpoint<XBreakpointProperties> breakpoint, @NotNull Project project) {
    return myEditorProvider;
  }

  @Override
  public String getBreakpointsDialogHelpTopic() {
    return "reference.dialogs.breakpoints";
  }
}
