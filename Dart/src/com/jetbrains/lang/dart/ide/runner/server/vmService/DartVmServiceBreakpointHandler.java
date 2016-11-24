package com.jetbrains.lang.dart.ide.runner.server.vmService;

import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.jetbrains.lang.dart.ide.runner.DartLineBreakpointType;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.dartlang.vm.service.element.Breakpoint;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.intellij.icons.AllIcons.Debugger.Db_invalid_breakpoint;
import static com.intellij.icons.AllIcons.Debugger.Db_verified_breakpoint;

public class DartVmServiceBreakpointHandler extends XBreakpointHandler<XLineBreakpoint<XBreakpointProperties>> {

  private final DartVmServiceDebugProcess myDebugProcess;
  private final Set<XLineBreakpoint<XBreakpointProperties>> myXBreakpoints = new THashSet<>();
  private final Map<String, IsolateBreakpointInfo> myIsolateInfo = new THashMap<>();
  private final Map<String, XLineBreakpoint<XBreakpointProperties>> myVmBreakpointIdToXBreakpointMap = new THashMap<>();

  protected DartVmServiceBreakpointHandler(@NotNull final DartVmServiceDebugProcess debugProcess) {
    super(DartLineBreakpointType.class);
    myDebugProcess = debugProcess;
  }

  @Override
  public void registerBreakpoint(@NotNull final XLineBreakpoint<XBreakpointProperties> xBreakpoint) {
    myXBreakpoints.add(xBreakpoint);

    final VmServiceWrapper vmServiceWrapper = myDebugProcess.getVmServiceWrapper();
    if (vmServiceWrapper != null) {
      vmServiceWrapper.addBreakpointForIsolates(xBreakpoint, myDebugProcess.getIsolateInfos());
    }
  }

  @Override
  public void unregisterBreakpoint(@NotNull final XLineBreakpoint<XBreakpointProperties> xBreakpoint, boolean temporary) {
    myXBreakpoints.remove(xBreakpoint);

    for (IsolateBreakpointInfo info : myIsolateInfo.values()) {
      info.unregisterBreakpoint(xBreakpoint);
    }
  }

  public Set<XLineBreakpoint<XBreakpointProperties>> getXBreakpoints() {
    return myXBreakpoints;
  }

  public void vmBreakpointAdded(@NotNull final XLineBreakpoint<XBreakpointProperties> xBreakpoint,
                                @NotNull final String isolateId,
                                @NotNull final Breakpoint vmBreakpoint) {
    myVmBreakpointIdToXBreakpointMap.put(vmBreakpoint.getId(), xBreakpoint);

    IsolateBreakpointInfo info = getIsolateInfo(isolateId);
    info.vmBreakpointAdded(xBreakpoint, vmBreakpoint);

    if (vmBreakpoint.getResolved()) {
      breakpointResolved(vmBreakpoint);
    }
  }

  public void temporaryBreakpointAdded(String isolateId, Breakpoint breakpoint) {
    getIsolateInfo(isolateId).addTemporaryBreakpoint(breakpoint.getId());
  }

  public void removeTemporaryBreakpoints(String isolateId) {
    getIsolateInfo(isolateId).removeTemporaryBreakpoints();
  }

  private IsolateBreakpointInfo getIsolateInfo(String isolateId) {
    IsolateBreakpointInfo info = myIsolateInfo.get(isolateId);
    if (info == null) {
      info = new IsolateBreakpointInfo(isolateId, myDebugProcess);
      myIsolateInfo.put(isolateId, info);
    }
    return info;
  }

  public void breakpointResolved(@NotNull final Breakpoint vmBreakpoint) {
    final XLineBreakpoint<XBreakpointProperties> xBreakpoint = myVmBreakpointIdToXBreakpointMap.get(vmBreakpoint.getId());

    // This can be null when the breakpoint has been set by another debugger client.
    if (xBreakpoint != null) {
      myDebugProcess.getSession().updateBreakpointPresentation(xBreakpoint, Db_verified_breakpoint, null);
    }
  }

  public void breakpointFailed(@NotNull final XLineBreakpoint<XBreakpointProperties> xBreakpoint) {
    // can this xBreakpoint be resolved for other isolate?
    myDebugProcess.getSession().updateBreakpointPresentation(xBreakpoint, Db_invalid_breakpoint, null);
  }

  public XLineBreakpoint<XBreakpointProperties> getXBreakpoint(@NotNull final Breakpoint vmBreakpoint) {
    return myVmBreakpointIdToXBreakpointMap.get(vmBreakpoint.getId());
  }
}

class IsolateBreakpointInfo {
  private final String myIsolateId;
  private final DartVmServiceDebugProcess myDebugProcess;
  List<String> myTemporaryBreakpoints = new ArrayList<>();
  private final Map<XLineBreakpoint<XBreakpointProperties>, Set<String>> myXBreakpointToVmBreakpointIdsMap = new THashMap<>();

  IsolateBreakpointInfo(@NotNull String isolateId, @NotNull DartVmServiceDebugProcess debugProcess) {
    this.myIsolateId = isolateId;
    this.myDebugProcess = debugProcess;
  }

  public void removeTemporaryBreakpoints() {
    for (String breakpointId : myTemporaryBreakpoints) {
      myDebugProcess.getVmServiceWrapper().removeBreakpoint(myIsolateId, breakpointId);
    }
    myTemporaryBreakpoints.clear();
  }

  public void addTemporaryBreakpoint(String breakpointId) {
    myTemporaryBreakpoints.add(breakpointId);
  }

  public void vmBreakpointAdded(XLineBreakpoint<XBreakpointProperties> xBreakpoint, Breakpoint vmBreakpoint) {
    getVmBreakpoints(xBreakpoint).add(vmBreakpoint.getId());
  }

  public void unregisterBreakpoint(XLineBreakpoint<XBreakpointProperties> xBreakpoint) {
    if (myDebugProcess.isIsolateAlive(myIsolateId)) {
      for (String vmBreakpointId : getVmBreakpoints(xBreakpoint)) {
        myDebugProcess.getVmServiceWrapper().removeBreakpoint(myIsolateId, vmBreakpointId);
      }
    }
  }

  private Set<String> getVmBreakpoints(XLineBreakpoint<XBreakpointProperties> xBreakpoint) {
    Set<String> vmBreakpoints = myXBreakpointToVmBreakpointIdsMap.get(xBreakpoint);
    if (vmBreakpoints == null) {
      vmBreakpoints = new HashSet<>();
      myXBreakpointToVmBreakpointIdsMap.put(xBreakpoint, vmBreakpoints);
    }
    return vmBreakpoints;
  }
}
