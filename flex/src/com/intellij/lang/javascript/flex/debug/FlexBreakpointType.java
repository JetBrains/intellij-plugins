package com.intellij.lang.javascript.flex.debug;

import com.intellij.lang.javascript.ActionScriptFileType;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import com.intellij.xdebugger.breakpoints.ui.XBreakpointGroupingRule;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Maxim.Mossienko
 * Date: Jan 22, 2008
 * Time: 4:26:21 PM
 */
public class FlexBreakpointType extends XLineBreakpointType<XBreakpointProperties> {
  private final XDebuggerEditorsProvider myEditorProvider = new FlexDebuggerEditorsProvider();

  protected FlexBreakpointType() {
    super("flex", FlexBundle.message("flex.break.point.title"));
  }

  public boolean canPutAt(@NotNull final VirtualFile file, final int line, @NotNull Project project) {
    if(file.getFileType() == ActionScriptFileType.INSTANCE ||
       JavaScriptSupportLoader.isFlexMxmFile(file)
      ) {
      return true;
    }

    return false;
  }

  public XBreakpointProperties createBreakpointProperties(@NotNull final VirtualFile file, final int line) {
    return null;
  }

  @Override
  public List<XBreakpointGroupingRule<XLineBreakpoint<XBreakpointProperties>, ?>> getGroupingRules() {
    XBreakpointGroupingRule<XLineBreakpoint<XBreakpointProperties>, ?> byFile = XDebuggerUtil.getInstance().getGroupingByFileRule();
    List<XBreakpointGroupingRule<XLineBreakpoint<XBreakpointProperties>, ?>> rules = new ArrayList<XBreakpointGroupingRule<XLineBreakpoint<XBreakpointProperties>, ?>>();
    rules.add(byFile);
    return rules;
  }

  @Override
  public XDebuggerEditorsProvider getEditorsProvider() {
    return myEditorProvider;
  }

  @Override
  public String getBreakpointsDialogHelpTopic() {
    return "reference.dialogs.breakpoints";
  }
}
