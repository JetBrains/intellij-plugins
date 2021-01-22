// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.runner.server.vmService;

import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.jetbrains.lang.dart.ide.runner.DartLineBreakpointType;
import org.dartlang.vm.service.element.Breakpoint;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class DartVmServiceBreakpointHandler extends XBreakpointHandler<XLineBreakpoint<XBreakpointProperties>> {
  private final DartVmServiceDebugProcess myDebugProcess;
  private final Set<XLineBreakpoint<XBreakpointProperties>> myXBreakpoints = new HashSet<>();
  private final Map<String, IsolateBreakpointInfo> myIsolateInfo = new HashMap<>();
  private final Map<String, XLineBreakpoint<XBreakpointProperties>> myVmBreakpointIdToXBreakpointMap = new HashMap<>();

  public DartVmServiceBreakpointHandler(@NotNull final DartVmServiceDebugProcess debugProcess) {
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

  public void temporaryBreakpointAdded(String isolateId, Breakpoint vmBreakpoint) {
    getIsolateInfo(isolateId).temporaryVmBreakpointAdded(vmBreakpoint.getId());
  }

  public void removeTemporaryBreakpoints(String isolateId) {
    getIsolateInfo(isolateId).removeTemporaryBreakpoints();
  }

  public void removeAllVmBreakpoints(@NotNull String isolateId) {
    final Set<String> vmBreakpoints = getIsolateInfo(isolateId).removeAllVmBreakpoints();
    for (String vmBreakpointId : vmBreakpoints) {
      myVmBreakpointIdToXBreakpointMap.remove(vmBreakpointId);
    }
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
      myDebugProcess.getSession().setBreakpointVerified(xBreakpoint);
    }
  }

  public void breakpointFailed(@NotNull final XLineBreakpoint<XBreakpointProperties> xBreakpoint) {
    // can this xBreakpoint be resolved for other isolate?
    myDebugProcess.getSession().setBreakpointInvalid(xBreakpoint, null);
  }

  public XLineBreakpoint<XBreakpointProperties> getXBreakpoint(@NotNull final Breakpoint vmBreakpoint) {
    return myVmBreakpointIdToXBreakpointMap.get(vmBreakpoint.getId());
  }
}

class IsolateBreakpointInfo {
  private final String myIsolateId;
  private final DartVmServiceDebugProcess myDebugProcess;
  private final List<String> myTemporaryVmBreakpointIds = new ArrayList<>();
  private final Map<XLineBreakpoint<XBreakpointProperties>, Set<String>> myXBreakpointToVmBreakpointIdsMap = new HashMap<>();

  IsolateBreakpointInfo(@NotNull String isolateId, @NotNull DartVmServiceDebugProcess debugProcess) {
    this.myIsolateId = isolateId;
    this.myDebugProcess = debugProcess;
  }

  public void removeTemporaryBreakpoints() {
    for (String breakpointId : myTemporaryVmBreakpointIds) {
      myDebugProcess.getVmServiceWrapper().removeBreakpoint(myIsolateId, breakpointId);
    }
    myTemporaryVmBreakpointIds.clear();
  }

  public Set<String> removeAllVmBreakpoints() {
    if (!myDebugProcess.isIsolateAlive(myIsolateId)) {
      return new HashSet<>();
    }

    final Set<String> allVmBreakpoints = new HashSet<>();

    synchronized (myXBreakpointToVmBreakpointIdsMap) {
      for (Set<String> bps : myXBreakpointToVmBreakpointIdsMap.values()) {
        allVmBreakpoints.addAll(bps);
      }
      myXBreakpointToVmBreakpointIdsMap.clear();
    }

    for (String vmBreakpointId : allVmBreakpoints) {
      myDebugProcess.getVmServiceWrapper().removeBreakpoint(myIsolateId, vmBreakpointId);
    }

    return allVmBreakpoints;
  }

  public void temporaryVmBreakpointAdded(String vmBreakpointId) {
    myTemporaryVmBreakpointIds.add(vmBreakpointId);
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

    myXBreakpointToVmBreakpointIdsMap.remove(xBreakpoint);
  }

  private Set<String> getVmBreakpoints(XLineBreakpoint<XBreakpointProperties> xBreakpoint) {
    synchronized (myXBreakpointToVmBreakpointIdsMap) {
      Set<String> vmBreakpoints = myXBreakpointToVmBreakpointIdsMap.get(xBreakpoint);
      if (vmBreakpoints == null) {
        vmBreakpoints = new HashSet<>();
        myXBreakpointToVmBreakpointIdsMap.put(xBreakpoint, vmBreakpoints);
      }
      return vmBreakpoints;
    }
  }
}
