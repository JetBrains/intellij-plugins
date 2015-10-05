package com.jetbrains.lang.dart.ide.runner.server.vmService;

import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.jetbrains.lang.dart.ide.runner.DartLineBreakpointType;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.dartlang.vm.service.element.Breakpoint;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

import static com.intellij.icons.AllIcons.Debugger.Db_invalid_breakpoint;
import static com.intellij.icons.AllIcons.Debugger.Db_verified_breakpoint;

public class DartVmServiceBreakpointHandler extends XBreakpointHandler<XLineBreakpoint<XBreakpointProperties>> {

  private final DartVmServiceDebugProcess myDebugProcess;
  private final IsolatesInfo myIsolatesInfo;

  private final Set<XLineBreakpoint<XBreakpointProperties>> myXBreakpoints = new THashSet<XLineBreakpoint<XBreakpointProperties>>();
  private final Map<String, XLineBreakpoint<XBreakpointProperties>> myVmBreakpointIdToXBreakpointMap =
    new THashMap<String, XLineBreakpoint<XBreakpointProperties>>();

  protected DartVmServiceBreakpointHandler(@NotNull final DartVmServiceDebugProcess debugProcess,
                                           @NotNull final IsolatesInfo isolatesInfo) {
    super(DartLineBreakpointType.class);
    myDebugProcess = debugProcess;
    myIsolatesInfo = isolatesInfo;
  }

  @Override
  public void registerBreakpoint(@NotNull final XLineBreakpoint<XBreakpointProperties> xBreakpoint) {
    myXBreakpoints.add(xBreakpoint);
  }

  @Override
  public void unregisterBreakpoint(@NotNull final XLineBreakpoint<XBreakpointProperties> xBreakpoint, boolean temporary) {
    myXBreakpoints.remove(xBreakpoint);
  }

  public Set<XLineBreakpoint<XBreakpointProperties>> getXBreakpoints() {
    return myXBreakpoints;
  }

  public void vmBreakpointAdded(@NotNull final XLineBreakpoint<XBreakpointProperties> xBreakpoint, @NotNull final Breakpoint vmBreakpoint) {
    if (vmBreakpoint.getResolved()) {
      breakpointResolved(vmBreakpoint);
    }
    myVmBreakpointIdToXBreakpointMap.put(vmBreakpoint.getId(), xBreakpoint);
    // todo remember backward mapping
  }

  public void breakpointResolved(@NotNull final Breakpoint vmBreakpoint) {
    final XLineBreakpoint<XBreakpointProperties> xBreakpoint = myVmBreakpointIdToXBreakpointMap.get(vmBreakpoint.getId());
    myDebugProcess.getSession().updateBreakpointPresentation(xBreakpoint, Db_verified_breakpoint, null);
  }

  public void breakpointFailed(@NotNull final XLineBreakpoint<XBreakpointProperties> xBreakpoint) {
    // can this xBreakpoint be resolved for other isolate?
    myDebugProcess.getSession().updateBreakpointPresentation(xBreakpoint, Db_invalid_breakpoint, null);
  }
}
